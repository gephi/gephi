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

import java.util.Observable;
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
public class LayoutControllerImpl extends Observable implements LayoutController {

    private Layout layout;
    private ExecutorService executor;
    private LayoutRun layoutRun;
    private boolean running;

    public LayoutControllerImpl() {
        executor = Executors.newSingleThreadExecutor();
        running = false;
    }

    public void executeLayout() {
        layoutRun = new LayoutRun(layout);
        setRunning(true);
        executor.execute(layoutRun);
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
        injectGraph();
        setChanged();
        notifyObservers();
    }

    public void injectGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (layout != null) {
            layout.setGraphController(graphController);
        }
        System.out.println("LayoutController: Injecting graphController");
    }

    public Layout getLayout() {
        return layout;
    }

    public void stopLayout() {
        setRunning(false);
    }

    public boolean canExecute() {
        return layout != null && running == false;
    }

    public boolean canStop() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
        setChanged();
        notifyObservers();
    }

    class LayoutRun implements Runnable {

        private Layout layout;

        public LayoutRun(Layout layout) {
            this.layout = layout;
        }

        public void run() {
            System.out.println("LayoutRun: run():");

            layout.initAlgo();
            while (layout.canAlgo() && running) {
                layout.goAlgo();
            }
            layout.endAlgo();
            setRunning(false);
            System.out.println("LayoutRun: Layout end.");
        }
    }
}
