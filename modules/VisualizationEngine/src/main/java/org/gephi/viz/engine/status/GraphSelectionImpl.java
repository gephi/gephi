package org.gephi.viz.engine.status;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.joml.Vector2f;

public class GraphSelectionImpl implements GraphSelection {

    private final BitSet nodes = new BitSet();
    private final BitSet nodesWithNeighbours = new BitSet();
    private final BitSet edges = new BitSet();
    private final List<Node> nodesList = new ArrayList<>();

    private GraphSelection.GraphSelectionMode selectionMode;
    private float simpleMouseSelectionDiameter = 1f;
    private float simpleMouseSelectionMVPScale = 1.0f;
    private boolean mouseSelectionDiameterZoomProportional = false;

    public GraphSelectionImpl() {
        this.selectionMode = GraphSelectionMode.SIMPLE_MOUSE_SELECTION;
        nodes.clear();
    }

    @Override
    public void setMouseSelectionDiameter(float diameter) {
        this.simpleMouseSelectionDiameter = diameter >= 1 ? diameter : 1;
    }

    @Override
    public float getMouseSelectionDiameter() {
        return this.simpleMouseSelectionDiameter;
    }

    public void setSimpleMouseSelectionMVPScale(float scale) {
        this.simpleMouseSelectionMVPScale = scale;
    }

    public float getSimpleMouseSelectionMVPScale() {
        return this.simpleMouseSelectionMVPScale;
    }

    public void setMouseSelectionDiameterZoomProportional(boolean isZoomProportional) {
        this.mouseSelectionDiameterZoomProportional = isZoomProportional;
    }

    public float getMouseSelectionEffectiveDiameter() {
        if (this.mouseSelectionDiameterZoomProportional) {
            return this.simpleMouseSelectionDiameter;
        }
        return (float) ((this.simpleMouseSelectionDiameter / this.simpleMouseSelectionMVPScale) * 0.001);
    }

    public boolean getMouseSelectionDiameterZoomProportional() {
        return this.mouseSelectionDiameterZoomProportional;
    }

    @Override
    public boolean someNodesOrEdgesSelection() {
        return !nodes.isEmpty() || !edges.isEmpty();
    }

    @Override
    public boolean isNodeSelected(Node node) {
        return nodes.get(node.getStoreId());
    }

    @Override
    public boolean isNodeOrNeighbourSelected(Node node) {
        return nodesWithNeighbours.get(node.getStoreId());
    }

    @Override
    public Collection<Node> getSelectedNodes() {
        return Collections.unmodifiableList(nodesList);
    }

    @Override
    public void setSelectedNodes(Graph graph, NodeIterable nodesIterable, boolean autoSelectNeighbours,
                                 boolean selectEdges) {
        final Iterator<Node> nodeIterator = nodesIterable.iterator();

        // Resets
        nodes.clear();
        nodesWithNeighbours.clear();
        edges.clear();
        nodesList.clear();

        final boolean selectNeighbours = autoSelectNeighbours &&
            getMode() != GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION;
        while (nodeIterator.hasNext()) {
            final Node node = nodeIterator.next();

            nodes.set(node.getStoreId());
            nodesList.add(node);
            nodesWithNeighbours.set(node.getStoreId());
            if (!selectEdges && !selectNeighbours) {
                continue;
            }
            EdgeIterable edgeIterable = graph.getEdges(node);

            for (Edge edge : edgeIterable) {
                edges.set(edge.getStoreId());
                if (selectNeighbours) {
                    Node oppositeNode = graph.getOpposite(node, edge);
                    if (oppositeNode != null && oppositeNode != node) {
                        nodesWithNeighbours.set(oppositeNode.getStoreId());
                    }
                }
            }
        }
    }

    @Override
    public void setSelectedNodes(Node[] nodes) {
        this.nodes.clear();
        this.nodesWithNeighbours.clear();
        if (nodes != null) {
            for (Node node : nodes) {
                this.nodes.set(node.getStoreId());
                this.nodesWithNeighbours.set(node.getStoreId());
            }
        }
    }

    @Override
    public void clearSelectedNodes() {
        this.nodes.clear();
        this.nodesWithNeighbours.clear();
    }

    @Override
    public boolean isEdgeSelected(Edge edge) {
        return edges.get(edge.getStoreId());
    }

    @Override
    public void setSelectedEdges(Edge[] edges) {
        this.edges.clear();
        if (edges != null) {
            for (Edge edge : edges) {
                this.edges.set(edge.getStoreId());
            }
        }
    }

    @Override
    public void clearSelectedEdges() {
        this.edges.clear();
    }

    @Override
    public void clearSelection() {
        clearSelectedEdges();
        clearSelectedNodes();
    }

    @Override
    public GraphSelectionMode getMode() {
        return selectionMode;
    }

    @Override
    public void setMode(GraphSelectionMode mode) {
        if (mode != null) {
            selectionMode = mode;
            clearSelection();
        }
    }

    private Vector2f rectangleSelectionInitialPosition;
    private Vector2f rectangleSelectionCurrentPosition;

    @Override
    public void startRectangleSelection(Vector2f initialPosition) {
        rectangleSelectionInitialPosition = initialPosition;
        rectangleSelectionCurrentPosition = initialPosition;
    }

    @Override
    public void stopRectangleSelection(Vector2f endPosition) {
        this.rectangleSelectionInitialPosition = null;
        this.rectangleSelectionCurrentPosition = null;
    }

    @Override
    public void updateRectangleSelection(Vector2f updatedPosition) {
        this.rectangleSelectionCurrentPosition = updatedPosition;
    }

    @Override
    public Vector2f getRectangleInitialPosition() {
        return this.rectangleSelectionInitialPosition;
    }

    @Override
    public Vector2f getRectangleCurrentPosition() {
        return this.rectangleSelectionCurrentPosition;
    }

    private Vector2f mousePosition;

    @Override
    public void updateMousePosition(Vector2f mousePosition) {
        this.mousePosition = mousePosition;
    }

    @Override
    public Vector2f getMousePosition() {
        return mousePosition;
    }
}
