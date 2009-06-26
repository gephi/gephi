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
package org.gephi.ui.utils;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXBusyLabel;

/**
 *
 * @author Mathieu Bastian
 */
public class BusyUtils {

    public static BusyLabel createCenteredBusyLabel(JScrollPane scrollPane, String text, JComponent component) {
        return new BusyLabel(scrollPane, text, component);
    }

    public static class BusyLabel {

        private JScrollPane scrollPane;
        private JXBusyLabel busyLabel;
        private JComponent component;

        private BusyLabel(JScrollPane scrollpane, String text, JComponent component) {
            this.scrollPane = scrollpane;
            this.component = component;
            busyLabel = new JXBusyLabel(new Dimension(20, 20));
            busyLabel.setText(text);
            busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        public void setBusy(boolean busy) {
            if (busy) {
                if (scrollPane != null) {
                    scrollPane.setViewportView(busyLabel);
                }

                busyLabel.setBusy(true);
            } else {
                busyLabel.setBusy(false);
                if (scrollPane != null) {
                    scrollPane.setViewportView(component);
                }
            }
        }
    }
}
