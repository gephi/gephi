/*
Copyright 2008-2011 Gephi
Authors : Sébastien Heymann <sebastien.heymann@gephi.org>
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
package org.gephi.statistics.plugin.dynamic;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.statistics.spi.DynamicStatistics;
import org.openide.util.Lookup;

/**
 *
 * @author Sébastien Heymann
 */
public class DynamicNbNodes implements DynamicStatistics {
    
    //Data
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private double window;
    private double tick;
    private Interval bounds;
    //Result
    private List<Interval<Integer>> counts;

    public void execute(GraphModel graphModel, AttributeModel model) {
        this.graphModel = graphModel;
        this.counts = new ArrayList<Interval<Integer>>();
        this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());
    }

    public String getReport() {
        String report = "<HTML> <BODY> <h1>Dynamic Number of Nodes Report </h1> "
                + "<hr>"
                + "<br> Bounds: " + bounds.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE))
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Number of nodes: </h2>";

        for (Interval<Integer> count : counts) {
            report += count.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }
        report += "<br /><br /></BODY></HTML>";
        return report;
    }
    
    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(window);
        
        int count = graph.getNodeCount();
        counts.add(new Interval<Integer>(interval, count));
    }

    public void end() {
    }

    public void setBounds(Interval bounds) {
        this.bounds = bounds;
    }

    public void setWindow(double window) {
        this.window = window;
    }

    public void setTick(double tick) {
        this.tick = tick;
    }

    public double getWindow() {
        return window;
    }

    public double getTick() {
        return tick;
    }

    public Interval getBounds() {
        return bounds;
    }

}
