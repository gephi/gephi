package org.gephi.filters;

import javax.xml.stream.XMLStreamReader;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import org.gephi.filters.plugin.graph.EgoBuilder;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.filters.plugin.graph.HasSelfLoopBuilder;
import org.gephi.filters.plugin.operator.INTERSECTIONBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PersistenceProviderTest {

    @Test
    public void testEmpty() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModel();
        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
    }

    @Test
    public void testSimpleFilter() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModel();
        FilterQueryImpl query = new FilterQueryImpl(null, new GiantComponentBuilder.GiantComponentFilter());
        filterModel.addFirst(query);
        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
    }

    @Test
    public void testFilterWithParameters() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModel();
        EgoBuilder.EgoFilter egoFilter = new EgoBuilder.EgoFilter();
        egoFilter.setPattern("test");

        FilterQueryImpl query = new FilterQueryImpl(null, egoFilter);
        filterModel.addFirst(query);
        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
    }

    @Test
    public void testAndOperator() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModel();
        OperatorQueryImpl query = new OperatorQueryImpl(new INTERSECTIONBuilder.IntersectionOperator());
        filterModel.setSubQuery(query, new FilterQueryImpl(null, new GiantComponentBuilder.GiantComponentFilter()));
        filterModel.setSubQuery(query, new FilterQueryImpl(null, new HasSelfLoopBuilder.HasSelfLoopFilter()));
        filterModel.addFirst(query);
        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
    }

    @Test
    public void testFilterRename() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModel();
        EgoBuilder.EgoFilter egoFilter = new EgoBuilder.EgoFilter();
        egoFilter.setPattern("test");

        FilterQueryImpl query = new FilterQueryImpl(null, egoFilter);
        query.setName("* Foo");
        filterModel.addFirst(query);
        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
    }

    @Test
    public void testEqualStringFilterWithoutPattern() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        Column column = filterModel.getGraphModel().getNodeTable().addColumn("country", String.class);
        AttributeEqualBuilder.EqualStringFilter filter = new AttributeEqualBuilder.EqualStringFilter.Node(column);

        FilterQueryImpl query = new FilterQueryImpl(null, filter);
        filterModel.addFirst(query);
        FilterModelPersistenceProvider provider = new FilterModelPersistenceProvider() {
            @Override
            public void readXML(XMLStreamReader reader, Workspace workspace) {
                //A project restores the graph and its columns before the filters
                workspace.getLookup().lookup(GraphModel.class).getNodeTable().addColumn("country", String.class);
                super.readXML(reader, workspace);
            }
        };
        GephiFormat.testXMLPersistenceProvider(provider, filterModel.getWorkspace());
    }

    @Test
    @Ignore
    public void testAttributeFilter() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();

        FilterBuilder[] builders = new AttributeEqualBuilder().getBuilders(filterModel.getWorkspace());
        Assert.assertEquals(1, builders.length);
        FilterBuilder builder = builders[0];
        Filter filter = builder.getFilter(filterModel.getWorkspace());

        FilterQueryImpl query = new FilterQueryImpl(builder, filter);
        filterModel.addFirst(query);
        new FilterProcessor().init(filter, filterModel.getGraphModel().getGraph());
        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
    }
}
