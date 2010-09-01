/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.tools.api;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * Controller API for requesting the opening and usage of edit window.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface  EditWindowController {
    void openEditWindow();

    void closeEditWindow();

    boolean isOpen();

    void editNode(Node node);

    void editNodes(final Node[] nodes);

    void editEdge(final Edge edge);

    void editEdges(final Edge[] edges);

    void disableEdit();
}
