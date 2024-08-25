package org.gephi.desktop.project.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.gephi.desktop.project.ProjectControllerUIImpl;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.project.actions.DuplicateWorkspace", category = "Workspace")
@ActionRegistration(displayName = "#CTL_DuplicateWorkspace", lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/Workspace", position = 2700)
})
public final class DuplicateWorkspace extends AbstractAction {

    DuplicateWorkspace() {
        super(NbBundle.getMessage(DuplicateWorkspace.class, "CTL_DuplicateWorkspace"),
            ImageUtilities.loadImageIcon("DesktopProject/duplicateWorkspace.svg", false));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isEnabled()) {
            Lookup.getDefault().lookup(ProjectControllerUIImpl.class).duplicateWorkspace();
        }
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUIImpl.class).canDuplicateWorkspace();
    }
}