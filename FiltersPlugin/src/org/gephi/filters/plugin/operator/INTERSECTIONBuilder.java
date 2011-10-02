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
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.filters.spi.Operator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class INTERSECTIONBuilder implements FilterBuilder {

    public Category getCategory() {
        return new Category("Operator");
    }

    public String getName() {
        return NbBundle.getMessage(INTERSECTIONBuilder.class, "INTERSECTIONBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(INTERSECTIONBuilder.class, "INTERSECTIONBuilder.description");
    }

    public Filter getFilter() {
        return new IntersectionOperator();
    }

    public JPanel getPanel(Filter filter) {
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class IntersectionOperator implements Operator {

        public int getInputCount() {
            return Integer.MAX_VALUE;
        }

        public String getName() {
            return NbBundle.getMessage(INTERSECTIONBuilder.class, "INTERSECTIONBuilder.name");
        }

        public FilterProperty[] getProperties() {
            return null;
        }

        public Graph filter(Graph[] graphs) {
            HierarchicalGraph minHGraph = (HierarchicalGraph) graphs[0];
            int minElements = Integer.MAX_VALUE;
            for (int i = 0; i < graphs.length; i++) {
                int count = ((HierarchicalGraph)graphs[i]).getNodeCount();
                if (count < minElements) {
                    minHGraph = (HierarchicalGraph) graphs[i];
                    minElements = count;
                }
            }
            for (Node n : minHGraph.getNodes().toArray()) {
                for (int i = 0; i < graphs.length; i++) {
                    if ((HierarchicalGraph)graphs[i] != minHGraph) {
                        if (!((HierarchicalGraph)graphs[i]).contains(n)) {
                            minHGraph.removeNode(n);
                            break;
                        }
                    }
                }
            }
            for (Edge e : minHGraph.getEdges().toArray()) {
                for (int i = 0; i < graphs.length; i++) {
                    if ((HierarchicalGraph)graphs[i] != minHGraph) {
                        if (!((HierarchicalGraph)graphs[i]).contains(e)) {
                            minHGraph.removeEdge(e);
                            break;
                        }
                    }
                }
            }
            return minHGraph;
        }

        public Graph filter(Graph graph, Filter[] filters) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
            List<NodeFilter> nodeFilters = new ArrayList<NodeFilter>();
            List<EdgeFilter> edgeFilters = new ArrayList<EdgeFilter>();
            for (Filter f : filters) {
                if (f instanceof NodeFilter) {
                    nodeFilters.add((NodeFilter) f);
                } else if (f instanceof EdgeFilter) {
                    edgeFilters.add((EdgeFilter) f);
                }
            }
            if (nodeFilters.size() > 0) {
                for (Iterator<NodeFilter> itr = nodeFilters.iterator(); itr.hasNext();) {
                    NodeFilter nf = itr.next();
                    if (!nf.init(hgraph)) {
                        itr.remove();
                    }
                }
                List<Node> nodesToRemove = new ArrayList<Node>();
                for (Node n : hgraph.getNodes()) {
                    for (NodeFilter nf : nodeFilters) {
                        if (!nf.evaluate(hgraph, n)) {
                            nodesToRemove.add(n);
                            break;
                        }
                    }
                }

                for (Node n : nodesToRemove) {
                    hgraph.removeNode(n);
                }

                for (NodeFilter nf : nodeFilters) {
                    nf.finish();
                }
            }
            if (edgeFilters.size() > 0) {
                for (Iterator<EdgeFilter> itr = edgeFilters.iterator(); itr.hasNext();) {
                    EdgeFilter ef = itr.next();
                    if (!ef.init(hgraph)) {
                        itr.remove();
                    }
                }
                List<Edge> edgesToRemove = new ArrayList<Edge>();
                for (Edge e : hgraph.getEdges()) {
                    for (EdgeFilter ef : edgeFilters) {
                        if (!ef.evaluate(hgraph, e)) {
                            edgesToRemove.add(e);
                            break;
                        }
                    }
                }

                for (Edge e : edgesToRemove) {
                    hgraph.removeEdge(e);
                }

                for (EdgeFilter ef : edgeFilters) {
                    ef.finish();
                }
            }
            return hgraph;
        }
    }
}
