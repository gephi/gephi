package org.gephi.desktop.project.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.gephi.desktop.project.ProjectControllerUIImpl;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.project.actions.ManageProjects", category = "File")
@ActionRegistration(displayName = "#CTL_ManageProjects", lazy = false)
@ActionReference(path = "Menu/File", position = 490)
public final class ManageProjects extends AbstractAction {

    ManageProjects() {
        super(NbBundle.getMessage(ManageProjects.class, "CTL_ManageProjects"));
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUIImpl.class).canProjectProperties();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (isEnabled()) {
            Lookup.getDefault().lookup(ProjectControllerUIImpl.class).manageProjects();
        }
    }
}
