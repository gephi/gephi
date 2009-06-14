/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import org.gephi.graph.api.ClusteredGraph;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public interface CoarseningStrategy {

    public void coarsen(ClusteredGraph graph);
    public void refine(ClusteredGraph graph);
}
