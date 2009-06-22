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

import org.gephi.data.attributes.api.AttributeManager;

/**
 *
 * @author Mathieu
 */
public interface ContainerLoader {

    public void addEdge(EdgeDraft edgeDraft);

    public void addNode(NodeDraft nodeDraft);

    public NodeDraft getNode(String id);

    public boolean nodeExists(String id);

    public EdgeDraft getEdge(String id);

    public EdgeDraft getEdge(NodeDraft source, NodeDraft target);

    public boolean edgeExists(String id);

    public boolean edgeExists(NodeDraft source, NodeDraft target);

    public void setEdgeDefault(EdgeDefault edgeDefault);

    public AttributeManager getAttributeManager();

    public ContainerFactory factory();

    public interface ContainerFactory {

        public NodeDraft newNodeDraft();

        public EdgeDraft newEdgeDraft();
    }
}
