/*
Copyright 2008-2010 Gephi
Authors : Vojtech Bardiovsky <vojtech.bardiovsky@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
 * @author Vojtech Bardiovsky
 */
@ServiceProvider(service = ImporterUI.class)
public class ImporterVnaUI implements ImporterUI {

    private ImporterVNA importer;
    private JComboBox comboBox;
    private JTextField textField;
    private JLabel messageLabel;
    private JPanel panel;

    private final String MESSAGE_LINEAR = NbBundle.getMessage(getClass(), "ImporterVnaUI.message.linear");
    private final String MESSAGE_SQUARE_ROOT = NbBundle.getMessage(getClass(), "ImporterVnaUI.message.square_root");
    private final String MESSAGE_LOGARITHMIC = NbBundle.getMessage(getClass(), "ImporterVnaUI.message.logarithmic");

    @Override
    public void setup(Importer importer) {
        this.importer = (ImporterVNA) importer;
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
                } catch (NumberFormatException e) {}
            }
            importer.setEdgeWidthFunction(new EdgeWidthFunction((EdgeWidthFunction.Function) comboBox.getSelectedItem(), coefficient));
        }
        panel = null;
        importer = null;
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
