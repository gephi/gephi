/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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

/**
 * Timeline model which holds timeline bounds, interval and animation flags.
 * <p>
 * The timeline controls the currently selected time interval, accessible in the
 * <code>DynamicAPI</code>. When enabled, it maintains the time scale and the position
 * of the interval.
 * <p>
 * It also holds configuration values for animation such as speed and step size.
 * 
 * @author Julian Bilcke, Mathieu Bastian
 */
public interface TimelineModel {

    /**
     * Defines how the interval is moved when animating.
     */
    public enum PlayMode {

        /**
         * Only one bound of the interval is moved. The interval is therefore resized.
         */
        ONE_BOUND,
        /**
         * Both interval bounds are moved. The interval size remains unchanged.
         */
        TWO_BOUNDS
    }

    /**
     * Returns <code>true</code> if the timeline is enabled. When enabled, the timeline
     * is filtering the current graph.
     * @return <code>true</code> if the timeline is enabled, <code>false</code>
     * otherwise
     */
    public boolean isEnabled();

    /**
     * Returns the min value of the time scale. This is the start of the earliest interval
     * in the workspace.
     * @return the min
     */
    public double getMin();

    /**
     * Returns the max value of the time scale. This is the end of the latest interval
     * in the workspace.
     * @return the max
     */
    public double getMax();

    /**
     * Returns the custom min value. This value can't be inferior than <code>min</code>
     * @return the custom min
     */
    public double getCustomMin();

    /**
     * Returns the custom max value. This value can't be superior than <code>max</code>
     * @return the custom max
     */
    public double getCustomMax();

    /**
     * Returns <code>true</code> if custom bounds are defined. Returns <code>false</code>
     * when custom bounds are equal to <code>min</code> and <code>max</code>.
     * @return <code>true</code> if custom bounds are defined, <code>false</code>
     * otherwise.
     */
    public boolean hasCustomBounds();

    /**
     * Returns <code>true</code> if none of the min and max time values are infinity.
     * @return <code>true</code> if the time scale is valid, <code>false</code>
     * otherwise
     */
    public boolean hasValidBounds();

    /**
     * Returns the lower bound of the interval.
     * @return the interval start
     */
    public double getIntervalStart();

    /**
     * Returns the upper bound of the interval.
     * @return the interval end
     */
    public double getIntervalEnd();

    /**
     * Returns the current time format. Default is <code>DOUBLE</code>
     * @return the current tie
     */
    public TimeFormat getTimeFormat();

    /**
     * Returns the play delay in milliseconds. Defines the time between each interval
     * shift.
     * @return the play delay 
     */
    public int getPlayDelay();

    /**
     * Returns the play step. Defines how much the interval is moved at each step
     * during animation. Defined in percentage of the total interval.
     * @return the play step
     */
    public double getPlayStep();

    /**
     * Returns <code>true</code> if the timeline is playing.
     * @return <code>true</code> is playing, <code>false</code> otherwise
     */
    public boolean isPlaying();

    /**
     * Returns the play mode. This defines how the interval is moved.
     * @return the play mode
     */
    public PlayMode getPlayMode();

    /**
     * Returns the current timeline chart or <code>null</code> if node.
     * @return the timeline chart or <code>null</code>
     */
    public TimelineChart getChart();
}
