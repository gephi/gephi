/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
public interface Item {

    public static final String NODE = "node";
    public static final String EDGE = "edge";
    public static final String NODE_LABEL = "node_label";
    public static final String EDGE_LABEL = "edge_label";

    public Object getSource();

    public String getType();

    public <D> D getData(String key);

    public void setData(String key, Object value);

    public String[] getKeys();
}
