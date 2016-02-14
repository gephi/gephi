/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.datalab.tables.popup;

import java.awt.Point;
import java.util.List;
import javax.swing.JPopupMenu;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.desktop.datalab.tables.AbstractElementsDataTable;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Node;

/**
 *
 * @author Eduardo Ramos
 */
public class NodesPopupAdapter extends AbstractPopupAdapter<Node> {

    public NodesPopupAdapter(AbstractElementsDataTable<Node> elementsDataTable) {
        super(elementsDataTable);
    }

    @Override
    protected JPopupMenu createPopup(Point p) {
        final List<Node> selectedElements = elementsDataTable.getElementsFromSelectedRows();
        final Node clickedElement = elementsDataTable.getElementFromRow(table.rowAtPoint(p));
        JPopupMenu contextMenu = new JPopupMenu();

        //First add edges manipulators items:
        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
        Integer lastManipulatorType = null;
        for (NodesManipulator em : dlh.getNodesManipulators()) {
            em.setup(selectedElements.toArray(new Node[0]), clickedElement);
            if (lastManipulatorType == null) {
                lastManipulatorType = em.getType();
            }
            if (lastManipulatorType != em.getType()) {
                contextMenu.addSeparator();
            }
            lastManipulatorType = em.getType();
            if (em.isAvailable()) {
                contextMenu.add(PopupMenuUtils.createMenuItemFromNodesManipulator(em, clickedElement, selectedElements.toArray(new Node[0])));
            }
        }

        //Add AttributeValues manipulators submenu:
        Column column = elementsDataTable.getColumnAtIndex(table.columnAtPoint(p));
        if (column != null) {
            contextMenu.add(PopupMenuUtils.createSubMenuFromRowColumn(clickedElement, column));
        }
        return contextMenu;
    }
}
