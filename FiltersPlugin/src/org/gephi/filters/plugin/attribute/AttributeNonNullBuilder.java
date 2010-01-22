/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.attribute;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder.EqualStringFilter;
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
public class AttributeNonNullBuilder implements CategoryBuilder {

    private final static Category NONNULL = new Category(
            NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeNonNullBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    public Category getCategory() {
        return NONNULL;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumn[] columns = am.getNodeTable().getColumns();
        for (AttributeColumn c : columns) {
            AttributeNonNullFilterBuilder b = new AttributeNonNullFilterBuilder(c);
            builders.add(b);
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class AttributeNonNullFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public AttributeNonNullFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return NONNULL;
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

        public AttributeNonNullFilter getFilter() {
            AttributeNonNullFilter f = new AttributeNonNullFilter();
            f.setColumn(column);
            return f;
        }

        public JPanel getPanel(Filter filter) {
            return null;
        }
    }

    public static class AttributeNonNullFilter implements Filter {

        private FilterProperty[] filterProperties;
        private AttributeColumn column;

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeNonNullBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }
    }
}
