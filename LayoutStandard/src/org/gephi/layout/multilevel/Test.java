/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.spi.LayoutUI;
import org.gephi.ui.propertyeditor.NodeColumnStringEditor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
@ServiceProvider(service = LayoutBuilder.class)
public class Test implements LayoutBuilder {

    private TestLayoutUI ui = new TestLayoutUI();

    public String getName() {
        return "TEST";
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
            graph = (HierarchicalGraph) graphController.getModel().getGraphVisible();
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
        private AttributeColumn columnTest;

        public LayoutProperty[] getProperties() {
            List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
            try {
                properties.add(LayoutProperty.createProperty(this, Boolean.class, "Refine", null, "Refine", "isRefine", "setRefine"));
                properties.add(LayoutProperty.createProperty(this, AttributeColumn.class, "Column", null, "Refine", "getColumnTest", "setColumnTest", NodeColumnStringEditor.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return properties.toArray(new LayoutProperty[0]);
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

        public AttributeColumn getColumnTest() {
            return columnTest;
        }

        public void setColumnTest(AttributeColumn columnTest) {
            this.columnTest = columnTest;
        }
    }

    public LayoutUI getUI() {
        return ui;
    }

    private static class TestLayoutUI implements LayoutUI {

        public String getDescription() {
            return "TEST";
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return 0;
        }

        public int getSpeedRank() {
            return 0;
        }
    }
}
