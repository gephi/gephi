/*
Copyright 2008-2012 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
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
package org.gephi.filters.plugin.edge;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.plugin.AbstractFilter;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FilterBuilder.class)
public class EdgeTypeBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.EDGE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EdgeTypeBuilder.class, "EdgeTypeBuilder.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(EdgeTypeBuilder.class, "EdgeTypeBuilder.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        GraphModel am = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        return new EdgeTypeFilter(am);
    }

    @Override
    public JPanel getPanel(Filter filter) {
        final EdgeTypeBuilder.EdgeTypeFilter edgeTypeFilter = (EdgeTypeBuilder.EdgeTypeFilter) filter;
        EdgeTypeUI ui = Lookup.getDefault().lookup(EdgeTypeUI.class);
        if (ui != null) {
            return ui.getPanel(edgeTypeFilter);
        }
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class EdgeTypeFilter extends AbstractFilter implements EdgeFilter {

        private final GraphModel graphModel;
        private Integer type;

        public EdgeTypeFilter(GraphModel graphModel) {
            super(NbBundle.getMessage(EdgeTypeBuilder.class, "EdgeTypeBuilder.name"));
            this.graphModel = graphModel;

            addProperty(Integer.class, "type");
        }

        @Override
        public boolean init(Graph graph) {
            return true;
        }

        @Override
        public boolean evaluate(Graph graph, Edge edge) {
            return type.equals(edge.getType());
        }

        @Override
        public void finish() {
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Collection getEdgeTypeLabels() {
            Graph graph = graphModel.getGraph();
            Object[] labels = graphModel.getEdgeTypeLabels();
            ArrayList<Object> col = new ArrayList<>(labels.length);
            for (Object l : labels) {
                if (graph.getEdgeCount(graphModel.getEdgeType(l)) > 0) {
                    col.add(l);
                }
            }
            return col;
        }

        public void setEdgeTypeLabel(Object obj) {
            int id = graphModel.getEdgeType(obj);
            if (id != -1) {
                getProperties()[0].setValue(id);
            }
        }
    }
}
