package org.gephi.desktop.search.impl;

import org.gephi.desktop.search.api.SearchCategory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

public abstract class SearchCategoryImpl implements SearchCategory {

    @ServiceProvider(service = SearchCategory.class, position = 100)
    public static final class NodeSearchCategoryImpl extends SearchCategoryImpl {

        @Override
        public String getId() {
            return "Nodes";
        }
    }

    @ServiceProvider(service = SearchCategory.class, position = 200)
    public static final class EdgeSearchCategoryImpl extends SearchCategoryImpl {

        @Override
        public String getId() {
            return "Edges";
        }
    }

    public static SearchCategory NODES() {
        return Lookup.getDefault().lookup(NodeSearchCategoryImpl.class);
    }

    public static SearchCategory EDGES() {
        return Lookup.getDefault().lookup(EdgeSearchCategoryImpl.class);
    }

    @Override
    public abstract String getId();

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SearchCategoryImpl.class, "Category." + getId() + ".displayName");
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
