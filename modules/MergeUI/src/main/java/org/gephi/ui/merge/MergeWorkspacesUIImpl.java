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
    
    public JPanel getPanel(){
        return null;
    }

    public void setup(Project project){}

    public void unsetup(Project project){}
}
