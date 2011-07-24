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
     * @return the display name of this ranking
     */
    public String getName();

    /**
     * Return the type of element this ranking is manipulating. Value can either be
     * <code>Ranking.NODE_ELEMENT</code> or <code>Ranking.EDGE_ELEMENT</code>.
     * @return the type of element this ranking is manipulating
     */
    public String getElementType();
}
