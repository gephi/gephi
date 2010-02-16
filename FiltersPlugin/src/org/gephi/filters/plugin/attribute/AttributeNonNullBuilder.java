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
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
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
        for (AttributeColumn c : am.getNodeTable().getColumns()) {
            AttributeNonNullFilterBuilder b = new AttributeNonNullFilterBuilder(c);
            builders.add(b);
        }
        for (AttributeColumn c : am.getEdgeTable().getColumns()) {
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
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                NodeAttributeNonNullFilter f = new NodeAttributeNonNullFilter();
                f.setColumn(column);
                return f;
            } else {
                EdgeAttributeNonNullFilter f = new EdgeAttributeNonNullFilter();
                f.setColumn(column);
                return f;
            }
        }

        public JPanel getPanel(Filter filter) {
            return null;
        }
    }

    public static class NodeAttributeNonNullFilter extends AttributeNonNullFilter implements NodeFilter {
    }

    public static class EdgeAttributeNonNullFilter extends AttributeNonNullFilter implements EdgeFilter {
    }

    public static class AttributeNonNullFilter implements Filter {

        private FilterProperty[] filterProperties;
        private AttributeColumn column;

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeNonNullBuilder.name");
        }

        public boolean init(Graph graph) {
            return true;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            return edge.getEdgeData().getAttributes().getValue(column.getIndex()) != null;
        }

        public boolean evaluate(Graph graph, Node node) {
            return node.getNodeData().getAttributes().getValue(column.getIndex()) != null;
        }

        public void finish() {
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
