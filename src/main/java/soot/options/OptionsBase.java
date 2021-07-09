package soot.options;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Ondrej Lhotak
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

import java.util.Deque;
import java.util.LinkedList;
import java.util.StringTokenizer;

import soot.Pack;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.plugins.internal.PluginLoader;

/**
 * Soot command-line options parser base class.
 *
 * @author Ondrej Lhotak
 */
abstract class OptionsBase {

  private final Deque<String> options = new LinkedList<>();
  protected LinkedList<String> classes = new LinkedList<String>();

  private String pad(int initial, String opts, int tab, String desc) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < initial; i++) {
      b.append(' ');
    }
    b.append(opts);
    int i;
    if (tab <= opts.length()) {
      b.append('\n');
      i = 0;
    } else {
      i = opts.length() + initial;
    }
    for (; i <= tab; i++) {
      b.append(' ');
    }
    for (StringTokenizer t = new StringTokenizer(desc); t.hasMoreTokens();) {
      String s = t.nextToken();
      if (i + s.length() > 78) {
        b.append('\n');
        i = 0;
        for (; i <= tab; i++) {
          b.append(' ');
        }
      }
      b.append(s);
      b.append(' ');
      i += s.length() + 1;
    }
    b.append('\n');
    return b.toString();
  }

  protected String padOpt(String opts, String desc) {
    return pad(1, opts, 30, desc);
  }

  protected String padVal(String vals, String desc) {
    return pad(4, vals, 32, desc);
  }

  protected String getPhaseUsage() {
    StringBuilder b = new StringBuilder();
    b.append("\nPhases and phase options:\n");
    for (Pack p : PackManager.v().allPacks()) {
      b.append(padOpt(p.getPhaseName(), p.getDeclaredOptions()));
      for (Transform ph : p) {
        b.append(padVal(ph.getPhaseName(), ph.getDeclaredOptions()));
      }
    }
    return b.toString();
  }

  protected void pushOption(String option) {
    options.push(option);
  }

  protected boolean hasMoreOptions() {
    return !options.isEmpty();
  }

  protected String nextOption() {
    return options.removeFirst();
  }

  public LinkedList<String> classes() {
    return classes;
  }

  public void addArgClass(String classname) {
    classes.add(classname);
    System.out.println("The classes in optionBase are: " + classes.toString());
  }

  public boolean setPhaseOption(String phase, String option) {
    return PhaseOptions.v().processPhaseOptions(phase, option);
  }

  /**
   * Handles the value of a plugin parameter.
   *
   * @param file
   *          the plugin parameter value.
   * @return {@code true} on success.
   */
  protected boolean loadPluginConfiguration(String file) {
    return PluginLoader.load(file);
  }
}
