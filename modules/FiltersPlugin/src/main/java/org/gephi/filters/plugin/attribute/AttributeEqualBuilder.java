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
import java.util.regex.Pattern;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.AbstractAttributeFilter;
import org.gephi.filters.plugin.AbstractAttributeFilterBuilder;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.types.IntervalBooleanMap;
import org.gephi.graph.api.types.IntervalStringMap;
import org.gephi.graph.api.types.TimestampBooleanMap;
import org.gephi.graph.api.types.TimestampStringMap;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class AttributeEqualBuilder implements CategoryBuilder {

    private final static Category EQUAL = new Category(
            NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    @Override
    public Category getCategory() {
        return EQUAL;
    }

    @Override
    public FilterBuilder[] getBuilders(Workspace workspace) {
        List<FilterBuilder> builders = new ArrayList<>();
        GraphModel am = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        List<Column> columns = new ArrayList<>();
        columns.addAll(am.getNodeTable().toList());
        columns.addAll(am.getEdgeTable().toList());
        for (Column c : columns) {
            if (!c.isProperty()) {
                if (c.getTypeClass().equals(String.class) || c.getTypeClass().equals(TimestampStringMap.class) || c.getTypeClass().equals(IntervalStringMap.class) || c.isArray()) {
                    EqualStringFilterBuilder b = new EqualStringFilterBuilder(c);
                    builders.add(b);
                } else if (AttributeUtils.isNumberType(c.getTypeClass())) {
                    EqualNumberFilterBuilder b = new EqualNumberFilterBuilder(c);
                    builders.add(b);
                } else if (c.getTypeClass().equals(Boolean.class) || c.getTypeClass().equals(TimestampBooleanMap.class) || c.getTypeClass().equals(IntervalBooleanMap.class)) {
                    EqualBooleanFilterBuilder b = new EqualBooleanFilterBuilder(c);
                    builders.add(b);
                }
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class EqualStringFilterBuilder extends AbstractAttributeFilterBuilder {

        public EqualStringFilterBuilder(Column column) {
            super(column,
                    EQUAL,
                    NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.description"),
                    null);
        }

        @Override
        public EqualStringFilter getFilter(Workspace workspace) {
            return AttributeUtils.isNodeColumn(column) ? new EqualStringFilter.Node(column) : new EqualStringFilter.Edge(column);
        }

        @Override
        public JPanel getPanel(Filter filter) {
            EqualStringUI ui = Lookup.getDefault().lookup(EqualStringUI.class);
            if (ui != null) {
                return ui.getPanel((EqualStringFilter) filter);
            }
            return null;
        }
    }

    public static abstract class EqualStringFilter<K extends Element> extends AbstractAttributeFilter<K> {

        private String pattern;
        private boolean useRegex;
        private Pattern regex;

        public EqualStringFilter(Column column) {
            super(NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name"),
                    column);

            //Add ptoperties
            addProperty(String.class, "pattern");
            addProperty(Boolean.class, "useRegex");
        }

        @Override
        public boolean init(Graph graph) {
            return true;
        }

        @Override
        public boolean evaluate(Graph graph, Element element) {
            if (pattern == null) {
                return true;
            }
            Object val = element.getAttribute(column, graph.getView());
            if (val != null) {
                String valString = column.isArray() ? AttributeUtils.printArray(val) : val.toString();
                if (useRegex) {
                    return regex.matcher(valString).matches();
                } else {
                    return pattern.equals(valString);
                }
            }
            return false;
        }

        @Override
        public void finish() {
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
            this.regex = Pattern.compile(pattern);
        }

        public boolean isUseRegex() {
            return useRegex;
        }

        public void setUseRegex(boolean useRegex) {
            this.useRegex = useRegex;
        }

        public static class Node extends EqualStringFilter<org.gephi.graph.api.Node> implements NodeFilter {

            public Node(Column column) {
                super(column);
            }
        }

        public static class Edge extends EqualStringFilter<org.gephi.graph.api.Edge> implements EdgeFilter {

            public Edge(Column column) {
                super(column);
            }
        }
    }

    private static class EqualNumberFilterBuilder extends AbstractAttributeFilterBuilder {

        public EqualNumberFilterBuilder(Column column) {
            super(column,
                    EQUAL,
                    NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.description"),
                    null);
        }

        @Override
        public EqualNumberFilter getFilter(Workspace workspace) {
            return AttributeUtils.isNodeColumn(column) ? new EqualNumberFilter.Node(column) : new EqualNumberFilter.Edge(column);
        }

        @Override
        public JPanel getPanel(Filter filter) {
            EqualNumberUI ui = Lookup.getDefault().lookup(EqualNumberUI.class);
            if (ui != null) {
                return ui.getPanel((EqualNumberFilter) filter);
            }
            return null;
        }
    }

    public static abstract class EqualNumberFilter<K extends Element> extends AbstractAttributeFilter<K> implements RangeFilter {

        private Number match;
        private Range range;

        public EqualNumberFilter(Column column) {
            super(NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name"), column);

            //App property
            addProperty(Number.class, "match");
            addProperty(Range.class, "range");
        }

        @Override
        public boolean init(Graph graph) {
            if (AttributeUtils.isNodeColumn(column)) {
                if (graph.getNodeCount() == 0) {
                    return false;
                }
            } else if (AttributeUtils.isEdgeColumn(column)) {
                if (graph.getEdgeCount() == 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean evaluate(Graph graph, Element element) {
            Object val = element.getAttribute(column, graph.getView());
            if (val != null) {
                return val.equals(match);
            }
            return false;
        }

        @Override
        public void finish() {
        }

        @Override
        public Number[] getValues(Graph graph) {
            List<Number> vals = new ArrayList<>();
            if (AttributeUtils.isNodeColumn(column)) {
                for (Element n : graph.getNodes()) {
                    Object val = n.getAttribute(column, graph.getView());
                    if (val != null) {
                        vals.add((Number) val);
                    }
                }
            } else {
                for (Element e : graph.getEdges()) {
                    Object val = e.getAttribute(column, graph.getView());
                    if (val != null) {
                        vals.add((Number) val);
                    }
                }
            }
            return vals.toArray(new Number[0]);
        }

        @Override
        public FilterProperty getRangeProperty() {
            return getProperties()[2];
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
            if (match == null) {
                match = range.getMinimum();
            } else {
                match = Range.trimToBounds(range.getMinimum(), range.getMaximum(), match);
            }
        }

        public Number getMatch() {
            return match;
        }

        public void setMatch(Number match) {
            this.match = match;
        }

        public static class Node extends EqualNumberFilter<org.gephi.graph.api.Node> implements NodeFilter {

            public Node(Column column) {
                super(column);
            }
        }

        public static class Edge extends EqualNumberFilter<org.gephi.graph.api.Edge> implements EdgeFilter {

            public Edge(Column column) {
                super(column);
            }
        }
    }

    private static class EqualBooleanFilterBuilder extends AbstractAttributeFilterBuilder {

        public EqualBooleanFilterBuilder(Column column) {
            super(column,
                    EQUAL,
                    NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.description"), null);
        }

        @Override
        public EqualBooleanFilter getFilter(Workspace workspace) {
            return AttributeUtils.isNodeColumn(column) ? new EqualBooleanFilter.Node(column) : new EqualBooleanFilter.Edge(column);
        }

        @Override
        public JPanel getPanel(Filter filter) {
            EqualBooleanUI ui = Lookup.getDefault().lookup(EqualBooleanUI.class);
            if (ui != null) {
                return ui.getPanel((EqualBooleanFilter) filter);
            }
            return null;
        }
    }

    public static abstract class EqualBooleanFilter<K extends Element> extends AbstractAttributeFilter<K> {

        private boolean match = false;

        public EqualBooleanFilter(Column column) {
            super(NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name"),
                    column);

            //Add property
            addProperty(Boolean.class, "match");
        }

        @Override
        public boolean init(Graph graph) {
            return true;
        }

        @Override
        public boolean evaluate(Graph graph, Element element) {
            Object val = element.getAttribute(column, graph.getView());
            if (val != null) {
                return val.equals(match);
            }
            return false;
        }

        @Override
        public void finish() {
        }

        public boolean isMatch() {
            return match;
        }

        public void setMatch(boolean match) {
            this.match = match;
        }

        public static class Node extends EqualBooleanFilter<org.gephi.graph.api.Node> implements NodeFilter {

            public Node(Column column) {
                super(column);
            }
        }

        public static class Edge extends EqualBooleanFilter<org.gephi.graph.api.Edge> implements EdgeFilter {

            public Edge(Column column) {
                super(column);
            }
        }
    }
}
