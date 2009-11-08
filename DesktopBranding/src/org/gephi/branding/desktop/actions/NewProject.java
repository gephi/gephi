/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

public final class NewProject implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
    }
}
