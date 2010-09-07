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

import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.EdgeRanking;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
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
        NodeRanking nodeRanking = null;
        switch (column.getType()) {
            case DOUBLE:
                nodeRanking = new NodeAttributeDoubleRanking(column);
                break;
            case FLOAT:
                nodeRanking = new NodeAttributeFloatRanking(column);
                break;
            case LONG:
                nodeRanking = new NodeAttributeLongRanking(column);
                break;
            case INT:
                nodeRanking = new NodeAttributeIntegerRanking(column);
                break;
            default:
                throw new IllegalArgumentException("The column must be a Number type");
        }
        setMinMax((AbstractRanking) nodeRanking, graph);
        return nodeRanking;
    }

    public static EdgeRanking getEdgeAttributeRanking(AttributeColumn column, Graph graph) {
        EdgeRanking edgeRanking = null;
        switch (column.getType()) {
            case DOUBLE:
                edgeRanking = new EdgeAttributeDoubleRanking(column);
                break;
            case FLOAT:
                edgeRanking = new EdgeAttributeFloatRanking(column);
                break;
            case LONG:
                edgeRanking = new EdgeAttributeLongRanking(column);
                break;
            case INT:
                edgeRanking = new EdgeAttributeIntegerRanking(column);
                break;
            default:
                throw new IllegalArgumentException("The column must be a Number type");
        }
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
                || type == AttributeType.LONG) {
            return true;
        }
        return false;
    }

    protected static void setMinMax(AbstractRanking ranking, Graph graph) {
        if (ranking instanceof NodeRanking) {
            if (ranking.getType().equals(Double.class)) {
                Double minValue = Double.POSITIVE_INFINITY;
                Double maxValue = Double.NEGATIVE_INFINITY;
                for (Node node : graph.getNodes().toArray()) {
                    Double value = (Double) ranking.getValue(node);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            } else if (ranking.getType().equals(Float.class)) {
                Float minValue = Float.POSITIVE_INFINITY;
                Float maxValue = Float.NEGATIVE_INFINITY;
                for (Node node : graph.getNodes().toArray()) {
                    Float value = (Float) ranking.getValue(node);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            } else if (ranking.getType().equals(Integer.class)) {
                Integer minValue = Integer.MAX_VALUE;
                Integer maxValue = Integer.MIN_VALUE;
                for (Node node : graph.getNodes().toArray()) {
                    Integer value = (Integer) ranking.getValue(node);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            } else if (ranking.getType().equals(Long.class)) {
                Long minValue = Long.MAX_VALUE;
                Long maxValue = Long.MIN_VALUE;
                for (Node node : graph.getNodes().toArray()) {
                    Long value = (Long) ranking.getValue(node);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            }
        } else if (ranking instanceof EdgeRanking) {
            if (ranking.getType().equals(Double.class)) {
                Double minValue = Double.POSITIVE_INFINITY;
                Double maxValue = Double.NEGATIVE_INFINITY;
                for (Edge edge : graph.getEdges().toArray()) {
                    Double value = (Double) ranking.getValue(edge);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            } else if (ranking.getType().equals(Float.class)) {
                Float minValue = Float.POSITIVE_INFINITY;
                Float maxValue = Float.NEGATIVE_INFINITY;
                for (Edge edge : graph.getEdges().toArray()) {
                    Float value = (Float) ranking.getValue(edge);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            } else if (ranking.getType().equals(Integer.class)) {
                Integer minValue = Integer.MAX_VALUE;
                Integer maxValue = Integer.MIN_VALUE;
                for (Edge edge : graph.getEdges().toArray()) {
                    Integer value = (Integer) ranking.getValue(edge);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            } else if (ranking.getType().equals(Long.class)) {
                Long minValue = Long.MAX_VALUE;
                Long maxValue = Long.MIN_VALUE;
                for (Edge edge : graph.getEdges().toArray()) {
                    Long value = (Long) ranking.getValue(edge);
                    if (value != null) {
                        minValue = Math.min(value, minValue);
                        maxValue = Math.max(value, maxValue);
                    }
                }
                ranking.setMinimumValue(minValue);
                ranking.setMaximumValue(maxValue);
            }
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
            return ((DirectedGraph)graph).getInDegree(element);
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
            return ((DirectedGraph)graph).getOutDegree(element);
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
            return ((HierarchicalGraph)graph).getChildrenCount(element);
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

    protected abstract static class AttributeRanking<Element, Type> extends AbstractRanking<Element, Type> {

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
    }

    private abstract static class NodeAttributeRanking<Type> extends AttributeRanking<Node, Type> implements NodeRanking<Type> {

        public NodeAttributeRanking(AttributeColumn column) {
            super(column);
        }

        public Type getValue(Node node) {
            return (Type) node.getNodeData().getAttributes().getValue(column.getIndex());
        }
    }

    private abstract static class EdgeAttributeRanking<Type> extends AttributeRanking<Edge, Type> implements EdgeRanking<Type> {

        public EdgeAttributeRanking(AttributeColumn column) {
            super(column);
        }

        public Type getValue(Edge edge) {
            return (Type) edge.getEdgeData().getAttributes().getValue(column.getIndex());
        }
    }

    private static class EdgeAttributeFloatRanking extends EdgeAttributeRanking<Float> {

        public EdgeAttributeFloatRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Float value) {
            return (value - minimum) / (maximum - minimum);
        }

        public Float unNormalize(float normalizedValue) {
            return (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class EdgeAttributeDoubleRanking extends EdgeAttributeRanking<Double> {

        public EdgeAttributeDoubleRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Double value) {
            return (float) ((value - minimum) / (maximum - minimum));
        }

        public Double unNormalize(float normalizedValue) {
            return (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class EdgeAttributeIntegerRanking extends EdgeAttributeRanking<Integer> {

        public EdgeAttributeIntegerRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Integer value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class EdgeAttributeLongRanking extends EdgeAttributeRanking<Long> {

        public EdgeAttributeLongRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Long value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Long unNormalize(float normalizedValue) {
            return (long) (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class NodeAttributeFloatRanking extends NodeAttributeRanking<Float> {

        public NodeAttributeFloatRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Float value) {
            return (value - minimum) / (maximum - minimum);
        }

        public Float unNormalize(float normalizedValue) {
            return (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class NodeAttributeDoubleRanking extends NodeAttributeRanking<Double> {

        public NodeAttributeDoubleRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Double value) {
            return (float) ((value - minimum) / (maximum - minimum));
        }

        public Double unNormalize(float normalizedValue) {
            return (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class NodeAttributeIntegerRanking extends NodeAttributeRanking<Integer> {

        public NodeAttributeIntegerRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Integer value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (maximum - minimum)) + minimum;
        }
    }

    private static class NodeAttributeLongRanking extends NodeAttributeRanking<Long> {

        public NodeAttributeLongRanking(AttributeColumn column) {
            super(column);
        }

        public float normalize(Long value) {
            return (float) ((value - minimum) / (float) (maximum - minimum));
        }

        public Long unNormalize(float normalizedValue) {
            return (long) (normalizedValue * (maximum - minimum)) + minimum;
        }
    }
}
