/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.visualization.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Lightweight wrapper around the heavyweight graph canvas that draws a focus ring in a margin the canvas does not
 * cover. The margin is always reserved, so toggling the ring never shifts the layout.
 */
class FocusRingPanel extends JPanel {

    static final int THICKNESS = 2;
    private static final double MIN_CONTRAST = 3.0;
    private static final Color FALLBACK_COLOR = new Color(0x2675BF);

    private boolean focused = false;

    FocusRingPanel(Component content) {
        super(new BorderLayout());
        add(content, BorderLayout.CENTER);
        updateBorder();
    }

    void setFocused(boolean focused) {
        if (this.focused != focused) {
            this.focused = focused;
            updateBorder();
        }
    }

    boolean isFocused() {
        return focused;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateBorder();
    }

    private void updateBorder() {
        final Border border;
        if (focused) {
            border = BorderFactory.createLineBorder(focusColor(getBackground()), THICKNESS);
        } else {
            border = BorderFactory.createEmptyBorder(THICKNESS, THICKNESS, THICKNESS, THICKNESS);
        }
        setBorder(border);
    }

    /**
     * Theme accent colour, pushed towards black or white until it stands out from the background.
     */
    static Color focusColor(Color background) {
        Color accent = firstColor("Component.accentColor", "Component.focusedBorderColor", "Component.focusColor");
        if (accent == null) {
            accent = FALLBACK_COLOR;
        }
        if (background == null) {
            return accent;
        }
        final Color target =
            contrast(Color.WHITE, background) >= contrast(Color.BLACK, background) ? Color.WHITE : Color.BLACK;
        Color color = accent;
        for (int step = 1; contrast(color, background) < MIN_CONTRAST && step <= 10; step++) {
            color = blend(accent, target, step / 10.0);
        }
        return color;
    }

    private static Color firstColor(String... keys) {
        for (String key : keys) {
            final Color color = UIManager.getColor(key);
            if (color != null) {
                return new Color(color.getRGB());
            }
        }
        return null;
    }

    private static Color blend(Color from, Color to, double ratio) {
        return new Color(
            (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * ratio),
            (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * ratio),
            (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * ratio));
    }

    private static double contrast(Color a, Color b) {
        final double la = luminance(a);
        final double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(Color color) {
        return 0.2126 * linear(color.getRed()) + 0.7152 * linear(color.getGreen()) + 0.0722 * linear(color.getBlue());
    }

    private static double linear(int channel) {
        final double value = channel / 255.0;
        return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }
}
