package org.gephi.desktop.search.plugin;

import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.impl.SearchCategoryImpl;
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
        if (request.isCategoryIncluded(SearchCategoryImpl.NODES())) {
            Node node = request.getGraph().getNode(request.getQuery());

            if (node != null) {
                resultsBuilder.addResult(node, toHtmlDisplay(node), NbBundle.getMessage(NodeIdSearchProvider.class,
                    "NodeIdSearchProvider.match"));
            }
        }
    }

    public static String toHtmlDisplay(Node node) {
        return NbBundle.getMessage(NodeIdSearchProvider.class,
            "NodeIdSearchProvider.result", node.getId());
    }
}
