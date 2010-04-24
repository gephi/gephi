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
package org.gephi.filters.spi;

import org.gephi.filters.api.FilterLibrary;

/**
 * Classes that implements this interface can be registered to the filter
 * library to programmatically enable or disable categories, i.e. filters
 * container. That is useful for instance to disable filters working on undirected
 * graphs if the current graph is directed.
 * <p>
 * When registered, masks are asked whether the category is valid.
 *
 * @author Mathieu Bastian
 * @see FilterLibrary
 */
public interface FilterLibraryMask {

    /**
     * Returns the <code>Category</code> this masks is associated.
     * @return      the <code>Category</code> this filter is describing
     */
    public Category getCategory();

    /**
     * Returns <code>true</code> if this masks's category is valid.
     * @return      <code>true</code> if the category is valid, <code>false</code>
     * otherwise
     */
    public boolean isValid();
}
