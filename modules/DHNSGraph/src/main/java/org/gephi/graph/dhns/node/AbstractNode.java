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
package org.gephi.graph.dhns.node;

import org.gephi.graph.api.Attributes;
import org.gephi.utils.collection.avl.AVLItem;
import org.gephi.graph.api.Group;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;

/**
 * Implementation of node with default behaviour.
 *
 * @author Mathieu Bastian
 */
public class AbstractNode implements Node, Group, AVLItem {

    //For all views
    protected final NodeDataImpl nodeData;
    //Particular
    protected final int viewId;
    public AbstractNode parent;
    public int pre;
    public int size;
    public int level;
    public int post;
    public DurableAVLNode avlNode;
    protected boolean enabled;
    private EdgeOppositeTree edgesOutTree;
    private EdgeOppositeTree edgesInTree;
    private MetaEdgeTree metaEdgesOutTree;
    private MetaEdgeTree metaEdgesInTree;
    //Counting
    private int enabledInDegree;
    private int enabledOutDegree;
    private int enabledMutualDegree;
    private int mutualMetaEdgeDegree;

    public AbstractNode(int ID, int viewId) {
        this(viewId, new NodeDataImpl(ID, null), 0, 0, 0, null);
        nodeData.getNodes().add(this);
    }

    public AbstractNode(NodeDataImpl nodeData, int viewId) {
        this(viewId, nodeData, 0, 0, 0, null);
        nodeData.getNodes().add(this);
    }

    public AbstractNode(int ID, int viewId, int pre, int size, int level, AbstractNode parent) {
        this(viewId, new NodeDataImpl(ID, null), pre, size, level, parent);
        nodeData.getNodes().add(this);
    }

    public AbstractNode(NodeDataImpl nodeData, int viewId, int pre, int size, int level, AbstractNode parent) {
        this(viewId, nodeData, pre, size, level, parent);
        nodeData.getNodes().add(this);
    }

    private AbstractNode(int viewId, NodeDataImpl nodeData, int pre, int size, int level, AbstractNode parent) {
        this.viewId = viewId;
        this.parent = parent;
        this.pre = pre;
        this.size = size;
        this.level = level;
        this.post = pre - level + size;
        this.nodeData = nodeData;
        edgesOutTree = new EdgeOppositeTree(this);
        edgesInTree = new EdgeOppositeTree(this);
        metaEdgesOutTree = new MetaEdgeTree(this);
        metaEdgesInTree = new MetaEdgeTree(this);
    }

    public int getViewId() {
        return viewId;
    }

    public int getPre() {
        return avlNode.getIndex();
    }

    public int getPost() {
        this.post = pre - level + size;
        return post;
    }

    @Override
    public int getId() {
        return nodeData.ID;
    }

    @Override
    public int getNumber() {
        return nodeData.ID;
    }

    @Override
    public NodeDataImpl getNodeData() {
        return nodeData;
    }

    @Override
    public GroupData getGroupData() {
        return nodeData;
    }

    public Attributes getAttributes() {
        return nodeData.getAttributes();
    }

    public EdgeOppositeTree getEdgesInTree() {
        return edgesInTree;
    }

    public void setEdgesInTree(EdgeOppositeTree edgesInTree) {
        this.edgesInTree = edgesInTree;
    }

    public EdgeOppositeTree getEdgesOutTree() {
        return edgesOutTree;
    }

    public void setEdgesOutTree(EdgeOppositeTree edgesOutTree) {
        this.edgesOutTree = edgesOutTree;
    }

    public MetaEdgeTree getMetaEdgesInTree() {
        return metaEdgesInTree;
    }

    public void setMetaEdgesInTree(MetaEdgeTree metaEdgesInTree) {
        this.metaEdgesInTree = metaEdgesInTree;
    }

    public MetaEdgeTree getMetaEdgesOutTree() {
        return metaEdgesOutTree;
    }

    public void setMetaEdgesOutTree(MetaEdgeTree metaEdgesOutTree) {
        this.metaEdgesOutTree = metaEdgesOutTree;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void clearMetaEdges() {
        metaEdgesOutTree = new MetaEdgeTree(this);
        metaEdgesInTree = new MetaEdgeTree(this);
    }

    public boolean isValid(int viewId) {
        return avlNode != null && this.viewId == viewId;
    }

    public AbstractNode getInView(int viewId) {
        if (avlNode == null) {
            return null;
        }
        if (this.viewId == viewId) {
            return this;
        }
        return nodeData.getNodes().get(viewId);
    }

    public void removeFromView(int viewId) {
        nodeData.getNodes().remove(viewId);
    }

    public int countInViews() {
        return nodeData.getNodes().getCount();
    }

    public void incEnabledInDegree() {
        enabledInDegree++;
    }

    public void incEnabledOutDegree() {
        enabledOutDegree++;
    }

    public void incEnabledMutualDegree() {
        enabledMutualDegree++;
    }

    public void incMutualMetaEdgeDegree() {
        mutualMetaEdgeDegree++;
    }

    public void decEnabledInDegree() {
        enabledInDegree--;
    }

    public void decEnabledOutDegree() {
        enabledOutDegree--;
    }

    public void decEnabledMutualDegree() {
        enabledMutualDegree--;
    }

    public void decMutualMetaEdgeDegree() {
        mutualMetaEdgeDegree--;
    }

    public int getEnabledInDegree() {
        return enabledInDegree;
    }

    public void setEnabledInDegree(int enabledInDegree) {
        this.enabledInDegree = enabledInDegree;
    }

    public int getEnabledMutualDegree() {
        return enabledMutualDegree;
    }

    public void setEnabledMutualDegree(int enabledMutualDegree) {
        this.enabledMutualDegree = enabledMutualDegree;
    }

    public int getEnabledOutDegree() {
        return enabledOutDegree;
    }

    public void setEnabledOutDegree(int enabledOutDegree) {
        this.enabledOutDegree = enabledOutDegree;
    }

    public int getMutualMetaEdgeDegree() {
        return mutualMetaEdgeDegree;
    }

    public void setMutualMetaEdgeDegree(int mutualMetaEdgeDegree) {
        this.mutualMetaEdgeDegree = mutualMetaEdgeDegree;
    }

    @Override
    public String toString() {
        return nodeData.getId();
    }
}
