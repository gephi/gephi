package org.gephi.desktop.search.filter;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.desktop.search.api.SearchController;
import org.gephi.desktop.search.api.SearchRequest;
import org.gephi.desktop.search.api.SearchResult;
import org.gephi.desktop.search.impl.SearchCategoryImpl;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = FilterBuilder.class)
public class SearchFilterBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.ATTRIBUTES;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SearchFilterBuilder.class, "SearchFilterBuilder.name");
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("DesktopSearch/search.png", false);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(SearchFilterBuilder.class, "SearchFilterBuilder.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new SearchFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        SearchPanel panel = new SearchPanel();
        panel.setup((SearchFilter) filter);
        return panel;
    }

    @Override
    public void destroy(Filter filter) {

    }

    public static class SearchFilter implements ComplexFilter {

        private String query;
        private String type;

        public SearchFilter() {
            type = SearchCategoryImpl.NODES().getId();
        }

        @Override
        public Graph filter(Graph graph) {
            if (query == null || query.isEmpty()) {
                return graph;
            }
            SearchRequest request =
                SearchRequest.builder().query(query).graph(graph).parallel(false).limitResults(false).build();
            SearchController searchController = Lookup.getDefault().lookup(SearchController.class);
            Subgraph subgraph = graph.getModel().getGraph(graph.getView());
            if (type.equalsIgnoreCase(SearchCategoryImpl.NODES().getId())) {
                List<Node> nodes = searchController.search(request, Node.class).stream()
                    .map(SearchResult::getResult).collect(Collectors.toList());
                subgraph.retainNodes(nodes);
            } else if (type.equalsIgnoreCase(SearchCategoryImpl.EDGES().getId())) {
                List<Edge> edges = searchController.search(request, Edge.class).stream()
                    .map(SearchResult::getResult).collect(Collectors.toList());
                subgraph.retainEdges(edges);
            }
            return subgraph;
        }

        public String getName() {
            return NbBundle.getMessage(SearchFilterBuilder.class, "SearchFilter.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[] {
                    FilterProperty.createProperty(this, String.class, "query"),
                    FilterProperty.createProperty(this, String.class, "type")};
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new FilterProperty[0];
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
