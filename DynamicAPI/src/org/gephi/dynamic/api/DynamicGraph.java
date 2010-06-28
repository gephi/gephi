/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
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
	 * each dynamic attribute.
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
	 * time interval of time using given {@code Estimators}. The length of the
	 * {@code estimators} table must be the same as the count of attributes.
	 * Otherwise an {@code IllegalArgumentException} will be thrown.
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
	 * each dynamic attribute.
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
	 * Returns a "weak snapshot graph", i.e. a graph for the given point of
	 * time. "Weak" means that if any time interval of considered node/edge
	 * overlaps with the {@code point} it is considered as a part of snapshot.
	 *
	 * @param point the given point of time
	 *
	 * @return a "snapshot graph", i.e. a graph for the given point of time.
	 *
	 * @throws IllegalArgumentException if {@code point} is out of range
	 *                                  wrapped by this {@code DynamicGraph}.
	 */
	public Graph getWeakSnapshotGraph(double point);

	/**
	 * Returns a "weak snapshot graph", i.e. a graph for the given time interval.
	 * "Weak" means that if any time interval of considered node/edge overlaps
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
	public Graph getWeakSnapshotGraph(double low, double high);

	/**
	 * Returns a "strong snapshot graph", i.e. a graph for the given point of
	 * time. "Strong" means that if every time interval of considered node/edge
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
	 * "Strong" means that if every time interval of considered node/edge overlaps
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
	 * Returns the wrapped graph.
	 *
	 * @return the wrapped graph.
	 */
	public Graph getUnderlyingGraph();
}
