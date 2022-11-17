package org.gephi.desktop.search;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.gephi.project.api.ProjectController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.search.actions.Search", category = "Tools")
@ActionRegistration(displayName = "#CTL_Search", lazy = false)
@ActionReference(path = "Menu/Tools", position = 400)
public final class SearchAction extends AbstractAction {

    SearchAction() {
        super(NbBundle.getMessage(SearchAction.class, "CTL_Search"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isEnabled()) {

        }
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectController.class).hasCurrentProject();
    }
}

