/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gephi.org>.
 */
package org.gephi.layout;

import org.gephi.graph.api.GraphController;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;

/**
 * Base class for layout algorithms.
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public abstract class AbstractLayout implements Layout {

    private LayoutBuilder layoutBuilder;
    protected GraphController graphController;
    private boolean converged;

    public AbstractLayout(LayoutBuilder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
    }

    public LayoutBuilder getBuilder() {
        return layoutBuilder;
    }

    public void setGraphController(GraphController graphController) {
        this.graphController = graphController;
    }

    public boolean canAlgo() {
        return !isConverged();
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public boolean isConverged() {
        return converged;
    }
}
