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

import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
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

    public void setup(ExplorerManager manager, FilterModel model, FilterUIModel uiModel) {
        this.manager = manager;

        this.uiModel = uiModel;
        if (model != null) {
            this.filterLibrary = model.getLibrary();
            manager.setRootContext(new CategoryNode(new Utils(), null));
        } else {
            this.filterLibrary = null;
            manager.setRootContext(new AbstractNode(Children.LEAF) {

                @Override
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            });
        }
        updateEnabled(model != null);
    }

    protected class Utils implements LookupListener {

        private Lookup.Result<FilterBuilder> lookupResult;
        private Lookup.Result<Query> lookupResult2;

        public Utils() {
            lookupResult = filterLibrary.getLookup().lookupResult(FilterBuilder.class);
            lookupResult.addLookupListener(this);
            lookupResult2 = filterLibrary.getLookup().lookupResult(Query.class);
            lookupResult2.addLookupListener(this);
        }

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
            Set<Object> cats = new HashSet<Object>();

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
                        for (FilterBuilder fb : cb.getBuilders()) {
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

    private void updateEnabled(boolean enabled) {
        setRootVisible(enabled);
        setEnabled(enabled);
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
