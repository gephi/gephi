/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 * 
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic.api;

import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicType;
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
     * The way the time is represented, either a simple real value (DOUBLE) or
     * a date.
     */
    public enum TimeFormat {

        DATE, DOUBLE
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
     * wrapping the given {@code TimeInterval}.
     *
     * @param graph the underlying graph
     *
     * @return a new a new {@code DynamicGraph}.
     */
    public DynamicGraph createDynamicGraph(Graph graph, TimeInterval interval);

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
     * i.e. when the graph has either dynamic topology, attribute sor both.
     * @return <code>true</code> if the graph is dynamic, <code>false</code>
     * otherwise
     */
    public boolean isDynamicGraph();
}
