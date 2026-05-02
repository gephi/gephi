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

import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.processor.spi.Processor;
import java.time.ZoneId;

/**
 * Interface for unloading a container.
 * <p>
 * Gives access to the draft graph elements, columns attributes. Also gives
 * access to basic settings and properties about the container's content.
 * <p>
 * Unloaders are used by <code>Processor</code> to load data from the container
 * to the main data structure.
 *
 * @author Mathieu Bastian
 * @see Processor
 */
public interface ContainerUnloader {

    /**
     * Returns all nodes in this container.
     *
     * @return an iterable of all node drafts
     */
    Iterable<NodeDraft> getNodes();

    /**
     * Returns the number of nodes in this container.
     *
     * @return node count
     */
    int getNodeCount();

    /**
     * Returns all edges in this container.
     *
     * @return an iterable of all edge drafts
     */
    Iterable<EdgeDraft> getEdges();

    /**
     * Returns the number of edges in this container.
     *
     * @return edge count
     */
    int getEdgeCount();

    /**
     * Returns the number of mutual (directed) edges in the container;
     *
     * @return mutual edge count
     */
    int getMutualEdgeCount();

    /**
     * Returns true if a node column with the given key exists in this container.
     *
     * @param key node column identifier
     * @return true if the column exists, false otherwise
     */
    boolean hasNodeColumn(String key);

    /**
     * Returns true if an edge column with the given key exists in this container.
     *
     * @param key edge column identifier
     * @return true if the column exists, false otherwise
     */
    boolean hasEdgeColumn(String key);

    /**
     * Returns true if the container contains nodes that were auto-created from edges.
     *
     * @return true if contains auto nodes, false otherwise
     */
    boolean containsAutoNodes();

    /**
     * Returns the node column draft with <code>key</code> as identifier.
     *
     * @param key node column key
     * @return column draft or null if not found
     */
    ColumnDraft getNodeColumn(String key);

    /**
     * Returns the edge column draft with <code>key</code> as identifier.
     *
     * @param key edge column key
     * @return column draft or null if not found
     */
    ColumnDraft getEdgeColumn(String key);

    /**
     * Returns all node columns in this container.
     *
     * @return an iterable of all node column drafts
     */
    Iterable<ColumnDraft> getNodeColumns();

    /**
     * Returns all edge columns in this container.
     *
     * @return an iterable of all edge column drafts
     */
    Iterable<ColumnDraft> getEdgeColumns();

    /**
     * Returns the default edge direction setting for this container.
     *
     * @return edge direction default
     */
    EdgeDirectionDefault getEdgeDefault();

    /**
     * Returns the time format used for dynamic data in this container.
     *
     * @return time format
     */
    TimeFormat getTimeFormat();

    /**
     * Returns the time representation used for dynamic data in this container, either {@code TIMESTAMP} or
     * {@code INTERVAL}.
     *
     * @return time representation
     */
    TimeRepresentation getTimeRepresentation();

    /**
     * Returns the time zone used to parse date and time values in this container.
     *
     * @return time zone
     */
    ZoneId getTimeZone();

    /**
     * Returns the source of the data in this container (e.g. a file name), or {@code null} if not set.
     *
     * @return source or null
     */
    String getSource();

    /**
     * Returns the class used for edge type labels, or {@code null} if the default (null label) is used.
     *
     * @return edge type label class or null
     */
    Class getEdgeTypeLabelClass();

    /**
     * Returns the graph-level timestamp applied to all elements in this container, or {@code null} if not set.
     *
     * @return graph timestamp or null
     */
    Double getTimestamp();

    /**
     * Returns the graph-level interval applied to all elements in this container, or {@code null} if not set.
     *
     * @return graph interval or null
     */
    Interval getInterval();

    /**
     * Returns the element id type used by this container.
     *
     * @return element id type
     */
    ElementIdType getElementIdType();

    /**
     * Returns the graph metadata in this container, or {@code null} if not set.
     *
     * @return metadata or null
     */
    MetadataDraft getMetadata();

    /**
     * Returns whether self-loops are allowed in this container.
     *
     * @return true if self-loops are allowed, false otherwise
     */
    boolean allowSelfLoop();

    /**
     * Returns whether nodes are automatically created from edges when the source or target node is not declared.
     *
     * @return true if auto-node creation is allowed, false otherwise
     */
    boolean allowAutoNode();

    /**
     * Returns whether parallel edges (multiple edges between the same pair of nodes) are allowed.
     *
     * @return true if parallel edges are allowed, false otherwise
     */
    boolean allowParallelEdges();

    /**
     * Returns whether auto-scaling is enabled for this container.
     * <p>
     * When enabled, node positions are scaled to fit the default viewport after import.
     *
     * @return true if auto-scaling is enabled, false otherwise
     */
    boolean isAutoScale();

    /**
     * Returns whether node labels should be filled with the node id when no label is set.
     *
     * @return true if labels should be filled with ids, false otherwise
     */
    boolean isFillLabelWithId();

    /**
     * Returns the strategy used for merging parallel edge weights.
     *
     * @return edge merge strategy
     */
    EdgeMergeStrategy getEdgesMergeStrategy();

    /**
     * Returns true if this container contains a dynamic graph.
     * <p>
     * A dynamic graph has elements that appear or disappear over time.
     *
     * @return true if dynamic, false otherwise
     */
    boolean isDynamicGraph();

    /**
     * Returns true if this container contains elements that have dynamic
     * attributes.
     * <p>
     * Dynamic attributes are attributes with different values over time.
     *
     * @return true if dynamic attributes, false otherwise
     */
    boolean hasDynamicAttributes();
}
