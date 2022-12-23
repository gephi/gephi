package org.gephi.desktop.search.impl;

import org.gephi.desktop.search.api.SearchCategory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public abstract class SearchCategoryImpl implements SearchCategory {

    private static final String NODE_ID = "Nodes";
    private static final String EDGE_ID = "Edges";

    @ServiceProvider(service = SearchCategory.class, position = 100)
    public static final class NodeSearchCategoryImpl extends SearchCategoryImpl {

        @Override
        public String getId() {
            return NODE_ID;
        }
    }

    @ServiceProvider(service = SearchCategory.class, position = 200)
    public static final class EdgeSearchCategoryImpl extends SearchCategoryImpl {

        @Override
        public String getId() {
            return EDGE_ID;
        }
    }

    public static SearchCategory NODES() {
        return Lookup.getDefault().lookupAll(SearchCategory.class).stream()
            .filter(c -> c.getId().equals(NODE_ID)).findFirst().orElse(null);
    }

    public static SearchCategory EDGES() {
        return Lookup.getDefault().lookupAll(SearchCategory.class).stream()
            .filter(c -> c.getId().equals(EDGE_ID)).findFirst().orElse(null);
    }

    @Override
    public abstract String getId();

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SearchCategoryImpl.class, "Category." + getId() + ".displayName");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchCategoryImpl)) {
            return false;
        }

        SearchCategory that = (SearchCategory) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
