package soot.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.List;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.exceptions.ThrowAnalysisFactory;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.FullExceptionalUnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.InitAnalysis;

public enum CheckInitValidator implements BodyValidator {
  INSTANCE;

  public static CheckInitValidator v() {
    return INSTANCE;
  }

  @Override
  public void validate(Body body, List<ValidationException> exception) {
    ExceptionalUnitGraph g = new FullExceptionalUnitGraph(body, ThrowAnalysisFactory.checkInitThrowAnalysis());

    InitAnalysis analysis = new InitAnalysis(g);
    for (Unit s : body.getUnits()) {
      FlowSet<Local> init = analysis.getFlowBefore(s);
      for (ValueBox vBox : s.getUseBoxes()) {
        Value v = vBox.getValue();
        if (v instanceof Local) {
          Local l = (Local) v;
          if (!init.contains(l)) {
            String pfx = "Local variable " + l.getName() + " is not definitively defined at ";
            exception.add(new ValidationException(s, pfx + "this point", "Warning: " + pfx + s + " in " + body.getMethod()));
          }
        }
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
