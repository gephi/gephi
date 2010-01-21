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

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.io.importer.spi.Importer;

/**
 * Interface for a loading a {@link Container} with graph and attributes data from an importers.
 * Data pushed to a container are not appended directly to the main data structure, <code>Processor</code>
 * are doing this job. 
 * <p>
 * Use the draft factory for getting <code>NodeDraft</code> and <code>EdgeDraft</code> instances.
 *
 * @author Mathieu Bastian
 * @see Importer
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

    public AttributeModel getAttributeModel();

    public DraftFactory factory();

    /**
     * Node and edge draft factory. Creates node and edge to push in the container.
     */
    public interface DraftFactory {

        /**
         * Returns an empy node draft instance.
         * @return an instance of <code>NodeDraft</code>
         */
        public NodeDraft newNodeDraft();

        /**
         * Returns an empty edge draft instance. Note that <b>source</b> and <b>target</b> have to be
         * set.
         * @return an instance of <code>EdgeDraft</code>
         */
        public EdgeDraft newEdgeDraft();
    }
}
