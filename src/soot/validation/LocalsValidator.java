package soot.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import soot.Body;
import soot.Local;
import soot.Value;
import soot.ValueBox;

public class LocalsValidator implements BodyValidator {

	private static final Logger logger =LoggerFactory.getLogger(LocalsValidator.class);
	public static LocalsValidator INSTANCE;
	
	
	public static LocalsValidator v() {
		if (INSTANCE == null)
		{
			INSTANCE = new LocalsValidator();
		}
		return INSTANCE;
	}


	@Override
	/** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
	public void validate(Body body, List<ValidationException> exception) {
        for (ValueBox vb : body.getUseBoxes()) {
            validateLocal(body, vb, exception);
        }
        for (ValueBox vb : body.getDefBoxes()) {
            validateLocal(body, vb, exception);
        }
    }
	

    private void validateLocal(Body body, ValueBox vb, List<ValidationException> exception ) {
        Value value;
        if( (value = vb.getValue()) instanceof Local) {
            if(!body.getLocals().contains(value))
                exception.add(new ValidationException(value, "Local not in chain : "+value+" in "+ body.getMethod()));
        }
    }


	@Override
	public boolean isBasicValidator() {
		return true;
	}
}
