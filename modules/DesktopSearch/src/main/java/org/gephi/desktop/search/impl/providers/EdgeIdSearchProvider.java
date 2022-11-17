package org.gephi.desktop.search.impl.providers;

import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchProvider.class, position = 110)
public class EdgeIdSearchProvider implements SearchProvider<Edge> {

    @Override
    public void search(SearchRequest request, SearchResultsBuilder<Edge> resultsBuilder) {
        GraphModel graphModel = request.workspace().getLookup().lookup(GraphModel.class);
        Edge edge = graphModel.getGraphVisible().getEdge(request.getQuery());

        if (edge != null) {
            resultsBuilder.addResult(edge, toHtmlDisplay(edge));
        }
    }

    private String toHtmlDisplay(Edge edge) {
        return NbBundle.getMessage(EdgeIdSearchProvider.class,
            "EdgeIdSearchProvider.result", edge.getId());
    }
}
