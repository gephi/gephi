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
package org.gephi.layout.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.gephi.graph.api.GraphController;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutController;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutControllerImpl implements LayoutController {

    private List<Layout> layouts;
    private ExecutorService executor;

    public LayoutControllerImpl() {
        layouts = new ArrayList<Layout>(Lookup.getDefault().lookupAll(Layout.class));
        executor = Executors.newSingleThreadExecutor();
    }

    public void executeLayout() {
        executeLayout(layouts.get(2));
    }

    public void executeLayout(final Layout layout) {
        executor.execute(new Runnable() {

            public void run() {
                GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
                // DirectedGraph graph = graphController.getDirectedGraph();

                layout.initAlgo(graphController);
                layout.resetPropertiesValues();
                while (layout.canAlgo()) {
                    layout.goAlgo();
                }

                layout.endAlgo();
            }
        });
    }
}
