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
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class GiantComponentBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new GiantComponentFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class GiantComponentFilter implements NodeFilter {

        private static final String GIANT_COMPONENT_FILTER = "giantcomponent";

        private int componentId;
        private Column column;

        public GiantComponentFilter() {
        }

        @Override
        public boolean init(Graph graph) {
            ConnectedComponents cc = new ConnectedComponents();
            UndirectedGraph undirectedGraph = graph.getModel().getUndirectedGraph(graph.getView());

            column = graph.getModel().getNodeTable().getColumn(GIANT_COMPONENT_FILTER);
            if (column == null) {
                column = graph.getModel().getNodeTable().addColumn(GIANT_COMPONENT_FILTER, Integer.class);
            }
            graph.readLock();
            try {
                cc.weaklyConnected(undirectedGraph, column);
            } finally {
                graph.readUnlock();
            }
            componentId = cc.getGiantComponent();

            return column != null && componentId != -1;
        }

        @Override
        public boolean evaluate(Graph graph, Node node) {
            Integer component = (Integer) node.getAttribute(column);
            if (component != null) {
                return component.equals(componentId);
            }
            return false;
        }

        @Override
        public void finish() {
            column.getTable().removeColumn(column);
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(GiantComponentBuilder.class, "GiantComponentBuilder.name");
        }

        @Override
        public FilterProperty[] getProperties() {
            return new FilterProperty[0];
        }
    }
}
