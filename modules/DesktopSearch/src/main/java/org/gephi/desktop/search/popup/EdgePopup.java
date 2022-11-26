package org.gephi.desktop.search.popup;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JPopupMenu;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.graph.api.Edge;

public class EdgePopup {

    protected static JPopupMenu createPopup(Edge selectedElement) {
        JPopupMenu contextMenu = new JPopupMenu();

        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
        Integer lastManipulatorType = null;
        for (EdgesManipulator em : dlh.getEdgesManipulators()) {
            em.setup(new Edge[] {selectedElement}, selectedElement);
            if (lastManipulatorType == null) {
                lastManipulatorType = em.getType();
            }
            if (lastManipulatorType != em.getType()) {
                contextMenu.addSeparator();
            }
            lastManipulatorType = em.getType();
            if (em.isAvailable()) {
                contextMenu.add(PopupMenuUtils
                    .createMenuItemFromEdgesManipulator(em, selectedElement, new Edge[] {selectedElement}));
            }
        }

        return contextMenu;
    }
}
