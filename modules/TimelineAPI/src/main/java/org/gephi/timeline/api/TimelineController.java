/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
Website : http://www.gephi.org

This file is part of Gephi.

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
package org.gephi.timeline.api;

import org.gephi.graph.api.TimeFormat;
import org.gephi.project.api.Workspace;

/**
 * Controls the timeline bounds and animation features.
 * <p>
 * By default the timeline is disabled and can be enabled with the
 * <code>setEnabled()</code> method. Once enabled, the controller is setting its
 * interval value to the {@link DynamicModel}.
 * <p>
 * The interval can be animated using the <code>startPlay()</code> and
 * <code>stopPlay()</code> methods. Configuration parameters are also available.
 * <p>
 * This controller also allows to lookup graph attribute columns that can be
 * used as sparklines (e.g. node count, average degree...). Use the
 * <code>selectColumn()</code> to create a {@link TimelineChart} accessible from
 * the <code>TimelineModel</code>.
 *
 * @author Julian Bilcke, Mathieu Bastian
 * @see TimelineModel
 */
public interface TimelineController {

    /**
     * Returns the timeline model from <code>workspace</code>.
     *
     * @param workspace the workspace to get the model from
     * @return the timeline model for this workspace
     */
    public TimelineModel getModel(Workspace workspace);

    /**
     * Get the current model from the current workspace
     *
     * @return the current model, or <code>null</code> if no active workspace
     */
    public TimelineModel getModel();

    /**
     * Sets the timeline custom bounds. Custom bounds still need to be included
     * in the min and max bound of the time scale. The timeline will resize
     * accordingly.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @throws IllegalArgumentException if <code>min<code> is superior or equal than
     * <code>max</code> or out of bounds
     */
    public void setCustomBounds(double min, double max);

    /**
     * Sets the timeline enable status.
     *
     * @param enabled the enabled value to set
     */
    public void setEnabled(boolean enabled);

    /**
     * Sets the current timeline interval. This is propagated to the
     * <code>DynamicModel</code> and defines the interval the graph is filtered
     * with.
     *
     * @param from the lower bound
     * @param to the upper bound
     * @throws IllegalArgumentException if <code>min<code> is superior or equal than
     * <code>max</code> or out of bounds
     */
    public void setInterval(double from, double to);
    
    public void setTimeFormat(TimeFormat timeFormat);
    

    /**
     * Starts the timeline animation using the current delay, step size and play
     * mode.
     */
    public void startPlay();

    /**
     * Stops the timeline animation.
     */
    public void stopPlay();

    /**
     * Sets the play delay in milliseconds. Defines the time between each
     * interval shift.
     *
     * @param delay the delay in milliseconds
     */
    public void setPlaySpeed(int delay);

    /**
     * Sets the play step. Defines how much the interval is moved at each step
     * during animation. Defined in percentage of the total interval.
     *
     * @param step the step, between 0 and 1
     */
    public void setPlayStep(double step);

    /**
     * Sets the play mode. This defines how the interval is moved.
     *
     * @param playMode the play mode
     */
    public void setPlayMode(TimelineModel.PlayMode playMode);

    /**
     * Returns all the possible dynamic attribute columns. This is essentially
     * all number-based dynamic columns defined in the graph table.
     *
     * @return all dynamic number columns in the graph table
     */
    public String[] getDynamicGraphColumns();

    /**
     * Select a column to make a {@link TimelineChart} of it. The column must be
     * member of the graph table.
     *
     * @param column the column to select
     * @throws IllegalArgumentException if <code>column</code> is not a graph
     * column
     */
    public void selectColumn(String column);

    /**
     * Add <code>listener</code> to the list of event listerners.
     *
     * @param listener the listener to add
     */
    public void addListener(TimelineModelListener listener);

    /**
     * Remove <code>listerner</code> from the list of event listeners.
     *
     * @param listener the listener to remove
     */
    public void removeListener(TimelineModelListener listener);
}
