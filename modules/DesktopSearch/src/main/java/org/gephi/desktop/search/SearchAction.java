package org.gephi.desktop.search;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.gephi.project.api.ProjectController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

@ActionID(id = "org.gephi.desktop.search.actions.Search", category = "Tools")
@ActionRegistration(displayName = "#CTL_Search", lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 400),
    @ActionReference(path = "Shortcuts", name = "D-F")
})
public final class SearchAction extends AbstractAction {

    // Singleton so it persists when the dialog is closed
    private final SearchUIModel uiModel;

    SearchAction() {
        super(NbBundle.getMessage(SearchAction.class, "CTL_Search"));

        uiModel = new SearchUIModel();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (isEnabled()) {
            SearchDialog panel = new SearchDialog(uiModel);
            JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(SearchAction.class, "SearchDialog.title"), false);

            // Close behavior
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.getRootPane().registerKeyboardAction(e -> {
                dialog.dispose();
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            dialog.addWindowFocusListener(new WindowAdapter() {

                @Override
                public void windowLostFocus(WindowEvent e) {
                    dialog.dispose();
                }
            });

            dialog.setUndecorated(true);
            dialog.getContentPane().add(panel);
            dialog.setBounds(212, 237, 679, 378);
            dialog.setVisible(true);
        }
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectController.class).hasCurrentProject();
    }
}

