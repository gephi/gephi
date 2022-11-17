package org.gephi.desktop.search.impl.providers;

import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = SearchProvider.class, position = 130)
public class FuzzyElementLabelSearchProvider extends ElementLabelSearchProvider {

    @Override
    protected boolean match(Element element, String query) {
        return element.getLabel() != null && element.getLabel().toLowerCase().contains(query.toLowerCase()) &&
            !super.match(element, query);
    }

    @Override
    protected String toHtmlDisplay(Element element, String query) {
        String label = element.getLabel();
        int index = label.toLowerCase().indexOf(query.toLowerCase());
        String before = label.substring(0, index);
        String after = label.substring(index + query.length());
        return NbBundle.getMessage(FuzzyElementLabelSearchProvider.class,
            "FuzzyElementLabelSearchProvider." + (element instanceof Node ? "node" : "edge") + ".result",
            before, query, after);
    }
}

