/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin;

import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 * Base class for layout algorithms.
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public abstract class AbstractLayout implements Layout {

    private final LayoutBuilder layoutBuilder;
    protected GraphModel graphModel;
    protected DynamicModel dynamicModel;
    private boolean converged;

    public AbstractLayout(LayoutBuilder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
    }

    public LayoutBuilder getBuilder() {
        return layoutBuilder;
    }

    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        Workspace workspace = graphModel.getWorkspace();
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        if (dynamicController != null && workspace != null) {
            dynamicModel = dynamicController.getModel(workspace);
        }
    }

    public boolean canAlgo() {
        return !isConverged() && graphModel != null;
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public boolean isConverged() {
        return converged;
    }
}
