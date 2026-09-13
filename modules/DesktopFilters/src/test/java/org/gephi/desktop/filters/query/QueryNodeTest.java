package org.gephi.desktop.filters.query;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.GraphView;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.netbeans.junit.MockServices;

/**
 * Regression test for GEPHI-6C5: getHtmlDisplayName() must not throw a NullPointerException
 * when FilterController.getModel() returns null, which happens whenever no workspace is
 * currently selected (see the documented contract of Controller.getModel()).
 */
public class QueryNodeTest {

    @Before
    public void setUp() {
        MockServices.setServices(TestFilterController.class);
        TestFilterController.model = null;
    }

    @After
    public void tearDown() {
        TestFilterController.model = null;
        // Services are registered globally, don't leak them into the next test
        MockServices.setServices();
    }

    @Test
    public void testGetHtmlDisplayNameWithNoCurrentWorkspace() {
        // FilterController.getModel() returns null whenever no workspace is selected
        TestFilterController.model = null;

        Query query = Mockito.mock(Query.class);
        Mockito.when(query.getName()).thenReturn("My Query");

        QueryNode node = new QueryNode(query);

        Assert.assertEquals("My Query", node.getHtmlDisplayName());
    }

    @Test
    public void testGetHtmlDisplayNameWhenQueryIsCurrentAndFiltering() {
        Query query = Mockito.mock(Query.class);
        Mockito.when(query.getName()).thenReturn("My Query");

        FilterModel model = Mockito.mock(FilterModel.class);
        Mockito.when(model.isFiltering()).thenReturn(true);
        Mockito.when(model.getCurrentQuery()).thenReturn(query);
        TestFilterController.model = model;

        QueryNode node = new QueryNode(query);

        Assert.assertEquals("<b>My Query</b>", node.getHtmlDisplayName());
    }

    public static class TestFilterController implements FilterController {

        static volatile FilterModel model;

        @Override
        public Query createQuery(FilterBuilder builder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query createQuery(Filter filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rename(Query query, String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSubQuery(Query query, Query subQuery) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeSubQuery(Query query, Query parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void filterVisible(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectVisible(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public GraphView filter(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void exportToColumn(String title, Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void exportToNewWorkspace(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void exportToLabelVisible(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAutoRefresh(boolean autoRefresh) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCurrentQuery(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FilterModel getModel() {
            return model;
        }

        @Override
        public FilterModel getModel(Workspace workspace) {
            return model;
        }
    }
}
