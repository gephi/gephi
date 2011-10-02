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
package org.gephi.graph.dhns.edge;

import org.gephi.utils.collection.avl.AVLItem;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Abstract edge with one source and one target.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractEdge implements Edge, AVLItem {

    protected final int ID;
    protected final AbstractNode source;
    protected final AbstractNode target;
    protected EdgeDataImpl edgeData;

    public AbstractEdge(int ID, AbstractNode source, AbstractNode target) {
        this.source = source;
        this.target = target;
        this.ID = ID;
        this.edgeData = new EdgeDataImpl(this);
    }

    public AbstractEdge(AbstractEdge edge, AbstractNode source, AbstractNode target) {
        this.source = source;
        this.target = target;
        this.ID = edge.ID;
        this.edgeData = edge.edgeData;
    }

    public AbstractNode getSource() {
        return source;
    }

    public AbstractNode getTarget() {
        return target;
    }

    public AbstractNode getSource(int viewId) {
        return source.getInView(viewId);
    }

    public AbstractNode getTarget(int viewId) {
        return target.getInView(viewId);
    }

    public float getWeight() {
        return edgeData.getWeight();
    }

    public float getWeight(double low, double high) {
        return edgeData.getWeight(low, high);
    }

    public void setWeight(float weight) {
        edgeData.setWeight(weight);
    }

    public int getNumber() {
        return ID;
    }

    public EdgeDataImpl getEdgeData() {
        return edgeData;
    }

    public Attributes getAttributes() {
        return edgeData.getAttributes();
    }

    public AbstractEdge getUndirected(int viewId) {
        if (source == target) {
            return this;
        }
        AbstractEdge mutual = getSource(viewId).getEdgesInTree().getItem(target.getNumber());
        if (mutual != null && mutual.getId() < ID) {
            return mutual;
        }
        return this;
    }

    public boolean isDirected() {
        return true;
    }

    public boolean isSelfLoop() {
        return source == target;
    }

    public boolean isValid(int viewId) {
        return source.isValid(viewId) && target.isValid(viewId);
    }

    public boolean isValid() {
        return source.avlNode != null && target.avlNode != null;
    }

    public boolean isMetaEdge() {
        return false;
    }

    public boolean isMixed() {
        return false;
    }

    public boolean hasAttributes() {
        return edgeData.getAttributes() != null;
    }

    public void setAttributes(Attributes attributes) {
        if (attributes != null) {
            edgeData.setAttributes(attributes);
        }
    }

    public int getId() {
        return ID;
    }

    @Override
    public String toString() {
        return source.getId() + "-" + target.getId();
    }
}
