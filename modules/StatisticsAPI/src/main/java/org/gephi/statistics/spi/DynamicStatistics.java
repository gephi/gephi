/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.statistics.spi;

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;

/**
 * Define a dynamic statistics implementation. A Dynamic Statistics uses
 * a sliding window on a dynamic network to compute results.
 * <p>
 * The dynamic statistic execution is a three-steps process:
 * <ol><li>The <code>execute()</code> method is called to init the statistic
 * with the graph and attribute model.</li>
 * <li>For every interval the <code>loop()</code> method is called with the
 * network at this interval as parameter.</li>
 * <li>The <code>end()</code> method is finally called.</li></ol>
 * <p>
 * 
 * @author Mathieu Bastian
 */
public interface DynamicStatistics extends Statistics {

    /**
     * First method to be executed in the dynamic statistic process. Initialize
     * the statistics with the graph and attributes. The graph model holds the
     * graph structure and the attribute model the attribute columns.
     * @param graphModel the graph model
     */
    @Override
    public void execute(GraphModel graphModel);

    /**
     * Iteration of the dynamic statistics algorithm on a new interval. The 
     * graph window is a snapshot of the graph at the current <code>interval</code>.
     * @param window a snapshot of the graph at the current interval
     * @param interval the interval of the current snapshot
     */
    public void loop(GraphView window, Interval interval);

    /**
     * Called at the end of the process after all loops.
     */
    public void end();

    /**
     * Sets the minimum and maximum bound
     * @param bounds the min and max bounds
     */
    public void setBounds(Interval bounds);

    /**
     * Sets the window duration
     * @param window the window duration
     */
    public void setWindow(double window);

    /**
     * Sets the tick. The tick is how much the window is moved to the right 
     * at each iteration.
     * @param tick the tick
     */
    public void setTick(double tick);

    /**
     * Returns the window duration
     * @return the window duration
     */
    public double getWindow();

    /**
     * Returns the tick. The tick is how much the window is moved to the right 
     * at each iteration.
     * @return the tick
     */
    public double getTick();

    /**
     * Returns the min and max bounds.
     * @return the bounds
     */
    public Interval getBounds();
}
