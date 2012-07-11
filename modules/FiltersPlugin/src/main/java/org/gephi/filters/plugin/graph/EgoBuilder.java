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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class EgoBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.description");
    }

    public Filter getFilter() {
        return new EgoFilter();
    }

    public JPanel getPanel(Filter filter) {
        EgoUI ui = Lookup.getDefault().lookup(EgoUI.class);
        if (ui != null) {
            return ui.getPanel((EgoFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class EgoFilter implements ComplexFilter {

        private String pattern = "";
        private boolean self = true;
        private int depth = 1;

        public Graph filter(Graph graph) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;

            String str = pattern.toLowerCase();

            List<Node> nodes = new ArrayList<Node>();
            for (Node n : hgraph.getNodes()) {
                if (n.getNodeData().getId().toLowerCase().equals(str)) {
                    nodes.add(n);
                } else if ((n.getNodeData().getLabel() != null) && n.getNodeData().getLabel().toLowerCase().equals(str)) {
                    nodes.add(n);
                }
            }

            Set<Node> result = new HashSet<Node>();

            Set<Node> neighbours = new HashSet<Node>();
            neighbours.addAll(nodes);

            for (int i = 0; i < depth; i++) {
                Node[] nei = neighbours.toArray(new Node[0]);
                neighbours.clear();
                for (Node n : nei) {
                    for (Node neighbor : hgraph.getNeighbors(n)) {
                        if (!result.contains(neighbor)) {
                            neighbours.add(neighbor);
                            result.add(neighbor);
                        }
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

            for (Node node : hgraph.getNodes().toArray()) {
                if (!result.contains(node)) {
                    hgraph.removeNode(node);
                }
            }

            return hgraph;
        }

        public String getName() {
            return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, String.class, "pattern"),
                            FilterProperty.createProperty(this, Integer.class, "depth"),
                            FilterProperty.createProperty(this, Boolean.class, "self")};
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
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
