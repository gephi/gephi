
/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;

/**
 *
 * @author Eduardo
 */
public abstract class BasicItem implements GraphContextMenuItem{
    protected Node[] nodes;
    protected HierarchicalGraph graph;

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        this.nodes = nodes;
        this.graph = graph;
    }

    public String getDescription() {
        return null;
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public ContextMenuItemManipulator[] getSubItems() {
        return null;
    }

    public Integer getMnemonicKey() {
        return null;
    }
}
