package org.gephi.desktop.search.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.gephi.desktop.search.api.SearchListener;
import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.api.SearchResult;
import org.gephi.desktop.search.impl.providers.NodeIdSearchProvider;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.netbeans.junit.MockServices;

@RunWith(MockitoJUnitRunner.class)
public class SearchControllerImplTest {

    private SearchControllerImpl controller;

    @Mock
    private SearchListener searchListener;

    @Before
    public void setUp() {
        controller = new SearchControllerImpl();
    }

    @After
    public void cleanUp() {
        controller.shutdown();
    }

    @Test
    public void testNodeId() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph();

        SearchRequest request = buildRequest(GraphGenerator.FIRST_NODE, generator);
        Collection<SearchResult<Node>> results = controller.search(request, Node.class);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(GraphGenerator.FIRST_NODE, results.iterator().next().getResult().getId());
    }

    @Test
    public void testNodeLabel() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph();

        generator.getGraph().getNode(GraphGenerator.FIRST_NODE).setLabel("foo");

        SearchRequest request = buildRequest("foo", generator);
        Collection<SearchResult<Node>> results = controller.search(request, Node.class);

        Assert.assertEquals(1, results.size());
        SearchResult<Node> result = results.iterator().next();
        Assert.assertEquals(GraphGenerator.FIRST_NODE, result.getResult().getId());

    }

    @Test
    public void testUniqueNode() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph();

        generator.getGraph().getNode(GraphGenerator.FIRST_NODE).setLabel(GraphGenerator.FIRST_NODE);

        SearchRequest request = buildRequest(GraphGenerator.FIRST_NODE, generator);
        Collection<SearchResult<Node>> results = controller.search(request, Node.class);

        Assert.assertEquals(1, results.size());
        SearchResult<Node> result = results.iterator().next();
        Assert.assertEquals(GraphGenerator.FIRST_NODE, result.getResult().getId());
        Assert.assertEquals(NodeIdSearchProvider.toHtmlDisplay(result.getResult()), result.getHtmlDisplay());
    }

    @Test
    public void testElement() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph();

        SearchRequest request = buildRequest(GraphGenerator.FIRST_NODE, generator);
        List<Element> results = toList(controller.search(request, Element.class));

        Assert.assertEquals(2, results.size());
        Assert.assertSame(generator.getGraph().getNode(GraphGenerator.FIRST_NODE), results.get(0));
        Assert.assertSame(generator.getGraph().getEdge(GraphGenerator.FIRST_EDGE), results.get(1));
    }

    @Test
    public void testFuzzyLabel() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph();

        Node node = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        node.setLabel("foobar");

        List<Element> r1 = toList(controller.search(buildRequest("foo", generator), Element.class));
        List<Element> r2 = toList(controller.search(buildRequest("bar", generator), Element.class));
        List<Element> r3 = toList(controller.search(buildRequest("oo", generator), Element.class));

        Assert.assertEquals(List.of(node), r1);
        Assert.assertEquals(List.of(node), r2);
        Assert.assertEquals(List.of(node), r3);
    }

    @Test
    public void testAsync() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph();

        SearchRequest request = buildRequest(GraphGenerator.FIRST_NODE, generator);
        controller.search(request, searchListener);

        Awaitility.await().untilAsserted(() -> {
            Mockito.verify(searchListener).started(request);
            Mockito.verify(searchListener).finished(Mockito.eq(request), Mockito.argThat(list -> list.size() == 2));
        });
    }

    @Test
    public void testAsyncCancel() {
        MockServices.setServices(SleepProvider.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace();

        SearchRequest request1 = buildRequest("sleep", generator);
        SearchRequest request2 = buildRequest("bar", generator);
        controller.search(request1, searchListener);
        controller.search(request2, searchListener);

        Awaitility.await().untilAsserted(() -> {
            Mockito.verify(searchListener).started(request1);
            Mockito.verify(searchListener).started(request2);
            Mockito.verify(searchListener).cancelled();
            Mockito.verify(searchListener).finished(Mockito.eq(request2), Mockito.any());
        });
    }

    // Utility

    public static class SleepProvider implements SearchProvider {

        @Override
        public void search(SearchRequest request, SearchResultsBuilder resultsBuilder) {
            if (request.getQuery().equals("sleep")) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private <T> List<T> toList(Collection<SearchResult<T>> results) {
        return results.stream().map(SearchResult::getResult).collect(Collectors.toList());
    }

    private SearchRequest buildRequest(String query, GraphGenerator generator) {
        return SearchRequest.builder().query(query).workspace(generator.getWorkspace()).build();
    }
}
