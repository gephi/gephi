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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Sebastien Heymann
 */
@ServiceProvider(service = FilterBuilder.class)
public class NeighborsBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NeighborsBuilder.class, "NeighborsBuilder.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(NeighborsBuilder.class, "NeighborsBuilder.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new NeighborsFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        NeighborsUI ui = Lookup.getDefault().lookup(NeighborsUI.class);
        if (ui != null) {
            return ui.getPanel((NeighborsFilter) filter);
        }
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class NeighborsFilter implements ComplexFilter {

        private boolean self = true;
        private int depth = 1;

        @Override
        public Graph filter(Graph graph) {

            GraphView graphView = graph.getView();

            Collection<Node> nodes = graph.getNodes().toCollection();

            Set<Node> result = new HashSet<>();

            Set<Node> neighbours = new HashSet<>();
            neighbours.addAll(nodes);

            //Put all neighbors into result
            Graph mainGraph = graph.getModel().getGraph();
            for (int i = 0; i < depth; i++) {
                Node[] nei = neighbours.toArray(new Node[0]);
                neighbours.clear();
                for (Node n : nei) {
                    //Extract all neighbors of n
                    for (Node neighbor : mainGraph.getNeighbors(n)) {
                        neighbours.add(neighbor);
                        result.add(neighbor);
                    }
                }
                if (neighbours.isEmpty()) {
                    break;
                }
            }

            if (self) {
                result.addAll(nodes);
            } else {
                result.removeAll(nodes);
            }

            //Update nodes
            for (Node node : mainGraph.getNodes()) {
                if (result.contains(node)) {
                    graph.addNode(node);
                } else if (graph.contains(node)) {
                    graph.removeNode(node);
                }
            }

            //Update edges
            for (Edge edge : mainGraph.getEdges()) {
                if (graph.contains(edge.getSource()) && graph.contains(edge.getTarget())) {
                    graph.addEdge(edge);
                }
            }

            return graph;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(NeighborsBuilder.class, "NeighborsBuilder.name");
        }

        @Override
        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                    FilterProperty.createProperty(this, Integer.class, "depth"),
                    FilterProperty.createProperty(this, Boolean.class, "self")};
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public Integer getDepth() {
            return depth;
        }

        public void setDepth(Integer depth) {
            this.depth = depth;
        }

        public boolean isSelf() {
            return self;
        }

        public void setSelf(boolean self) {
            this.self = self;
        }
    }
}
