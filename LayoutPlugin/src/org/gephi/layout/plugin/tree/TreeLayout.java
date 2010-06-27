package org.gephi.layout.plugin.tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Škurla
 */
public class TreeLayout extends AbstractLayout {

    private Map<Integer, List<Node>> depthMapper;
    private Map<Node, Double> nodeToRadiusMapper;
    private int maxShortestPath;
    private int nodeId;
    private Graph graph;

    public TreeLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void initAlgo() {
        graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        depthMapper = new HashMap<Integer, List<Node>>();
        nodeToRadiusMapper = new HashMap<Node, Double>();
        maxShortestPath = 0;
        setConverged(false);
    }

    public void goAlgo() {
        Node rootNode = GraphUtils.determineRootNode((DirectedGraph) graph);
        fillShortestPathMap(rootNode);

        for (int depth = maxShortestPath - 1; depth >= 0; depth--)
            updateCoordinatesForDepth(depth, depth == (maxShortestPath - 1));

        setConverged(true);
    }

    private void fillShortestPathMap(Node rootNode) {
        for (Node node : graph.getNodes()) {
            if (node != rootNode) {
                int shortestPath = GraphUtils.calculateShortestPath(rootNode, node);
                if (maxShortestPath < shortestPath) {
                    maxShortestPath = shortestPath;
                }

                List<Node> nodes = depthMapper.get(shortestPath);
                if (nodes == null) {
                    nodes = new LinkedList<Node>();
                    depthMapper.put(shortestPath, nodes);
                }

                nodes.add(node);
            }
        }

        depthMapper.put(0, Arrays.asList(new Node[] {rootNode}));
    }

    private void updateCoordinatesForDepth(int depth, boolean firstLevel) {
        List<Node> nodes = depthMapper.get(depth);

        for (Node node : nodes) {
            int outEdgesCount = GraphUtils.outEdgesCount(node);
            if (GraphUtils.isLeaf(node))
                continue;
            if (outEdgesCount == 1) {
                nodeToRadiusMapper.put(node, 1D);
                continue;
            }

            double circumference = 0;
            double radius = 0;
            if (firstLevel)
                circumference = outEdgesCount;
            else {
                for (Edge edge : ((DirectedGraph) graph).getOutEdges(node))
                    circumference += 2 * countNodeRadius(node);

                radius = circumference / (2 * Math.PI);
            }
            nodeToRadiusMapper.put(node, radius);
        }

        for (Node node : nodes) {
            if (!GraphUtils.isLeaf(node))
                setNodeHierarchyPosition(node);
        }
    }

    private double countNodeRadius(Node mainNode) {
        double totalRadius = 0;

        for (Edge outEdge : ((DirectedGraph) graph).getOutEdges(mainNode)) {
            Node oppositeNode = graph.getOpposite(mainNode, outEdge);

            Double radius = nodeToRadiusMapper.get(oppositeNode);
            if (radius == null)
                radius = 1D;

            totalRadius += radius;
        }

        return totalRadius;
    }

    private void setNodeHierarchyPosition(Node mainNode) {
        NodeData mainNodeData = mainNode.getNodeData();

        int nodeCount = 0;
        Node previousNode = null;
        for (Edge edge : ((DirectedGraph) graph).getOutEdges(mainNode)) {
            Node currentNode = graph.getOpposite(mainNode, edge);
            NodeData currentNodeData =  currentNode.getNodeData();
            double currentAngleInDegrees = 0;

            // first node
            if (nodeCount == 0) {
                Double dx = nodeToRadiusMapper.get(currentNode);

                currentNodeData.setX(mainNodeData.x() + (dx != null ? (float) (double) dx : 1.0F));
                currentNodeData.setY(mainNodeData.y());
            }
            // other nodes
            else {
                double angle = countAngle(nodeToRadiusMapper.get(previousNode),
                                          nodeToRadiusMapper.get(currentNode),
                                          nodeToRadiusMapper.get(mainNode));
                currentAngleInDegrees += Math.toDegrees(angle);

                double acuteAngleInDegrees;
                boolean isPositiveXAxis = true;
                boolean isPositiveYAxis = true;
                if (currentAngleInDegrees < 90)
                    acuteAngleInDegrees = currentAngleInDegrees;
                else if (currentAngleInDegrees < 180) {
                    acuteAngleInDegrees = currentAngleInDegrees - (2 * (currentAngleInDegrees - 90));
                    isPositiveXAxis = false;
                }
                else if (currentAngleInDegrees < 270) {
                    acuteAngleInDegrees = currentAngleInDegrees - 180;
                    isPositiveXAxis = false;
                    isPositiveYAxis = false;
                }
                else {// currentAngleInDegrees <270, 360>
                    acuteAngleInDegrees = currentAngleInDegrees - 180 - (2 * (currentAngleInDegrees - 90));
                    isPositiveYAxis = false;
                }

                Double radius = nodeToRadiusMapper.get(currentNode);
                if (radius == null)
                    radius = 1.0;
                
                double angleInRadians = Math.toRadians(acuteAngleInDegrees);
                float dx = (float) (radius * Math.cos(angleInRadians));
                float dy = (float) (radius * Math.sin(angleInRadians));

                setPositionRecursiveForAll(mainNode, currentNode, isPositiveXAxis ? dx : -dx, isPositiveYAxis ? dy : -dy);
            }
            nodeCount++;
            previousNode = currentNode;
        }
    }

    private void setPositionRecursiveForAll(Node mainNode, Node node, float dx, float dy) {
        NodeData nodeData = node.getNodeData();

        float otherNodesDx = nodeData.x() - (mainNode.getNodeData().x() + dx);
        float otherNodesDy = nodeData.y() - (mainNode.getNodeData().y() + dy);

        nodeData.setX(mainNode.getNodeData().x() + dx);
        nodeData.setY(mainNode.getNodeData().y() + dy);

        setChangeOfPositionForAllChildNodes(node, otherNodesDx, otherNodesDy);
    }

    private void setChangeOfPositionForAllChildNodes(Node parentNode, float dx, float dy) {
        for (Edge outEdge : ((DirectedGraph) graph).getOutEdges(parentNode)) {
            Node childNode = graph.getOpposite(parentNode, outEdge);

            NodeData childNodeData = childNode.getNodeData();
            childNodeData.setX(childNodeData.x() + dx);
            childNodeData.setY(childNodeData.y() + dy);

            setChangeOfPositionForAllChildNodes(childNode, dx, dy);
        }
    }

    private double countAngle(Double previousNodeRadius, Double currentNodeRadius, Double parentNodeRadius) {
        if (previousNodeRadius == null)
            previousNodeRadius = 1D;
        if (currentNodeRadius == null)
            currentNodeRadius = 1D;
        if (parentNodeRadius == null)
            parentNodeRadius = 1D;

        return (previousNodeRadius + currentNodeRadius) / parentNodeRadius * 2 * Math.PI;
    }

    public LayoutProperty[] getProperties() {
        LayoutProperty idProperty;
        try {
            idProperty = LayoutProperty.createProperty(this, int.class, "Node id:", "Property category", "Id of starting node", "getNodeId", "setNodeId");
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }

        return new LayoutProperty[]{idProperty};
    }

    public void resetPropertiesValues() {
    }

    public void endAlgo() {
    }

// <editor-fold defaultstate="collapsed" desc="getters and setters">
    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }// </editor-fold>
}
