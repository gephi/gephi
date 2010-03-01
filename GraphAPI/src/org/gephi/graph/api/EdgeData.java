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
 * Contains all extended data related to an edge, including access to its
 * attributes.
 * 
 * @author Mathieu Bastian
 * @see #getAttributes()
 */
public interface EdgeData extends Renderable {

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
     * If no identifier has been set, returns the system integer identifier.
     * @return              the node identifier
     */
    public String getId();

    /**
     * Sets the string identifier of this edge. This identifier can be set
     * by users, in contrario of {@link Edge#getId()} which is set by the system.
     * @param id            the id that is to be set for this edge
     */
    public void setId(String id);

    /**
     * Returns the edge label, or <code>null</code> if none has been set.
     * @return              the edge lable, or <code>null</code>
     */
    public String getLabel();

    /**
     * Sets this edge label.
     * @param string        the label that is to be set as this edge label
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
