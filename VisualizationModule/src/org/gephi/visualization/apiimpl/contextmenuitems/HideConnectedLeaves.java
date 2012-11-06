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
public class HideConnectedLeaves extends BasicItem {

    private GraphElementsController gec;
    
    @Override
    public void setup(HierarchicalGraph graph, Node[] nodes) {
        super.setup(graph, nodes);
        
        gec = Lookup.getDefault().lookup(GraphElementsController.class);
    }
    
    public void execute() {
        gec.hideLeaves(nodes);
    }

    public String getName() {
        return NbBundle.getMessage(SelectConnectedNodes.class, "GraphContextMenu_HideConnectedLeaves");
    }

    public boolean canExecute() {
        return gec.canHideNodes(nodes);
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/hide-connected-leaves.png", false);
    }
    
}
