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
package org.gephi.filters.attribute;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
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
            NbBundle.getMessage(AttributeEqualBuilder.class, "AttributesEqualBuilder.name"),
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
            AttributeEqualFilterBuilder b = new AttributeEqualFilterBuilder(c);
            builders.add(b);
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class AttributeEqualFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public AttributeEqualFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return EQUAL;
        }

        public String getName() {
            return column.getTitle();
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public AttributeEqualFilter getFilter() {
            AttributeEqualFilter f = new AttributeEqualFilter();
            f.setColumn(column);
            return f;
        }

        public JPanel getPanel(Filter filter) {
            AttributeEqualUI ui = Lookup.getDefault().lookup(AttributeEqualUI.class);
            if (ui != null) {
                return ui.getPanel((AttributeEqualFilter) filter);
            }
            return null;
        }
    }

    public static class AttributeEqualFilter implements Filter {

        private FilterProperty[] filterProperties;
        private String pattern;
        private boolean useRegex;
        private AttributeColumn column;

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributesEqualBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
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
}
