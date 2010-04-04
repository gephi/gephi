/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public final class NewProject extends SystemAction {

    public void actionPerformed(ActionEvent e) {
        Lookup.getDefault().lookup(ProjectControllerUI.class).newProject();
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUI.class).canNewProject();
    }

    @Override
    protected String iconResource() {
        return "org/gephi/branding/desktop/actions/resources/newProject.png";
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NewProject.class, "CTL_NewProject");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
