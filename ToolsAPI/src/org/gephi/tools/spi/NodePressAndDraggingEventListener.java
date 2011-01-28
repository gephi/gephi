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
package org.gephi.tools.spi;

import org.gephi.graph.api.Node;

/**
 * Tool mouse press and dragging listener. Listen to a single node press on the
 * visualization window and trigger selected nodes, then triggers continuously
 * the recorded drag displacement.
 * <p>
 * The drag displacement is the difference between the start position and the
 * current position in a 2D bottom-left coordinate system.
 * <p>
 * A tool which declares this listener is notified at a certain rate, up to
 * multiple times per second, the selected nodes.
 *
 * @author Mathieu Bastian
 * @see Tool
 */
public interface NodePressAndDraggingEventListener extends ToolEventListener {

    /**
     * Notify <code>nodes</code> have been pressed by user on the visualization window.
     * @param nodes the clicked nodes
     */
    public void pressNodes(Node[] nodes);

    /**
     * Notify mouse is dragging
     */
    public void drag(float displacementX, float displacementY);

    /**
     * Notify mouse has been released.
     */
    public void released();
}
