package org.gephi.manageneighbors.apiimpl.contextmenuitems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.contextmenuitems.BasicItem;
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
public class SelectConnectedNodes extends BasicItem {

    @Override
    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        
        Set<Node> allNodes = new HashSet<Node>();
        
        allNodes.addAll(Arrays.asList(nodes));
        
        for (Node node : nodes) {
            allNodes.addAll(Arrays.asList(gec.getNodeNeighbours(node)));
        }
        
        VizController.getInstance().selectNodes(allNodes.toArray(new Node[0]));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SelectConnectedNodes.class, "GraphContextMenu_SelectConnectedNodes");
    }

    @Override
    public boolean canExecute() {
        return nodes.length > 0;
    }

    @Override
    public int getType() {
        return 160;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/manageneighbors/api/resources/select-connected-leaves.png", false);
    }
    
}
