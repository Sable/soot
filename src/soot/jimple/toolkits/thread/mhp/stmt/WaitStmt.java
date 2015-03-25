
package soot.jimple.toolkits.thread.mhp.stmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import soot.*;
import soot.toolkits.graph.*;

// *** USE AT YOUR OWN RISK ***
// May Happen in Parallel (MHP) analysis by Lin Li.
// This code should be treated as beta-quality code.
// It was written in 2003, but not incorporated into Soot until 2006.
// As such, it may contain incorrect assumptions about the usage
// of certain Soot classes.
// Some portions of this MHP analysis have been quality-checked, and are
// now used by the Transactions toolkit.
//
// -Richard L. Halpert, 2006-11-30


public class WaitStmt extends JPegStmt
{

	private static final Logger logger =LoggerFactory.getLogger(WaitStmt.class);
	
	public WaitStmt(String obj, String ca, Unit un, UnitGraph ug, SootMethod sm)
	{
		this.object = obj;
		this.name = "wait";
		this.caller = ca;
		this.unit = un;
		this.unitGraph = ug;
		this.sootMethod = sm;
	}
	
	
	
	
	
}
