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

/**
 * Category builder is a convenient way to define multiple builders from a single
 * source and grouped in a single category.
 * <p>
 * Implement <code>CategoryBuilder</code>
 * for instance for creating a set of filter builders working on attributes, with
 * one <code>FilterBuilder</code> per attribute column.
 * <p>
 * Note that filter builders returned by category builders don't have to be
 * registered on they own, once here is enough.
 *
 * @author Mathieu Bastian
 * @see FilterBuilder
 */
public interface CategoryBuilder {

    /**
     * Returns the filter builders this category builder is building.
     * @return  the builders this category builder is building
     */
    public FilterBuilder[] getBuilders();

    /**
     * Returns the category builders are to be grouped in. It can't be a
     * default category.
     * @return  the category builders belong to
     */
    public Category getCategory();
}
