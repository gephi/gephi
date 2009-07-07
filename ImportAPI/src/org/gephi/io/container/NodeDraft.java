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
package org.gephi.io.container;

import java.awt.Color;
import org.gephi.data.attributes.api.AttributeColumn;

/**
 * Draft node, hosted by import containers to represent nodes found when importing. <code>Processors</code>
 * decide if this node will finally be appended to the graph or not.
 *
 * @author Mathieu Bastian
 * @see ContainerLoader
 */
public interface NodeDraft {

    public void setColor(Color color);

    public void setColor(String r, String g, String b);

    public void setColor(float r, float g, float b);

    public void setColor(int r, int g, int b);

    public void setColor(String color);

    public void setLabel(String label);

    public void setSize(float size);

    public void setId(String id);

    public void setX(float x);

    public void setY(float y);

    public void setZ(float z);

    public void setFixed(boolean fixed);

    public void setLabelVisible(boolean labelVisible);

    public void setLabelSize(float size);

    public void setVisible(boolean visible);

    public void addAttributeValue(AttributeColumn column, Object value);

    public void addChild(NodeDraft child);

    public void setParent(NodeDraft draft);
}
