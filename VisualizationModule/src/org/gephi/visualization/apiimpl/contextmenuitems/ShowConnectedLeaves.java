package org.gephi.visualization.apiimpl.contextmenuitems;

import javax.swing.Icon;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
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
        
        gec = Lookup.getDefault().lookup(GraphElementsController.class);
    }
    
    public void execute() {
        gec.showNodes(nodes);
    }

    public String getName() {
        return NbBundle.getMessage(SelectConnectedNodes.class, "GraphContextMenu_ShowConnectedLeaves");
    }

    public boolean canExecute() {
        for (Node node : nodes) {
            if (gec.canShowNode(node)) {
                return true;
            }
        }
        return false;
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/show-connected-leaves.png", false);
    }
    
}
