package soot.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.exceptions.ThrowAnalysisFactory;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.InitAnalysis;

public class CheckInitValidator implements BodyValidator {

	private static final Logger logger =LoggerFactory.getLogger(CheckInitValidator.class);
	public static CheckInitValidator INSTANCE;
	
	
	public static CheckInitValidator v() {
		if (INSTANCE == null)
		{
			INSTANCE = new CheckInitValidator();
		}
		return INSTANCE;
	}


	@Override
	public void validate(Body body, List<ValidationException> exception) {
        ExceptionalUnitGraph g = new ExceptionalUnitGraph
	    (body, ThrowAnalysisFactory.checkInitThrowAnalysis(), false);

		InitAnalysis analysis = new InitAnalysis(g);
		for (Unit s : body.getUnits()) {
			FlowSet<Local> init = analysis.getFlowBefore(s);
		    for (ValueBox vBox : s.getUseBoxes()) {
				Value v=vBox.getValue();
				if(v instanceof Local) {
				    Local l=(Local) v;
				    if(!init.contains(l))
						throw new ValidationException(s, "Local variable $1 is not definitively defined at this point".replace("$1", l.getName()),
								"Warning: Local variable "+l
								   +" not definitely defined at "+s
								   +" in "+body.getMethod(), false);
				}
		    }
		}
   }

	@Override
	public boolean isBasicValidator() {
		return false;
	}
}
