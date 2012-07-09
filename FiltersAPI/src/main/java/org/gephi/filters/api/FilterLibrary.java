/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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

    /**
     * Delete a saved <code>query</code> from the library. Deleted
     * queries are deleted from the project.
     * @param query         the query that is to be deleted
     */
    public void deleteQuery(Query query);
}
