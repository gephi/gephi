package org.gephi.filters;

import org.gephi.filters.plugin.graph.EgoBuilder;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.filters.plugin.graph.HasSelfLoopBuilder;
import org.gephi.filters.plugin.operator.INTERSECTIONBuilder;
import org.gephi.project.io.utils.GephiFormat;
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

//    @Test
//    public void testAttributeFilter() throws Exception {
//        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
//
//        FilterBuilder[] builders = new AttributeEqualBuilder().getBuilders(filterModel.getWorkspace());
//        Assert.assertEquals(1, builders.length);
//        FilterBuilder builder = builders[0];
//        Filter filter = builder.getFilter(filterModel.getWorkspace());
//
//        FilterQueryImpl query = new FilterQueryImpl(builder, filter);
//        filterModel.addFirst(query);
//        new FilterProcessor().init(filter, filterModel.getGraphModel().getGraph());
//        GephiFormat.testXMLPersistenceProvider(new FilterModelPersistenceProvider(), filterModel.getWorkspace());
//    }
}
