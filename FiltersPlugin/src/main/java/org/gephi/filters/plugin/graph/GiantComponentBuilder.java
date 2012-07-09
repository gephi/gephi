/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.filters.plugin.graph;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class GiantComponentBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.description");
    }

    public Filter getFilter() {
        return new GiantComponentFilter();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class GiantComponentFilter implements NodeFilter {

        private AttributeModel attributeModel;
        private int componentId;
        private AttributeColumn column;

        public GiantComponentFilter() {
        }

        public boolean init(Graph graph) {
            ConnectedComponents cc = new ConnectedComponents();
            HierarchicalUndirectedGraph undirectedGraph = null;
            if (cc instanceof UndirectedGraph) {
                undirectedGraph = (HierarchicalUndirectedGraph) graph;
            } else {
                undirectedGraph = graph.getView().getGraphModel().getHierarchicalUndirectedGraph(graph.getView());
            }

            attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(graph.getGraphModel().getWorkspace());
            cc.weaklyConnected(undirectedGraph, attributeModel);
            componentId = cc.getGiantComponent();
            column = attributeModel.getNodeTable().getColumn(ConnectedComponents.WEAKLY);

            return column != null && componentId != -1;
        }

        public boolean evaluate(Graph graph, Node node) {
            Integer component = (Integer) node.getNodeData().getAttributes().getValue(column.getIndex());
            if (component != null) {
                return component.equals(componentId);
            }
            return false;
        }

        public void finish() {
        }

        public String getName() {
            return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.name");
        }

        public FilterProperty[] getProperties() {
            return new FilterProperty[0];
        }
    }
}
