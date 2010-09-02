/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 * 
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic.api;

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
 */
public interface DynamicController {

    /**
     * Returns the dynamic model for the current workspace, or {@code null}
     * if the project is empty.
     *
     * @return the current dynamic model.
     */
    public DynamicModel getModel();

    /**
     * Returns the dynamic model for the given {@code workspace}.
     *
     * @param workspace the workspace that dynamic model is to be returned
     *
     * @return the {@code workspace}'s dynamic model.
     */
    public DynamicModel getModel(Workspace workspace);

    /**
     * Sets the time interval wrapped by the {@code DynamicGraph} of
     * the current workspace.
     *
     * @param interval an object to get endpoints from
     */
    public void setVisibleInterval(TimeInterval interval);

    /**
     * Sets the time interval wrapped by the {@code DynamicGraph} of
     * the current workspace.
     *
     * @param low  the left endpoint
     * @param high the right endpoint
     */
    public void setVisibleInterval(double low, double high);

    /**
     * Sets the current time format. This should be done when the model is inited.
     * @param timeFormat the time format that is to be set as current
     */
    public void setTimeFormat(DynamicModel.TimeFormat timeFormat);

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
