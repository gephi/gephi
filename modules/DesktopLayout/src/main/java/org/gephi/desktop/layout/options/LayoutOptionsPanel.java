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

package org.gephi.desktop.layout.options;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import org.gephi.desktop.layout.LayoutPanel;
import org.gephi.desktop.layout.LayoutPanel.LayoutBuilderWrapper;
import org.gephi.layout.spi.LayoutBuilder;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class LayoutOptionsPanel extends JPanel {

    private final LayoutOptionsPanelController controller;

    private JComboBox<Object> defaultLayoutCombobox;

    LayoutOptionsPanel(LayoutOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        JLabel titleDefaultSettings = new JLabel();
        JSeparator titleSeparator = new JSeparator();
        JLabel labelDefaultLayout = new JLabel();
        defaultLayoutCombobox = new JComboBox<>();

        Font boldFont = titleDefaultSettings.getFont().deriveFont(Font.BOLD);
        titleDefaultSettings.setFont(boldFont);
        titleDefaultSettings.setText(
            NbBundle.getMessage(LayoutOptionsPanel.class, "LayoutOptionsPanel.titleDefaultSettings.text"));

        Mnemonics.setLocalizedText(labelDefaultLayout,
            NbBundle.getMessage(LayoutOptionsPanel.class, "LayoutOptionsPanel.labelDefaultLayout.text"));

        populateLayoutCombobox();
        defaultLayoutCombobox.addActionListener(e -> controller.changed());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(titleDefaultSettings)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(titleSeparator))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addComponent(labelDefaultLayout)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(defaultLayoutCombobox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(titleDefaultSettings)
                        .addComponent(titleSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelDefaultLayout)
                        .addComponent(defaultLayoutCombobox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    private void populateLayoutCombobox() {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        model.addElement(NbBundle.getMessage(LayoutOptionsPanel.class, "LayoutOptionsPanel.noLayout.text"));

        List<LayoutBuilder> builders = new ArrayList<>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
        builders.sort(Comparator.comparing(LayoutBuilder::getName));
        for (LayoutBuilder builder : builders) {
            model.addElement(new LayoutBuilderWrapper(builder));
        }
        defaultLayoutCombobox.setModel(model);
    }

    void load() {
        String defaultBuilderClass =
            NbPreferences.forModule(LayoutPanel.class).get(LayoutPanel.DEFAULT_LAYOUT_PREF, "");

        if (defaultBuilderClass.isEmpty()) {
            defaultLayoutCombobox.setSelectedIndex(0);
        } else {
            boolean found = false;
            for (int i = 1; i < defaultLayoutCombobox.getItemCount(); i++) {
                Object item = defaultLayoutCombobox.getItemAt(i);
                if (item instanceof LayoutBuilderWrapper &&
                    ((LayoutBuilderWrapper) item).getLayoutBuilder().getClass().getName()
                        .equals(defaultBuilderClass)) {
                    defaultLayoutCombobox.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                defaultLayoutCombobox.setSelectedIndex(0);
            }
        }
    }

    void store() {
        Object selected = defaultLayoutCombobox.getSelectedItem();
        if (selected instanceof LayoutBuilderWrapper) {
            NbPreferences.forModule(LayoutPanel.class)
                .put(LayoutPanel.DEFAULT_LAYOUT_PREF,
                    ((LayoutBuilderWrapper) selected).getLayoutBuilder().getClass().getName());
        } else {
            NbPreferences.forModule(LayoutPanel.class).put(LayoutPanel.DEFAULT_LAYOUT_PREF, "");
        }
    }
}
