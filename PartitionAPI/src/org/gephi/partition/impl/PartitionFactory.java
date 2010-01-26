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
package org.gephi.partition.impl;

import com.google.common.collect.ArrayListMultimap;
import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;
import org.gephi.ui.utils.PaletteUtils;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionFactory {

    public static boolean isNodePartitionColumn(AttributeColumn column, Graph graph) {
        if (column.getType().equals(AttributeType.STRING)
                || column.getType().equals(AttributeType.BOOLEAN)
                || column.getType().equals(AttributeType.INT)) {
            Set values = new HashSet();
            int nonNullvalues = 0;
            for (Node n : graph.getNodes()) {
                Object value = n.getNodeData().getAttributes().getValue(column.getIndex());
                if (value != null) {
                    nonNullvalues++;
                }
                values.add(value);
            }
            if (values.size() < 2f / 3f * nonNullvalues) {      //If #different values is < 2:3 of total non-null values
                return true;
            }
        }
        return false;
    }

    public static boolean isEdgePartitionColumn(AttributeColumn column, Graph graph) {
        if (column.getType().equals(AttributeType.STRING)
                || column.getType().equals(AttributeType.BOOLEAN)
                || column.getType().equals(AttributeType.INT)) {
            Set values = new HashSet();
            int nonNullvalues = 0;
            for (Edge n : graph.getEdges()) {
                Object value = n.getEdgeData().getAttributes().getValue(column.getIndex());
                if (value != null) {
                    nonNullvalues++;
                }
                values.add(value);
            }
            if (values.size() < 2f / 3f * nonNullvalues) {      //If #different values is < 2:3 of total non-null values
                return true;
            }
        }
        return false;
    }

    public static NodePartition createNodePartition(AttributeColumn column) {
        return new NodePartitionImpl(column);
    }

    public static EdgePartition createEdgePartition(AttributeColumn column) {
        return new EdgePartitionImpl(column);
    }

    public static boolean isPartitionBuilt(Partition partition) {
        return partition.getParts().length > 0;
    }

    public static void buildNodePartition(NodePartition partition, Graph graph) {

        NodePartitionImpl partitionImpl = (NodePartitionImpl) partition;
        ArrayListMultimap<Object, Node> multimap = ArrayListMultimap.create();
        for (Node n : graph.getNodes()) {
            Object value = n.getNodeData().getAttributes().getValue(partitionImpl.column.getIndex());
            multimap.put(value, n);
        }

        PartImpl<Node>[] parts = new PartImpl[multimap.keySet().size()];
        Map<Object, Collection<Node>> map = multimap.asMap();
        int i = 0;
        for (Entry<Object, Collection<Node>> entry : map.entrySet()) {
            PartImpl<Node> part = new PartImpl<Node>(partition, entry.getKey(), entry.getValue().toArray(new Node[0]));
            parts[i] = part;
            i++;
        }
        partitionImpl.setParts(parts);
    }

    public static void buildEdgePartition(EdgePartition partition, Graph graph) {
        EdgePartitionImpl partitionImpl = (EdgePartitionImpl) partition;

        ArrayListMultimap<Object, Edge> multimap = ArrayListMultimap.create();
        for (Edge n : graph.getEdges()) {
            Object value = n.getEdgeData().getAttributes().getValue(partitionImpl.column.getIndex());
            multimap.put(value, n);
        }

        PartImpl<Edge>[] parts = new PartImpl[multimap.keySet().size()];
        Map<Object, Collection<Edge>> map = multimap.asMap();
        int i = 0;
        for (Entry<Object, Collection<Edge>> entry : map.entrySet()) {
            PartImpl<Edge> part = new PartImpl<Edge>(partition, entry.getKey(), entry.getValue().toArray(new Edge[0]));
            parts[i] = part;
            i++;
        }
        partitionImpl.setParts(parts);
    }

    private static class NodePartitionImpl implements NodePartition {

        private HashMap<Node, Part<Node>> nodeMap;
        private PartImpl<Node>[] parts;
        private AttributeColumn column;

        public NodePartitionImpl(AttributeColumn column) {
            this.column = column;
            nodeMap = new HashMap<Node, Part<Node>>();
            parts = new PartImpl[0];
        }

        public int getPartsCount() {
            return parts.length;
        }

        public Part<Node>[] getParts() {
            return parts;
        }

        public Map<Node, Part<Node>> getMap() {
            return nodeMap;
        }

        public Part<Node> getPart(Node element) {
            return nodeMap.get(element);
        }

        public void setParts(PartImpl<Node>[] parts) {
            this.parts = parts;
            List<Color> colors = PaletteUtils.getSequenceColors(parts.length);
            int i = 0;
            for (PartImpl<Node> p : parts) {
                for (Node n : p.objects) {
                    nodeMap.put(n, p);
                }
                p.setColor(colors.get(i));
                i++;
            }
        }

        public AttributeColumn getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return column.getTitle();
        }
    }

    private static class EdgePartitionImpl implements EdgePartition {

        private HashMap<Edge, Part<Edge>> edgeMap;
        private PartImpl<Edge>[] parts;
        private AttributeColumn column;

        public EdgePartitionImpl(AttributeColumn column) {
            this.column = column;
            edgeMap = new HashMap<Edge, Part<Edge>>();
            parts = new PartImpl[0];
        }

        public int getPartsCount() {
            return parts.length;
        }

        public Part<Edge>[] getParts() {
            return parts;
        }

        public Map<Edge, Part<Edge>> getMap() {
            return edgeMap;
        }

        public Part<Edge> getPart(Edge element) {
            return edgeMap.get(element);
        }

        public void setParts(PartImpl<Edge>[] parts) {
            this.parts = parts;
            List<Color> colors = PaletteUtils.getSequenceColors(parts.length);
            int i = 0;
            for (PartImpl<Edge> p : parts) {
                for (Edge n : p.objects) {
                    edgeMap.put(n, p);
                }
                p.setColor(colors.get(i));
                i++;
            }
        }

        public AttributeColumn getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return column.getTitle();
        }
    }

    private static class PartImpl<Element> implements Part<Element> {

        private static final String NULL = "null";
        private Partition<Element> partition;
        private Element[] objects;
        private Object value;
        private Color color;

        public PartImpl(Partition<Element> partition, Object value, Element[] objects) {
            this.partition = partition;
            this.value = value;
            this.objects = objects;
        }

        public Element[] getObjects() {
            return objects;
        }

        public Object getValue() {
            return value;
        }

        public String getDisplayName() {
            return value != null ? value.toString() : NULL;
        }

        public boolean isInPart(Element element) {
            return partition.getPart(element) == this;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public float getPercentage() {
            return objects.length / (float) partition.getMap().size();
        }

        public Partition getPartition() {
            return partition;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }
}
