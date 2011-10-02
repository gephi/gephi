/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 * 
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
package org.gephi.dynamic.api;

import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.project.api.Workspace;

/**
 * This controller maintains the dynamic models, one per {@code Workspace}.
 * <p>
 * It is a service and can therefore be found in Lookup:
 * <pre>
 * DynamicController dc = Lookup.getDefault().lookup(DynamicController.class);
 * </pre>
 * 
 * @author Cezary Bartosiak
 * @author Mathieu Bastian
 */
public interface DynamicController {

    /**
     * Returns the dynamic model for the current workspace, or {@code null}
     * if the project is empty.
     *
     * @return              the current dynamic model.
     */
    public DynamicModel getModel();

    /**
     * Returns the dynamic model for the given {@code workspace}.
     *
     * @param workspace the workspace that dynamic model is to be returned
     *
     * @return              the {@code workspace}'s dynamic model.
     */
    public DynamicModel getModel(Workspace workspace);

    /**
     * Sets the time interval wrapped by the {@code DynamicGraph} of
     * the current workspace.
     *
     * @param interval      an object to get endpoints from
     */
    public void setVisibleInterval(TimeInterval interval);

    /**
     * Sets the time interval wrapped by the {@code DynamicGraph} of
     * the current workspace.
     *
     * @param low           the left endpoint
     * @param high          the right endpoint
     */
    public void setVisibleInterval(double low, double high);

    /**
     * Sets the current time format. This should be done when the model is inited.
     * @param timeFormat    the time format that is to be set as current
     */
    public void setTimeFormat(DynamicModel.TimeFormat timeFormat);

    /**
     * Sets the current <code>ESTIMATOR</code> used to get values from
     * {@link org.gephi.data.attributes.type.DynamicType}. Default is <b><code>Estimator.FIRST</code></b>.
     * @param estimator     the estimator that is to be set
     */
    public void setEstimator(Estimator estimator);

    /**
     * Sets the current number <code>ESTIMATOR</code> used to get values from
     * {@link org.gephi.data.attributes.type.DynamicType}. Default is <b><code>Estimator.AVERAGE</code></b>.
     * @param estimator     the number estimator that is to be set
     */
    public void setNumberEstimator(Estimator estimator);

    /**
     * Adds <code>listener</code> to the listeners of this model. It receives
     * events when model is changed.
     * @param listener      the listener that is to be added
     */
    public void addModelListener(DynamicModelListener listener);

    /**
     * Removes <code>listener</code> to the listeners of this model.
     * @param listener      the listener that is to be removed
     */
    public void removeModelListener(DynamicModelListener listener);
}
