package org.gephi.desktop.search.impl.providers;

import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.impl.SearchCategoryImpl;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchProvider.class, position = 120)
public class ElementLabelSearchProvider implements SearchProvider<Element> {

    @Override
    public void search(SearchRequest request, SearchResultsBuilder<Element> resultsBuilder) {
        Workspace workspace = request.getWorkspace();
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        String query = request.getQuery();

        // Exact node label
        if (request.isCategoryIncluded(SearchCategoryImpl.NODES())) {
            matchElementLabel(graphModel.getGraphVisible().getNodes(), query, resultsBuilder);
        }


        // Exact edge label
        if (request.isCategoryIncluded(SearchCategoryImpl.EDGES())) {
            matchElementLabel(graphModel.getGraphVisible().getEdges(), query, resultsBuilder);
        }
    }

    protected void matchElementLabel(ElementIterable<? extends Element> iterable, String query,
                                     SearchResultsBuilder<Element> resultsBuilder) {
        // Exact Node label
        for (Element element : iterable) {
            if (match(element, query)) {
                if (!resultsBuilder.addResult(element, toHtmlDisplay(element, query))) {
                    iterable.doBreak();
                    break;
                }
            }
            if (resultsBuilder.isObsolete()) {
                iterable.doBreak();
                break;
            }
        }
    }

    protected boolean match(Element element, String query) {
        return element.getLabel() != null && element.getLabel().equalsIgnoreCase(query);
    }

    protected String toHtmlDisplay(Element element, String query) {
        return NbBundle.getMessage(ElementLabelSearchProvider.class,
            "ElementLabelSearchProvider." + (element instanceof Node ? "node" : "edge") + ".result",
            element.getLabel());
    }
}
