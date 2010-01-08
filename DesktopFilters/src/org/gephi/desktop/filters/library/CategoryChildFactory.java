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
package org.gephi.desktop.filters.library;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class CategoryChildFactory extends ChildFactory<Object> {

    private Category category;
    private FiltersExplorer.Utils utils;

    public CategoryChildFactory(FiltersExplorer.Utils utils, Category category) {
        this.utils = utils;
        this.category = category;
    }

    @Override
    protected boolean createKeys(List<Object> toPopulate) {
        Object[] children = utils.getChildren(category);
        Arrays.sort(children, new Comparator() {

            public int compare(Object o1, Object o2) {
                String s1;
                String s2;
                if (o1 == FiltersExplorer.QUERIES || o2 == FiltersExplorer.QUERIES) {
                    return o1 == FiltersExplorer.QUERIES ? 1 : -1;
                } else if (o1 instanceof Category && o2 instanceof Category) {
                    s1 = ((Category) o1).getName();
                    s2 = ((Category) o2).getName();
                    return s1.compareTo(s2);
                } else if (o1 instanceof FilterBuilder && o2 instanceof FilterBuilder) {
                    s1 = ((FilterBuilder) o1).getName();
                    s2 = ((FilterBuilder) o2).getName();
                    return s1.compareTo(s2);
                } else if (o1 instanceof Query && o2 instanceof Query) {
                    s1 = ((Query) o1).getName();
                    s2 = ((Query) o2).getName();
                    return s1.compareTo(s2);
                } else if (o1 instanceof Category) {
                    return -1;
                }
                return 1;
            }
        });
        toPopulate.addAll(Arrays.asList(children));
        return true;
    }

    @Override
    protected Node[] createNodesForKey(Object key) {
        if (key instanceof Category) {
            return new Node[]{new CategoryNode(utils, (Category) key)};
        } else if (key instanceof FilterBuilder) {
            return new Node[]{new FilterBuilderNode((FilterBuilder) key)};
        } else {
            return new Node[]{new SavedQueryNode((Query) key)};
        }
    }
}
