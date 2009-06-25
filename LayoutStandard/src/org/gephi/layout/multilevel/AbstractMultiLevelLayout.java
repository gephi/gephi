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
    private int level;
    private YifanHu layout;

    protected abstract YifanHu getForceLayout();

    protected abstract CoarseningStrategy getCoarseningStrategy();

    public boolean testAlgo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void initAlgo(GraphController graphController) {
        graph = graphController.getClusteredDirectedGraph();
        level = 1;
        CoarseningStrategy coarsening = getCoarseningStrategy();
        coarsening.coarsen(getGraph());
        layout = getForceLayout();
//        while (getGraph().getLevelSize(0) > 100) {
//            CoarseningStrategy coarsening = getCoarseningStrategy();
//            coarsening.coarsen(getGraph());
//            level++;
//        }
    }

    private void layoutLevel(int level) {
        YifanHu layout = getForceLayout();

        System.out.println("K = " + layout.optimalDistance);
        while (layout.canAlgo()) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }

    private void recursiveLayout(int level) {
        System.out.println(getGraph().getLevelSize(0) + ", " + getGraph().getEdgeCount());

//        if (getGraph().getLevelSize(0) > 100) {
        if (level < 2) {
            CoarseningStrategy coarsening = getCoarseningStrategy();
            coarsening.coarsen(getGraph());
            recursiveLayout(level + 1);
            layoutLevel(level);
            coarsening.refine(getGraph());
        }
        System.out.println("level = " + level);
    }

    public void goAlgo() {
//        System.out.println("level = " + level);
//        if (level < 0) {
//            return;
//        }

        if (layout.canAlgo()) {
            layout.goAlgo();
        } else {
            layout.endAlgo();
            if (level > 0) {
                //acabou = true;
                CoarseningStrategy coarsening = getCoarseningStrategy();
                coarsening.refine(getGraph());
                level--;
            } else {
                acabou = true;
            }
        }

    // coarsening.refine(graph);
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

    /**
     * @return the graph
     */
    protected ClusteredGraph getGraph() {
        return graph;
    }
}
