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
package org.gephi.filters.plugin.operator;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class MASKBuilderEdge implements FilterBuilder {

    @Override
    public Category getCategory() {
        return new Category(NbBundle.getMessage(MASKBuilderEdge.class, "Operator.category"));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MASKBuilderEdge.class, "MASKBuilderEdge.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MASKBuilderEdge.class, "MASKBuilderEdge.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new MaskEdgeOperator();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        MASKEdgeUI ui = Lookup.getDefault().lookup(MASKEdgeUI.class);
        if (ui != null) {
            return ui.getPanel((MaskEdgeOperator) filter);
        }
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class MaskEdgeOperator implements Operator {

        public enum EdgesOptions {

            SOURCE, TARGET, ANY, BOTH
        }

        private EdgesOptions option = EdgesOptions.ANY;
        private FilterProperty[] filterProperties;

        @Override
        public int getInputCount() {
            return 1;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(MASKBuilderEdge.class, "MASKBuilderEdge.name");
        }

        @Override
        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                        FilterProperty.createProperty(this, String.class, "option")
                    };
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        @Override
        public Graph filter(Subgraph[] graphs) {
            if (graphs.length > 1) {
                throw new IllegalArgumentException("Filter accepts a single graph in parameter");
            }

            Graph graph = (Graph) graphs[0];
            Graph mainGraph = graph.getView().getGraphModel().getGraph();

            List<Edge> edgesToKeep = new ArrayList<>();
            for (Edge e : mainGraph.getEdges()) {
                boolean source = graph.contains(e.getSource());
                boolean target = graph.contains(e.getTarget());
                boolean keep = false;
                switch (option) {
                    case SOURCE:
                        keep = source;
                        break;
                    case TARGET:
                        keep = target;
                        break;
                    case BOTH:
                        keep = source && target;
                        break;
                    case ANY:
                        keep = source || target;
                        break;
                }
                if (keep) {
                    edgesToKeep.add(e);
                }
            }

            graph.clearEdges();

            for (Node n : mainGraph.getNodes()) {
                if (!graph.contains(n)) {
                    graph.addNode(n);
                }
            }

            for (Edge e : edgesToKeep) {
                graph.addEdge(e);
            }

            return graph;
        }

        @Override
        public Graph filter(Graph graph, Filter[] filters) {
            if (filters.length > 1) {
                throw new IllegalArgumentException("Filter accepts a single filter in parameter");
            }

            if (filters[0] instanceof NodeFilter && ((NodeFilter) filters[0]).init(graph)) {
                NodeFilter filter = (NodeFilter) filters[0];
                for (Edge e : graph.getEdges()) {
                    Node source = e.getSource();
                    Node target = e.getTarget();
                    boolean remove = false;
                    switch (option) {
                        case SOURCE:
                            remove = !filter.evaluate(graph, source);
                            break;
                        case TARGET:
                            remove = !filter.evaluate(graph, target);
                            break;
                        case BOTH:
                            remove = !filter.evaluate(graph, source) || !filter.evaluate(graph, target);
                            break;
                        case ANY:
                            remove = !filter.evaluate(graph, source) && !filter.evaluate(graph, target);
                            break;
                    }
                    if (remove) {
                        graph.removeEdge(e);
                    }
                }
                filter.finish();
            }
            return graph;
        }

        public String getOption() {
            return option.toString();
        }

        public void setOption(String option) {
            this.option = EdgesOptions.valueOf(option);
        }
    }
}
