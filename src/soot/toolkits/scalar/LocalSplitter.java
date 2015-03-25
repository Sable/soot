/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */





package soot.toolkits.scalar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.Singletons;
import soot.Timers;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.options.Options;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

/**
 *    A BodyTransformer that attemps to indentify and separate uses of a local
 *    varible that are independent of each other. Conceptually the inverse transform
 *    with respect to the LocalPacker transform.
 *
 *    For example the code:
 *
 *    for(int i; i < k; i++);
 *    for(int i; i < k; i++);
 *
 *    would be transformed into:
 *    for(int i; i < k; i++);
 *    for(int j; j < k; j++);
 *
 *
 *    @see BodyTransformer
 *    @see LocalPacker
 *    @see Body 
 */
public class LocalSplitter extends BodyTransformer
{

	private static final Logger logger =LoggerFactory.getLogger(LocalSplitter.class);
	
	protected ThrowAnalysis throwAnalysis = null;
	protected boolean forceOmitExceptingUnitEdges = false;

	public LocalSplitter( Singletons.Global g ) {
	}
	
	public LocalSplitter( ThrowAnalysis ta ) {
		this.throwAnalysis = ta;
	}

	public LocalSplitter( ThrowAnalysis ta, boolean forceOmitExceptingUnitEdges ) {
		this.throwAnalysis = ta;
		this.forceOmitExceptingUnitEdges = forceOmitExceptingUnitEdges;
	}
	
	public static LocalSplitter v() { return G.v().soot_toolkits_scalar_LocalSplitter(); }
    
	@Override
    protected void internalTransform(Body body, String phaseName, Map<String, String> options)
    {
		if (this.throwAnalysis == null)
			this.throwAnalysis = Scene.v().getDefaultThrowAnalysis();
		
        Chain<Unit> units = body.getUnits();
        List<List<ValueBox>> webs = new ArrayList<List<ValueBox>>();

        if(Options.v().verbose())
            logger.info("[" + body.getMethod().getName() + "] Splitting locals...");

        if(Options.v().time())
                Timers.v().splitPhase1Timer.start();

        // Go through the definitions, building the webs
        {
            ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body,this.throwAnalysis,
            		forceOmitExceptingUnitEdges || Options.v().omit_excepting_unit_edges());

            final LocalDefs localDefs = new SmartLocalDefs(graph,
    				new SimpleLiveLocals(graph));
    		final LocalUses localUses = new SimpleLocalUses(graph, localDefs);
    		
            if(Options.v().time())
                Timers.v().splitPhase1Timer.end();
            if(Options.v().time())
                Timers.v().splitPhase2Timer.start();

            Set<ValueBox> markedBoxes = new HashSet<ValueBox>();
            Map<ValueBox, Unit> boxToUnit = new HashMap<ValueBox, Unit>(units.size() * 2 + 1, 0.7f);
            
            for (Unit s : units) {
                if (s.getDefBoxes().size() > 1)
                    throw new RuntimeException("stmt with more than 1 defbox!");
                if (s.getDefBoxes().size() < 1)
                    continue;
                
                ValueBox loBox = s.getDefBoxes().get(0);
                Value lo = loBox.getValue();
                
                if(lo instanceof Local && !markedBoxes.contains(loBox))
                {
                    Deque<Unit> defsToVisit = new ArrayDeque<Unit>();
                    Deque<ValueBox> boxesToVisit = new ArrayDeque<ValueBox>();

                    List<ValueBox> web = new ArrayList<ValueBox>();
                    webs.add(web);
                    
                    defsToVisit.add(s);
                    markedBoxes.add(loBox);
                    
                    while(!boxesToVisit.isEmpty() || !defsToVisit.isEmpty())
                    {
                        if(!defsToVisit.isEmpty())
                        {
                            Unit d = defsToVisit.poll();
                            web.add(d.getDefBoxes().get(0));
                            
                            // Add all the uses of this definition to the queue
                            for (UnitValueBoxPair use : localUses.getUsesOf(d)) {
                            	if(!markedBoxes.contains(use.valueBox)) {
                            		markedBoxes.add(use.valueBox);
                            		boxesToVisit.add(use.valueBox);
                            		boxToUnit.put(use.valueBox, use.unit);
                            	}
                            }
                        }
                        else {
                            ValueBox box = boxesToVisit.poll();
                            web.add(box);

                            // Add all the definitions of this use to the queue.
                            List<Unit> defs = localDefs.getDefsOfAt((Local) box.getValue(),
                            		boxToUnit.get(box));
                            for (Unit u : defs) {
                            	for (ValueBox b : u.getDefBoxes()) {
                            		if(!markedBoxes.contains(b)) {
                            			markedBoxes.add(b);
                            			defsToVisit.add(u);
                            		}
                            	}
                            }
                        }
                    }
                }
            }
        }
        
        // Assign locals appropriately.
        {
            Map<Local, Integer> localToUseCount = new HashMap<Local, Integer>(body.getLocalCount() * 2 + 1, 0.7f);

            for (List<ValueBox> web : webs) {
                ValueBox rep = web.get(0);
                Local desiredLocal = (Local) rep.getValue();

                if(!localToUseCount.containsKey(desiredLocal))
                {
                    // claim this local for this set
                    localToUseCount.put(desiredLocal, new Integer(1));
                }
                else {
                    // generate a new local
                    int useCount = localToUseCount.get(desiredLocal).intValue() + 1;
                    localToUseCount.put(desiredLocal, new Integer(useCount));
        
                    Local local = (Local) desiredLocal.clone();
                    local.setName(desiredLocal.getName() + "#" + useCount);
                    
                    body.getLocals().add(local);

                    // Change all boxes to point to this new local
                    for (ValueBox box : web) {
                    	box.setValue(local);
                    }
                }
            }
        }
        
        if(Options.v().time())
            Timers.v().splitPhase2Timer.end();
    }   
}
