/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.filters;

import java.util.HashMap;
import java.util.Map;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterLibraryMask;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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
    private Map<Class<? extends Filter>, FilterBuilder> buildersMap;

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

    private void buildBuildersMap() {
        buildersMap = new HashMap<Class<? extends Filter>, FilterBuilder>();
        for (FilterBuilder builder : lookup.lookupAll(FilterBuilder.class)) {
            try {
                Filter f = builder.getFilter();
                buildersMap.put(f.getClass(), builder);
                builder.destroy(f);
            } catch (Exception e) {
            }
        }
        for (CategoryBuilder catBuilder : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            for (FilterBuilder builder : catBuilder.getBuilders()) {
                try {
                    Filter f = builder.getFilter();
                    buildersMap.put(f.getClass(), builder);
                    builder.destroy(f);
                } catch (Exception e) {
                }
            }
        }
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
        if (buildersMap == null) {
            buildBuildersMap();
        }
        if (buildersMap.get(filter.getClass()) != null) {
            return buildersMap.get(filter.getClass());
        }
        buildBuildersMap();
        if (buildersMap.get(filter.getClass()) != null) {
            return buildersMap.get(filter.getClass());
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
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            return graphModel.isHierarchical();
        }
    }
}
