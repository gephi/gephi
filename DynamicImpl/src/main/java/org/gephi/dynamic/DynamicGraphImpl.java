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
package org.gephi.dynamic;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
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
import org.openide.util.Lookup;

/**
 * The wrapper for graph and time interval.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicGraphImpl implements DynamicGraph {

    private GraphModel model;
    private AttributeModel attributeModel;
    private GraphView sourceView;
    private GraphView currentView;
    private double low;
    private double high;

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
        if (graph == null) {
            throw new NullPointerException("The graph cannot be null.");
        }

        if (low > high) {
            throw new IllegalArgumentException(
                    "The left endpoint of the interval must be less than "
                    + "the right endpoint.");
        }

        model = graph.getGraphModel();
        sourceView = graph.getView();
        currentView = model.copyView(sourceView);

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        attributeModel = ac.getModel(graph.getGraphModel().getWorkspace());

        this.low = low;
        this.high = high;

        if (low != Double.NEGATIVE_INFINITY || high != Double.POSITIVE_INFINITY) {
            Graph vgraph = model.getGraph(currentView);
            for (Node n : vgraph.getNodes().toArray()) {
                TimeInterval ti = (TimeInterval) n.getNodeData().getAttributes().getValue(
                        DynamicModel.TIMEINTERVAL_COLUMN);
                if (ti != null && !ti.isInRange(low, high)) {
                    vgraph.removeNode(n);
                }
            }
            for (Edge e : vgraph.getEdges().toArray()) {
                TimeInterval ti = (TimeInterval) e.getEdgeData().getAttributes().getValue(
                        DynamicModel.TIMEINTERVAL_COLUMN);
                if (ti != null && !ti.isInRange(low, high)) {
                    vgraph.removeEdge(e);
                }
            }
        }
    }

    @Override
    public Object[] getAttributesValues(Node node, double point) {
        int count = node.getNodeData().getAttributes().countValues();
        Estimator[] estimators = new Estimator[count];
        for (int i = 0; i < count; ++i) {
            estimators[i] = Estimator.FIRST;
        }
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
        for (int i = 0; i < count; ++i) {
            estimators[i] = Estimator.FIRST;
        }
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
        Object[] values = new Object[attributes.countValues()];

        for (int i = 0; i < attributes.countValues(); ++i) {
            values[i] = attributes.getValue(i);
            if (values[i] instanceof DynamicType) {
                values[i] = ((DynamicType) values[i]).getValue(estimators[i]);
            }
        }

        return values;
    }

    @Override
    public Object[] getAttributesValues(Edge edge, double point) {
        int count = edge.getEdgeData().getAttributes().countValues();
        Estimator[] estimators = new Estimator[count];
        for (int i = 0; i < count; ++i) {
            estimators[i] = Estimator.FIRST;
        }
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
        for (int i = 0; i < count; ++i) {
            estimators[i] = Estimator.FIRST;
        }
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
        Object[] values = new Object[attributes.countValues()];

        for (int i = 0; i < attributes.countValues(); ++i) {
            values[i] = attributes.getValue(i);
            if (values[i] instanceof DynamicType) {
                values[i] = ((DynamicType) values[i]).getValue(estimators[i]);
            }
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
        Graph graph = model.getGraph(sourceView);
        Graph vgraph = model.getGraph(currentView);

        graph.writeLock();

        if (attributeModel.getNodeTable().hasColumn(DynamicModel.TIMEINTERVAL_COLUMN)) {
            for (Node n : graph.getNodes().toArray()) {
                TimeInterval ti = (TimeInterval) n.getNodeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
                if (ti == null && !vgraph.contains(n)) {
                    vgraph.addNode(n);
                } else if (ti != null) {
                    boolean isInRange = ti.isInRange(interval);
                    boolean isInGraph = vgraph.contains(n);
                    if (!isInRange && isInGraph) {
                        vgraph.removeNode(n);
                    } else if (isInRange && !isInGraph) {
                        vgraph.addNode(n);
                    }
                }
            }
        }
        if (attributeModel.getEdgeTable().hasColumn(DynamicModel.TIMEINTERVAL_COLUMN)) {
            for (Edge e : graph.getEdges().toArray()) {
                TimeInterval ti = (TimeInterval) e.getEdgeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
                if (ti == null && !vgraph.contains(e)
                        && vgraph.contains(e.getSource()) && vgraph.contains(e.getTarget())) {
                    vgraph.addEdge(e);
                } else if (ti != null) {
                    boolean isInRange = ti.isInRange(interval);
                    boolean isInGraph = vgraph.contains(e);
                    if (!isInRange && isInGraph) {
                        vgraph.removeEdge(e);
                    } else if (isInRange && !isInGraph && vgraph.contains(e.getSource()) && vgraph.contains(e.getTarget())) {
                        vgraph.addEdge(e);
                    }
                }
            }
        }
        graph.writeUnlock();
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
        Graph graph = model.getGraph(sourceView);
        Graph vgraph = model.getGraph(currentView);
        for (Node n : graph.getNodes().toArray()) {
            TimeInterval ti = (TimeInterval) n.getNodeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
            if (ti.getValues(interval).size() < ti.getValues().size() && vgraph.contains(n)) {
                vgraph.removeNode(n);
            } else if (ti.getValues(interval).size() == ti.getValues().size() && !vgraph.contains(n)) {
                vgraph.addNode(n);
            }
        }
        for (Edge e : graph.getEdges().toArray()) {
            TimeInterval ti = (TimeInterval) e.getEdgeData().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
            if (ti.getValues(interval).size() < ti.getValues().size() && vgraph.contains(e)) {
                vgraph.removeEdge(e);
            } else if (ti.getValues(interval).size() == ti.getValues().size() && !vgraph.contains(e)
                    && vgraph.contains(e.getSource()) && vgraph.contains(e.getTarget())) {
                vgraph.addEdge(e);
            }
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
        if (low > high) {
            throw new IllegalArgumentException(
                    "The left endpoint of the interval must be less than "
                    + "the right endpoint.");
        }

        this.low = low;
        this.high = high;
    }

    private void checkPoint(double point) {
        if (point < low || point > high) {
            throw new IllegalArgumentException(
                    "The point cannot be out of range "
                    + "wrapped by this DynamicGraph");
        }
    }

    private void checkLowHigh(double low, double high) {
        if (low > high) {
            throw new IllegalArgumentException(
                    "The left endpoint of the interval must be less than "
                    + "the right endpoint.");
        }
        if (high < this.low || low > this.high) {
            throw new IllegalArgumentException(
                    "The time interval [low, high] cannot be out of range "
                    + "wrapped by this DynamicGraph");
        }
    }

    private void checkEstimators(Node node, Estimator[] estimators) {
        int count = node.getNodeData().getAttributes().countValues();
        if (count != estimators.length) {
            throw new IllegalArgumentException(
                    "The length of the estimators table must be the same as "
                    + "the count of attributes.");
        }
    }

    private void checkEstimators(Edge edge, Estimator[] estimators) {
        int count = edge.getEdgeData().getAttributes().countValues();
        if (count != estimators.length) {
            throw new IllegalArgumentException(
                    "The length of the estimators table must be the same as "
                    + "the count of attributes.");
        }
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
                && ((DynamicGraphImpl) obj).model.getGraph().equals(model.getGraph())) {
            return true;
        }
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
