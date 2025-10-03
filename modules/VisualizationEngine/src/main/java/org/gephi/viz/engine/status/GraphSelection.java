package org.gephi.viz.engine.status;

import java.util.Collection;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.joml.Vector2f;

/**
 * @author Eduardo Ramos
 */
public interface GraphSelection {

    enum GraphSelectionMode {
        SIMPLE_MOUSE_SELECTION,
        SINGLE_NODE_SELECTION,
        MULTI_NODE_SELECTION,
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

    boolean isNodeOrNeighbourSelected(Node node);

    Collection<Node> getSelectedNodes();

    void setSelectedNodes(Graph graph, NodeIterable nodesIterable, boolean autoSelectNeighbours, boolean selectEdges);

    void setSelectedNodes(Node[] nodes);

    void clearSelectedNodes();

    boolean isEdgeSelected(Edge edge);

    void setSelectedEdges(Edge[] edges);

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
