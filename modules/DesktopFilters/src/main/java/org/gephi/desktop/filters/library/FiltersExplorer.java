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
package org.gephi.desktop.filters.library;

import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.gephi.desktop.filters.FilterUIModel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterLibraryMask;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class FiltersExplorer extends BeanTreeView {

    private ExplorerManager manager;
    private FilterLibrary filterLibrary;
    private FilterUIModel uiModel;

    public FiltersExplorer() {
    }

    public void setup(final ExplorerManager manager, FilterModel model, FilterUIModel uiModel) {
        this.manager = manager;

        this.uiModel = uiModel;
        if (model != null) {
            this.filterLibrary = model.getLibrary();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    manager.setRootContext(new CategoryNode(new Utils(), null));
                }
            });
        } else {
            this.filterLibrary = null;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    manager.setRootContext(new AbstractNode(Children.LEAF) {

                        @Override
                        public Action[] getActions(boolean context) {
                            return new Action[0];
                        }
                    });
                }
            });
        }
        updateEnabled(model != null);
    }

    protected class Utils implements LookupListener {

        private final Lookup.Result<FilterBuilder> lookupResult;
        private final Lookup.Result<Query> lookupResult2;

        public Utils() {
            lookupResult = filterLibrary.getLookup().lookupResult(FilterBuilder.class);
            lookupResult.addLookupListener(this);
            lookupResult2 = filterLibrary.getLookup().lookupResult(Query.class);
            lookupResult2.addLookupListener(this);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            saveExpandStatus((CategoryNode) manager.getRootContext());
            manager.setRootContext(new CategoryNode(this, null));
            loadExpandStatus((CategoryNode) manager.getRootContext());
        }

        public boolean isLeaf(Category category) {
            if (category == null) {
                return false;
            }
            if (category.equals(QUERIES)) {
                return filterLibrary.getLookup().lookupAll(Query.class).isEmpty();
            }
            for (FilterBuilder fb : filterLibrary.getLookup().lookupAll(FilterBuilder.class)) {
                if (fb.getCategory() == null && category.equals(UNSORTED)) {
                    return false;
                }
                if (fb.getCategory() != null && fb.getCategory().getParent() != null && fb.getCategory().getParent().equals(category)) {
                    return false;
                }
                if (fb.getCategory() != null && fb.getCategory().equals(category)) {
                    return false;
                }
            }
            for (CategoryBuilder cb : filterLibrary.getLookup().lookupAll(CategoryBuilder.class)) {
                if (cb.getCategory().equals(category)) {
                    return false;
                }
                if (cb.getCategory().getParent() != null && cb.getCategory().getParent().equals(category)) {
                    return false;
                }
            }
            return true;
        }

        public Object[] getChildren(Category category) {
            Set<Object> cats = new HashSet<>();

            if (category != null && category.equals(QUERIES)) {
                for (Query q : filterLibrary.getLookup().lookupAll(Query.class)) {
                    cats.add(q);
                }
            } else {
                if (category == null) {
                    cats.add(QUERIES);
                }
                //get categories from filter builders
                for (FilterBuilder fb : filterLibrary.getLookup().lookupAll(FilterBuilder.class)) {
                    if (fb.getCategory() == null) {
                        if (category == null) {
                            cats.add(UNSORTED);
                        } else if (category.equals(UNSORTED)) {
                            cats.add(fb);
                        }
                    } else if (fb.getCategory().getParent() == category) {
                        if (isValid(fb.getCategory())) {
                            cats.add(fb.getCategory());
                        }
                    } else if (fb.getCategory().getParent() != null && fb.getCategory().getParent().equals(category)) {
                        if (isValid(fb.getCategory())) {
                            cats.add(fb.getCategory());
                        }
                    } else if (fb.getCategory().equals(category)) {
                        cats.add(fb);
                    }
                }
                //get categories from cat builders
                for (CategoryBuilder cb : filterLibrary.getLookup().lookupAll(CategoryBuilder.class)) {
                    if (cb.getCategory().getParent() == category) {
                        cats.add(cb.getCategory());
                    } else if (cb.getCategory().getParent() != null && cb.getCategory().getParent().getParent() == category) {
                        cats.add(cb.getCategory().getParent());
                    } else if (cb.getCategory() == category) {
                        for (FilterBuilder fb : cb.getBuilders(uiModel.getWorkspace())) {
                            cats.add(fb);
                        }
                    }
                }
            }
            return cats.toArray();
        }

        public boolean isValid(Category category) {
            for (FilterLibraryMask mask : filterLibrary.getLookup().lookupAll(FilterLibraryMask.class)) {
                if (mask.getCategory().equals(category)) {
                    return mask.isValid();
                }
            }
            return true;
        }
    }
    private final Category UNSORTED = new Category(
            NbBundle.getMessage(FiltersExplorer.class, "FiltersExplorer.UnsortedCategory"),
            null,
            null);
    public static final Category QUERIES = new Category(
            NbBundle.getMessage(FiltersExplorer.class, "FiltersExplorer.Queries"),
            null,
            null);

    private void updateEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setRootVisible(enabled);
                setEnabled(enabled);
            }
        });
    }

    private void loadExpandStatus(CategoryNode node) {
        if (uiModel == null) {
            return;
        }
        if (uiModel.isExpanded(node.getCategory())) {
            expandNode(node);
        }
        for (Node n : node.getChildren().getNodes()) {
            if (n instanceof CategoryNode) {
                loadExpandStatus((CategoryNode) n);
            }
        }
    }

    private void saveExpandStatus(CategoryNode node) {
        if (uiModel == null) {
            return;
        }
        uiModel.setExpand(node.getCategory(), isExpanded(node));
        for (Node n : node.getChildren().getNodes()) {
            if (n instanceof CategoryNode) {
                saveExpandStatus((CategoryNode) n);
            }
        }
    }
}
