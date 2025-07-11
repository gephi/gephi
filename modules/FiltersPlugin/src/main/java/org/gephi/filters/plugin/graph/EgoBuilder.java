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

import java.util.*;

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
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian, flomzey
 */
@ServiceProvider(service = FilterBuilder.class)
public class EgoBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new EgoFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        EgoUI ui = Lookup.getDefault().lookup(EgoUI.class);
        if (ui != null) {
            return ui.getPanel((EgoFilter) filter);
        }
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class EgoFilter implements ComplexFilter {

        public enum Mode {
            OUTGOING, INCOMING, BOTH
        }

        private Mode mode = Mode.BOTH;
        private String inputID = "";
        private boolean self = true;
        private boolean considerNonDirected = true;
        private String depth = "1";

        @Override
        public Graph filter(Graph graph) {
            String str = inputID.toLowerCase();
            String depthStr = depth.toLowerCase();
            Node ego = null;
            int depth = 0;

            HashSet<Node> nodes = new HashSet<>();
            HashSet<Edge> edges = new HashSet<>();

            if (depthStr.isEmpty()) {
                depth = Integer.MAX_VALUE;
            }
            if (depthStr.matches("[0-9]+")) {
                depth = Integer.parseInt(depthStr);
            }

            for (Node n : graph.getNodes()) {
                if (n.getId().toString().toLowerCase().equals(str)) {
                    ego = n;
                    break; //if found, stop looking
                } else if ((n.getLabel() != null) && n.getLabel().toLowerCase().equals(str)) {
                    ego = n;
                }
            }

            bfs(graph, ego, depth, nodes, edges);

            if (!self) {
                nodes.remove(ego);
            }

            for (Node n : graph.getNodes()) {
                if (!nodes.contains(n)) {
                    graph.removeNode(n);
                }
            }

            for (Edge e : graph.getEdges()) {
                if (!edges.contains(e)) {
                    graph.removeEdge(e);
                }
            }

            return graph;
        }

        /**
         * Breadth First Search algorithm that iterates over the given graph from the ego starting point,
         * stopping at a depth of k. The nodes and edges which will be in the resulting graph
         * are stored in a HashSet via reference.
         *
         * @param graph    the graph to iterate over
         * @param ego      the node which the filter will be centered around
         * @param k        depth of the resulting graph
         * @param resNodes reference to the nodes HashSet
         * @param resEdges reference to the edges HashSet
         */
        private void bfs(Graph graph, Node ego, int k, HashSet<Node> resNodes, HashSet<Edge> resEdges) {
            if (ego == null) {
                return;
            }
            Queue<Node> q = new ArrayDeque<>();
            q.offer(ego);

            while (!q.isEmpty() && k >= 0) {
                int elems = q.size();

                for (int i = 0; i < elems; i++) {
                    Node current = q.poll();
                    resNodes.add(current);

                    if (k == 0) {
                        continue; //prevent nodes from adding edges that go beyond depth k
                    }

                    for (Edge e : graph.getEdges(current)) {
                        if (!isRelevantEdge(current, e)) {
                            continue;
                        }
                        resEdges.add(e);

                        Node next = getNextNode(current, e);
                        if (resNodes.contains(next)) {
                            continue;
                        }

                        q.offer(next);
                    }
                }
                k--;
            }
        }

        /**
         * Decides whether an Edge is relevant for the resulting ego graph on Following conditions:
         * OUTGOING: takes every edge pointing away from the {@code ego} node.
         * INCOMING: takes every edge pointing to the {@code ego} node.
         * BOTH: takes both.
         *
         * @param ego current ego of the iteration
         * @param e   the questioned edge
         * @return    whether {@param e} is relevant for current {@param ego}
         */
        private boolean isRelevantEdge(Node ego, Edge e) {
            if (considerNonDirected && !e.isDirected()) {
                return true;
            }
            if (!e.isDirected()) {
                return false;
            }
            switch (mode) {
                case OUTGOING:
                    return e.getSource() == ego;
                case INCOMING:
                    return e.getTarget() == ego;
                case BOTH:
                    return true;
            }
            return false;
        }

        private Node getNextNode(Node ego, Edge e) {
            if (e.getSource() == ego) {
                return e.getTarget();
            }
            return e.getSource();
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
        }

        @Override
        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[] {
                        FilterProperty.createProperty(this, String.class, "pattern"),
                        FilterProperty.createProperty(this, String.class, "depth"),
                        FilterProperty.createProperty(this, Boolean.class, "self"),
                        FilterProperty.createProperty(this, Boolean.class, "considerNonDirected"),
                        FilterProperty.createProperty(this, Mode.class, "mode")
                };
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new FilterProperty[0];
        }

        public String getPattern() {
            return inputID;
        }

        public void setPattern(String pattern) {
            this.inputID = pattern;
        }

        public String getDepth() {
            return depth;
        }

        public void setDepth(String depth) {
            this.depth = depth;
        }

        public boolean isSelf() {
            return self;
        }

        public void setSelf(boolean self) {
            this.self = self;
        }

        public boolean isConsiderNonDirected() {
            return considerNonDirected;
        }

        public void setConsiderNonDirected(boolean considerNonDirected) {
            this.considerNonDirected = considerNonDirected;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }
    }
}
