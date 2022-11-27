package org.gephi.desktop.search.plugin;

import org.gephi.desktop.search.spi.SearchProvider;
import org.gephi.graph.api.Element;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = SearchProvider.class, position = 130)
public class StartWithElementLabelSearchProvider extends ElementLabelSearchProvider {

    @Override
    protected boolean match(Element element, String query) {
        return element.getLabel() != null && element.getLabel().toLowerCase().startsWith(query.toLowerCase()) &&
            !super.match(element, query);
    }

    @Override
    protected String toMatchLocation() {
        return NbBundle.getMessage(StartWithElementLabelSearchProvider.class, "StartWithElementLabelSearchProvider.match");
    }

    @Override
    protected String toHtmlDisplay(Element element, String query) {
        String label = element.getLabel();

        String match = label.substring(0, query.length());
        String after = label.substring(query.length());
        return NbBundle.getMessage(StartWithElementLabelSearchProvider.class,
            "StartWithElementLabelSearchProvider.result",
            match, after);
    }
}

