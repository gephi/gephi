package org.gephi.desktop.search.api;

import org.openide.util.Lookup;

public interface SearchCategory {

    String getId();

    String getDisplayName();

    static SearchCategory findById(String id) {
        return Lookup.getDefault().lookupAll(SearchCategory.class).stream()
            .filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
}
