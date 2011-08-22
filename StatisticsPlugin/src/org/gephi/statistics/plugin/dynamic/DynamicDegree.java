/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.DynamicStatistics;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicDegree implements DynamicStatistics {

    public static final String DYNAMIC_INDEGREE = "dynamic_indegree";
    public static final String DYNAMIC_OUTDEGREE = "dynamic_outdegree";
    public static final String DYNAMIC_DEGREE = "dynamic_degree";
    //Data
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private double window;
    private double tick;
    private Interval bounds;
    private boolean isDirected;
    //Cols
    private AttributeColumn dynamicInDegreeColumn;
    private AttributeColumn dynamicOutDegreeColumn;
    private AttributeColumn dynamicDegreeColumn;
    //Result
    private List<Interval<Double>> averages;

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        this.graphModel = graphModel;
        this.averages = new ArrayList<Interval<Double>>();
        this.isDirected = graphModel.isDirected();
        this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());

        //Attributes cols
        AttributeTable nodeTable = attributeModel.getNodeTable();
        dynamicInDegreeColumn = nodeTable.getColumn(DYNAMIC_INDEGREE);
        dynamicOutDegreeColumn = nodeTable.getColumn(DYNAMIC_OUTDEGREE);
        dynamicDegreeColumn = nodeTable.getColumn(DYNAMIC_DEGREE);
        if (isDirected) {
            if (dynamicInDegreeColumn == null) {
                dynamicInDegreeColumn = nodeTable.addColumn(DYNAMIC_INDEGREE, "Dynamic In-Degree", AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
            }
            if (dynamicOutDegreeColumn == null) {
                dynamicOutDegreeColumn = nodeTable.addColumn(DYNAMIC_OUTDEGREE, "Dynamic Out-Degree", AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
            }
        }
        if (dynamicDegreeColumn == null) {
            dynamicDegreeColumn = nodeTable.addColumn(DYNAMIC_DEGREE, "Dynamic Degree", AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
        }
    }

    public String getReport() {
        String report = "<HTML> <BODY> <h1>Dynamic Degree Report </h1> "
                + "<hr>"
                + "<br> Bounds: " + bounds.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE))
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Average degrees: </h2>";

        for (Interval<Double> average : averages) {
            report += average.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }
        report += "<br /><br /></BODY></HTML>";
        return report;
    }

    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(window);
        HierarchicalDirectedGraph directedGraph = null;
        if (isDirected) {
            directedGraph = graphModel.getHierarchicalDirectedGraph(window);
        }

        long sum = 0;
        for (Node n : graph.getNodes()) {
            int degree = graph.getTotalDegree(n);
            Interval<Integer> degreeInInterval = new Interval<Integer>(interval, degree);
            DynamicInteger val = (DynamicInteger) n.getAttributes().getValue(dynamicDegreeColumn.getIndex());
            if (val == null) {
                val = new DynamicInteger(degreeInInterval);
            } else {
                val = new DynamicInteger(val, degreeInInterval);
            }
            n.getAttributes().setValue(dynamicDegreeColumn.getIndex(), val);
            sum += degree;
            if (isDirected) {
                int indegree = directedGraph.getTotalInDegree(n);
                Interval<Integer> inDegreeInInterval = new Interval<Integer>(interval, indegree);
                DynamicInteger inVal = (DynamicInteger) n.getAttributes().getValue(dynamicInDegreeColumn.getIndex());
                if (inVal == null) {
                    inVal = new DynamicInteger(inDegreeInInterval);
                } else {
                    inVal = new DynamicInteger(inVal, inDegreeInInterval);
                }
                n.getAttributes().setValue(dynamicInDegreeColumn.getIndex(), inVal);

                int outdegree = directedGraph.getTotalOutDegree(n);
                Interval<Integer> outDegreeInInterval = new Interval<Integer>(interval, outdegree);
                DynamicInteger outVal = (DynamicInteger) n.getAttributes().getValue(dynamicOutDegreeColumn.getIndex());
                if (outVal == null) {
                    outVal = new DynamicInteger(outDegreeInInterval);
                } else {
                    outVal = new DynamicInteger(outVal, outDegreeInInterval);
                }
                n.getAttributes().setValue(dynamicOutDegreeColumn.getIndex(), outVal);
            }
        }

        double average = sum / (double) graph.getNodeCount();
        averages.add(new Interval<Double>(interval, average));
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
