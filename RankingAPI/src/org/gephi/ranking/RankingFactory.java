/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ranking;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingFactory {

    public static NodeRanking getNodeAttributeRanking(AttributeColumn column) {
        switch (column.getAttributeType()) {
            case DOUBLE:
                return new NodeAttributeRanking<Double>(column);
            case FLOAT:
                return new NodeAttributeRanking<Float>(column);
            case LONG:
                return new NodeAttributeRanking<Long>(column);
            case INT:
                return new NodeAttributeRanking<Integer>(column);
        }
        throw new IllegalArgumentException("The column must be a Number type");
    }

    public static EdgeRanking getEdgeAttributeRanking(AttributeColumn column) {
        switch (column.getAttributeType()) {
            case DOUBLE:
                return new EdgeAttributeRanking<Double>(column);
            case FLOAT:
                return new EdgeAttributeRanking<Float>(column);
            case LONG:
                return new EdgeAttributeRanking<Long>(column);
            case INT:
                return new EdgeAttributeRanking<Integer>(column);
        }
        throw new IllegalArgumentException("The column must be a Number type");
    }

    public static boolean isNumberColumn(AttributeColumn column) {
        AttributeType type = column.getAttributeType();
        if (type == AttributeType.DOUBLE ||
                type == AttributeType.FLOAT ||
                type == AttributeType.INT ||
                type == AttributeType.LONG) {
            return true;
        }
        return false;
    }

    private static class NodeAttributeRanking<Type> implements NodeRanking<Type> {

        private AttributeColumn column;

        public NodeAttributeRanking(AttributeColumn column) {
            this.column = column;
        }

        public Type getValue(Node node) {
            return (Type) node.getNodeData().getAttributes().getValue(column.getIndex());
        }

        public String getName() {
            return column.getTitle();
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return column.getAttributeType().getType();
        }
    }

    private static class EdgeAttributeRanking<Type> implements EdgeRanking<Type> {

        private AttributeColumn column;

        public EdgeAttributeRanking(AttributeColumn column) {
            this.column = column;
        }

        public Type getValue(Edge edge) {
            return (Type) edge.getEdgeData().getAttributes().getValue(column.getIndex());
        }

        public String getName() {
            return column.getTitle();
        }

        @Override
        public String toString() {
            return getName();
        }

        public Class getType() {
            return column.getAttributeType().getType();
        }
    }
}
