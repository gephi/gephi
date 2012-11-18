/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
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
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * The wrapper for graph and time interval.
 *
 * @author Cezary Bartosiak
 */
public interface DynamicGraph {
	/**
	 * Returns values of attributes of the given {@code Node} in the given
	 * {@code point} of time using {@code Estimator.FIRST} for each dynamic
	 * attribute.
	 *
	 * @param node  the given {@code Node}
	 * @param point the given point of time
	 *
	 * @return values of attributes of the given {@code Node} in the given
	 *         {@code point} of time using {@code Estimator.FIRST} for
	 *         each dynamic attribute.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 */
	public Object[] getAttributesValues(Node node, double point);

	/**
	 * Returns values of attributes of the given {@code Node} in the given
	 * {@code point} of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
	 *
	 * <p>Note that it doesn't matter what estimators you give for 'static'
	 * attributes.
	 *
	 * @param node       the given {@code Node}
	 * @param point      the given point of time
	 * @param estimators determine how to estimate individual values
	 *
	 * @return values of attributes of the given {@code Node} in the given
	 *         {@code point} of time using given {@code Estimators}.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 * @throws IllegalArgumentException if the length of the {@code estimators}
	 *                                  table differ from the count of attributes.
	 */
	public Object[] getAttributesValues(Node node, double point, Estimator[] estimators);

	/**
	 * Returns values of attributes of the given {@code Node} in the given
	 * time interval of time using {@code Estimator.FIRST} for
	 * each dynamic attribute. Both bounds are included by default.
	 *
	 * @param node the given {@code Node}
	 * @param low  the left endpoint of the given time interval
	 * @param high the right endpoint of the given time interval
	 *
	 * @return values of attributes of the given {@code Node} in the given
	 *         time interval of time using {@code Estimator.FIRST} for
	 *         each dynamic attribute.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 */
	public Object[] getAttributesValues(Node node, double low, double high);

	/**
	 * Returns values of attributes of the given {@code Node} in the given
	 * time interval of time using {@code Estimator.FIRST} for
	 * each dynamic attribute.
	 *
	 * @param node     the given {@code Node}
	 * @param interval the given time interval
	 *
	 * @return values of attributes of the given {@code Node} in the given
	 *         time interval of time using {@code Estimator.FIRST} for
	 *         each dynamic attribute.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 */
	public Object[] getAttributesValues(Node node, Interval interval);

	/**
	 * Returns values of attributes of the given {@code Node} in the given
	 * time interval of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
	 * Both bounds are included by default.
	 *
	 * <p>Note that it doesn't matter what estimators you give for 'static'
	 * attributes.
	 *
	 * @param node       the given {@code Node}
	 * @param low        the left endpoint of the given time interval
	 * @param high       the right endpoint of the given time interval
	 * @param estimators determine how to estimate individual values
	 *
	 * @return values of attributes of the given {@code Node} in the given
	 *         time interval of time using given {@code Estimators}.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 * @throws IllegalArgumentException if the length of the {@code estimators}
	 *                                  table differ from the count of attributes.
	 */
	public Object[] getAttributesValues(Node node, double low, double high, Estimator[] estimators);

	/**
	 * Returns values of attributes of the given {@code Node} in the given
	 * time interval of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
	 *
	 * <p>Note that it doesn't matter what estimators you give for 'static'
	 * attributes.
	 *
	 * @param node       the given {@code Node}
	 * @param interval   the given time interval
	 * @param estimators determine how to estimate individual values
	 *
	 * @return values of attributes of the given {@code Node} in the given
	 *         time interval of time using given {@code Estimators}.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 * @throws IllegalArgumentException if the length of the {@code estimators}
	 *                                  table differ from the count of attributes.
	 */
	public Object[] getAttributesValues(Node node, Interval interval, Estimator[] estimators);

	/**
	 * Returns values of attributes of the given {@code Edge} in the given
	 * {@code point} of time using {@code Estimator.FIRST} for
	 * each dynamic attribute.
	 *
	 * @param edge  the given {@code Edge}
	 * @param point the given point of time
	 *
	 * @return values of attributes of the given {@code Edge} in the given
	 *         {@code point} of time using {@code Estimator.FIRST} for
	 *         each dynamic attribute.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 */
	public Object[] getAttributesValues(Edge edge, double point);

	/**
	 * Returns values of attributes of the given {@code Edge} in the given
	 * {@code point} of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
	 *
	 * <p>Note that it doesn't matter what estimators you give for 'static'
	 * attributes.
	 *
	 * @param edge       the given {@code Edge}
	 * @param point      the given point of time
	 * @param estimators determine how to estimate individual values
	 *
	 * @return values of attributes of the given {@code Edge} in the given
	 *         {@code point} of time using given {@code Estimators}.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 * @throws IllegalArgumentException if the length of the {@code estimators}
	 *                                  table differ from the count of attributes.
	 */
	public Object[] getAttributesValues(Edge edge, double point, Estimator[] estimators);

	/**
	 * Returns values of attributes of the given {@code Edge} in the given
	 * time interval of time using {@code Estimator.FIRST} for
	 * each dynamic attribute. Both bounds are included by default.
	 *
	 * @param edge the given {@code Edge}
	 * @param low  the left endpoint of the given time interval
	 * @param high the right endpoint of the given time interval
	 *
	 * @return values of attributes of the given {@code Edge} in the given
	 *         time interval of time using {@code Estimator.FIRST} for
	 *         each dynamic attribute.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 */
	public Object[] getAttributesValues(Edge edge, double low, double high);

	/**
	 * Returns values of attributes of the given {@code Edge} in the given
	 * time interval of time using {@code Estimator.FIRST} for
	 * each dynamic attribute.
	 *
	 * @param edge     the given {@code Edge}
	 * @param interval the given time interval
	 *
	 * @return values of attributes of the given {@code Edge} in the given
	 *         time interval of time using {@code Estimator.FIRST} for
	 *         each dynamic attribute.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 */
	public Object[] getAttributesValues(Edge edge, Interval interval);

	/**
	 * Returns values of attributes of the given {@code Edge} in the given
	 * time interval of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
	 *
	 * <p>Note that it doesn't matter what estimators you give for 'static'
	 * attributes.
	 *
	 * @param edge       the given {@code Edge}
	 * @param low        the left endpoint of the given time interval
	 * @param high       the right endpoint of the given time interval
	 * @param estimators determine how to estimate individual values
	 *
	 * @return values of attributes of the given {@code Edge} in the given
	 *         time interval of time using given {@code Estimators}.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 * @throws IllegalArgumentException if the length of the {@code estimators}
	 *                                  table differ from the count of attributes.
	 */
	public Object[] getAttributesValues(Edge edge, double low, double high, Estimator[] estimators);

	/**
	 * Returns values of attributes of the given {@code Edge} in the given
	 * time interval of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
	 *
	 * <p>Note that it doesn't matter what estimators you give for 'static'
	 * attributes.
	 *
	 * @param edge       the given {@code Edge}
	 * @param interval   the given time interval
	 * @param estimators determine how to estimate individual values
	 *
	 * @return values of attributes of the given {@code Edge} in the given
	 *         time interval of time using given {@code Estimators}.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 * @throws IllegalArgumentException if the length of the {@code estimators}
	 *                                  table differ from the count of attributes.
	 */
	public Object[] getAttributesValues(Edge edge, Interval interval, Estimator[] estimators);

	/**
	 * Returns the left endpoint of the time interval wrapped by this
	 * {@code DynamicGraph}.
	 *
	 * @return the left endpoint of the time interval wrapped by this
	 *         {@code DynamicGraph}.
	 */
	public double getLow();

	/**
	 * Returns the right endpoint of the time interval wrapped by this
	 * {@code DynamicGraph}.
	 *
	 * @return the right endpoint of the time interval wrapped by this
	 *         {@code DynamicGraph}.
	 */
	public double getHigh();

	/**
	 * Returns a "snapshot graph", i.e. a graph for the given point of time.
	 * The default estimator is used ({@code Estimator.FIRST}). It means
	 * that the first time intervals of nodes/edges are checked for
	 * overlapping with the {@code point}.
	 *
	 * @param point the given point of time
	 *
	 * @return a "snapshot graph", i.e. a graph for the given point of time.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 */
	public Graph getSnapshotGraph(double point);

	/**
	 * Returns a "snapshot graph", i.e. a graph for the given point of time
	 * using the given {@code Estimator}. It means that time intervals of
	 * nodes/edges determined by the {@code Estimator} are checked for
	 * overlapping with the {@code point}.
	 *
	 * @param point     the given point of time
	 * @param estimator determines how to estimate a snapshot
	 *
	 * @return a "snapshot graph", i.e. a graph for the given point of time.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 */
	public Graph getSnapshotGraph(double point, Estimator estimator);

	/**
	 * Returns a "snapshot graph", i.e. a graph for the given time interval.
	 * The default estimator is used ({@code Estimator.FIRST}). It means
	 * that the first time intervals of nodes/edges are checked for
	 * overlapping with the time interval [{@code low}, {@code high}].
	 *
	 * @param low  the left endpoint of the given time interval
	 * @param high the right endpoint of the given time interval
	 *
	 * @return a "snapshot graph", i.e. a graph for the given time interval.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 */
	public Graph getSnapshotGraph(double low, double high);

	/**
	 * Returns a "snapshot graph", i.e. a graph for the given time interval.
	 * The default estimator is used ({@code Estimator.FIRST}). It means
	 * that the first time intervals of nodes/edges are checked for
	 * overlapping with the given time interval.
	 *
	 * @param interval the given time interval
	 *
	 * @return a "snapshot graph", i.e. a graph for the given time interval.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 */
	public Graph getSnapshotGraph(Interval interval);

	/**
	 * Returns a "snapshot graph", i.e. a graph for the given time interval
	 * using the given {@code Estimator}. It means that time intervals of
	 * nodes/edges determined by the {@code Estimator} are checked for
	 * overlapping with the time interval [{@code low}, {@code high}].
	 *
	 * @param low       the left endpoint of the given time interval
	 * @param high      the right endpoint of the given time interval
	 * @param estimator determines how to estimate a snapshot
	 *
	 * @return a "snapshot graph", i.e. a graph for the given time interval.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 */
	public Graph getSnapshotGraph(double low, double high, Estimator estimator);

	/**
	 * Returns a "snapshot graph", i.e. a graph for the given time interval
	 * using the given {@code Estimator}. It means that time intervals of
	 * nodes/edges determined by the {@code Estimator} are checked for
	 * overlapping with the given time interval.
	 *
	 * @param interval  the given time interval
	 * @param estimator determines how to estimate a snapshot
	 *
	 * @return a "snapshot graph", i.e. a graph for the given time interval.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 */
	public Graph getSnapshotGraph(Interval interval, Estimator estimator);

	/**
	 * Returns a "strong snapshot graph", i.e. a graph for the given point of
	 * time. "Strong" means that if EVERY time interval of considered node/edge
	 * overlaps with the {@code point} it is considered as a part of snapshot.
	 *
	 * @param point the given point of time
	 *
	 * @return a "snapshot graph", i.e. a graph for the given point of time.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 */
	public Graph getStrongSnapshotGraph(double point);

	/**
	 * Returns a "strong snapshot graph", i.e. a graph for the given time interval.
	 * "Strong" means that if EVERY time interval of considered node/edge overlaps
	 * with the time interval [{@code low}, {@code high}] it is considered as
	 * a part of snapshot.
	 *
	 * @param low  the left endpoint of the given time interval
	 * @param high the right endpoint of the given time interval
	 *
	 * @return a "snapshot graph", i.e. a graph for the given time interval.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high} or the
	 *                                  time interval [{@code low}, {@code high}]
	 *                                  is out of range wrapped by this DynamicGraph.
	 */
	public Graph getStrongSnapshotGraph(double low, double high);

	/**
	 * Returns a "strong snapshot graph", i.e. a graph for the given time interval.
	 * "Strong" means that if EVERY time interval of considered node/edge overlaps
	 * with the given time interval it is considered as a part of snapshot.
	 *
	 * @param interval the given time interval
	 *
	 * @return a "snapshot graph", i.e. a graph for the given time interval.
	 *
	 * @throws IllegalArgumentException if the given time interval is out of
	 *                                  range wrapped by this DynamicGraph.
	 */
	public Graph getStrongSnapshotGraph(Interval interval);

	/**
	 * Returns the wrapped graph.
	 *
	 * @return the wrapped graph.
	 */
	public Graph getUnderlyingGraph();

	/**
	 * Returns the time interval wrapped by this {@code DynamicGraph}.
	 *
	 * @return the time interval wrapped by this {@code DynamicGraph}.
	 */
	public TimeInterval getInterval();

	/**
	 * Sets the time interval wrapped by this {@code DynamicGraph}.
	 *
	 * @param interval an object to get endpoints from
	 */
	public void setInterval(TimeInterval interval);

	/**
	 * Sets the time interval wrapped by this {@code DynamicGraph}.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 */
	public void setInterval(double low, double high);
}
