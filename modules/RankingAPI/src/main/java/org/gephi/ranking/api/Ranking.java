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
package org.gephi.ranking.api;

import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;

/**
 * Rankings role is to provide numerical values from objects. These values are
 * then send to transformer to be converted in visual signs (e.g. color or size).
 * <p>
 * For instance for nodes, ranking can be the degree of the node or a numerical
 * value like an 'age' or 'duration'.
 * <p>
 * The <code>getElementType()</code> method should return either
 * <code>Ranking.NODE_ELEMENT</code> or <code>Ranking.EDGE_ELEMENT</code> to
 * define if it works with node or edge elements. This is important because it
 * defines which objects the <code>getValue()</code> eventually receives. For nodes,
 * it is given a {@link NodeData} object and for edges a {@link EdgeData}.
 * <p>
 * One can reuse the <code>AbstractRanking</code> class defined in the
 * <code>RankingPlugin</code> module.
 * 
 * @see Transformer
 * @author Mathieu Bastian
 */
public interface Ranking<Element> {

    /**
     * Element type for nodes. The ranking receives a <code>NodeData</code>
     * object.
     */
    public static final String NODE_ELEMENT = "nodes";
    /**
     * Element type for edges. The ranking receives a <code>EdgeData</code>
     * object.
     */
    public static final String EDGE_ELEMENT = "edges";
    /**
     * Default in degree ranking's name
     */
    public static final String DEGREE_RANKING = "degree";
    /**
     * Default out degree ranking's name
     */
    public static final String INDEGREE_RANKING = "indegree";
    /**
     * Default out degree ranking's name
     */
    public static final String OUTDEGREE_RANKING = "outdegree";

    /**
     * Returns the value of the element. 
     * @param element the element to get the value from
     * @return the element's value
     */
    public Number getValue(Element element);

    /**
     * Returns the minimum value of this ranking.
     * @return the minimum value
     */
    public Number getMinimumValue();

    /**
     * Returns the maximum value of this ranking.
     * @return the maximum value
     */
    public Number getMaximumValue();

    /**
     * Normalize <code>value</code> between 0 and 1 using the minimum and the
     * maximum value. For example if <code>value</code> is equal to the maximum,
     * it returns 1.0.
     * @param value the value to normalize
     * @return the normalized value between zero and one
     */
    public float normalize(Number value);

    /**
     * Unnormalize <code>normalizedValue</code> and returns the original element
     * value.
     * @param normalizedValue the value to unnormalize
     * @return the original value of the element
     */
    public Number unNormalize(float normalizedValue);

    /**
     * Returns the display name of this ranking.
     * @return the display name of this ranking
     */
    public String getDisplayName();

    /**
     * Returns the name of this ranking. It should be unique.
     * @return the name of this ranking
     */
    public String getName();

    /**
     * Return the type of element this ranking is manipulating. Value can either be
     * <code>Ranking.NODE_ELEMENT</code> or <code>Ranking.EDGE_ELEMENT</code>.
     * @return the type of element this ranking is manipulating
     */
    public String getElementType();
}
