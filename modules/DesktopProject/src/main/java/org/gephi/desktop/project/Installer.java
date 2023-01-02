package org.gephi.desktop.project;

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
        projectControllerUI.saveProjects();
        return true;
    }
}
