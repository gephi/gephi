package org.gephi.branding.desktop;

import java.awt.AWTEvent;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * Toolbar buttons are focusable to be reachable by keyboard, but the focus ring is only painted when the focus comes
 * from the keyboard, not after a mouse click.
 */
final class ToolbarFocusIndicator {

    private static boolean mouseInput = false;

    private ToolbarFocusIndicator() {
    }

    static void install() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                mouseInput = true;
            } else if (event.getID() == KeyEvent.KEY_PRESSED) {
                mouseInput = false;
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", evt -> {
            if (evt.getNewValue() instanceof AbstractButton button
                && SwingUtilities.getAncestorOfClass(JToolBar.class, button) != null) {
                button.setFocusPainted(!mouseInput);
            }
        });
    }
}
