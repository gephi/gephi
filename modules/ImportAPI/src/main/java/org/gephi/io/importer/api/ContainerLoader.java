/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.io.importer.api;

import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.importer.spi.Importer;
import org.joda.time.DateTimeZone;

/**
 * Interface for loading a {@link Container} with graph and attributes data from
 * an importer.
 * <p>
 * Data pushed to a container are not directly appended to the main graph
 * structure and <code>Processor</code> are doing this job.
 * <p>
 * Use the draft factory for getting <code>NodeDraft</code> and
 * <code>EdgeDraft</code> instances.
 *
 * @author Mathieu Bastian
 * @see Importer
 */
public interface ContainerLoader {

    /**
     * Adds an edge to this container. The edge must have <b>source</b> and
     * <b>target</b> defined. If the edge already exist, it is ignored. Source
     * and target nodes must be added to the container before pushing
     * <code>edgeDraft</code>.
     *
     * @param edgeDraft edge that is to be pushed to this container
     */
    public void addEdge(EdgeDraft edgeDraft);

    /**
     * Adds a node to this container. Identified by its <b>id</b>. If no id is
     * present, a unique identifier is generated.
     *
     * @param nodeDraft node that is to be pushed to this container
     */
    public void addNode(NodeDraft nodeDraft);

    /**
     * Removes an edge from this container. Do nothing if the edge is not in the
     * container.
     *
     * @param edgeDraft edge that is to be removed from this container
     */
    public void removeEdge(EdgeDraft edgeDraft);

    /**
     * Returns the node with the given <code>id</code>, or create a new node
     * with this id if not found.
     *
     * @param id node identifier
     * @return found node, or a new default node
     */
    public NodeDraft getNode(String id);

    /**
     * Returns <code>true</code> if a node exists with the given
     * <code>id</code>.
     *
     * @param id node identifier
     * @return <code>true</code> if node exists, <code>false</code> otherwise
     */
    public boolean nodeExists(String id);

    /**
     * Returns the edge with the given <code>id</code>, or <code>null</code> if
     * not found.
     *
     * @param id edge identifier
     * @return edge with <code>id</code> as an identifier, or <code>null</code>
     * if not found
     */
    public EdgeDraft getEdge(String id);

    /**
     * Returns <code>true</code> if an edge exists with the given
     * <code>id</code>.
     *
     * @param id an edge identifier
     * @return <code>true</code> if edge exists, <code>false</code> otherwise
     */
    public boolean edgeExists(String id);

    /**
     * Returns <code>true</code> if an edge exists from <code>source</code> to
     * <code>target</code>.
     *
     * @param source edge source node
     * @param target edge target node
     * @return <code>true</code> if edges exists, <code>false</code> otherwise
     */
    public boolean edgeExists(String source, String target);

    /**
     * Set edge default type: <b>DIRECTED</b>, <b>UNDIRECTED</b> or
     * <b>MIXED</b>. Default value is directed.
     *
     * @param edgeDefault edge default type value
     */
    public void setEdgeDefault(EdgeDirectionDefault edgeDefault);

    /**
     * Returns the node column draft with <code>key</code> as identifier.
     *
     * @param key node column key
     * @return column draft or null if not found
     */
    public ColumnDraft getNodeColumn(String key);

    /**
     * Returns the edge column draft with <code>key</code> as identifier.
     *
     * @param key edge column key
     * @return column draft or null if not found
     */
    public ColumnDraft getEdgeColumn(String key);

    /**
     * Adds a new node column to this container.
     * <p>
     * If a column with this key already exists, it is ignored and return the
     * existing column.
     *
     * @param key node column identifier
     * @param typeClass node column type
     * @return column draft
     */
    public ColumnDraft addNodeColumn(String key, Class typeClass);

    /**
     * Adds a new edge column to this container.
     * <p>
     * If a column with this key already exists, it is ignored and return the
     * existing column.
     *
     * @param key edge column identifier
     * @param typeClass edge column type
     * @return column draft
     */
    public ColumnDraft addEdgeColumn(String key, Class typeClass);

    /**
     * Adds a new dynamic node column to this container.
     * <p>
     * Dynamic attributes have values over time.
     * <p>
     * If a column with this key already exists, it is ignored and return the
     * existing column.
     *
     * @param key node column identifier
     * @param typeClass node column type
     * @param dynamic true if the column needs to be dynamic, false otherwise
     * @return column draft
     */
    public ColumnDraft addNodeColumn(String key, Class typeClass, boolean dynamic);

    /**
     * Adds a new dynamic edge column to this container.
     * <p>
     * Dynamic attributes have values over time.
     * <p>
     * If a column with this key already exists, it is ignored and return the
     * existing column.
     *
     * @param key edge column identifier
     * @param typeClass edge column type
     * @param dynamic true if the column needs to be dynamic, false otherwise
     * @return column draft
     */
    public ColumnDraft addEdgeColumn(String key, Class typeClass, boolean dynamic);

    /**
     * Returns the <b>factory</b> for building nodes and edges instances.
     *
     * @return the draft factory
     */
    public ElementDraft.Factory factory();

    /**
     * Sets the current Time Format for dynamic data, either <code>DATE</code>,
     * <code>DATETIME</code> or <code>DOUBLE</code>. It configures how the dates
     * are formatted.
     * <p>
     * The default value is <code>DOUBLE</code>.
     *
     * @param timeFormat time format
     */
    public void setTimeFormat(TimeFormat timeFormat);

    /**
     * Sets the current time representation, either <code>TIMESTAMP</code> or
     * <code>INTERVAL</code>.
     * <p>
     * The default value is <code>INTERVAL</code>.
     *
     * @param timeRepresentation time representation
     */
    public void setTimeRepresentation(TimeRepresentation timeRepresentation);

    /**
     * Sets the time zone that is used to parse date and time.
     * <p>
     * If not set, the local time zone is used.
     *
     * @param timeZone time zone
     */
    public void setTimeZone(DateTimeZone timeZone);

    //PARAMETERS SETTERS
    public void setAllowSelfLoop(boolean value);

    public void setAllowAutoNode(boolean value);

    public void setAllowParallelEdge(boolean value);

    public void setAutoScale(boolean autoscale);

    public void setEdgesMergeStrategy(EdgeWeightMergeStrategy edgesMergeStrategy);
}
