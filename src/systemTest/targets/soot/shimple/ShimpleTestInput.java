package soot.shimple;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Timothy Hoffman
 */
public class ShimpleTestInput {

  public boolean sync() throws Exception {
    boolean success = false;
    IOException exc;
    for (int retry = 0; !success && retry < 2; retry++) {
      FileInputStream file = null;
      try {
        try {
          file = new FileInputStream("delete_me.txt");
          success = true;
        } finally {
          if (file != null) {
            file.close();
          }
        }
      } catch (IOException ioe) {
        exc = ioe;
        Thread.sleep(5);
      }
    }
    return success;
  }

  public Class<?> hasMethod() {
    Class<?> clazz = null;
    try {
      if (clazz == null) {
        clazz = ShimpleTestInput.class;
      }
      clazz.getDeclaredMethod("toString");
    } catch (NoSuchMethodException ex) {
    }
    return clazz;
  }

  public InputStream readProp() {
    InputStream is = null;
    try {
      synchronized (this) {
        File f = new File("in.properties");
        is = new FileInputStream(f);
      }
    } catch (FileNotFoundException ex) {
    }
    return is;
  }

  /**
   * Run this test from the command line as "java -cp soot\target\systemTest-target-classes soot.shimple.ShimpleTestInput"
   * and observe that it runs without error and prints "false" and then "true" to the command line.
   *
   * @param args
   *
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    System.out.println(new ShimpleTestInput().sync());
    System.out.println(new ShimpleTestInput().hasMethod() == ShimpleTestInput.class);
  }
}
