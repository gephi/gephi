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
package org.gephi.filters.api;

import org.gephi.filters.spi.FilterLibraryMask;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The Filter Library is the place where filter builders are registered and
 * ready to be used. It also has default <b>Categories</b> that filters use to be
 * sorted and well-described.
 * <p>
 * Modules can dynamically create new filter builders and serve it ot users
 * by using {@link #addBuilder(org.gephi.filters.spi.FilterBuilder) }.
 * @author Mathieu Bastian
 */
public interface FilterLibrary extends Lookup.Provider {

    /**
     * Default <code>Category</code> for topological filters. Use this category
     * for filters working on graph topology, i.e. the structure of nodes and
     * edges.
     */
    public final static Category TOPOLOGY = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Topology"),
            null,
            null);

    /**
     * Default <code>Category</code> for attributes filters. Use this category
     * for filters working on attribute values.
     */
    public final static Category ATTRIBUTES = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Attributes"),
            null,
            null);

    /**
     * Default <code>Category</code> for filters working on the graph hierarchy.
     */
    public final static Category HIERARCHY = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Hierarchy"),
            null,
            TOPOLOGY);

    /**
     * Default <code>Category</code> for filters working on edges only.
     */
    public final static Category EDGE = new Category(
            NbBundle.getMessage(FilterLibrary.class, "FiltersLibrary.Category.Edge"),
            null,
            null);

    /**
     * Adds <code>builder</code> to this library.
     * @param builder       the builder that is to be added
     */
    public void addBuilder(FilterBuilder builder);

    /**
     * Removes <code>builder</code> from this library.
     * @param builder       the builder that is to be removed
     */
    public void removeBuilder(FilterBuilder builder);

    /**
     * Returns this library's lookup. The lookup is a general container for
     * objects and contains:
     * <ul><li>{@link FilterBuilder}: Builders, these are building filters.</li>
     * <li>{@link CategoryBuilder}: Category builders, these are building.
     * categories, i.e. filters containers.</li>
     * <li>{@link FilterLibraryMask}: Masks, for enable/disable categories according
     * to the context.</li>
     * <li>{@link Query}: Saved queries, look at <code>FilterController</code> for
     * active queries.</li></ul>
     * The lists of all <code>FilterBuilder</code> in the library can be obtained
     * by doing the following command:
     * <pre>
     * FilterLibrary.getLookup().lookupAll(FilterBuilder.class);
     * </pre>
     * @return              the lookup container of this library
     */
    public Lookup getLookup();

    /**
     * Registers <code>mask</code> as a new <code>FilterLibraryMask</code>. Such
     * masks have categories enable/disable flag. Useful to disable for instance
     * filters for undirected graphs when the current graph is directed.
     * @param mask          the mask that is to be registered
     */
    public void registerMask(FilterLibraryMask mask);

    /**
     * Unregisters <code>mask</code> in the library. The mask will no longer be
     * used.
     * @param mask          the mask that is to be unregistered
     */
    public void unregisterMask(FilterLibraryMask mask);

    /**
     * Returns the builder that has created <code>filter</code>.
     * @param filter        the filter that the builder is to be returned
     * @return              the builder that has created <code>filter</code>
     */
    public FilterBuilder getBuilder(Filter filter);

    /**
     * Save <code>query</code> in the library in order it can be reused. Saved
     * queries are saved to the project.
     * @param query         the query that is to be saved
     */
    public void saveQuery(Query query);
}
