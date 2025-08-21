package org.gephi.viz.engine.status;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.joml.Vector2f;

public class GraphSelectionImpl implements GraphSelection {

    private final Set<Node> nodes = new HashSet<>();
    private final Set<Node> nodesWithNeighbours = new HashSet<>();
    private final Set<Edge> edges = new HashSet<>();
    private GraphSelection.GraphSelectionMode selectionMode;
    private float simpleMouseSelectionDiameter = 1f;
    private float simpleMouseSelectionMVPScale = 1.0f;
    private boolean mouseSelectionDiameterZoomProportional = false;

    public GraphSelectionImpl() {
        this.selectionMode = GraphSelectionMode.SIMPLE_MOUSE_SELECTION;
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
        return nodesWithNeighbours.contains(node);
    }

    @Override
    public Set<Node> getSelectedNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public Set<Node> getSelectedNodesWithNeighbours() {
        return Collections.unmodifiableSet(nodesWithNeighbours);
    }

    @Override
    public void setSelectedNodes(Collection<Node> nodes, Collection<Node> neighbours) {
        this.nodes.clear();
        this.nodesWithNeighbours.clear();
        if (nodes != null) {
            this.nodes.addAll(nodes);
            this.nodesWithNeighbours.addAll(nodes);
            if (neighbours != null) {
                this.nodesWithNeighbours.addAll(neighbours);
            }
        }
    }

    @Override
    public void addSelectedNodes(Collection<Node> nodes, Collection<Node> neighbours) {
        if (nodes != null) {
            this.nodes.addAll(nodes);
            this.nodesWithNeighbours.addAll(nodes);
            if (neighbours != null) {
                this.nodesWithNeighbours.addAll(neighbours);
            }
        }
    }

    @Override
    public void setSelectedNode(Node node, Collection<Node> neighbours) {
        if (node == null) {
            this.clearSelectedNodes();
        } else {
            this.nodes.clear();
            this.nodes.add(node);
            this.nodesWithNeighbours.clear();
            this.nodesWithNeighbours.add(node);
            if (neighbours != null) {
                this.nodesWithNeighbours.addAll(neighbours);
            }
        }
    }

    @Override
    public void addSelectedNode(Node node, Collection<Node> neighbours) {
        if (node != null) {
            this.nodes.add(node);
            this.nodesWithNeighbours.add(node);
            if (neighbours != null) {
                this.nodesWithNeighbours.addAll(neighbours);
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
        return edges.contains(edge);
    }

    @Override
    public int getSelectedEdgesCount() {
        return edges.size();
    }

    @Override
    public Set<Edge> getSelectedEdges() {
        return Collections.unmodifiableSet(edges);
    }

    @Override
    public void setSelectedEdges(Collection<Edge> edges) {
        this.edges.clear();
        if (edges != null) {
            this.edges.addAll(edges);
        }
    }

    @Override
    public void addSelectedEdges(Collection<Edge> edges) {
        if (edges != null) {
            this.edges.addAll(edges);
        }
    }

    @Override
    public void removeSelectedEdges(Collection<Edge> edges) {
        if (edges != null) {
            this.edges.removeAll(edges);
        }
    }

    @Override
    public void setSelectedEdge(Edge edge) {
        if (edge == null) {
            this.clearSelectedEdges();
        } else {
            this.edges.clear();
            this.edges.add(edge);
        }
    }

    @Override
    public void addSelectedEdge(Edge edge) {
        if (edge != null) {
            this.edges.add(edge);
        }
    }

    @Override
    public void removeSelectedEdge(Edge edge) {
        if (edge != null) {
            this.edges.remove(edge);
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
