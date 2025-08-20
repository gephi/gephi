package org.gephi.viz.engine.status;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.joml.Vector2f;

import java.util.Collection;
import java.util.Set;

/**
 * @author Eduardo Ramos
 */
public interface GraphSelection {

    enum GraphSelectionMode {
        SIMPLE_MOUSE_SELECTION,
        SINGLE_NODE_SELECTION,
        RECTANGLE_SELECTION,
        NO_SELECTION,
        CUSTOM_SELECTION
    }
    void setMouseSelectionDiameter(float radius);
    float getMouseSelectionDiameter();
    float getMouseSelectionEffectiveDiameter();

    void setSimpleMouseSelectionMVPScale(float scale);
    float getSimpleMouseSelectionMVPScale();

    void setMouseSelectionDiameterZoomProportional(boolean isZoomProportional);
    boolean getMouseSelectionDiameterZoomProportional();


    boolean someNodesOrEdgesSelection();

    boolean isNodeSelected(Node node);

    Set<Node> getSelectedNodes();

    Set<Node> getSelectedNodesWithNeighbours();

    void setSelectedNodes(Collection<Node> nodes, Collection<Node> neighbours);

    void addSelectedNodes(Collection<Node> nodes, Collection<Node> neighbours);

    void setSelectedNode(Node node, Collection<Node> neighbours);

    void addSelectedNode(Node node, Collection<Node> neighbours);

    void clearSelectedNodes();

    boolean isEdgeSelected(Edge edge);

    int getSelectedEdgesCount();

    Set<Edge> getSelectedEdges();

    void setSelectedEdges(Collection<Edge> edges);

    void addSelectedEdges(Collection<Edge> edges);

    void removeSelectedEdges(Collection<Edge> edges);

    void setSelectedEdge(Edge edge);

    void addSelectedEdge(Edge edge);

    void removeSelectedEdge(Edge edge);

    void clearSelectedEdges();

    GraphSelectionMode getMode();

    void setMode(GraphSelectionMode mode);

    void clearSelection();

    void startRectangleSelection(Vector2f initialPosition);

    void stopRectangleSelection(Vector2f endPosition);

    void updateRectangleSelection(Vector2f updatedPosition);

    Vector2f getRectangleInitialPosition();

    Vector2f getRectangleCurrentPosition();

    void updateMousePosition(Vector2f updatedPosition);

    Vector2f getMousePosition();
}
