package org.gephi.desktop.search.plugin;

import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.impl.SearchCategoryImpl;
import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.desktop.search.spi.SearchResultsBuilder;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchProvider.class, position = 120)
public class ElementLabelSearchProvider implements SearchProvider<Element> {

    @Override
    public void search(SearchRequest request, SearchResultsBuilder<Element> resultsBuilder) {

        String query = request.getQuery();

        // Exact node label
        if (request.isCategoryIncluded(SearchCategoryImpl.NODES())) {
            matchElementLabel(request.getGraph().getNodes(), query, resultsBuilder);
        }


        // Exact edge label
        if (request.isCategoryIncluded(SearchCategoryImpl.EDGES())) {
            matchElementLabel(request.getGraph().getEdges(), query, resultsBuilder);
        }
    }

    protected void matchElementLabel(ElementIterable<? extends Element> iterable, String query,
                                     SearchResultsBuilder<Element> resultsBuilder) {
        final String matchLocation = toMatchLocation();

        // Exact Node label
        for (Element element : iterable) {
            if (match(element, query)) {
                if (!resultsBuilder.addResult(element, toHtmlDisplay(element, query), matchLocation)) {
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

    protected String toMatchLocation() {
        return NbBundle.getMessage(ElementLabelSearchProvider.class, "ElementLabelSearchProvider.match");
    }

    protected String toHtmlDisplay(Element element, String query) {
        return NbBundle.getMessage(ElementLabelSearchProvider.class,
            "ElementLabelSearchProvider.result", element.getLabel());
    }
}
