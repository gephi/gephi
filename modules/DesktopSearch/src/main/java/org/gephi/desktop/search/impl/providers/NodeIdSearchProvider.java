package org.gephi.desktop.search.impl.providers;

import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.api.SearchCategory;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchProvider.class, position = 100)
public class NodeIdSearchProvider implements SearchProvider<Node> {

    @Override
    public void search(SearchRequest request, SearchResultsBuilder<Node> resultsBuilder) {
        GraphModel graphModel = request.getWorkspace().getLookup().lookup(GraphModel.class);
        Node node = graphModel.getGraphVisible().getNode(request.getQuery());

        if (node != null) {
            resultsBuilder.addResult(node, toHtmlDisplay(node));
        }
    }

    @Override
    public SearchCategory getCategory() {
        return new GraphCategory();
    }

    public static String toHtmlDisplay(Node node) {
        return NbBundle.getMessage(NodeIdSearchProvider.class,
            "NodeIdSearchProvider.result", node.getId());
    }
}
