/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gephi.org>.
 */
package org.gephi.layout.plugin;

import org.gephi.graph.api.GraphController;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;

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
        return !isConverged() && graphController != null;
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public boolean isConverged() {
        return converged;
    }
}
