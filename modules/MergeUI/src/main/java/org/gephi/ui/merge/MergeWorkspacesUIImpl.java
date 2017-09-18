/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.merge;

/**
 *
 * @author Alex Puig
 */
import javax.swing.JPanel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.MergeWorkspacesUI;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = MergeWorkspacesUI.class)
public class MergeWorkspacesUIImpl implements MergeWorkspacesUI{
    
    private MergeWorkspacesEditor panel;
    
    @Override
    public JPanel getPanel(){
        panel = new MergeWorkspacesEditor();
        return panel;
    }
    
    @Override
    public void mergeWorkspaces(JPanel panel){
        Workspace toMerge = ((MergeWorkspacesEditor) panel).getSelectedWorkspace();
        Workspace currentWorkspace = ((MergeWorkspacesEditor) panel).getCurrentWorkspace();
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        
        GraphModel currentGraphModel = graphController.getGraphModel(currentWorkspace);
        GraphModel toMergeGraphModel = graphController.getGraphModel(toMerge);

        Graph toMergeGraph = toMergeGraphModel.getGraph();
        
        currentGraphModel.bridge().copyNodes(toMergeGraph.getNodes().toArray());
    }
}
