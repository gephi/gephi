package org.gephi.desktop.search.impl.actions;

import org.gephi.desktop.search.spi.SearchActionProvider;
import org.gephi.graph.api.Node;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchActionProvider.class, position = 100)
public class FindNodeOnGraphAction implements SearchActionProvider<Node> {

    @Override
    public boolean canAction(Node result) {
        return true;
    }

    @Override
    public void action(Node result) {
        VisualizationController visualisationController = Lookup.getDefault().lookup(VisualizationController.class);
        if (visualisationController != null) {
            visualisationController.centerOnNode(result);
        }
    }

    @Override
    public String getDescription(Node result) {
        return NbBundle.getMessage(FindNodeOnGraphAction.class, "FindNodeOnGraphAction.description", result.getId());
    }
}
