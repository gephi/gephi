/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import javax.swing.Icon;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class Test implements LayoutBuilder {

    public String getName() {
        return "TEST";
    }

    public String getDescription() {
        return "TEST";
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Layout buildLayout() {
        return new TestLayout(this);
    }

    public class TestLayout extends AbstractLayout implements Layout {

        private boolean converged;
        private boolean refine = false;
        private MaximalMatchingCoarsening x = new MaximalMatchingCoarsening();
        private HierarchicalGraph graph;

        private TestLayout(LayoutBuilder layoutBuilder) {
            super(layoutBuilder);
        }

        @Override
        public void setGraphController(GraphController graphController) {
            super.setGraphController(graphController);
            graph = (HierarchicalGraph)graphController.getModel().getGraphVisible();
        }

        public void initAlgo() {
            converged = false;
        }

//        public void print(HierarchicalDirectedGraph graph) {
//            System.out.println("------ print ------");
//            for (int i = 0; i <= graph.getHeight(); i++) {
//                System.out.printf("Level %d: %d nodes\n", i, graph.getNodes(i).toArray().length);
//            }
//            System.out.println("Topnodes: " + graph.getTopNodes().toArray().length);
//        }
        public void goAlgo() {
            if (refine) {
                System.out.println("------------REFINE-----------");
                x.refine(graph);
            } else {
                System.out.println("------------COARSEN-----------");
                x.coarsen(graph);
            }
            converged = true;
        }

        public boolean canAlgo() {
            return !converged;
        }

        public void endAlgo() {
        }

        public PropertySet[] getPropertySets() throws NoSuchMethodException {
            Sheet.Set set = Sheet.createPropertiesSet();
            set.put(LayoutProperty.createProperty(
                this, Boolean.class, "Refine", "Refine", "isRefine", "setRefine"));
            return new PropertySet[]{set};
        }

        public void resetPropertiesValues() {
        }

        /**
         * @return the refine
         */
        public Boolean isRefine() {
            return refine;
        }

        /**
         * @param refine the refine to set
         */
        public void setRefine(Boolean refine) {
            this.refine = refine;
        }
    }
}
