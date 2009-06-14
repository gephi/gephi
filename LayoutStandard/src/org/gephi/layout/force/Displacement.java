/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.NodeData;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public interface Displacement {

    public void moveNode(NodeData node, ForceVector forceData);
}
