/*
Copyright 2008-2010 Gephi
Authors : Vojtech Bardiovsky <vojtech.bardiovsky@gmail.com>
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
package org.gephi.ui.importer.plugin;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gephi.io.importer.plugin.file.ImporterVNA;
import org.gephi.io.importer.plugin.file.ImporterVNA.EdgeWidthFunction;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * VNA importer UI.
 *
 * @author Vojtech Bardiovsky
 */
@ServiceProvider(service = ImporterUI.class)
public class ImporterVnaUI implements ImporterUI {

    private ImporterVNA[] importers;
    private JComboBox comboBox;
    private JTextField textField;
    private JLabel messageLabel;
    private JPanel panel;

    private final String MESSAGE_LINEAR = NbBundle.getMessage(getClass(), "ImporterVnaUI.message.linear");
    private final String MESSAGE_SQUARE_ROOT = NbBundle.getMessage(getClass(), "ImporterVnaUI.message.square_root");
    private final String MESSAGE_LOGARITHMIC = NbBundle.getMessage(getClass(), "ImporterVnaUI.message.logarithmic");

    @Override
    public void setup(Importer[] importers) {
        this.importers = (ImporterVNA[]) importers;
    }

    @Override
    public JPanel getPanel() {
        panel = new JPanel(new GridBagLayout());
        comboBox = new JComboBox(EdgeWidthFunction.Function.values());
        textField = new JTextField("1", 5);
        messageLabel = new JLabel(MESSAGE_LINEAR);

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comboBoxSelectionChanged(e);
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(comboBox, constraints);

        constraints.gridx = 1;
        panel.add(textField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(messageLabel, constraints);

        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.VERTICAL;
        panel.add(new JPanel());

        panel.setPreferredSize(new Dimension(350, 100));
        panel.setSize(new Dimension(350, 100));

        return panel;
    }

    private void comboBoxSelectionChanged(ActionEvent e) {
        switch ((EdgeWidthFunction.Function) comboBox.getSelectedItem()) {
            case LINEAR:
                messageLabel.setText(MESSAGE_LINEAR);
                textField.setEditable(true);
                break;
            case LOGARITHMIC:
                messageLabel.setText(MESSAGE_LOGARITHMIC);
                textField.setEditable(false);
                break;
            case SQUARE_ROOT:
                messageLabel.setText(MESSAGE_SQUARE_ROOT);
                textField.setEditable(false);
                break;
        }
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            float coefficient = 1;
            if (((EdgeWidthFunction.Function) comboBox.getSelectedItem()).equals(EdgeWidthFunction.Function.LINEAR)) {
                try {
                    coefficient = Float.parseFloat(textField.getText());
                } catch (NumberFormatException e) {
                }
            }
            for (ImporterVNA importer : importers) {
                importer.setEdgeWidthFunction(new EdgeWidthFunction((EdgeWidthFunction.Function) comboBox.getSelectedItem(), coefficient));
            }
        }
        panel = null;
        importers = null;
        textField = null;
        messageLabel = null;
        comboBox = null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "ImporterVnaUI.displayName");
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof ImporterVNA;
    }

}
