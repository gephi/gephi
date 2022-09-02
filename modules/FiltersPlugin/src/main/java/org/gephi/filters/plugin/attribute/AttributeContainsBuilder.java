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

package org.gephi.filters.plugin.attribute;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.plugin.AbstractAttributeFilter;
import org.gephi.filters.plugin.AbstractAttributeFilterBuilder;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Sukankana Chakraborty
 */
@ServiceProvider(service = CategoryBuilder.class)
public class AttributeContainsBuilder implements CategoryBuilder {

    private final static Category CONTAINS = new Category(
            NbBundle.getMessage(AttributeContainsBuilder.class, "AttributeContainsBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    @Override
    public Category getCategory() {
        return CONTAINS;
    }

    @Override
    public FilterBuilder[] getBuilders(Workspace workspace) {
        List<FilterBuilder> builders = new ArrayList<>();
        GraphModel am = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        for (Column col : am.getNodeTable()) {
            System.out.println(col.isArray());
            if (col.isArray()) {
                AttributeContainsFilterBuilder b = new AttributeContainsFilterBuilder(col);
                /* to do: insert loop to add each element in the list */
                builders.add(b);
            }
        }
        for (Column col : am.getEdgeTable()) {
            if (col.isArray()) {
                AttributeContainsFilterBuilder b = new AttributeContainsFilterBuilder(col);
                /* to do: insert loop to add each element in the list */
                builders.add(b);
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class AttributeContainsFilterBuilder extends AbstractAttributeFilterBuilder {

        public AttributeContainsFilterBuilder(Column column) {
            super(column,
                    CONTAINS,
                    NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeContainsBuilder.description"),
                    null);
        }

        @Override
        public AttributeContainsFilter getFilter(Workspace workspace) {
            return AttributeUtils.isNodeColumn(column) ? new AttributeContainsFilter.Node(column) :
                    new AttributeContainsFilter.Edge(column);
        }

        @Override
        public JPanel getPanel(Filter filter) {
            return null;
        }
    }

    public static abstract class AttributeContainsFilter<K extends Element> extends AbstractAttributeFilter<K> {

        public AttributeContainsFilter(Column column) {
            super(NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeContainsBuilder.name"),
                    column);
        }

        @Override
        public boolean init(Graph graph) {
            if (AttributeUtils.isNodeColumn(column)) {
                return graph.getNodeCount() != 0;
            } else if (AttributeUtils.isEdgeColumn(column)) {
                return graph.getEdgeCount() != 0;
            }
            return true;
        }

        @Override
        public boolean evaluate(Graph graph, Element element) {
            return element.getAttribute(column) != null;
        }

        @Override
        public void finish() {
        }

        public static class Node extends AttributeContainsFilter<org.gephi.graph.api.Node> implements NodeFilter {

            public Node(Column column) {
                super(column);
            }
        }

        public static class Edge extends AttributeContainsFilter<org.gephi.graph.api.Edge> implements EdgeFilter {

            public Edge(Column column) {
                super(column);
            }
        }
    }
}
