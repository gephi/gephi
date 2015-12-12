/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.visualization.model.node;

import org.gephi.graph.api.ElementProperties;
import org.gephi.graph.api.Node;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.model.TextModel;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.octree.Octant;

/**
 *
 * @author mbastian
 */
public abstract class NodeModel implements Model, TextModel {

    protected final Node node;
    protected float cameraDistance;
    protected float[] dragDistance;
    //Octant
    protected Octant octant;
    protected int octantId;
    //Flags
    protected boolean selected;
    protected boolean highlight;
    public int markTime;
    public boolean mark;
    //Edges
    protected EdgeModel[] edges;
    protected int edgeLength;

    public NodeModel(Node node) {
        this.node = node;

        //Default
        dragDistance = new float[2];
        selected = false;
        mark = false;
        markTime = 0;

        //Edges
        edges = new EdgeModel[0];
    }

    public int octreePosition(float centerX, float centerY, float centerZ, float size) {
        //float radius = obj.getRadius();
        int index = 0;

        if (node.y() < centerY) {
            index += 4;
        }
        if (node.z() > centerZ) {
            index += 2;
        }
        if (node.x() < centerX) {
            index += 1;
        }

        return index;
    }

    public boolean isInOctreeLeaf(Octant leaf) {
//        float radius = node.size() / 2f;
        if (Math.abs(node.x() - leaf.getPosX()) > (leaf.getSize() / 2)
                || Math.abs(node.y() - leaf.getPosY()) > (leaf.getSize() / 2)
                || Math.abs(node.z() - leaf.getPosZ()) > (leaf.getSize() / 2)) {
            return false;
        }
        return true;
    }

    public abstract boolean selectionTest(Vecf distanceFromMouse, float selectionSize);

    public abstract float getCollisionDistance(double angle);

    public Node getNode() {
        return node;
    }

    public void setCameraDistance(float cameraDistance) {
        this.cameraDistance = cameraDistance;
    }

    public float getCameraDistance() {
        return cameraDistance;
    }

    public float getX() {
        return node.x();
    }

    public float getY() {
        return node.y();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public Octant getOctant() {
        return octant;
    }

    public void setOctant(Octant octant) {
        this.octant = octant;
    }

    public void setOctantId(int octantId) {
        this.octantId = octantId;
    }

    public int getOctantId() {
        return octantId;
    }

    @Override
    public boolean hasCustomTextColor() {
        return node.getTextProperties().getAlpha() > 0;
    }

    @Override
    public void setText(String text) {
        node.getTextProperties().setText(text);
    }

    @Override
    public float getTextWidth() {
        return node.getTextProperties().getWidth();
    }

    @Override
    public float getTextHeight() {
        return node.getTextProperties().getWidth();
    }

    @Override
    public String getText() {
        return node.getTextProperties().getText();
    }

    @Override
    public float getTextSize() {
        return node.getTextProperties().getSize();
    }

    @Override
    public float getTextR() {
        return node.getTextProperties().getR();
    }

    @Override
    public float getTextG() {
        return node.getTextProperties().getG();
    }

    @Override
    public float getTextB() {
        return node.getTextProperties().getB();
    }

    @Override
    public float getTextAlpha() {
        return node.getTextProperties().getAlpha();
    }

    @Override
    public boolean isTextVisible() {
        return node.getTextProperties().isVisible();
    }

    @Override
    public ElementProperties getElementProperties() {
        return node;
    }

    public float[] getDragDistanceFromMouse() {
        return dragDistance;
    }

    public void addEdge(EdgeModel model) {
        int id = edgeLength++;
        growEdges(id);
        edges[id] = model;
        if (model.getSourceModel() == this) {
            model.setOctantSourceId(id);
        } else {
            model.setOctantTargetId(id);
        }
    }

    public void removeEdge(EdgeModel model) {
        int id;
        if (model.getSourceModel() == this) {
            id = model.getOctantSourceId();
        } else {
            id = model.getOctantTargetId();
        }
        edges[id] = null;
    }

    public EdgeModel[] getEdges() {
        return edges;
    }
    protected static final long ONEOVERPHI = 106039;

    private void growEdges(final int index) {
        if (index >= edges.length) {
            final int newLength = (int) Math.min(Math.max((ONEOVERPHI * edges.length) >>> 16, index + 1), Integer.MAX_VALUE);
            final EdgeModel t[] = new EdgeModel[newLength];
            System.arraycopy(edges, 0, t, 0, edges.length);
            edges = t;
        }
    }
}
