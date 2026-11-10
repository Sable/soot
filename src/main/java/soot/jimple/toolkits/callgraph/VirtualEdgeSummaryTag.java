package soot.jimple.toolkits.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Soot contributors
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

import soot.Kind;
import soot.jimple.toolkits.callgraph.VirtualEdgesSummaries.VirtualEdge;
import soot.tagkit.Tag;

/**
 * An analysis client may attach an already matched summary to one invoke statement.
 * This overrides signature-only summaries at that statement, without changing class
 * ownership or the handling of ordinary call targets. A null summary explicitly
 * suppresses the default virtual summary. The tag is analysis metadata, not bytecode.
 * Non-null summaries require {@link Kind#GENERIC_FAKE}, one direct target, and
 * direct parameter mappings. Source matching metadata is optional because the tag
 * already identifies the invocation.
 * Complete the summary before attaching this tag. Do not mutate its targets or
 * parameter mappings while call graph or points-to construction is using it.
 */
public final class VirtualEdgeSummaryTag implements Tag {
  public static final String NAME = "VirtualEdgeSummaryTag";
  private final VirtualEdge summary;

  public VirtualEdgeSummaryTag(VirtualEdge summary) {
    if (summary != null && summary.getEdgeType() != Kind.GENERIC_FAKE) {
      throw new IllegalArgumentException("Call-site summaries require GENERIC_FAKE edges");
    }
    // Edge does not retain which summary target produced it. Restrict the new
    // call-site API so mappings from two same-signature targets cannot mix.
    if (summary != null && (summary.getTargets().size() != 1
        || !(summary.getTargets().iterator().next() instanceof VirtualEdgesSummaries.DirectTarget))) {
      throw new IllegalArgumentException("Call-site summaries require one direct target");
    }
    this.summary = summary;
  }

  public VirtualEdge getSummary() {
    return summary;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
