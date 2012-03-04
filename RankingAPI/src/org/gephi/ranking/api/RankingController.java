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
     * Sets whether rankings use a local or a global scale. When calculating the
     * minimum and maximum value (i.e. the scale) rankings can use the complete graph
     * or only the currently visible graph. When using the visible graph it is called
     * the <b>local</b> scale.
     * @param useLocalScale <code>true</code> for local, <code>false</code> for global
     */
    public void setUseLocalScale(boolean useLocalScale);

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
