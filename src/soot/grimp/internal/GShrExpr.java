/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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






package soot.grimp.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.grimp.*;
import soot.jimple.*;
import soot.util.*;

public class GShrExpr extends AbstractGrimpIntLongBinopExpr implements ShrExpr
{

	private static final Logger logger =LoggerFactory.getLogger(GShrExpr.class);
    public GShrExpr(Value op1, Value op2) { super(op1, op2); }
    public String getSymbol() { return " >> "; }
    public int getPrecedence() { return 650; }
    public void apply(Switch sw) { ((ExprSwitch) sw).caseShrExpr(this); }
     
    public Object clone() 
    {
        return new GShrExpr(Grimp.cloneIfNecessary(getOp1()), Grimp.cloneIfNecessary(getOp2()));
    }

}
