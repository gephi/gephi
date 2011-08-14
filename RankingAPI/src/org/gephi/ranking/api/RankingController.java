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
 * Controller that maintains the ranking models, one per workspace.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>RankingController rc = Lookup.getDefault().lookup(RankingController.class);</pre>
 * <p>
 * Use <code>transform()</code> to apply transformers on ranking's elements. Transform
 * is a one shot action. For continuous transformation, start an auto transformer
 * using <code>startAutoTransform()</code>.
 * @see RankingModel
 * @author Mathieu Bastian
 */
public interface RankingController {

    /**
     * Returns the ranking model of the current workspace.
     * @return the ranking model of the current workspace
     */
    public RankingModel getModel();

    /**
     * Returns the ranking model of <code>workspace</code>. If it doesn't exists,
     * it creates one and put it in the workspace.
     * @param workspace the workspace containing the model
     * @return the ranking model of this workspace
     */
    public RankingModel getModel(Workspace workspace);

    /**
     * Sets the interpolator to be used when transforming values. This is set to the
     * current model only. If the model is changed (i.e. switch workspace), call 
     * this again.
     * <p>
     * Default interpolator implementations can be found in the {@link Interpolator}
     * class.
     * @param interpolator the interpolator to use for transformation. 
     */
    public void setInterpolator(Interpolator interpolator);

    /**
     * Apply the transformation of <code>transformer</code> on <code>ranking</code>.
     * The transformer will modify element's color or size according to the values
     * returned by the ranking. Before passing values to the transformer, they may
     * be transformer by the current interpolator.
     * @param ranking the ranking to give to the transformer
     * @param transformer the transformer to apply on the ranking's elements
     */
    public void transform(Ranking ranking, Transformer transformer);

    /**
     * Starts an auto transformation using <code>ranking</code> and 
     * <code>transformer</code>. The transformation is continuously applied to
     * the current graph. The operation is the same as <code>transform()</code>, 
     * except it is applied in a loop until <code>stopAutoTransform()</code> is
     * called.
     * <p>
     * Note that auto transformation work only in the current workspace and are
     * paused when the workspace is not current.
     * @param ranking the ranking to give to the transformer
     * @param transformer the transformer to apply on the ranking's elements
     */
    public void startAutoTransform(Ranking ranking, Transformer transformer);

    /**
     * Stops the auto transformation of <code>transfromer</code>.
     * @param transformer the transformer to stop auto transformation
     */
    public void stopAutoTransform(Transformer transformer);
}
