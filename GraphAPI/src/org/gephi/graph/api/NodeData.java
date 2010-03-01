/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
public interface NodeData extends Renderable {

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
     * @param string        the label that is to be set as this node label
     */
    public void setLabel(String label);

    /**
     * Returns the string identifier of this node. This identifier can be set
     * by users, in contrario of {@link Node#getId()} which is set by the system.
     * <p>
     * If no identifier has been set, returns the system integer identifier.
     * @return              the node identifier
     */
    public String getId();

    /**
     * Sets the string identifier of this node. This identifier can be set
     * by users, in contrario of {@link Node#getId()} which is set by the system.
     * @param id            the id that is to be set for this node
     */
    public void setId(String id);

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

    /**
     * Returns node's attributes.
     * @return              node's attributes
     */
    public Attributes getAttributes();
}
