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
