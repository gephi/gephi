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
package org.gephi.dynamic;

import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
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
public final class DynamicGraphImpl implements DynamicGraph {
	private GraphModel model;
	private GraphView  sourceView;
	private GraphView  currentView;
	private double     low;
	private double     high;

	/**
	 * Constructs a new {@code DynamicGraph} that wraps a given {@code Graph}.
	 * The time interval is [{@code -infinity}, {@code +infinity}].
	 *
	 * @param graph wrapped {@code Graph}
	 */
	public DynamicGraphImpl(Graph graph) {
		this(graph, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Constructs a new {@code DynamicGraph} that wraps a given {@code Graph}
	 * and a time interval [{@code low}, {@code high}].
	 *
	 * @param graph wrapped {@code Graph}
	 * @param low   the left endpoint of the interval
	 * @param high  the right endpoint of the interval
	 *
	 * @throws NullPointerException     if {@code graph} is null.
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public DynamicGraphImpl(Graph graph, double low, double high) {
		if (graph == null)
			throw new NullPointerException("The graph cannot be null.");

		if (low > high)
			throw new IllegalArgumentException(
					"The left endpoint of the interval must be less than "
					+ "the right endpoint.");

		model       = graph.getGraphModel();
		sourceView  = graph.getView();
		currentView = model.copyView(sourceView);

		this.low  = low;
		this.high = high;

		if (low != Double.NEGATIVE_INFINITY || high != Double.POSITIVE_INFINITY) {
			Graph vgraph = model.getGraph(currentView);
			for (Node n : vgraph.getNodes().toArray()) {
				TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(
						DynamicModel.TIMEINTERVAL_COLUMN);
				if (!ti.isInRange(low, high))
					vgraph.removeNode(n);
			}
			for (Edge e : vgraph.getEdges().toArray()) {
				TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(
						DynamicModel.TIMEINTERVAL_COLUMN);
				if (!ti.isInRange(low, high))
					vgraph.removeEdge(e);
			}
		}
	}

	@Override
	public Object[] getAttributesValues(Node node, double point) {
		int count = node.getNodeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(node, point, estimators);
	}

	@Override
	public Object[] getAttributesValues(Node node, double point, Estimator[] estimators) {
		checkPoint(point);
		return getAttributesValues(node, point, point, estimators);
	}

	@Override
	public Object[] getAttributesValues(Node node, double low, double high) {
		checkLowHigh(low, high);
		return getAttributesValues(node, new Interval(low, high));
	}

	@Override
	public Object[] getAttributesValues(Node node, Interval interval) {
		int count = node.getNodeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(node, interval, estimators);
	}

	@Override
	public Object[] getAttributesValues(Node node, double low, double high, Estimator[] estimators) {
		checkLowHigh(low, high);
		return getAttributesValues(node, new Interval(low, high), estimators);
	}

	@Override
	public Object[] getAttributesValues(Node node, Interval interval, Estimator[] estimators) {
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

	@Override
	public Object[] getAttributesValues(Edge edge, double point) {
		int count = edge.getEdgeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(edge, point, estimators);
	}

	@Override
	public Object[] getAttributesValues(Edge edge, double point, Estimator[] estimators) {
		checkPoint(point);
		return getAttributesValues(edge, point, point, estimators);
	}

	@Override
	public Object[] getAttributesValues(Edge edge, double low, double high) {
		checkLowHigh(low, high);
		return getAttributesValues(edge, new Interval(low, high));
	}

	@Override
	public Object[] getAttributesValues(Edge edge, Interval interval) {
		int count = edge.getEdgeData().getAttributes().countValues();
		Estimator[] estimators = new Estimator[count];
		for (int i = 0; i < count; ++i)
			estimators[i] = Estimator.FIRST;
		return getAttributesValues(edge, interval, estimators);
	}

	@Override
	public Object[] getAttributesValues(Edge edge, double low, double high, Estimator[] estimators) {
		checkLowHigh(low, high);
		return getAttributesValues(edge, new Interval(low, high), estimators);
	}

	@Override
	public Object[] getAttributesValues(Edge edge, Interval interval, Estimator[] estimators) {
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

	@Override
	public double getLow() {
		return low;
	}

	@Override
	public double getHigh() {
		return high;
	}

	@Override
	public Graph getSnapshotGraph(double point) {
		return getSnapshotGraph(point, Estimator.FIRST);
	}

	@Override
	public Graph getSnapshotGraph(double point, Estimator estimator) {
		checkPoint(point);
		return getSnapshotGraph(point, point, estimator);
	}

	@Override
	public Graph getSnapshotGraph(double low, double high) {
		return getSnapshotGraph(low, high, Estimator.FIRST);
	}

	@Override
	public Graph getSnapshotGraph(Interval interval) {
		return getSnapshotGraph(interval, Estimator.FIRST);
	}

	@Override
	public Graph getSnapshotGraph(double low, double high, Estimator estimator) {
		checkLowHigh(low, high);
		return getSnapshotGraph(new Interval(low, high), Estimator.FIRST);
	}

	@Override
	public Graph getSnapshotGraph(Interval interval, Estimator estimator) {
		Graph graph  = model.getGraph(sourceView);
		Graph vgraph = model.getGraph(currentView);
		for (Node n : graph.getNodes().toArray()) {
			TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
			if (ti.getValue(interval, estimator) == null && vgraph.contains(n))
				vgraph.removeNode(n);
			else if (ti.getValue(interval, estimator) != null && !vgraph.contains(n))
				vgraph.addNode(n);
		}
		for (Edge e : graph.getEdges().toArray()) {
			TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
			if (ti.getValue(interval, estimator) == null && vgraph.contains(e))
				vgraph.removeEdge(e);
			else if (ti.getValue(interval, estimator) != null && !vgraph.contains(e) &&
					vgraph.contains(e.getSource()) && vgraph.contains(e.getTarget()))
				vgraph.addEdge(e);
		}
		return vgraph;
	}

	@Override
	public Graph getStrongSnapshotGraph(double point) {
		checkPoint(point);
		return getStrongSnapshotGraph(point, point);
	}

	@Override
	public Graph getStrongSnapshotGraph(double low, double high) {
		checkLowHigh(low, high);
		return getStrongSnapshotGraph(new Interval(low, high));
	}

	@Override
	public Graph getStrongSnapshotGraph(Interval interval) {
		Graph graph  = model.getGraph(sourceView);
		Graph vgraph = model.getGraph(currentView);
		for (Node n : graph.getNodes().toArray()) {
			TimeInterval ti = (TimeInterval)n.getNodeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
			if (ti.getValues(interval).size() < ti.getValues().size() && vgraph.contains(n))
				vgraph.removeNode(n);
			else if (ti.getValues(interval).size() == ti.getValues().size() && !vgraph.contains(n))
				vgraph.addNode(n);
		}
		for (Edge e : graph.getEdges().toArray()) {
			TimeInterval ti = (TimeInterval)e.getEdgeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
			if (ti.getValues(interval).size() < ti.getValues().size() && vgraph.contains(e))
				vgraph.removeEdge(e);
			else if (ti.getValues(interval).size() == ti.getValues().size() && !vgraph.contains(e) &&
					vgraph.contains(e.getSource()) && vgraph.contains(e.getTarget()))
				vgraph.addEdge(e);
		}
		return vgraph;
	}

	@Override
	public Graph getUnderlyingGraph() {
		return model.getGraph();
	}

	@Override
	public TimeInterval getInterval() {
		return new TimeInterval(low, high);
	}

	@Override
	public void setInterval(TimeInterval interval) {
		setInterval(interval.getLow(), interval.getHigh());
	}

	@Override
	public void setInterval(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
					"The left endpoint of the interval must be less than "
					+ "the right endpoint.");

		this.low  = low;
		this.high = high;
	}

	private void checkPoint(double point) {
		if (point < low || point > high)
			throw new IllegalArgumentException(
					"The point cannot be out of range "
					+ "wrapped by this DynamicGraph");
	}

	private void checkLowHigh(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
					"The left endpoint of the interval must be less than "
					+ "the right endpoint.");
		if (high < this.low || low > this.high)
			throw new IllegalArgumentException(
					"The time interval [low, high] cannot be out of range "
					+ "wrapped by this DynamicGraph");
	}

	private void checkEstimators(Node node, Estimator[] estimators) {
		int count = node.getNodeData().getAttributes().countValues();
		if (count != estimators.length)
			throw new IllegalArgumentException(
					"The length of the estimators table must be the same as "
					+ "the count of attributes.");
	}

	private void checkEstimators(Edge edge, Estimator[] estimators) {
		int count = edge.getEdgeData().getAttributes().countValues();
		if (count != estimators.length)
			throw new IllegalArgumentException(
					"The length of the estimators table must be the same as "
					+ "the count of attributes.");
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
		if (obj != null && obj.getClass().equals(this.getClass())
				&& ((DynamicGraphImpl) obj).model.getGraph().equals(model.getGraph()))
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
