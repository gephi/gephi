package org.gephi.desktop.search.popup;

import java.util.List;
import java.util.Set;
import javax.swing.JPopupMenu;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.plugin.manipulators.edges.CopyEdgeDataToOtherEdges;
import org.gephi.datalab.plugin.manipulators.edges.SelectNodesOnTable;
import org.gephi.datalab.plugin.manipulators.edges.SelectOnGraph;
import org.gephi.datalab.plugin.manipulators.edges.SelectSourceOnGraph;
import org.gephi.datalab.plugin.manipulators.edges.SelectTargetOnGraph;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.desktop.search.SearchDialog;
import org.gephi.graph.api.Edge;

public class EdgePopup {

    protected static final Set<Class<? extends Manipulator>> excludedManipulators = Set.copyOf(List.of(
        CopyEdgeDataToOtherEdges.class));

    protected static final Set<Class<? extends Manipulator>> graphManipulators = Set.copyOf(List.of(
        SelectOnGraph.class,
        SelectSourceOnGraph.class,
        SelectTargetOnGraph.class));

    protected static final Set<Class<? extends Manipulator>> datalabManipulators = Set.copyOf(List.of(
        SelectNodesOnTable.class));

    protected static JPopupMenu createPopup(Edge selectedElement) {
        boolean graphOpened = SearchDialog.isGraphOpened();
        boolean datalabOpened = SearchDialog.isDataLabOpened();

        JPopupMenu contextMenu = new JPopupMenu();

        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
        Integer lastManipulatorType = null;
        for (EdgesManipulator em : dlh.getEdgesManipulators()) {
            if (!excludedManipulators.contains(em.getClass())) {
                if (!graphOpened && graphManipulators.contains(em.getClass())) {
                    continue;
                }
                if (!datalabOpened && datalabManipulators.contains(em.getClass())) {
                    continue;
                }
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
        }

        return contextMenu;
    }
}
