/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import javax.swing.JPanel;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutProperty;
import org.gephi.layout.force.yifanHu.YifanHu;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public abstract class AbstractMultiLevelLayout implements Layout {

    private ClusteredGraph graph;
    private boolean acabou;

    protected abstract YifanHu getForceLayout();

    protected abstract CoarseningStrategy getCoarseningStrategy();

    public boolean testAlgo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void initAlgo(GraphController graphController) {
        graph = graphController.getClusteredDirectedGraph();
    }

    private void layoutLevel(int level) {
        YifanHu layout = getForceLayout();

        layout.initAlgo(graph);
        layout.resetPropertiesValues();

        layout.optimalDistance = 10000000;
        layout.optimalDistance *= Math.pow(4.0/7, level / 2.0);
        System.out.println("K = " + layout.optimalDistance);
        while (layout.canAlgo()) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }

    private void recursiveLayout(int level) {
        System.out.println(graph.getLevelSize(0) + ", " + graph.getEdgeCount());

        if (graph.getLevelSize(0) > 100) {
            CoarseningStrategy coarsening = getCoarseningStrategy();
            coarsening.coarsen(graph);
            recursiveLayout(level + 1);
            layoutLevel(level);
            coarsening.refine(graph);
        }
        System.out.println("level = " + level);
    }

    public void goAlgo() {
        recursiveLayout(0);

        // coarsening.refine(graph);
        acabou = true;
    }

    public boolean canAlgo() {
        return !acabou;
    }

    public void endAlgo() {
    }

    public LayoutProperty[] getProperties() {
        return null;
    }

    public void resetPropertiesValues() {
        acabou = false;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
