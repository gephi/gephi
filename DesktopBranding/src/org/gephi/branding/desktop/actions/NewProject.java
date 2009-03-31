/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Projects;
import org.openide.util.Lookup;

public final class NewProject implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        //Projects p = Lookup.lookup(Projects.class);
        //System.out.println(p.getProjects().get(0).toString());
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
    }
}
