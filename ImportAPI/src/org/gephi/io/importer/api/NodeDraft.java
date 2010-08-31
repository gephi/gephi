/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.io.importer.api;

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

    public Color getColor();

    public void setLabel(String label);

    public void setSize(float size);

    public void setId(String id);

    public void setX(float x);

    public void setY(float y);

    public void setZ(float z);

    public void setFixed(boolean fixed);

    public void setLabelVisible(boolean labelVisible);

    public void setLabelSize(float size);

    public void setLabelColor(Color color);

    public void setLabelColor(String r, String g, String b);

    public void setLabelColor(float r, float g, float b);

    public void setLabelColor(int r, int g, int b);

    public void setLabelColor(String color);

    public void setVisible(boolean visible);

    public void addAttributeValue(AttributeColumn column, Object value);

    public void addAttributeValue(AttributeColumn column, Object value, String start, String end) throws IllegalArgumentException;

    public void addChild(NodeDraft child);

    public void setParent(NodeDraft draft);

    public void addTimeInterval(String start, String end) throws IllegalArgumentException;
}
