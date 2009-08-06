/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.components;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.gephi.ui.utils.UIUtils;

/**
 *
 * @author Mathieu Bastian
 */
public class CloseButton extends JButton {

    public CloseButton(Action a) {
        super(a);
        init();
    }

    public CloseButton() {
        init();
    }

    private void init() {
        if (UIUtils.isGTKLookAndFeel()) {
            setIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/gtk_bigclose_enabled.png")));
            setRolloverIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/gtk_bigclose_rollover.png")));
            setPressedIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/gtk_bigclose_pressed.png")));
        } else if (UIUtils.isWindowsClassicLookAndFeel()) {
            setIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/win_bigclose_enabled.png")));
            setRolloverIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/win_bigclose_rollover.png")));
            setPressedIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/win_bigclose_pressed.png")));
        } else if (UIUtils.isWindowsXPLookAndFeel()) {
            setIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/xp_bigclose_enabled.png")));
            setRolloverIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/xp_bigclose_rollover.png")));
            setPressedIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/xp_bigclose_pressed.png")));
        } else if (UIUtils.isAquaLookAndFeel()) {
            setIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/mac_bigclose_enabled.png")));
            setRolloverIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/mac_bigclose_rollover.png")));
            setPressedIcon(new ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/mac_bigclose_pressed.png")));
        }

        setText("");
        setBorder(javax.swing.BorderFactory.createEmptyBorder());
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusable(false);
        setOpaque(false);
    }
}
