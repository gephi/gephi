package org.gephi.desktop.search.plugin;

import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.impl.SearchCategoryImpl;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;
import org.gephi.graph.api.Edge;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchProvider.class, position = 110)
public class EdgeIdSearchProvider implements SearchProvider<Edge> {

    @Override
    public void search(SearchRequest request, SearchResultsBuilder<Edge> resultsBuilder) {
        if (request.isCategoryIncluded(SearchCategoryImpl.EDGES())) {
            Edge edge = request.getGraph().getEdge(request.getQuery());

            if (edge != null) {
                resultsBuilder.addResult(edge, toHtmlDisplay(edge), NbBundle.getMessage(EdgeIdSearchProvider.class,
                    "EdgeIdSearchProvider.match"));
            }
        }
    }

    private String toHtmlDisplay(Edge edge) {
        return NbBundle.getMessage(EdgeIdSearchProvider.class,
            "EdgeIdSearchProvider.result", edge.getId());
    }
}
