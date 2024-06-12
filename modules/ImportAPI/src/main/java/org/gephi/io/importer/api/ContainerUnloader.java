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

    Iterable<NodeDraft> getNodes();

    int getNodeCount();

    Iterable<EdgeDraft> getEdges();

    int getEdgeCount();

    /**
     * Returns the number of mutual (directed) edges in the container;
     *
     * @return mutual edge count
     */
    int getMutualEdgeCount();

    boolean hasNodeColumn(String key);

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

    Iterable<ColumnDraft> getNodeColumns();

    Iterable<ColumnDraft> getEdgeColumns();

    EdgeDirectionDefault getEdgeDefault();

    TimeFormat getTimeFormat();

    TimeRepresentation getTimeRepresentation();

    ZoneId getTimeZone();

    String getSource();

    Class getEdgeTypeLabelClass();

    Double getTimestamp();

    Interval getInterval();

    ElementIdType getElementIdType();

    MetadataDraft getMetadata();

    //PARAMETERS GETTERS
    boolean allowSelfLoop();

    boolean allowAutoNode();

    boolean allowParallelEdges();

    boolean isAutoScale();

    boolean isFillLabelWithId();

    EdgeMergeStrategy getEdgesMergeStrategy();
}
