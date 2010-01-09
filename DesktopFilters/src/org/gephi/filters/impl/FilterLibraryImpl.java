/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.filters.impl;

import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterLibraryMask;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterLibraryImpl implements FilterLibrary {

    private AbstractLookup lookup;
    private InstanceContent content;

    public FilterLibraryImpl() {
        content = new InstanceContent();
        lookup = new AbstractLookup(content);

        for (FilterBuilder builder : Lookup.getDefault().lookupAll(FilterBuilder.class)) {
            content.add(builder);
        }

        for (Query query : Lookup.getDefault().lookupAll(Query.class)) {
            content.add(query);
        }

        for (CategoryBuilder catBuilder : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            content.add(catBuilder);
        }

        content.add(new HierarchicalGraphMask());
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void addBuilder(FilterBuilder builder) {
        content.add(builder);
    }

    public void removeBuilder(FilterBuilder builder) {
        content.remove(builder);
    }

    public void registerMask(FilterLibraryMask mask) {
        content.add(mask);
    }

    public void unregisterMask(FilterLibraryMask mask) {
        content.remove(mask);
    }

    public FilterBuilder getBuilder(Filter filter) {
        for (FilterBuilder builder : lookup.lookupAll(FilterBuilder.class)) {
            try {
                if (builder.getFilter().getClass() == filter.getClass()) {
                    return builder;
                }
            } catch (Exception e) {
            }
        }
        for (CategoryBuilder catBuilder : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            for (FilterBuilder builder : catBuilder.getBuilders()) {
                try {
                    if (builder.getFilter().getClass() == filter.getClass()) {
                        return builder;
                    }
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public void saveQuery(Query query) {
        content.add(query);
    }

    private static class HierarchicalGraphMask implements FilterLibraryMask {

        public Category getCategory() {
            return FilterLibrary.HIERARCHY;
        }

        public boolean isValid() {
            return true;
        }
    }
}
