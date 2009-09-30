package org.gephi.preview;

import java.util.ArrayList;
import java.util.Iterator;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.supervisor.GraphSupervisor;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class GraphImpl implements Graph {

    private final GraphSupervisor supervisor;
    private final ArrayList<Node> m_nodes = new ArrayList<Node>();

    private final PVector m_minPos = new PVector();
    private final PVector m_maxPos =  new PVector();

    public GraphImpl(GraphSupervisor supervisor) {
        this.supervisor = supervisor;
    }

    public GraphSupervisor getSupervisor() {
        return supervisor;
    }

    public final Iterator<Node> getNodes() {
        return m_nodes.iterator();
    }

    public final PVector getMinPos() {
        return m_minPos;
    }

    public final PVector getMaxPos() {
        return m_maxPos;
    }

    public void addNode(NodeImpl node) {
        m_nodes.add(node);

        // update graph's bounding box
        {
            PVector topLeftPos = node.getTopLeftPosition();
            PVector bottomRightPos = node.getBottomRightPosition();

            if (m_nodes.size() == 1) {
                // first initialization
                m_minPos.set(topLeftPos);
                m_maxPos.set(bottomRightPos);
            } else {
                if (topLeftPos.x < m_minPos.x) {
                    m_minPos.x = topLeftPos.x;

                }
                if (topLeftPos.y < m_minPos.y) {
                    m_minPos.y = topLeftPos.y;

                }
                if (bottomRightPos.x > m_maxPos.x) {
                    m_maxPos.x = bottomRightPos.x;

                }
                if (bottomRightPos.y > m_maxPos.y) {
                    m_maxPos.y = bottomRightPos.y;

                }
            }
        }
    }

    public boolean showNodes() {
        return supervisor.getNodeSupervisor().getShowNodes();
    }
}
