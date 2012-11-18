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
package org.gephi.data.attributes.api;

import org.gephi.project.api.Workspace;

/**
 * This controller is the access door to {@link AttributeModel}, that contains
 * all attributes data. Attributes are simply any data that could be associated
 * with elements like nodes or edges. This module helps to organize data in
 * columsn and rows in a way they can be accessed in multiple, yet efficient ways.
 * <p>
 * This controller is a service, and exist in the system as a singleton. It can be
 * retrieved by using the following command:
 * <pre>
 * AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
 * </pre>
 * @author Mathieu Bastian
 */
public interface AttributeController {

    /**
     * Returns the model for the current <code>Workspace</code>. May return
     * <code>null</code> if there currently no <code>Worksapce</code> active.
     * <p>
     * The controller maintains the current project status and is responsible of
     * maintaining one <code>AttributeModel</code> instance per <code>Workspace</code>.
     * Hence, the model can also be accessed by using the following code:
     * <pre>
     * Workspace.getLookup().get(AttributeModel.class);
     * </pre>
     * @return the currently active model
     */
    public AttributeModel getModel();

    /**
     * Returns the model for the given <code>Workspace</code>.
     * <p>
     * The controller maintains the current project status and is responsible of
     * maintaining one <code>AttributeModel</code> instance per <code>Workspace</code>.
     * Hence, the model can also be accessed by using the following code:
     * <pre>
     * Workspace.getLookup().get(AttributeModel.class);
     * </pre>
     * @return the attribute model for <code>workspace</code>.
     */
    public AttributeModel getModel(Workspace workspace);

    /**
     * Create a new model independent from any <code>Workspace</code>. The model
     * can be used indepedently and then merged in another model.
     *
     * @return a new independent model
     * @see AttributeModel#mergeModel(org.gephi.data.attributes.api.AttributeModel)
     */
    public AttributeModel newModel();
}
