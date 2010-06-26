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
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

/**
 * The wrapper for graph and time interval.
 *
 * @author Cezary Bartosiak
 */
public class DynamicGraph {
	private static final String DYNAMIC_RANGE = "dynamicrange";

	private GraphModel model;
	private GraphView  view;
	private double     low;
	private double     high;

	/**
	 * Constructs a new {@code DynamicGraph} that wraps a given {@code Graph}.
	 * The time interval is [{@code -infinity}, {@code +infinity}].
	 *
	 * @param graph wrapped {@code Graph}
	 */
	public DynamicGraph(Graph graph) {
		this(graph, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Constructs a new {@code DynamicGraph} that wraps a given {@code Graph}
	 * and a time interval [{@code low}, {@code high}].
	 *
	 * @param graph wrapped {@code Graph}
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public DynamicGraph(Graph graph, double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
					"The left endpoint of the interval must be less than " +
					"the right endpoint.");

		model = graph.getGraphModel();
		view  = model.newView();

		this.low  = low;
		this.high = high;

		Graph vgraph = model.getGraph(view);
		for (Node n : vgraph.getNodes()) {
			TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (!ti.isInRange(low, high))
				vgraph.removeNode(n);
		}
		for (Edge e : vgraph.getEdges()) {
			TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (!ti.isInRange(low, high))
				vgraph.removeEdge(e);
		}
	}

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
	public Object[] getAttributesValues(Node node, double point) {
		int count = node.getNodeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(node, point, estimators);
	}

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
	public Object[] getAttributesValues(Node node, double point, Estimator[] estimators) {
		checkPoint(point);
		return getAttributesValues(node, point, point, estimators);
	}

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
	public Object[] getAttributesValues(Node node, double low, double high) {
		int count = node.getNodeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(node, low, high, estimators);
	}

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
	public Object[] getAttributesValues(Node node, double low, double high, Estimator[] estimators) {
		checkLowHigh(low, high);
		checkEstimators(node, estimators);

		Attributes attributes = node.getNodeData().getAttributes();
		Object[]   values     = new Object[attributes.countValues()];

		for (int i = 0; i < attributes.countValues(); ++i) {
			values[i] = attributes.getValue(i);
			if (values[i] instanceof DynamicType)
				values[i] = ((DynamicType)values[i]).getValue(estimators[i]);
		}

		return values;
	}

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
	public Object[] getAttributesValues(Edge edge, double point) {
		int count = edge.getEdgeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(edge, point, estimators);
	}

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
	public Object[] getAttributesValues(Edge edge, double point, Estimator[] estimators) {
		checkPoint(point);
		return getAttributesValues(edge, point, point, estimators);
	}

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
	public Object[] getAttributesValues(Edge edge, double low, double high) {
		int count = edge.getEdgeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(edge, low, high, estimators);
	}

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
	public Object[] getAttributesValues(Edge edge, double low, double high, Estimator[] estimators) {
		checkLowHigh(low, high);
		checkEstimators(edge, estimators);

		Attributes attributes = edge.getEdgeData().getAttributes();
		Object[]   values     = new Object[attributes.countValues()];

		for (int i = 0; i < attributes.countValues(); ++i) {
			values[i] = attributes.getValue(i);
			if (values[i] instanceof DynamicType)
				values[i] = ((DynamicType)values[i]).getValue(estimators[i]);
		}

		return values;
	}

	/**
	 * Returns the left endpoint of the time interval wrapped by this
	 * {@code DynamicGraph}.
	 *
	 * @return the left endpoint of the time interval wrapped by this
	 *         {@code DynamicGraph}.
	 */
	public double getLow() {
		return low;
	}

	/**
	 * Returns the right endpoint of the time interval wrapped by this
	 * {@code DynamicGraph}.
	 *
	 * @return the right endpoint of the time interval wrapped by this
	 *         {@code DynamicGraph}.
	 */
	public double getHigh() {
		return high;
	}

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
	public Graph getSnapshotGraph(double point) {
		return getSnapshotGraph(point, Estimator.FIRST);
	}

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
	public Graph getSnapshotGraph(double point, Estimator estimator) {
		checkPoint(point);
		return getSnapshotGraph(point, point, estimator);
	}

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
	public Graph getSnapshotGraph(double low, double high) {
		return getSnapshotGraph(low, high, Estimator.FIRST);
	}

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
	public Graph getSnapshotGraph(double low, double high, Estimator estimator) {
		checkLowHigh(low, high);
		Graph graph  = model.getGraph();
		Graph vgraph = model.getGraph(view);
		for (Node n : graph.getNodes()) {
			TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (ti.getValue(low, high, estimator) == null && vgraph.contains(n))
				vgraph.removeNode(n);
			else if (ti.getValue(low, high, estimator) != null && !vgraph.contains(n))
				vgraph.addNode(n);
		}
		for (Edge e : graph.getEdges()) {
			TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (ti.getValue(low, high, estimator) == null && vgraph.contains(e))
				vgraph.removeEdge(e);
			else if (ti.getValue(low, high, estimator) != null && !vgraph.contains(e))
				vgraph.addEdge(e);
		}
		return vgraph;
	}

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
	public Graph getWeakSnapshotGraph(double point) {
		checkPoint(point);
		return getWeakSnapshotGraph(point, point);
	}

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
	public Graph getWeakSnapshotGraph(double low, double high) {
		checkLowHigh(low, high);
		Graph graph  = model.getGraph();
		Graph vgraph = model.getGraph(view);
		for (Node n : graph.getNodes()) {
			TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (ti.getValues(low, high).isEmpty() && vgraph.contains(n))
				vgraph.removeNode(n);
			else if (!ti.getValues(low, high).isEmpty() && !vgraph.contains(n))
				vgraph.addNode(n);
		}
		for (Edge e : graph.getEdges()) {
			TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (ti.getValues(low, high).isEmpty() && vgraph.contains(e))
				vgraph.removeEdge(e);
			else if (!ti.getValues(low, high).isEmpty() && !vgraph.contains(e))
				vgraph.addEdge(e);
		}
		return vgraph;
	}

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
	public Graph getStrongSnapshotGraph(double point) {
		checkPoint(point);
		return getStrongSnapshotGraph(point, point);
	}

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
	public Graph getStrongSnapshotGraph(double low, double high) {
		checkLowHigh(low, high);
		Graph graph  = model.getGraph();
		Graph vgraph = model.getGraph(view);
		for (Node n : graph.getNodes()) {
			TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (ti.getValues(low, high).size() < ti.getValues().size() && vgraph.contains(n))
				vgraph.removeNode(n);
			else if (ti.getValues(low, high).size() == ti.getValues().size() && !vgraph.contains(n))
				vgraph.addNode(n);
		}
		for (Edge e : graph.getEdges()) {
			TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(DYNAMIC_RANGE);
			if (ti.getValues(low, high).size() < ti.getValues().size() && vgraph.contains(e))
				vgraph.removeEdge(e);
			else if (ti.getValues(low, high).size() == ti.getValues().size() && !vgraph.contains(e))
				vgraph.addEdge(e);
		}
		return vgraph;
	}

	/**
	 * Returns the wrapped graph.
	 *
	 * @return the wrapped graph.
	 */
	public Graph getUnderlyingGraph() {
		return model.getGraph();
	}

	private void checkPoint(double point) {
		if (point < low || point > high)
			throw new IllegalArgumentException(
					"The point cannot be out of range " +
					"wrapped by this DynamicGraph");
	}

	private void checkLowHigh(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
					"The left endpoint of the interval must be less than " +
					"the right endpoint.");
		if (high < this.low || low > this.high)
			throw new IllegalArgumentException(
					"The time interval [low, high] cannot be out of range " +
					"wrapped by this DynamicGraph");
	}

	private void checkEstimators(Node node, Estimator[] estimators) {
		int count = node.getNodeData().getAttributes().countValues();
		if (count != estimators.length)
			throw new IllegalArgumentException(
					"The length of the estimators table must be the same as " +
					"the count of attributes.");
	}

	private void checkEstimators(Edge edge, Estimator[] estimators) {
		int count = edge.getEdgeData().getAttributes().countValues();
		if (count != estimators.length)
			throw new IllegalArgumentException(
					"The length of the estimators table must be the same as " +
					"the count of attributes.");
	}

	/**
	 * Compares this instance with the specified object for equality.
	 *
	 * @param obj object to which this instance is to be compared
	 *
	 * @return {@code true} if and only if the specified {@code Object} is a
	 *         {@code DynamicGraph} which has the 'equal' graph.
	 *
     * @see #hashCode
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass()) &&
				((DynamicGraph)obj).model.getGraph().equals(model.getGraph()))
			return true;
		return false;
	}

	/**
	 * Returns a hashcode of this instance.
	 *
	 * @return a hashcode of this instance.
	 */
	@Override
	public int hashCode() {
		return model.getGraph().hashCode();
	}

	/**
	 * Returns a string representation of this instance in a format
	 * provided by the underlying graph.
	 *
	 * @return a string representation of this instance.
	 */
	@Override
	public String toString() {
		return model.getGraph().toString();
	}
}
