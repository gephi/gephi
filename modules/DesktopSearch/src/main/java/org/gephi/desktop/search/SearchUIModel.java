package org.gephi.desktop.search;

import java.util.Collection;
import org.gephi.desktop.search.api.SearchCategory;
import org.openide.util.Lookup;

public class SearchUIModel {

    protected String query = "";

    protected SearchCategory category;

    public void setCategory(SearchCategory category) {
        this.category = category;
    }

    protected Collection<? extends SearchCategory> getCategories() {
        return Lookup.getDefault().lookupAll(SearchCategory.class);
    }
}
