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

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.io.importer.spi.Importer;

/**
 * Interface for a loading a {@link Container} with graph and attributes data from an importers.
 * Data pushed to a container are not appended directly to the main data structure, <code>Processor</code>
 * are doing this job. 
 * <p>
 * Use the draft factory for getting <code>NodeDraft</code> and <code>EdgeDraft</code> instances.
 * <p>
 * For pushing columns associated to nodes and edges, retrieve attribute model by
 * calling {@link #getAttributeModel()}.
 * <h3>How to push nodes with attributes</h3>
 * There is two steps, first identify columns and then push values.
 * <pre>//Add a URL column to nodes, must be done once only before importing nodes
 * AttributeColumn col = getAttributeModel().getNodeTable().addColumn("url", AttributeType.STRING);
 * //Write the URL value to a node draft
 * nodeDraft.addAttributeValue(col, "http://gephi.org");
 *</pre>
 * @author Mathieu Bastian
 * @see Importer
 * @see AttributeModel
 */
public interface ContainerLoader {

    /**
     * Adds an edge to the container. The edge must have <b>source</b> and
     * <b>target</b> defined. If the edge already exist, it is ignored. Source
     * and target nodes must be added to the container before pushing
     * <code>edgeDraft</code>.
     * @param edgeDraft         the edge that is to be pushed to the container
     */
    public void addEdge(EdgeDraft edgeDraft);

    /**
     * Adds a node to the container. Identified by its <b>id</b>. If no id
     * is present, a unique identifier is generated.
     * @param nodeDraft         the node that is to be pushed to the container
     */
    public void addNode(NodeDraft nodeDraft);

    /**
     * Removes an edge from the container. Do nothing if the edge is not in the
     * container.
     * @param edgeDraft         the edge that is to be removed from the container
     */
    public void removeEdge(EdgeDraft edgeDraft);

    /**
     * Returns the node with the given <code>id</code>, or create a new node
     * with this id if not found.
     * @param id                a node identifier
     * @return                  the found node, or a new default node
     */
    public NodeDraft getNode(String id);

    /**
     * Returns <code>true</code> if a node exists with the given <code>id</code>.
     * @param id                a node identifier
     * @return                  <code>true</code> if node exists, <code>false</code>
     * otherwise
     */
    public boolean nodeExists(String id);

    /**
     * Returns the edge with the given <code>id</code>, or <code>null</code> if
     * not found.
     * @param id                an edge identifier
     * @return                  the edge with <code>id</code> as an identifier, or
     * <code>null</code> if not found
     */
    public EdgeDraft getEdge(String id);

    /**
     * Returns the edge with the given <code>source</code> and <code>target</code>
     * or <code>null</code> if not found.
     * @param source            the edge source node
     * @param target            the edge target node
     * @return                  the edge from <code>source</code> to
     * <code>target</code> or <code>null</code> if not found
     */
    public EdgeDraft getEdge(NodeDraft source, NodeDraft target);

    /**
     * Returns <code>true</code> if an edge exists with the given <code>id</code>.
     * @param id                an edge identifier
     * @return                  <code>true</code> if edge exists, <code>false</code>
     * otherwise
     */
    public boolean edgeExists(String id);

    /**
     * Returns <code>true</code> if an edge exists from <code>source</code> to
     * <code>target</code>.
     * @param source            the edge source node
     * @param target            the edge target node
     * @return                  <code>true</code> if edges exists, <code>false</code>
     * otherwise
     */
    public boolean edgeExists(NodeDraft source, NodeDraft target);

    /**
     * Set edge default type: <b>DIRECTED</b>, <b>UNDIRECTED</b> or <b>MIXED</b>.
     * Default value is directed.
     * @param edgeDefault       the edge default type value
     */
    public void setEdgeDefault(EdgeDefault edgeDefault);

    /**
     * Returns the attribute model for this container. Columns can be manipulated
     * from this model.
     * @return                  the attribute model
     */
    public AttributeModel getAttributeModel();

    /**
     * Returns the <b>factory</b> for building nodes and edges instances.
     * @return                  the draft factory
     */
    public DraftFactory factory();

    /**
     * Sets the time value where the interval starts. Nodes and edges can have
     * time intervals that defines their lifetime, this method sets the time
     * interval start. If not set by the user, default value is the yougest
     * element.
     * @param min
     */
    public void setTimeIntervalMin(String min);

    /**
     * Sets the time value where the interval ends. Nodes and edges can have
     * time intervals that defines their lifetime, this method sets the time
     * interval end. If not set by the user, default value is the oldest
     * element.
     * @param max
     */
    public void setTimeIntervalMax(String max);

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
