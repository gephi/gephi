/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.filters.plugin.attribute;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Exceptions;
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

    public Category getCategory() {
        return EQUAL;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumn[] columns = am.getNodeTable().getColumns();
        for (AttributeColumn c : columns) {
            if (AttributeUtils.getDefault().isStringColumn(c)) {
                EqualStringFilterBuilder b = new EqualStringFilterBuilder(c);
                builders.add(b);
            } else if (AttributeUtils.getDefault().isNumberColumn(c)) {
                EqualNumberFilterBuilder b = new EqualNumberFilterBuilder(c);
                builders.add(b);
            } else if (c.getType().equals(AttributeType.BOOLEAN)) {
                EqualBooleanFilterBuilder b = new EqualBooleanFilterBuilder(c);
                builders.add(b);
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class EqualStringFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public EqualStringFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return EQUAL;
        }

        public String getName() {
            return "<font color='#000000'>" + column.getTitle() + "</font> "
                    + "<font color='#999999'><i>" + column.getType().toString() + "</i></font>";
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public EqualStringFilter getFilter() {
            EqualStringFilter f = new EqualStringFilter();
            f.setColumn(column);
            return f;
        }

        public JPanel getPanel(Filter filter) {
            EqualStringUI ui = Lookup.getDefault().lookup(EqualStringUI.class);
            if (ui != null) {
                return ui.getPanel((EqualStringFilter) filter);
            }
            return null;
        }
    }

    public static class EqualStringFilter implements Filter {

        private FilterProperty[] filterProperties;
        private String pattern;
        private boolean useRegex;
        private AttributeColumn column;

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, String.class, "pattern"),
                                FilterProperty.createProperty(this, Boolean.class, "useRegex")
                            };
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isUseRegex() {
            return useRegex;
        }

        public void setUseRegex(boolean useRegex) {
            this.useRegex = useRegex;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }
    }

    private static class EqualNumberFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public EqualNumberFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return EQUAL;
        }

        public String getName() {
            return "<font color='#000000'>" + column.getTitle() + "</font> "
                    + "<font color='#999999'><i>" + column.getType().toString() + "</i></font>";
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public EqualNumberFilter getFilter() {
            EqualNumberFilter f = new EqualNumberFilter(column);
            f.setColumn(column);
            return f;
        }

        public JPanel getPanel(Filter filter) {
            EqualNumberUI ui = Lookup.getDefault().lookup(EqualNumberUI.class);
            if (ui != null) {
                return ui.getPanel((EqualNumberFilter) filter);
            }
            return null;
        }
    }

    public static class EqualNumberFilter implements Filter {

        private FilterProperty[] filterProperties;
        private Number match;
        private AttributeColumn column;
        private Object min;
        private Object max;

        public EqualNumberFilter(AttributeColumn column) {
            this.column = column;
            refreshMinMax();
        }

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name");
        }

        private void refreshMinMax() {
            Object[] values = getValues();
            min = AttributeUtils.getDefault().getMin(column, values);
            max = AttributeUtils.getDefault().getMax(column, values);
        }

        public Object[] getValues() {
            List<Object> vals = new ArrayList<Object>();
            GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
            Graph graph = gm.getGraphVisible();
            for (Node n : graph.getNodes()) {
                Object val = n.getNodeData().getAttributes().getValue(column.getIndex());
                if (val != null) {
                    vals.add(val);
                }
            }
            return vals.toArray();
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, Number.class, "match"),};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public Number getMatch() {
            return match;
        }

        public void setMatch(Number match) {
            this.match = match;
        }

        public Object getMinimun() {
            return min;
        }

        public Object getMaximum() {
            return max;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }
    }

    private static class EqualBooleanFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public EqualBooleanFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return EQUAL;
        }

        public String getName() {
            return "<font color='#000000'>" + column.getTitle() + "</font> "
                    + "<font color='#999999'><i>" + column.getType().toString() + "</i></font>";
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public EqualBooleanFilter getFilter() {
            EqualBooleanFilter f = new EqualBooleanFilter();
            f.setColumn(column);
            return f;
        }

        public JPanel getPanel(Filter filter) {
            EqualBooleanUI ui = Lookup.getDefault().lookup(EqualBooleanUI.class);
            if (ui != null) {
                return ui.getPanel((EqualBooleanFilter) filter);
            }
            return null;
        }
    }

    public static class EqualBooleanFilter implements Filter {

        private FilterProperty[] filterProperties;
        private boolean match = false;
        private AttributeColumn column;

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeEqualBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, Boolean.class, "match")
                            };
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public boolean isMatch() {
            return match;
        }

        public void setMatch(boolean match) {
            this.match = match;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }
    }
}
