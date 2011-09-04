/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.statistics.spi.DynamicStatistics;
import org.openide.util.Lookup;

/**
 *
 * @author Sébastien Heymann
 */
public class DynamicNbEdges implements DynamicStatistics {
    
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
        String report = "<HTML> <BODY> <h1>Dynamic Number of Edges Report </h1> "
                + "<hr>"
                + "<br> Bounds: " + bounds.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE))
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Number of edges: </h2>";

        for (Interval<Integer> count : counts) {
            report += count.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }
        report += "<br /><br /></BODY></HTML>";
        return report;
    }
    
    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(window);
        
        int count = graph.getEdgeCount();
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
