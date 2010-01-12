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
package org.gephi.graph.dhns.core;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.dhns.utils.avl.AbstractEdgeTree;
import org.gephi.graph.dhns.utils.avl.AbstractNodeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphStructure {

    private final GraphViewImpl mainView;
    private final List<GraphViewImpl> views;
    private final AbstractNodeTree nodeDictionnary;
    private final AbstractEdgeTree edgeDictionnary;
    private GraphViewImpl visibleView;

    public GraphStructure(Dhns dhns) {
        nodeDictionnary = new AbstractNodeTree();
        edgeDictionnary = new AbstractEdgeTree();
        views = new ArrayList<GraphViewImpl>();

        //Main view
        mainView = new GraphViewImpl(dhns, 0);
        views.add(mainView);
        visibleView = mainView;
    }

    public GraphViewImpl getMainView() {
        return mainView;
    }

    public AbstractNodeTree getNodeDictionnary() {
        return nodeDictionnary;
    }

    public AbstractEdgeTree getEdgeDictionnary() {
        return edgeDictionnary;
    }

    public GraphViewImpl getVisibleView() {
        return visibleView;
    }

    public void setVisibleView(GraphViewImpl visibleView) {
        this.visibleView = visibleView;
    }
}
