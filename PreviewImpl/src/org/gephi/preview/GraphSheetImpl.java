/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.preview;

import java.util.Iterator;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.Point;
import org.gephi.preview.util.Vector;

/**
 * Implementation of a preview graph sheet.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class GraphSheetImpl implements GraphSheet {

    private final Graph graph;
    private final PointImpl originalTopLeft, originalBottomRight;
    private PointImpl topLeft, bottomRight;
    private Float width, height;
    private float margin = 0f;

    /**
     * Constructor.
     *
     * The bounding box of the stored graph is computed.
     *
     * @param graph  the preview graph
     */
    public GraphSheetImpl(Graph graph) {
        this.graph = graph;

        Iterator<Node> it = graph.getNodes().iterator();

        if (!it.hasNext()) {
            originalTopLeft = new PointImpl(0f, 0f);
            originalBottomRight = new PointImpl(0f, 0f);
            return;
        }

        Node node = it.next();
        Vector topLeftVector = new Vector(node.getTopLeftPosition());
        Vector bottomRightVector = new Vector(node.getBottomRightPosition());

        while (it.hasNext()) {
            node = it.next();

            if (node.getTopLeftPosition().getX() < topLeftVector.x) {
                topLeftVector.x = node.getTopLeftPosition().getX();
            }
            if (node.getTopLeftPosition().getY() < topLeftVector.y) {
                topLeftVector.y = node.getTopLeftPosition().getY();
            }
            if (node.getBottomRightPosition().getX() > bottomRightVector.x) {
                bottomRightVector.x = node.getBottomRightPosition().getX();
            }
            if (node.getBottomRightPosition().getY() > bottomRightVector.y) {
                bottomRightVector.y = node.getBottomRightPosition().getY();
            }
        }

        originalTopLeft = new PointImpl(topLeftVector);
        originalBottomRight = new PointImpl(bottomRightVector);

        updateDimensions();
    }

    public Graph getGraph() {
        return graph;
    }

    public Point getTopLeftPosition() {
        return topLeft;
    }

    public Point getBottomRightPosition() {
        return bottomRight;
    }

    public Float getWidth() {
        return width;
    }

    public Float getHeight() {
        return height;
    }

    public void setMargin(float value) {
        if (value != margin) {
            margin = value;
            updateDimensions();
        }
    }

    /**
     * Updates the graph sheet's dimensions according to the set margin.
     */
    private void updateDimensions() {
        Vector topLeftVector = new Vector(originalTopLeft);
        topLeftVector.sub(margin, margin, 0);
        topLeft = new PointImpl(topLeftVector);

        Vector bottomRightVector = new Vector(originalBottomRight);
        bottomRightVector.add(margin, margin, 0);
        bottomRight = new PointImpl(bottomRightVector);

        Vector box = new Vector(bottomRightVector);
        box.sub(topLeftVector);
        width = box.x;
        height = box.y;
    }
}
