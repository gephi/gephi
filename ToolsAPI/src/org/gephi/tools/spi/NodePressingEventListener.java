/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gephi.tools.spi;

import org.gephi.graph.api.Node;

/**
 * Tool mouse pressing listener. Listen to continuous mouse pressing on the
 * visualization window and trigger selected nodes.
 * <p>
 * A tool which declares this listener is notified at a certain rate, up to
 * multiple times per second, the selected nodes.
 *
 * @author Mathieu Bastian
 * @see Tool
 */
public interface NodePressingEventListener extends ToolEventListener {

    /**
     * Notify <code>nodes</code> are currently pressed.
     * @param nodes the pressed nodes array
     */
    public void pressingNodes(Node[] nodes);

    /**
     * Notify mouse has been released.
     */
    public void released();
}
