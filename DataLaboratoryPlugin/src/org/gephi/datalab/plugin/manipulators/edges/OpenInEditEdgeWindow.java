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
package org.gephi.datalab.plugin.manipulators.edges;

import javax.swing.Icon;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.tools.api.EditWindowController;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens the selected edge(s) one or various in Edit window.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class OpenInEditEdgeWindow implements EdgesManipulator {
    Edge[] edges;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.edges=edges;
    }

    public void execute() {
        EditWindowController edc = Lookup.getDefault().lookup(EditWindowController.class);
        edc.openEditWindow();
        edc.editEdges(edges);
    }

    public String getName() {
        if (edges.length > 1) {
            return NbBundle.getMessage(OpenInEditEdgeWindow.class, "OpenInEditEdgeWindow.name.multiple");
        } else {
            return NbBundle.getMessage(OpenInEditEdgeWindow.class, "OpenInEditEdgeWindow.name");
        }
    }

    public String getDescription() {
        if (edges.length > 1) {
            return NbBundle.getMessage(OpenInEditEdgeWindow.class, "OpenInEditEdgeWindow.description.multiple");
        } else {
            return NbBundle.getMessage(OpenInEditEdgeWindow.class, "OpenInEditEdgeWindow.description");
        }
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/edit.png", true);
    }
}
