package org.gephi.desktop.search.impl.providers;

import org.gephi.desktop.search.api.SearchCategory;
import org.openide.util.NbBundle;

public class GraphCategory implements SearchCategory {

    @Override
    public String getId() {
        return "graph";
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(GraphCategory.class, "GraphCategory.displayName");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphCategory)) {
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
