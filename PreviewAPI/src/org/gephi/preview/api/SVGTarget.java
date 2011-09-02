/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.api;

import java.awt.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * @author Mathieu Bastian
 */
public interface SVGTarget extends RenderTarget {

    public static final String SCALE_STROKES = "svg.scale.strokes";
    public static final String TOP_NODES = "svg.top.nodes";
    public static final String TOP_EDGES = "svg.top.edges";
    public static final String TOP_NODE_LABELS = "svg.top.labels";

    public Element createElement(String qualifiedName);

    public Text createTextNode(String data);

    public Element getTopElement(String name);

    public Document getDocument();

    public float getScaleRatio();

    public String toHexString(Color color);
}
