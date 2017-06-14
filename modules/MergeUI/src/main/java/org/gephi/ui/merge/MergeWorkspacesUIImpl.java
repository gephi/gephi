/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.merge;

/**
 *
 * @author root
 */
import javax.swing.JPanel;
import org.gephi.project.api.Project;
import org.gephi.project.spi.MergeWorkspacesUI;
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
    public void setup(Project project){
        panel.load(project);
    }
    
    @Override
    public void unsetup(Project project){}
}
