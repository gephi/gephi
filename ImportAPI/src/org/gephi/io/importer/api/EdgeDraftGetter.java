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
package org.gephi.io.importer.api;

import java.awt.Color;
import java.util.List;
import org.gephi.data.attributes.api.AttributeValue;

/**
 *
 * @author Mathieu Bastian
 */
public interface EdgeDraftGetter extends EdgeDraft {

    public List<AttributeValue> getAttributeValues();

    public float getWeight();

    public Color getColor();

    public String getLabel();

    public boolean isVisible();

    public EdgeType getType();

    public boolean isLabelVisible();

    public float getLabelSize();

    public Color getLabelColor();

    public String getId();

    public NodeDraftGetter getSource();

    public NodeDraftGetter getTarget();

    public List<String[]> getSlices();
}
