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
 * Contains all extended data related to an edge, including access to its
 * attributes.
 * 
 * @author Mathieu Bastian
 * @see #getAttributes()
 */
public interface EdgeData extends Renderable, Attributable {

    /**
     * Returns the edge this edge data belongs.
     * @return      the edge
     */
    public Edge getEdge();

    /**
     * Returns the edge source node data. Similar as
     * <code>getEdge().getSource().getNodeData()</code>.
     * @return      the edge source node
     */
    public NodeData getSource();

    /**
     * Returns the edge target node data. Similar as
     * <code>getEdge().getSource().getNodeData()</code>.
     * @return      the edge source node
     */
    public NodeData getTarget();

    /**
     * Returns the string identifier of this edge. This identifier can be set
     * by users, in contrario of {@link Edge#getId()} which is set by the system.
     * <p>
     * Use <code>Graph.getEdge(String)</code> to find edges from this id.
     * <p>
     * If no identifier has been set, returns the system integer identifier.
     * @return              the node identifier
     */
    public String getId();

    /**
     * Returns the edge label, or <code>null</code> if none has been set.
     * @return              the edge lable, or <code>null</code>
     */
    public String getLabel();

    /**
     * Sets this edge label.
     * @param label         the label that is to be set as this edge label
     */
    public void setLabel(String label);

    /**
     * Returns the layout data object associated to this edge. Layout data are
     * temporary data layout algorithms can push to edges to save states when
     * computing.
     * @param <T>           must inherit from <code>LayoutData</code>
     * @return              the layout data of this edge, can be <code>null</code>
     */
    public <T extends LayoutData> T getLayoutData();

    /**
     * Sets the layout data of this edge. Layout data are temporary data layout
     * algorithms can push to edges to save states when computing.
     * @param layoutData    the layout data that is to be set for this edge
     */
    public void setLayoutData(LayoutData layoutData);

    /**
     * Gets the access to the attributes, all the custom data related to this
     * object.
     * @return  the attributes of this edge
     */
    public Attributes getAttributes();
}
