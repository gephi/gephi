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

package org.gephi.data.attributes;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeColumnImpl implements AttributeColumn {

    protected int index;
    protected String id;
    protected String title;
    protected AttributeType type;
    protected AttributeOrigin origin;
    protected Object defaultValue;

    public AttributeColumnImpl(int index, String id, String title, AttributeType attributeType, AttributeOrigin origin, Object defaultValue)
	{
		this.index = index;
		this.id = id;
		this.type=attributeType;
		this.title=title;
		this.origin = origin;
		this.defaultValue = defaultValue;
	}

    public AttributeType getAttributeType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }

    public AttributeOrigin getAttributeOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
	public boolean equals(Object obj)
	{
		if(obj instanceof AttributeColumn)
		{
			AttributeColumnImpl o = (AttributeColumnImpl)obj;
			return id.equals(o.id) && o.type==type;
		}
		return false;
	}

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

}
