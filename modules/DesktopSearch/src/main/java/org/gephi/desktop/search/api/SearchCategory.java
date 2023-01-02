package org.gephi.desktop.search.api;

import org.openide.util.Lookup;

public interface SearchCategory {

    /**
     * Return the unique identifier of this category.
     *
     * @return the unique identifier of this category
     */
    String getId();

    /**
     * Return the display name of this category.
     *
     * @return the display name of this category
     */
    String getDisplayName();

    /**
     * Find the category by its unique identifier.
     *
     * @param id the unique identifier of the category
     * @return the category or null if not found
     */
    static SearchCategory findById(String id) {
        return Lookup.getDefault().lookupAll(SearchCategory.class).stream()
            .filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
}
