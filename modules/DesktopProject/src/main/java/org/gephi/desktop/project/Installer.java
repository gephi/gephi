package org.gephi.desktop.project;

import javax.swing.JOptionPane;
import org.gephi.project.api.ProjectController;
import org.openide.awt.Actions;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        ProjectControllerUIImpl projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUIImpl.class);
        projectControllerUI.loadProjects();
    }

    @Override
    public boolean closing() {
        ProjectControllerUIImpl projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUIImpl.class);

        if (Lookup.getDefault().lookup(ProjectController.class).getCurrentProject() == null) {
            //Close directly if no project open
            projectControllerUI.saveProjects();
            return true;
        }
        boolean res = projectControllerUI.closeCurrentProject();
        if (res) {
            projectControllerUI.saveProjects();
        }
        return res;
    }
}
