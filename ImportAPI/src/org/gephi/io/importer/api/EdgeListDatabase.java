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

/**
 *
 * @author Mathieu Bastian
 */
public interface EdgeListDatabase extends Database {

    public String getNodeQuery();

    public String getEdgeQuery();

    public String getNodeAttributesQuery();

    public String getEdgeAttributesQuery();

    public void setNodeQuery(String nodeQuery);

    public void setEdgeQuery(String edgeQuery);

    public void setNodeAttributesQuery(String nodeAttributesQuery);

    public void setEdgeAttributesQuery(String edgeAttributesQuery);
}
