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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.Action;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterBuilderNode extends AbstractNode {

    private FilterBuilder filterBuilder;
    private FilterTransferable transferable;

    public FilterBuilderNode(FilterBuilder filterBuilder) {
        super(Children.LEAF);
        this.filterBuilder = filterBuilder;
        setName(filterBuilder.getName());
        transferable = new FilterTransferable();
    }

    @Override
    public Image getIcon(int type) {
        try {
            if (filterBuilder.getIcon() != null) {
                return ImageUtilities.icon2Image(filterBuilder.getIcon());
            }
        } catch (Exception e) {
        }
        return ImageUtilities.loadImage("filtersui/desktop/library/resources/filter.png");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action getPreferredAction() {
        return FilterBuilderNodeDefaultAction.instance;
    }

    public FilterBuilder getBuilder() {
        return filterBuilder;
    }

    @Override
    public Transferable drag() throws IOException {
        return transferable;
    }
    public static final DataFlavor DATA_FLAVOR = new DataFlavor(FilterBuilder.class, "filterbuilder");

    private class FilterTransferable implements Transferable {

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DATA_FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == DATA_FLAVOR;

        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor == DATA_FLAVOR) {
                return filterBuilder;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }
}
