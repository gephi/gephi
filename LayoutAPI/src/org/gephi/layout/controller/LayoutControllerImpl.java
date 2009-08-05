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
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutController;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutControllerImpl implements LayoutController {

    private List<LayoutBuilder> layouts;
    private Layout layout;
    private ExecutorService executor;
    private LayoutRun layoutRun;

    public LayoutControllerImpl() {
        layouts = new ArrayList<LayoutBuilder>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        executor = Executors.newSingleThreadExecutor();
    }

    public void executeLayout() {
        // setLayout(layouts.);
        layoutRun = new LayoutRun(layout);
        executor.execute(layoutRun);
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public List<LayoutBuilder> getLayouts() {
        return layouts;
    }

    public Layout getLayout() {
        return layout;
    }

    public void stopLayout() {
        layoutRun.stop();
    }
}

class LayoutRun implements Runnable {

    private Layout layout;
    private boolean stopRequested;

    public LayoutRun(Layout layout) {
        this.layout = layout;
    }

    public void run() {
        stopRequested = false;
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        System.out.println("Layout start.");
        layout.initAlgo(graphController);
        //layout.setGraph(graphController);
        layout.resetPropertiesValues();
        while (layout.canAlgo() && !stopRequested) {
            layout.goAlgo();
        }
        System.out.println("Layout end.");
        layout.endAlgo();
    }

    public void stop() {
        stopRequested = true;
    }
}