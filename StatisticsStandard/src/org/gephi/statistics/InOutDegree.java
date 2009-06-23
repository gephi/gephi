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
package org.gephi.statistics;

import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu
 */
public class InOutDegree implements Statistics, LongTask {

    /** The Average Node In-Degree. */
    private float avgInDegree;
    /** The Average Node Out-Degree. */
    private float avgOutDegree;
    /** Remembers if the Cancel function has been called. */
    private boolean isCanceled;
    /** Keep track of the work done. */
    private ProgressTicket progress;

    /**
     *
     * @return
     */
    public String toString() {
        return new String("In/Out Degree");
    }

    /**
     *
     * @param graphController
     * @param progressMonitor
     */
    public void execute(GraphController graphController) {
        isCanceled = false;

        //Attributes cols
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn inCol = nodeClass.addAttributeColumn("indegree", "In Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        AttributeColumn outCol = nodeClass.addAttributeColumn("outdegree", "Out Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);

        DirectedGraph graph = graphController.getDirectedGraph();
        int i = 0;

        progress.start(graph.getNodeCount());

        for (Node n : graph.getNodes()) {
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            row.setValue(inCol, graph.getInDegree(n));
            row.setValue(outCol, graph.getOutDegree(n));
            avgInDegree += graph.getInDegree(n);
            avgOutDegree += graph.getOutDegree(n);
            if (isCanceled) {
                break;
            }
            i++;
            progress.progress(i);
        }
        avgInDegree /= graph.getNodeCount();
        avgOutDegree /= graph.getNodeCount();
    }

    /**
     *
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(GraphDensity.class, "GraphDensity_name");
    }

    /**
     *
     * @return
     */
    public boolean isParamerizable() {
        return false;
    }


    /**
     *
     * @return
     */
    public String getReport() {
        return new String("Average In Degree: " + avgInDegree + "\n Average out Degree: " + avgOutDegree);
    }

    /**
     *
     * @return
     */
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }

    public StatisticsUI getUI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
