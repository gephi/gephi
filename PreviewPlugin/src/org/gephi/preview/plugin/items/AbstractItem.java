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
package org.gephi.preview.plugin.items;

import java.util.HashMap;
import java.util.Map;
import org.gephi.preview.api.Item;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractItem implements Item {

    protected final String type;
    protected final Object source;
    protected final Map<String, Object> data;

    public AbstractItem(Object source, String type) {
        this.type = type;
        this.source = source;
        this.data = new HashMap<String, Object>();
    }

    public String getType() {
        return type;
    }

    public Object getSource() {
        return source;
    }

    public <D> D getData(String key) {
        return (D) data.get(key);
    }

    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String[] getKeys() {
        return data.keySet().toArray(new String[0]);
    }
}
