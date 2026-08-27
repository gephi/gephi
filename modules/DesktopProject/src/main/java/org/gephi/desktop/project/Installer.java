package org.gephi.desktop.project;

import javax.swing.SwingUtilities;
import org.gephi.project.api.ProjectController;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        ProjectControllerUIImpl projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUIImpl.class);
        projectControllerUI.loadProjects();
    }

    @Override
    public boolean closing() {
        ProjectControllerUIImpl projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUIImpl.class);

        if (!Lookup.getDefault().lookup(ProjectController.class).hasCurrentProject()) {
            //Close directly if no project open. This is also the second pass of the sequence below,
            //the project having been closed in the meantime.
            projectControllerUI.saveProjects();
            return true;
        }

        //The user is asked here, on the Event Dispatch Thread, but the save and close run on the
        //Project IO thread and this thread must not wait for them. This shutdown pass is therefore
        //vetoed and exit() is triggered again once the project is effectively closed, which then
        //takes the branch above. When the user cancels, or when the save fails, nothing more happens
        //and the application stays open.
        projectControllerUI.confirmAndCloseCurrentProject(() -> {
            //Persisted here too, and not only on the second pass, so the project list survives a
            //shutdown that another module ends up vetoing
            projectControllerUI.saveProjects();
            SwingUtilities.invokeLater(() -> LifecycleManager.getDefault().exit());
        });
        return false;
    }
}
