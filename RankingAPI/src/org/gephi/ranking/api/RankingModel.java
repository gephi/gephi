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

import org.gephi.project.api.Workspace;

/**
 * Model for ranking data. 
 * <p>
 * That includes the list of rankings currently available,
 * separated in categories with different element types. It can returns all rankings
 * for nodes or edges, or any element type.
 * <p>
 * Rankings are builds thanks to <code>RankingBuilder</code> implementation. Implement
 * a new <code>RankingBuider</code> service to create new rankings.
 * <p>
 * The model also hosts the currently defined interpolator.
 * 
 * @see Ranking
 * @see Transformer
 * @author Mathieu Bastian
 */
public interface RankingModel {

    /**
     * Get all rankings for node elements. Rankings are classified with the type
     * of element they are manipulating. Rankings specific to node elements are
     * defined by the <code>Ranking.NODE_ELEMENT</code>.
     * @return All rankings for node elements
     */
    public Ranking[] getNodeRankings();

    /**
     * Get all rankings for edge elements. Rankings are classified with the type
     * of element they are manipulating. Rankings specific to edge elements are
     * defined by the <code>Ranking.EDGE_ELEMENT</code>.
     * @return All rankings for edge elements
     */
    public Ranking[] getEdgeRankings();

    /**
     * Get all rankings for <code>elementType</code> elements. Rankings are 
     * classified with the type of element they are manipulating. If 
     * <code>elementType</code> equals <code>Ranking.NODE_ELEMENT</code> this is
     * equivalent to {@link RankingModel#getNodeRankings() } method
     * @param  the element type of the rankings
     * @return All rankings for <code>elementType</code>
     */
    public Ranking[] getRankings(String elementType);

    /**
     * Return the specific ranking for <code>elementType</code> and with
     * the given <code>name</code>. Returns <code>null</code> if not found.
     * <p>
     * Default ranking names can be found in the {@link Ranking} interface. For 
     * attribute rankings, simply use the column identifier.
     * @param elementType the element type of the ranking
     * @param name the name of the ranking
     * @return the found ranking or <code>null</code> if not found
     */
    public Ranking getRanking(String elementType, String name);

    /**
     * Return all transformers specific to <code>elementType</code>. A transformer
     * defines his ability to transformer different element types. 
     * @param elementType the element type of the transformers
     * @return all transformers working with <code>elementType</code>
     */
    public Transformer[] getTransformers(String elementType);

    /**
     * Returns the specific transformer for <code>elementType</code> and with the
     * given <code>name</code>. Returns <code>null</code> if not found.
     * <p>
     * Default transformers name can be found in the {@link Transformer} interface.
     * @param elementType
     * @param name
     * @return 
     */
    public Transformer getTransformer(String elementType, String name);

    /**
     * Returns the current interpolator. The default interpolator is a simple
     * linear interpolation.
     * @return the current interpolator
     */
    public Interpolator getInterpolator();

    /**
     * Return the workspace this model is associated with
     * @return the workspace of this model
     */
    public Workspace getWorkspace();

    /**
     * If <code>transformer</code> is an auto transformer, returns the ranking
     * associated to it. 
     * @param transformer the transformer to obtain the ranking from
     * @return the ranking associated to <code>transformer</code> or <code>null</code>
     */
    public Ranking getAutoTransformerRanking(Transformer transformer);

    /**
     * Add <code>listener</code> as a ranking listener of this model
     * @param listener the listener to add
     */
    public void addRankingListener(RankingListener listener);

    /**
     * Remove <code>listener</code> as a ranking listener of this model
     * @param listener the listener to remove
     */
    public void removeRankingListener(RankingListener listener);
}
