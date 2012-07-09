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

    /**
     * Creates a new <code>JXBusyLabel</code> wrapper and set it at the center of <code>scrollPane</code>. When users
     * calls <code>BusyLabel.setBusy(false)</code>, the label is removed from <code>scrollPanel</code> and
     * <code>component</code> is set instead.
     * @param scrollPane the scroll Panel where the label is to be put
     * @param text the text set to the newly created label
     * @param component the component to set in <code>scrollPane</code> when it is not busy anymore
     * @return the newly created <code>JXBusyLabel</code> wrapper
     */
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

        public void setBusy(boolean busy, JComponent component) {
            this.component = component;
            setBusy(busy);
        }
    }
}
