package soot.jimple.toolkits.scalar;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Timothy Hoffman
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

/**
 * @author Timothy Hoffman
 */
public class CopyPropagatorTestInput {

  public static final int[] EXPECT = { 0, 16585, 144402 };

  public static int[] runTest() {
    final int[] state = { -271733879, -1732584194, 271733878 };
    new CopyPropagatorTestInput().implCompressSimpl(state);
    return state;
  }

  public void implCompressSimpl(int[] state) {
    int b = state[0];
    int c = state[1];
    int d = state[2];

    for (int i = 0; i < 20; i++) {
      int temp = (b & c) | (~b & d);
      d = c;
      c = (b >>> 2);
      b = temp;
    }

    state[0] = b;
    state[1] = c;
    state[2] = d;
  }
}
