package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.ui.merge.ProjectPropertiesEditor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public class MergeWorkspaces extends SystemAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectPropertiesEditor pe;
        Lookup.getDefault().lookup(ProjectControllerUI.class).mergeWorkspaces();
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUI.class).canDuplicateWorkspace();
    }

    @Override
    protected String iconResource() {
        return "org/gephi/branding/desktop/actions/resources/mergeWorkspaces.png";
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MergeWorkspaces.class, "CTL_MergeWorkspaces");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}