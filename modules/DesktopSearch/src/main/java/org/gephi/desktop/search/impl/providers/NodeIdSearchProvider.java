package org.gephi.desktop.search.impl.providers;

import org.gephi.desktop.search.api.SearchRequest;
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
        GraphModel graphModel = request.workspace().getLookup().lookup(GraphModel.class);
        Node node = graphModel.getGraphVisible().getNode(request.getQuery());

        if (node != null) {
            resultsBuilder.addResult(node, toHtmlDisplay(node));
        }
    }

    public static String toHtmlDisplay(Node node) {
        return NbBundle.getMessage(NodeIdSearchProvider.class,
            "NodeIdSearchProvider.result", node.getId());
    }
}
