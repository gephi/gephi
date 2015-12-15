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
        if (filterBuilder.getDescription() != null) {
            setShortDescription(filterBuilder.getDescription());
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return super.getName();
    }

    @Override
    public Image getIcon(int type) {
        try {
            if (filterBuilder.getIcon() != null) {
                return ImageUtilities.icon2Image(filterBuilder.getIcon());
            }
        } catch (Exception e) {
        }
        return ImageUtilities.loadImage("org/gephi/desktop/filters/library/resources/filter.png");
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

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DATA_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == DATA_FLAVOR;

        }

        @Override
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
