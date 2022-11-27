package org.gephi.desktop.search.popup;

import java.awt.Component;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPopupMenu;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.plugin.manipulators.nodes.CopyNodeDataToOtherNodes;
import org.gephi.datalab.plugin.manipulators.nodes.LinkNodes;
import org.gephi.datalab.plugin.manipulators.nodes.MergeNodes;
import org.gephi.datalab.plugin.manipulators.nodes.SelectEdgesOnTable;
import org.gephi.datalab.plugin.manipulators.nodes.SelectNeighboursOnTable;
import org.gephi.datalab.plugin.manipulators.nodes.SelectOnGraph;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.desktop.search.SearchDialog;
import org.gephi.graph.api.Node;

public class NodePopup {

    protected static final Set<Class<? extends Manipulator>> excludedManipulators = Set.copyOf(List.of(
        MergeNodes.class,
        LinkNodes.class,
        CopyNodeDataToOtherNodes.class));

    protected static final Set<Class<? extends Manipulator>> graphManipulators = Set.copyOf(List.of(
        SelectOnGraph.class));

    protected static final Set<Class<? extends Manipulator>> datalabManipulators = Set.copyOf(List.of(
        SelectNeighboursOnTable.class,
        SelectEdgesOnTable.class));

    protected static JPopupMenu createPopup(Node selectedElement) {
        boolean graphOpened = SearchDialog.isGraphOpened();
        boolean datalabOpened = SearchDialog.isDataLabOpened();

        JPopupMenu contextMenu = new JPopupMenu();

        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
        Integer lastManipulatorType = null;
        for (NodesManipulator em : dlh.getNodesManipulators()) {
            if (!excludedManipulators.contains(em.getClass())) {
                if (!graphOpened && graphManipulators.contains(em.getClass())) {
                    continue;
                }
                if (!datalabOpened && datalabManipulators.contains(em.getClass())) {
                    continue;
                }
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
        }

        return contextMenu;
    }
}
