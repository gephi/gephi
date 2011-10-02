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
package org.gephi.visualization.hull;

import java.util.Iterator;
import org.gephi.utils.collection.avl.AVLItemAccessor;
import org.gephi.utils.collection.avl.ParamAVLTree;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.graph.api.TextData;
import org.gephi.visualization.apiimpl.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class ConvexHull implements Renderable {

    private Node metaNode;
    private ParamAVLTree<Node> groupNodesTree;
    private ModelImpl[] hullNodes;
    private float alpha = 0.5f;
    private ModelImpl model;
    private float centroidX;
    private float centroidY;

    public ConvexHull() {
        groupNodesTree = new ParamAVLTree<Node>(new AVLItemAccessor<Node>() {

            public int getNumber(Node item) {
                return item.getId();
            }
        });
    }

    public void addNode(Node node) {
        groupNodesTree.add(node);
    }

    public Node getMetaNode() {
        return metaNode;
    }

    public void setMetaNode(Node metaNode) {
        this.metaNode = metaNode;
    }

    public ModelImpl[] getNodes() {
        return hullNodes;
    }

    public Node[] getGroupNodes() {
        return groupNodesTree.toArray(new Node[0]);
    }

    private ModelImpl[] computeHull() {
        Iterator<Node> itr = groupNodesTree.iterator();
        for (; itr.hasNext();) {
            Node n = itr.next();
            if (n.getNodeData().getModel() == null) {
                itr.remove();
            } else if (model != null && !n.getNodeData().getModel().isCacheMatching(model.getCacheMarker())) {
                itr.remove();
            }
        }
        Node[] n = AlgoHull.calculate(groupNodesTree.toArray(new Node[0]));
        ModelImpl[] models = new ModelImpl[n.length];
        float cenX = 0;
        float cenY = 0;
        int len = 0;
        for (int i = 0; i < n.length; i++) {
            NodeData nd = n[i].getNodeData();
            models[i] = (ModelImpl) nd.getModel();
            cenX += nd.x();
            cenY += nd.y();
            len++;
        }
        centroidX = cenX / len;
        centroidY = cenY / len;
        return models;
    }

    public void recompute() {
        this.hullNodes = computeHull();
    }

    public void setX(float x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setY(float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setZ(float z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getRadius() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSize(float size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float r() {
        return metaNode.getNodeData().r();
    }

    public float g() {

        return metaNode.getNodeData().g();
    }

    public float b() {
        return metaNode.getNodeData().b();
    }

    public void setR(float r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setG(float g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setB(float b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setColor(float r, float g, float b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model obj) {
        this.model = (ModelImpl) obj;
    }

    public TextData getTextData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTextData(TextData textData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float x() {
        return centroidX;
    }

    public float y() {
        return centroidY;
    }

    public float z() {
        return 0;
    }

    public boolean isLabelVisible() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLabelVisible(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLabelSize(float labelSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getLabelSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
