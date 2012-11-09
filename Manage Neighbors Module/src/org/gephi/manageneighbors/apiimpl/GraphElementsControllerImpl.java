package org.gephi.manageneighbors.apiimpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.manageneighbors.api.GraphElementsController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author Jozef Barčák - barcak.jozef@gmail.com - 2012
 */
public class GraphElementsControllerImpl implements GraphElementsController {
    
    @Override
    public boolean hideLeaves(Node[] nodes) {
        if (canHideNodes(nodes)) {
            HierarchicalGraph graph = getHierarchicalGraph();

            try {
                Set<Node> parentNodes = new HashSet<Node>(Arrays.asList(nodes));

                for (Node node : nodes) {
                    Set<Node> hideNodes = new HashSet<Node>();
                    Node[] neighboringNodes = getNodeNeighbours(node);

                    for (Node neighboringNode : neighboringNodes) {
                        if (getNodeNeighbours(neighboringNode).length <= 1 && !parentNodes.contains(neighboringNode)) {
                            hideNodes.add(neighboringNode);
                        }
                    }

                    if (hideNodes.size() > 0) {
                        hideNodes.add(node);

                        Node group = graph.groupNodes(hideNodes.toArray(new Node[0]));

                        String label = node.getNodeData().getLabel();
                        
                        group.getNodeData().setLabel(label == null ? "+" : label + " +");
                        group.getNodeData().setSize(node.getNodeData().getSize());
                        group.getNodeData().setColor(node.getNodeData().r(), node.getNodeData().g(), node.getNodeData().b());
                        group.getNodeData().setX(node.getNodeData().x());
                        group.getNodeData().setY(node.getNodeData().y());
                    }
                }
            } catch (Exception e) {
                graph.readUnlockAll();
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message(e.getMessage());
                DialogDisplayer.getDefault().notifyLater(nd);
                return false;
            }
            
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canHideNodes(Node[] nodes) {
        Set<Node> parentNodes = new HashSet<Node>(Arrays.asList(nodes));
        
        for (Node node : nodes) {
            Node[] neighboringNodes = getNodeNeighbours(node);

            for (Node neighboringNode : neighboringNodes) {
                if (getNodeNeighbours(neighboringNode).length <= 1 && !parentNodes.contains(neighboringNode)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean showNode(Node node) {
        if (canShowNode(node)) {
            HierarchicalGraph graph = getHierarchicalGraph();
            graph.ungroupNodes(node);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void showNodes(Node[] nodes) {
        for (Node node : nodes) {
            showNode(node);
        }
    }

    @Override
    public boolean canShowNode(Node node) {
        HierarchicalGraph graph = getHierarchicalGraph();
        return getNodeChildrenCount(graph, node) > 0;
    }
    
    private HierarchicalGraph getHierarchicalGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph();
    }
    
    private Node[] getNodeNeighbours(Node node) {
        return getGraph().getNeighbors(node).toArray();
    }
    
    private Graph getGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
    }
    
    private int getNodeChildrenCount(HierarchicalGraph hg, Node n) {
        hg.readLock();
        int childrenCount = hg.getChildrenCount(n);
        hg.readUnlock();
        return childrenCount;
    }
    
}
