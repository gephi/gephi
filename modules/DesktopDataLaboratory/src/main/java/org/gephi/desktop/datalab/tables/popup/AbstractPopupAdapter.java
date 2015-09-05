/*
 Copyright 2008-2015 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos <eduramiba@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2015 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2015 Gephi Consortium.
 */
package org.gephi.desktop.datalab.tables.popup;

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.gephi.desktop.datalab.tables.AbstractElementsDataTable;
import org.gephi.graph.api.Element;
import org.jdesktop.swingx.JXTable;
import org.openide.awt.MouseUtils;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractPopupAdapter<T extends Element> extends MouseUtils.PopupMouseAdapter {

    protected final AbstractElementsDataTable<T> elementsDataTable;
    protected final JXTable table;

    public AbstractPopupAdapter(AbstractElementsDataTable<T> elementsDataTable) {
        super();
        this.elementsDataTable = elementsDataTable;
        this.table = elementsDataTable.getTable();
    }

    @Override
    protected void showPopup(final MouseEvent e) {
        int selRow = table.rowAtPoint(e.getPoint());

        if (selRow != -1) {
            if (!table.getSelectionModel().isSelectedIndex(selRow)) {
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().setSelectionInterval(selRow, selRow);
            }
            final Point p = e.getPoint();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final JPopupMenu pop = createPopup(p);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            showPopup(p.x, p.y, pop);
                        }
                    });
                }
            }).start();
        } else {
            table.getSelectionModel().clearSelection();
        }
        e.consume();

    }

    private void showPopup(int xpos, int ypos, final JPopupMenu popup) {
        if ((popup != null) && (popup.getSubElements().length > 0)) {
            final PopupMenuListener p = new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popup.removePopupMenuListener(this);
                    table.requestFocus();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            };
            popup.addPopupMenuListener(p);
            popup.show(table, xpos, ypos);
        }
    }
    
    protected abstract JPopupMenu createPopup(Point p);
}
