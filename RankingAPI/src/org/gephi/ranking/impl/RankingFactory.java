/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ranking.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.EdgeRanking;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingFactory {

    public static NodeRanking getNodeAttributeRanking(AttributeColumn column, Graph graph) {
        NodeRanking nodeRanking = new NodeAttributeRanking(column);
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static EdgeRanking getEdgeAttributeRanking(AttributeColumn column, Graph graph) {
        EdgeRanking edgeRanking = new EdgeAttributeRanking(column);
        setMinMax((AbstractRanking) edgeRanking, graph);
        return edgeRanking;
    }

    public static NodeRanking getNodeDynamicAttributeRanking(AttributeColumn column, Graph graph, TimeInterval timeInterval, Estimator estimator) {
        DynamicNodeAttributeRanking nodeRanking = new DynamicNodeAttributeRanking(column, timeInterval, estimator);
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static EdgeRanking getEdgeDynamicAttributeRanking(AttributeColumn column, Graph graph, TimeInterval timeInterval, Estimator estimator) {
        DynamicEdgeAttributeRanking edgeRanking = new DynamicEdgeAttributeRanking(column, timeInterval, estimator);
        setMinMax((AbstractRanking) edgeRanking, graph);
        return edgeRanking;
    }

    public static NodeRanking getNodeDegreeRanking(Graph graph) {
        NodeRanking nodeRanking = new NodeDegreeRanking(graph);
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static NodeRanking getNodeInDegreeRanking(DirectedGraph graph) {
        NodeRanking nodeRanking = new NodeInDegreeRanking(graph);
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static NodeRanking getNodeOutDegreeRanking(DirectedGraph graph) {
        NodeRanking nodeRanking = new NodeOutDegreeRanking(graph);
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static NodeRanking getNodeChildrenCountRanking(HierarchicalGraph graph) {
        NodeRanking nodeRanking = new NodeChildrenCountRanking(graph);
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static boolean refreshRanking(AbstractRanking ranking, Graph graph) {
        Object min = ranking.getMinimumValue();
        Object max = ranking.getMaximumValue();
        ranking.setGraph(graph);
        setMinMax(ranking, graph);
        return !ranking.getMinimumValue().equals(min) || !ranking.getMaximumValue().equals(max);
    }

    public static boolean isNumberColumn(AttributeColumn column) {
        AttributeType type = column.getType();
        if (type == AttributeType.DOUBLE
                || type == AttributeType.FLOAT
                || type == AttributeType.INT
                || type == AttributeType.LONG
                || type == AttributeType.BYTE
                || type == AttributeType.BIGDECIMAL
                || type == AttributeType.BIGINTEGER
                || type == AttributeType.SHORT) {
            return true;
        }
        return false;
    }

    public static boolean isDynamicNumberColumn(AttributeColumn column) {
        AttributeType type = column.getType();
        AttributeUtils.getDefault().isDynamicNumberColumn(column);
        if (type == AttributeType.DYNAMIC_BIGDECIMAL
                || type == AttributeType.DYNAMIC_BIGINTEGER
                || type == AttributeType.DYNAMIC_BYTE
                || type == AttributeType.DYNAMIC_DOUBLE
                || type == AttributeType.DYNAMIC_FLOAT
                || type == AttributeType.DYNAMIC_INT
                || type == AttributeType.DYNAMIC_LONG
                || type == AttributeType.DYNAMIC_SHORT) {
            return true;
        }
        return false;
    }

    protected static void setMinMax(AbstractRanking ranking, Graph graph) {
        if (ranking instanceof NodeRanking) {
            List<Comparable> objects = new ArrayList<Comparable>();
            for (Node node : graph.getNodes().toArray()) {
                Comparable value = (Comparable) ranking.getValue(node);
                if (value != null) {
                    objects.add(value);
                }
            }
            ranking.setMinimumValue((Number) getMin(objects.toArray(new Comparable[0])));
            ranking.setMaximumValue((Number) getMax(objects.toArray(new Comparable[0])));
        } else if (ranking instanceof EdgeRanking) {
            List<Comparable> objects = new ArrayList<Comparable>();
            for (Edge edge : graph.getEdges().toArray()) {
                Comparable value = (Comparable) ranking.getValue(edge);
                if (value != null) {
                    objects.add(value);
                }
            }
            ranking.setMinimumValue((Number) getMin(objects.toArray(new Comparable[0])));
            ranking.setMaximumValue((Number) getMax(objects.toArray(new Comparable[0])));
        }
    }

    private static Object getMin(Comparable[] values) {
        switch (values.length) {
            case 0:
                return null;
            case 1:
                return values[0];
            // values.length > 1
            default:
                Comparable<?> min = values[0];

                for (int index = 1; index < values.length; index++) {
                    Comparable o = values[index];
                    if (o.compareTo(min) < 0) {
                        min = o;
                    }
                }

                return min;
        }
    }

    private static Object getMax(Comparable[] values) {
        switch (values.length) {
            case 0:
                return null;
            case 1:
                return values[0];
            // values.length > 1
            default:
                Comparable<?> max = values[0];

                for (int index = 1; index < values.length; index++) {
                    Comparable o = values[index];
                    if (o.compareTo(max) > 0) {
                        max = o;
                    }
                }

                return max;
        }
    }

    private static class NodeDegreeRanking extends AbstractRanking<Node, Integer> implements NodeRanking<Integer> {

        public NodeDegreeRanking(Graph graph) {
            this.graph = graph;
        }

        public Integer getValue(Node element) {
            return graph.getDegree(element);
        }

        public float normalize(Integer value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (maximum - minimum)) + minimum;
        }

        public String getName() {
            return "Degree";
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return Integer.class;
        }
    }

    private static class NodeInDegreeRanking extends AbstractRanking<Node, Integer> implements NodeRanking<Integer> {

        public NodeInDegreeRanking(DirectedGraph graph) {
            this.graph = graph;
        }

        public Integer getValue(Node element) {
            return ((DirectedGraph) graph).getInDegree(element);
        }

        public float normalize(Integer value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (maximum - minimum)) + minimum;
        }

        public String getName() {
            return "InDegree";
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return Integer.class;
        }
    }

    private static class NodeOutDegreeRanking extends AbstractRanking<Node, Integer> implements NodeRanking<Integer> {

        public NodeOutDegreeRanking(DirectedGraph graph) {
            this.graph = graph;
        }

        public Integer getValue(Node element) {
            return ((DirectedGraph) graph).getOutDegree(element);
        }

        public float normalize(Integer value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (maximum - minimum)) + minimum;
        }

        public String getName() {
            return "OutDegree";
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return Integer.class;
        }
    }

    private static class NodeChildrenCountRanking extends AbstractRanking<Node, Integer> implements NodeRanking<Integer> {

        public NodeChildrenCountRanking(HierarchicalGraph graph) {
            this.graph = graph;
        }

        public Integer getValue(Node element) {
            return ((HierarchicalGraph) graph).getChildrenCount(element);
        }

        public float normalize(Integer value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (maximum - minimum)) + minimum;
        }

        public String getName() {
            return "Children count";
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return Integer.class;
        }
    }

    protected abstract static class AttributeRanking<Element, Type extends Number> extends AbstractRanking<Element, Type> {

        protected AttributeColumn column;

        public AttributeRanking(AttributeColumn attributeColumn) {
            this.column = attributeColumn;
        }

        public String getName() {
            return column.getTitle();
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return column.getType().getType();
        }

        public float normalize(Type value) {
            return (value.floatValue() - minimum.floatValue()) / (float) (maximum.floatValue() - minimum.floatValue());
        }

        public Type unNormalize(float normalizedValue) {
            double val = (normalizedValue * (maximum.doubleValue() - minimum.doubleValue())) + minimum.doubleValue();
            switch (column.getType()) {
                case BIGDECIMAL:
                    return (Type) new BigDecimal(val);
                case BIGINTEGER:
                    return (Type) new BigInteger("" + val);
                case DOUBLE:
                    return (Type) new Double(val);
                case FLOAT:
                    return (Type) new Float(val);
                case INT:
                    return (Type) new Integer((int) val);
                case LONG:
                    return (Type) new Long((long) val);
                case SHORT:
                    return (Type) new Short((short) val);
                default:
                    return (Type) new Double(val);
            }
        }
    }

    private static class NodeAttributeRanking<Type extends Number> extends AttributeRanking<Node, Type> implements NodeRanking<Type> {

        public NodeAttributeRanking(AttributeColumn column) {
            super(column);
        }

        public Type getValue(Node node) {
            return (Type) node.getNodeData().getAttributes().getValue(column.getIndex());
        }
    }

    private static class EdgeAttributeRanking<Type extends Number> extends AttributeRanking<Edge, Type> implements EdgeRanking<Type> {

        public EdgeAttributeRanking(AttributeColumn column) {
            super(column);
        }

        public Type getValue(Edge edge) {
            return (Type) edge.getEdgeData().getAttributes().getValue(column.getIndex());
        }
    }

    private static class DynamicNodeAttributeRanking<Type extends Number> extends AttributeRanking<Node, Type> implements NodeRanking<Type> {

        private TimeInterval timeInterval;
        private Estimator estimator;

        public DynamicNodeAttributeRanking(AttributeColumn column, TimeInterval timeInterval, Estimator estimator) {
            super(column);
            this.timeInterval = timeInterval;
            this.estimator = estimator;
        }

        public Type getValue(Node node) {
            DynamicType<Type> dynamicType = (DynamicType<Type>) node.getNodeData().getAttributes().getValue(column.getIndex());
            if (dynamicType != null) {
                return (Type) dynamicType.getValue(timeInterval == null ? Double.NEGATIVE_INFINITY : timeInterval.getLow(),
                        timeInterval == null ? Double.POSITIVE_INFINITY : timeInterval.getHigh(), estimator);
            }
            return null;
        }
    }

    private static class DynamicEdgeAttributeRanking<Type extends Number> extends AttributeRanking<Edge, Type> implements EdgeRanking<Type> {

        private TimeInterval timeInterval;
        private Estimator estimator;

        public DynamicEdgeAttributeRanking(AttributeColumn column, TimeInterval timeInterval, Estimator estimator) {
            super(column);
            this.timeInterval = timeInterval;
            this.estimator = estimator;
        }

        public Type getValue(Edge edge) {
            DynamicType<Type> dynamicType = (DynamicType<Type>) edge.getEdgeData().getAttributes().getValue(column.getIndex());
            if (dynamicType != null) {
                return (Type) dynamicType.getValue(timeInterval == null ? Double.NEGATIVE_INFINITY : timeInterval.getLow(),
                        timeInterval == null ? Double.POSITIVE_INFINITY : timeInterval.getHigh(), estimator);
            }
            return null;
        }
    }
}
