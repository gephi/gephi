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
 *
 * @author Mathieu Bastian
 * @see #getAttributes()
 */
public interface NodeData extends Renderable {

    public Node getNode();

    public String getLabel();

    public void setLabel(String string);

    public String getId();

    public void setId(String id);

    public <T extends LayoutData> T getLayoutData();

    public void setLayoutData(LayoutData layoutData);

    public boolean isFixed();

    public void setFixed(boolean fixed);

    public Attributes getAttributes();
}
