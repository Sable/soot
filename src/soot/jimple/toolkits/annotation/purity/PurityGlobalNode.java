/* Soot - a J*va Optimization Framework
 * Copyright (C) 2005 Antoine Mine
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

/**
 * Implementation of the paper "A Combined Pointer and Purity Analysis for
 * Java Programs" by Alexandru Salcianu and Martin Rinard, within the
 * Soot Optimization Framework.
 *
 * by Antoine Mine, 2005/01/24
 */

package soot.jimple.toolkits.annotation.purity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * The GBL node.
 */
public class PurityGlobalNode implements PurityNode
{

	private static final Logger logger =LoggerFactory.getLogger(PurityGlobalNode.class);
    private PurityGlobalNode() {}

    public static PurityGlobalNode node = new PurityGlobalNode();

    public String toString()   
    { return "GBL"; }

    public int hashCode()  
    { return 0; }
    
    public boolean equals(Object o)
    { return o instanceof PurityGlobalNode; }
    
    public boolean isInside() 
    { return false; }

    public boolean isLoad()
    { return false; }

    public boolean isParam() 
    { return false; }
}
