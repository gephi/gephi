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
package org.gephi.filters.api;

import org.gephi.filters.spi.FilterLibraryMask;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public interface FilterLibrary extends Lookup.Provider {

    public final static Category TOPOLOGY = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Topology"),
            ImageUtilities.loadImageIcon("filtersui/api/resources/folder.png", false),
            null);
    public final static Category ATTRIBUTES = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Attributes"),
            ImageUtilities.loadImageIcon("filtersui/api/resources/folder.png", false),
            null);
    public final static Category HIERARCHY = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Hierarchy"),
            ImageUtilities.loadImageIcon("filtersui/api/resources/folder.png", false),
            TOPOLOGY);

    public void addBuilder(FilterBuilder instance);

    public void removeBuilder(FilterBuilder instance);

    public Lookup getLookup();

    public void registerMask(FilterLibraryMask mask);

    public void unregisterMask(FilterLibraryMask mask);

    public FilterBuilder getBuilder(Filter filter);

    public void saveQuery(Query query);
}
