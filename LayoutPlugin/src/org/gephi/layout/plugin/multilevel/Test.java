/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
