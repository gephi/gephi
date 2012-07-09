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

package org.gephi.graph.api;

import org.gephi.graph.spi.LayoutData;

/**
 * Contains all extended data related to a node, including access to its
 * attributes.
 * <p>
 * The node data is unique for a node, accross all views. Nodes can be get
 * from this node data by using <code>getRootNode()</code> or
 * <code>getNode(viewId)</code>.
 *
 * @author Mathieu Bastian
 * @see Node
 */
public interface NodeData extends Renderable, Attributable {

    /**
     * Returns the node this node data belongs in the <b>main</b> view. To get
     * the node in a particular view, see {@link #getNode(int) }.
     * @return              the node this node data belongs in the <b>main</b> view
     * @see GraphView
     */
    public Node getRootNode();

    /**
     * Returns the node this node data belongs in the view that has
     * <code>viewId</code> has identifier or <code>null</code> if the view
     * cannot be found.
     * @param viewId        the view identifier
     * @return              the node this node data belongs in the view
     * @see GraphView
     */
    public Node getNode(int viewId);

    /**
     * Returns the node label, or <code>null</code> if none has been set.
     * @return              the node lable, or <code>null</code>
     */
    public String getLabel();

    /**
     * Sets this node label.
     * @param label         the label that is to be set as this node label
     */
    public void setLabel(String label);

    /**
     * Returns the string identifier of this node. This identifier can be set
     * by users, in contrario of {@link Node#getId()} which is set by the system.
     * <p>
     * Use <code>Graph.getNode(String)</code> to find nodes from this id.
     * <p>
     * If no identifier has been set, returns the system integer identifier.
     * @return              the node identifier
     */
    public String getId();

    /**
     * Returns the layout data object associated to this node. Layout data are
     * temporary data layout algorithms can push to nodes to save states when
     * computing.
     * @param <T>           must inherit from <code>LayoutData</code>
     * @return              the layout data of this node, can be <code>null</code>
     */
    public <T extends LayoutData> T getLayoutData();

    /**
     * Sets the layout data of this node. Layout data are temporary data layout
     * algorithms can push to nodes to save states when computing.
     * @param layoutData    the layout data that is to be set for this node
     */
    public void setLayoutData(LayoutData layoutData);

    /**
     * Returns <code>true</code> if this node is fixed. A node can be fixed
     * to block it's position during layout.
     * @return              <code>true</code> if this node is fixed, <code>false</code>
     * otherwise
     */
    public boolean isFixed();

    /**
     * Sets this node fixed attribute. A node can be fixed
     * to block it's position during layout.
     * @param fixed         the fixed attribute value
     */
    public void setFixed(boolean fixed);
}
