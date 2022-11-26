package org.gephi.desktop.search.popup;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JPopupMenu;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.graph.api.Node;

public class NodePopup {

    protected static JPopupMenu createPopup(Node selectedElement) {
        JPopupMenu contextMenu = new JPopupMenu();

        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
        Integer lastManipulatorType = null;
        for (NodesManipulator em : dlh.getNodesManipulators()) {
            em.setup(new Node[]{selectedElement}, selectedElement);
            if (lastManipulatorType == null) {
                lastManipulatorType = em.getType();
            }
            if (lastManipulatorType != em.getType()) {
                contextMenu.addSeparator();
            }
            lastManipulatorType = em.getType();
            if (em.isAvailable()) {
                contextMenu.add(PopupMenuUtils
                    .createMenuItemFromNodesManipulator(em, selectedElement, new Node[]{selectedElement}));
            }
        }

        return contextMenu;
    }
}
