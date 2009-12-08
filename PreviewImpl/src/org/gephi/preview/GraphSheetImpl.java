package org.gephi.preview;

import java.util.Iterator;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.Node;
import processing.core.PVector;

/**
 * Implementation of a preview graph sheet.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class GraphSheetImpl implements GraphSheet {

    private final Graph graph;
    private final PVector topLeft = new PVector();
    private final PVector bottomRight = new PVector();

    /**
     * Constructor.
     *
     * @param graph  the preview graph
     */
    public GraphSheetImpl(Graph graph) {
        this.graph = graph;
        updateBoundingBox();
    }

    /**
     * @see GraphSheet#getGraph()
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @see GraphSheet#getTopLeftPosition()
     */
    public PVector getTopLeftPosition() {
        return topLeft;
    }

    /**
     * @see GraphSheet#getBottomRightPosition()
     */
    public PVector getBottomRightPosition() {
        return bottomRight;
    }

    /**
     * Updates the bounding box of preview graph to set the dimensions of the
     * graph sheet.
     */
    private void updateBoundingBox() {
        Iterator<Node> it = graph.getNodes().iterator();

        if (!it.hasNext()) {
            return;
        }

        Node node = it.next();
        topLeft.set(node.getTopLeftPosition());
        bottomRight.set(node.getBottomRightPosition());

        while (it.hasNext())
        {
            node = it.next();

            if (node.getTopLeftPosition().x < topLeft.x) {
                topLeft.x = node.getTopLeftPosition().x;
            }
            if (node.getTopLeftPosition().y < topLeft.y) {
                topLeft.y = node.getTopLeftPosition().y;
            }
            if (node.getBottomRightPosition().x > bottomRight.x) {
                bottomRight.x = node.getBottomRightPosition().x;
            }
            if (node.getBottomRightPosition().y > bottomRight.y) {
                bottomRight.y = node.getBottomRightPosition().y;
            }
        }
    }
}
