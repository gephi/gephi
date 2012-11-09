package org.gephi.manageneighbors.apiimpl.contextmenuitems;

import javax.swing.Icon;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.manageneighbors.api.GraphElementsController;
import org.gephi.manageneighbors.apiimpl.GraphElementsControllerImpl;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jozef Barčák - barcak.jozef@gmail.com - 2012
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class ShowConnectedLeaves extends BasicItem {
    
    private GraphElementsController gec;

    @Override
    public void setup(HierarchicalGraph graph, Node[] nodes) {
        super.setup(graph, nodes);
        
        gec = new GraphElementsControllerImpl();
    }
    
    @Override
    public void execute() {
        gec.showNodes(nodes);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ShowConnectedLeaves.class, "GraphContextMenu_ShowConnectedLeaves");
    }

    @Override
    public boolean canExecute() {
        for (Node node : nodes) {
            if (gec.canShowNode(node)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getType() {
        return 150;
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/manageneighbors/api/resources/show-connected-leaves.png", false);
    }
    
}
