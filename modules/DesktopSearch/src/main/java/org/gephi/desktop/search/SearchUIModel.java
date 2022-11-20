package org.gephi.desktop.search;

import java.util.LinkedHashSet;
import java.util.Set;
import org.gephi.desktop.search.api.SearchCategory;
import org.gephi.desktop.search.spi.SearchProvider;
import org.openide.util.Lookup;

public class SearchUIModel {

    protected String query = "";

    protected SearchCategory category;

    public void setCategory(SearchCategory category) {
        this.category = category;
    }

    protected Set<SearchCategory> getCategories() {
        LinkedHashSet<SearchCategory> categories = new LinkedHashSet<>();
        Lookup.getDefault().lookupAll(SearchProvider.class)
            .stream().map(SearchProvider::getCategory).forEachOrdered(categories::add);
        return categories;
    }
}
