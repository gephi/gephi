/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance.api;

import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.project.api.Workspace;

/**
 * Manage and controls the appearance of elements through visual
 * transformations.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class);</pre>
 */
public interface AppearanceController {

    /**
     * Sets whether rankings use a local or a global scale. When calculating the
     * minimum and maximum value (i.e. the scale) rankings can use the complete
     * graph or only the currently visible graph. When using the visible graph
     * it is called the <b>local</b> scale.
     *
     * @param useLocalScale <code>true</code> for local, <code>false</code> for
     * global
     */
    public void setUseLocalScale(boolean useLocalScale);

    public void forceRankingFunction(Function function);

    public void forcePartitionFunction(Function function);

    /**
     * Apply the function's transformer. If the function is for nodes all nodes
     * in the visible graph will be transformed. Similarly for edges.
     *
     * @param function function to transform
     */
    public void transform(Function function);

    /**
     * Returns the appearance model for the current workspace.
     *
     * @return appearance model
     */
    public AppearanceModel getModel();

    /**
     * Returns the appearance model for the given workspace.
     *
     * @param workspace workspace
     * @return appearance model
     */
    public AppearanceModel getModel(Workspace workspace);

    /**
     * Returns the transformer associated with the given transformer UI.
     *
     * @param ui user interface instance
     * @return transformer instance or null if not found
     */
    public Transformer getTransformer(TransformerUI ui);
}
