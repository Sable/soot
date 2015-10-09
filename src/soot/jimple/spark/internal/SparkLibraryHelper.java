package soot.jimple.spark.internal;

import soot.AnySubType;
import soot.ArrayType;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.TypeSwitch;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.spark.pag.FieldRefNode;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;

public class SparkLibraryHelper extends TypeSwitch {
	
	private PAG pag;
	private Node node;
	private SootMethod method;
	
	public SparkLibraryHelper(PAG pag, Node node, SootMethod method) {
		this.pag = pag;
		this.node = node;
		this.method = method;
	}
	
	@Override
	public void caseRefType(RefType t) {
		// var tmp;
		VarNode local = pag.makeLocalVarNode(new Object(), t, method);
		
		// new T();
		AllocNode alloc = pag.makeAllocNode(new Object(), AnySubType.v(t), method);
		
		// tmp = new T();
		pag.addAllocEdge(alloc, local);
		
		// x = tmp;
		pag.addEdge(local, node);
	}
	
	@Override
	public void caseArrayType(ArrayType type) {
		Node array = node;
		for (Type t = type; t instanceof ArrayType; t = ((ArrayType) t).getElementType()) {
    		ArrayType at = (ArrayType) t;
			if (at.baseType instanceof RefType) {
				
				// var tmpArray;
				LocalVarNode localArray = pag.makeLocalVarNode(new Object(), t, method);
				
				// x = tmpArray;
				pag.addEdge(localArray, array);
				
				// new T[]
    			AllocNode newArray = pag.makeAllocNode(new Object(), at, method);
    			
    			// tmpArray = new T[]
    			pag.addEdge(newArray, localArray);
    			
    			// tmpArray[i]
    			FieldRefNode arrayRef = pag.makeFieldRefNode( localArray, ArrayElement.v());
    			
    			// var tmp
    			LocalVarNode local = pag.makeLocalVarNode(new Object(), at.getElementType(), method);
    			
    			// tmpArray[i] = tmp
    			pag.addEdge(local, arrayRef);
    			
    			// x = tmp
    			array = local;

    			if (at.numDimensions == 1) {
    				// new T()
    				AllocNode alloc = pag.makeAllocNode(new Object(), AnySubType.v((RefType)at.baseType), method);
    				
    				// tmp = new T()
    				pag.addEdge(alloc, local);
    			}
    		}
		}
	}

}