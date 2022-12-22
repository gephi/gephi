package org.gephi.desktop.search;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.gephi.project.api.ProjectController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
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
        super(NbBundle.getMessage(SearchAction.class, "CTL_Search"),
            ImageUtilities.loadImageIcon("DesktopSearch/search.png", false));

        uiModel = new SearchUIModel();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (isEnabled()) {
            SearchDialog panel = new SearchDialog(uiModel);
            JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(SearchAction.class, "SearchDialog.title"), false);

            // Close behavior
            dialog.getRootPane().registerKeyboardAction(e -> {
                closeDialog(panel, dialog);
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            dialog.addWindowFocusListener(new WindowAdapter() {

                @Override
                public void windowLostFocus(WindowEvent e) {
                    closeDialog(panel, dialog);
                }
            });

            // Drag behavior
            panel.instrumentDragListener(dialog);

            // Show dialog
            dialog.setUndecorated(true);
            dialog.getContentPane().add(panel);
            dialog.setBounds(212, 237, 679, 378);
            dialog.setVisible(true);
        }
    }

    private void closeDialog(SearchDialog panel, JDialog dialog) {
        SwingUtilities.invokeLater(() -> {
            panel.unsetup();
            dialog.dispose();
        });
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectController.class).hasCurrentProject();
    }
}

