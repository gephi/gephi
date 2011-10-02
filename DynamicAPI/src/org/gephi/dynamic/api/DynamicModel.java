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
package org.gephi.dynamic.api;

import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Graph;

/**
 * Model that maintains the dynamic states of the application, which include the
 * minimum and the maximum bounds, as well as the current visible interval.
 * <p>
 * The min and the max are used to know what are the limits of the time in the
 * current data. The visible interval is typically configured by a timeline
 * component to select a range of time. The model also maintains what is the
 * current time format, either <code>DOUBLE</code> or <code>DATE</code>. Internally,
 * all times are double, but it can be converted to dates for user display. In
 * addition the model stores the current estimators used to compute dynamic
 * values.
 * <p>
 * The model is listening to graph and attributes events to track all intervals and
 * deduce minimum and maximum. It thows <code>MIN_CHANGED</code> or 
 * <code>MAX_CHANGED</code> events when these values are changed.
 * <p>
 * The model can also build {@link DynamicGraph} objets on demand. These objects
 * can work independently to states of this model.
 * 
 * @author Cezary Bartosiak
 * @author Mathieu Bastian
 *
 * @see DynamicController
 */
public interface DynamicModel {

    /**
     * The name of the column containing time intervals.
     */
    public static final String TIMEINTERVAL_COLUMN = "time_interval";

    /**
     * The way the time is represented, either a simple real value (DOUBLE),
     * a date or a datetime.
     */
    public enum TimeFormat {

        DATE, DATETIME, DOUBLE
    };

    /**
     * Builds a new {@code DynamicGraph} from the given {@code Graph} instance.
     *
     * @param graph the underlying graph
     *
     * @return a new a new {@code DynamicGraph}.
     */
    public DynamicGraph createDynamicGraph(Graph graph);

    /**
     * Builds a new {@code DynamicGraph} from the given {@code Graph} instance
     * wrapping the given {@code Interval}.
     *
     * @param graph the underlying graph
     * @param interval the interval to filter the graph
     *
     * @return a new a new {@code DynamicGraph}.
     */
    public DynamicGraph createDynamicGraph(Graph graph, Interval interval);

    /**
     * Returns the time interval wrapped by the {@code DynamicGraph} of
     * the current workspace.
     *
     * @return the time interval wrapped by the {@code DynamicGraph} of
     * the current workspace.
     */
    public TimeInterval getVisibleInterval();

    /**
     * Returns the minimum of the time intervals defined in elements (i.e. nodes
     * and edges) in the current workspace. This minimum is updated when data
     * change and excludes <code>Double.NEGATIVE_INFINITY</code>.
     *
     * @return the minimum time in the current workspace
     */
    public double getMin();

    /**
     * Returns the maximum of the time intervals defined in elements (i.e. nodes
     * and edges) in the current workspace. This maximum is updated when data
     * change and excludes <code>Double.POSITIVE_INFINITY</code>.
     *
     * @return the maximum time in the current workspace
     */
    public double getMax();

    /**
     * Gets the current time format for this model. Though all time values are stored
     * in double numbers, the time format inform how this values should be
     * converted to display to users.
     * @return the current time format
     */
    public TimeFormat getTimeFormat();

    /**
     * Returns the current <code>ESTIMATOR</code>, used to get values from
     * {@link DynamicType}. Default is <b><code>Estimator.FIRST</code></b>.
     * <p>
     * See the {@link #getNumberEstimator()} method for number types.
     * @return the current estimator
     */
    public Estimator getEstimator();

    /**
     * Returns the current number <code>ESTIMATOR</code>, used to get values
     * from number {@link DynamicType}, like {@link DynamicInteger}. Default is
     * <b><code>Estimator.AVERAGE</code></b>.
     * <p>
     * See the {@link #getEstimator()} method for non-number types.
     * @return the current number estimator
     */
    public Estimator getNumberEstimator();

    /**
     * Returns <code>true</code> if the graph in the current workspace is dynamic,
     * i.e. when the graph has either dynamic topology, attributes or both.
     * @return <code>true</code> if the graph is dynamic, <code>false</code>
     * otherwise
     */
    public boolean isDynamicGraph();
    
    /**
     * Returns <code>true</code> if the graph in the current workspace has dynamic
     * nodes. In other words if nodes are added or removed dynamically.
     * @return  <code>true</code> if the graph has dynamic nodes
     */
    public boolean hasDynamicNodes();
    
    /**
     * Returns <code>true</code> if the graph in the current workspace has dynamic
     * edges. In other words if edges are added or removed dynamically.
     * @return  <code>true</code> if the graph has dynamic edges
     */
    public boolean hasDynamicEdges();
}
