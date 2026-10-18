package org.gephi.branding.desktop;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import javax.swing.JComponent;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingUtilities;
import org.openide.windows.TopComponent;

/**
 * NetBeans top components are not focusable, so activating one (for instance with Ctrl+Tab) leaves the keyboard focus
 * in the previously active one. Move the focus to the first focusable Swing component of the activated top component.
 */
final class TopComponentFocus {

    private static final LayoutFocusTraversalPolicy POLICY = new LayoutFocusTraversalPolicy() {
        @Override
        protected boolean accept(Component aComponent) {
            return aComponent instanceof JComponent && super.accept(aComponent);
        }
    };

    private TopComponentFocus() {
    }

    static void install() {
        TopComponent.getRegistry().addPropertyChangeListener(evt -> {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())
                && evt.getNewValue() instanceof TopComponent tc) {
                SwingUtilities.invokeLater(() -> focus(tc));
            }
        });
    }

    private static void focus(TopComponent tc) {
        if (!tc.isShowing() || TopComponent.getRegistry().getActivated() != tc) {
            return;
        }
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if (owner != null && SwingUtilities.isDescendingFrom(owner, tc)) {
            return;
        }
        Component first = POLICY.getFirstComponent(tc);
        if (first != null) {
            first.requestFocusInWindow();
        }
    }
}
