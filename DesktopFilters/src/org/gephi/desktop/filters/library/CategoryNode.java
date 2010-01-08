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

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import javax.swing.Action;
import org.gephi.desktop.filters.query.QueryNode;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Mathieu Bastian
 */
public class CategoryNode extends AbstractNode {

    private Category category;

    public CategoryNode(FiltersExplorer.Utils utils, Category category) {
        super(utils.isLeaf(category) ? Children.LEAF : Children.create(new CategoryChildFactory(utils, category), true));
        this.category = category;
        if (category != null) {
            setName(category.getName());
        } else {
            setName(NbBundle.getMessage(CategoryNode.class, "RootNode.name"));
        }
    }

    @Override
    public Image getIcon(int type) {
        try {
            if (category.getIcon() != null) {
                return ImageUtilities.icon2Image(category.getIcon());
            }
        } catch (Exception e) {
        }
        if (category == null) {
            return ImageUtilities.loadImage("org/gephi/desktop/filters/library/resources/library.png");
        } else {
            return ImageUtilities.loadImage("org/gephi/desktop/filters/library/resources/folder.png");
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public PasteType getDropType(final Transferable t, int action, int index) {
        if (category == null || !category.equals(FiltersExplorer.QUERIES)) {
            return null;
        }
        final Node dropNode = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE);
        if (dropNode != null && dropNode instanceof QueryNode) {
            return new PasteType() {

                @Override
                public Transferable paste() throws IOException {
                    QueryNode queryNode = (QueryNode) dropNode;
                    FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                    FilterLibrary library = filterController.getModel().getLibrary();
                    library.saveQuery(queryNode.qetQuery());
                    return null;
                }
            };

        }
        return null;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }
}
