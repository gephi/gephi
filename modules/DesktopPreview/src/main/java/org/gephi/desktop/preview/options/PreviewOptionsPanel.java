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

package org.gephi.desktop.preview.options;

import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.border.EmptyBorder;
import org.gephi.desktop.preview.PreviewUIModelImpl;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.preview.api.PreviewPreset;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

final class PreviewOptionsPanel extends JPanel {

    // Sentinel string rendered as a horizontal separator in the combobox dropdown
    private static final String SEPARATOR = "---";

    private final PreviewOptionsPanelController controller;

    private JLabel titleDefaultSettings;
    private JSeparator titleSeparator;
    private JLabel labelDefaultPreset;
    private JComboBox<Object> defaultPresetCombobox;

    PreviewOptionsPanel(PreviewOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        titleDefaultSettings = new JLabel();
        titleSeparator = new JSeparator();
        labelDefaultPreset = new JLabel();
        defaultPresetCombobox = new JComboBox<>();

        Font boldFont = titleDefaultSettings.getFont().deriveFont(Font.BOLD);
        titleDefaultSettings.setFont(boldFont);
        titleDefaultSettings.setText(
            NbBundle.getMessage(PreviewOptionsPanel.class, "PreviewOptionsPanel.titleDefaultSettings.text"));

        Mnemonics.setLocalizedText(labelDefaultPreset,
            NbBundle.getMessage(PreviewOptionsPanel.class, "PreviewOptionsPanel.labelDefaultPreset.text"));

        defaultPresetCombobox.setRenderer(new PresetComboBoxRenderer());
        populatePresetCombobox();
        defaultPresetCombobox.addActionListener(e -> {
            // Do not fire changed when a separator is selected; revert to previous valid item
            Object selected = defaultPresetCombobox.getSelectedItem();
            if (SEPARATOR.equals(selected)) {
                defaultPresetCombobox.setSelectedIndex(0);
                return;
            }
            controller.changed();
        });

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
                            .addComponent(labelDefaultPreset)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(defaultPresetCombobox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                        .addComponent(labelDefaultPreset)
                        .addComponent(defaultPresetCombobox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    private void populatePresetCombobox() {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();

        PreviewUIController controller = Lookup.getDefault().lookup(PreviewUIController.class);
        if (controller != null) {
            for (PreviewPreset preset : controller.getDefaultPresets()) {
                model.addElement(preset);
            }
            PreviewPreset[] userPresets = controller.getUserPresets();
            if (userPresets.length > 0) {
                model.addElement(SEPARATOR);
                for (PreviewPreset preset : userPresets) {
                    model.addElement(preset);
                }
            }
        }
        defaultPresetCombobox.setModel(model);
    }

    void load() {
        String presetClass =
            NbPreferences.forModule(PreviewUIModelImpl.class).get(PreviewUIModelImpl.DEFAULT_PRESET_CLASS, "");
        String presetName =
            NbPreferences.forModule(PreviewUIModelImpl.class).get(PreviewUIModelImpl.DEFAULT_PRESET_NAME, "");

        if (presetClass.isEmpty() && presetName.isEmpty()) {
            defaultPresetCombobox.setSelectedIndex(0);
            return;
        }

        for (int i = 0; i < defaultPresetCombobox.getItemCount(); i++) {
            Object item = defaultPresetCombobox.getItemAt(i);
            if (item instanceof PreviewPreset) {
                PreviewPreset preset = (PreviewPreset) item;
                boolean matches = !presetClass.isEmpty()
                    ? preset.getClass().getName().equals(presetClass)
                    : preset.getName().equals(presetName);
                if (matches) {
                    defaultPresetCombobox.setSelectedIndex(i);
                    return;
                }
            }
        }

        // Preset not found (e.g. user preset was deleted) — fall back to Gephi default
        defaultPresetCombobox.setSelectedIndex(0);
    }

    void store() {
        Object selected = defaultPresetCombobox.getSelectedItem();
        if (selected instanceof PreviewPreset) {
            PreviewPreset preset = (PreviewPreset) selected;
            // Distinguish built-in presets (have a specific class) from user presets (base PreviewPreset class)
            boolean isBuiltIn = preset.getClass() != PreviewPreset.class;
            NbPreferences.forModule(PreviewUIModelImpl.class)
                .put(PreviewUIModelImpl.DEFAULT_PRESET_CLASS, isBuiltIn ? preset.getClass().getName() : "");
            NbPreferences.forModule(PreviewUIModelImpl.class)
                .put(PreviewUIModelImpl.DEFAULT_PRESET_NAME, isBuiltIn ? "" : preset.getName());
        } else {
            // No valid preset selected (empty combobox) — clear stored preference
            NbPreferences.forModule(PreviewUIModelImpl.class).put(PreviewUIModelImpl.DEFAULT_PRESET_CLASS, "");
            NbPreferences.forModule(PreviewUIModelImpl.class).put(PreviewUIModelImpl.DEFAULT_PRESET_NAME, "");
        }
    }

    private class PresetComboBoxRenderer extends DefaultListCellRenderer {

        private final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (SEPARATOR.equals(value)) {
                return separator;
            }
            JLabel label =
                (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(1, 1, 1, 1));
            return label;
        }
    }
}
