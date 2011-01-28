/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.multilevel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.layout.plugin.AbstractLayout;
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
//@ServiceProvider(service = LayoutBuilder.class)
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

        public void initAlgo() {
            converged = false;
            graph = graphModel.getHierarchicalGraphVisible();
        }

//        public void print(HierarchicalDirectedGraph graph) {
//            System.out.println("------ print ------");
//            for (int i = 0; i <= graph.getHeight(); i++) {
//                System.out.printf("Level %d: %d nodes\n", i, graph.getNodes(i).toArray().length);
//            }
//            System.out.println("Topnodes: " + graph.getTopNodes().toArray().length);
//        }
        public void goAlgo() {
            graph = graphModel.getHierarchicalGraphVisible();
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
