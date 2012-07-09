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
     * @param elementType the element type of the rankings
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
     * @param elementType   the element type of the transformer
     * @param name  the name of the transformer
     * @return the transformer defined as <code>name</code> and <code>elementType</code>
     * or <code>null</code> if not found
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
     * Returns <code>true</code> if rankings are using the currently visible
     * graph as a scale. If <code>false</code> the complete graph is used to determine
     * minimum and maximum values, the ranking scale.
     * @return <code>true</code> if using a local scale, <code>false</code> if
     * global scale
     */ 
    public boolean useLocalScale();

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
