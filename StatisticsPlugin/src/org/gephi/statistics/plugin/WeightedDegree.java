/*
Copyright 2008-2011 Gephi
Authors : Sebastien Heymann <seb@gephi.org>
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
package org.gephi.statistics.plugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Sebastien Heymann
 */
public class WeightedDegree implements Statistics, LongTask {

    public static final String WDEGREE = "weighted degree";
    private boolean isCanceled;
    private ProgressTicket progress;
    private double avgWDegree;

    public double getAverageDegree() {
        return avgWDegree;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        isCanceled = false;

        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn degCol = nodeTable.getColumn(WDEGREE);
        if (degCol == null) {
            degCol = nodeTable.addColumn(WDEGREE, "Weighted Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        }

        graph.readLock();

        Progress.start(progress, graph.getNodeCount());
        int i = 0;

        for (Node n : graph.getNodes()) {
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            float totalWeight = 0;
            for (Iterator it = graph.getEdgesAndMetaEdges(n).iterator(); it.hasNext();) {
                Edge e = (Edge) it.next();
                totalWeight += e.getWeight();
            }

            row.setValue(degCol, totalWeight);
            avgWDegree += totalWeight;

            if (isCanceled) {
                break;
            }
            i++;
            Progress.progress(progress, i);
        }

        avgWDegree /= graph.getNodeCount();

        graph.readUnlockAll();
    }

    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Weighted Degree Report </h1> "
                + "<hr>"
                + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Average Weighted Degree: " + f.format(avgWDegree)
                + "</BODY></HTML>";

        return report;
    }

    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
