/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.general.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.plugin.manipulators.general.MergeNodeDuplicates;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * UI for MergeNodeDuplicates PluginGeneralActionsManipulator
 * @author Eduardo Ramos
 */
public final class MergeNodeDuplicatesUI extends JPanel implements ManipulatorUI {

    private static final ImageIcon CONFIG_BUTTONS_ICON = ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/gear.png", true);
    private static final ImageIcon INFO_LABELS_ICON = ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/information.png", true);
    private MergeNodeDuplicates manipulator;
    private DialogControls dialogControls;
    private Column[] columns;
    private List<List<Node>> duplicateGroups;
    private JCheckBox deleteMergedNodesCheckBox;
    private JCheckBox caseSensitiveCheckBox;
    private JComboBox baseColumnComboBox;
    private Element[] rows;
    private StrategyComboBox[] strategiesComboBoxes;
    private StrategyConfigurationButton[] strategiesConfigurationButtons;

    /** Creates new form MergeNodeDuplicatesUI */
    public MergeNodeDuplicatesUI() {
        initComponents();
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        manipulator = (MergeNodeDuplicates) m;
        this.dialogControls = dialogControls;
        columns = manipulator.getColumns();
        loadSettings();
    }

    @Override
    public void unSetup() {
        manipulator.setDeleteMergedNodes(deleteMergedNodesCheckBox.isSelected());
        manipulator.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        if (duplicateGroups != null && duplicateGroups.size() > 0) {
            AttributeRowsMergeStrategy[] chosenStrategies = new AttributeRowsMergeStrategy[strategiesComboBoxes.length];
            for (int i = 0; i < strategiesComboBoxes.length; i++) {
                chosenStrategies[i] = strategiesComboBoxes[i].getSelectedItem() != null ? ((StrategyWrapper) strategiesComboBoxes[i].getSelectedItem()).getStrategy() : null;
            }
            manipulator.setMergeStrategies(chosenStrategies);
            manipulator.setDuplicateGroups(duplicateGroups);
        }
    }

    @Override
    public String getDisplayName() {
        return manipulator.getName();
    }

    @Override
    public JPanel getSettingsPanel() {
        return this;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    private void calculateDuplicates() {
        if (baseColumnComboBox.getSelectedIndex() != -1) {
            duplicateGroups = Lookup.getDefault().lookup(AttributeColumnsController.class).detectNodeDuplicatesByColumn(columns[baseColumnComboBox.getSelectedIndex()], caseSensitiveCheckBox.isSelected());
        }
    }

    public void loadSettings() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new MigLayout("fillx"));
        loadDescription(settingsPanel);
        loadBaseColumn(settingsPanel);
        loadDeleteMergedNodesCheckBox(settingsPanel);
        scroll.setViewportView(settingsPanel);

        refreshDuplicatesAndStrategies();
    }

    private void refreshDuplicatesAndStrategies() {
        calculateDuplicates();
        loadColumnsStrategies();
    }

    private void loadColumnsStrategies() {
        JPanel strategiesPanel = new JPanel();
        strategiesPanel.setLayout(new MigLayout("fillx"));
        if (duplicateGroups != null && duplicateGroups.size() > 0) {
            strategiesPanel.add(new JLabel(NbBundle.getMessage(MergeNodeDuplicatesUI.class, "MergeNodeDuplicatesUI.duplicateGroupsNumber",duplicateGroups.size())),"wrap 15px");
            
            List<Node> nodes = duplicateGroups.get(0);//Use first group of duplicated nodes to set strategies for all of them
            //Prepare node rows:
            rows = new Element[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                rows[i] = nodes.get(i);
            }

            strategiesConfigurationButtons = new StrategyConfigurationButton[columns.length];
            strategiesComboBoxes = new StrategyComboBox[columns.length];
            for (int i = 0; i < columns.length; i++) {
                //Strategy information label:
                StrategyInfoLabel infoLabel = new StrategyInfoLabel(i);

                //Strategy configuration button:
                strategiesConfigurationButtons[i] = new StrategyConfigurationButton(i);

                //Strategy selection:
                StrategyComboBox strategyComboBox = new StrategyComboBox(strategiesConfigurationButtons[i], infoLabel);
                strategiesComboBoxes[i] = strategyComboBox;
                for (AttributeRowsMergeStrategy strategy : getColumnAvailableStrategies(columns[i])) {
                    strategyComboBox.addItem(new StrategyWrapper(strategy));
                }
                strategyComboBox.refresh();

                strategiesPanel.add(new JLabel(columns[i].getTitle() + ": "), "wrap");

                strategiesPanel.add(infoLabel, "split 3");
                strategiesPanel.add(strategiesConfigurationButtons[i]);
                strategiesPanel.add(strategyComboBox, "growx, wrap 15px");
            }
            dialogControls.setOkButtonEnabled(true);
        } else {
            strategiesPanel.add(new JLabel(getMessage("MergeNodeDuplicatesUI.noDuplicatesText")));
            dialogControls.setOkButtonEnabled(false);
        }
        scrollStrategies.setViewportView(strategiesPanel);
    }

    private List<AttributeRowsMergeStrategy> getColumnAvailableStrategies(Column column) {
        ArrayList<AttributeRowsMergeStrategy> availableStrategies = new ArrayList<AttributeRowsMergeStrategy>();
        for (AttributeRowsMergeStrategy strategy : DataLaboratoryHelper.getDefault().getAttributeRowsMergeStrategies()) {
            strategy.setup(rows, rows[0], column);
            if (strategy.canExecute()) {
                availableStrategies.add(strategy);
            }
        }
        return availableStrategies;
    }

    private void loadDescription(JPanel settingsPanel) {
        JLabel descriptionLabel = new JLabel();
        descriptionLabel.setText(getMessage("MergeNodeDuplicatesUI.description"));
        settingsPanel.add(descriptionLabel, "wrap 25px");
    }

    private void loadBaseColumn(JPanel settingsPanel) {
        baseColumnComboBox = new JComboBox();
        for (Column column : columns) {
            baseColumnComboBox.addItem(column.getTitle());
        }
        settingsPanel.add(new JLabel(getMessage("MergeNodeDuplicatesUI.baseColumnText")), "split 2");
        settingsPanel.add(baseColumnComboBox, "growx, wrap");
        caseSensitiveCheckBox = new JCheckBox(getMessage("MergeNodeDuplicatesUI.caseSensitiveText"), manipulator.isCaseSensitive());
        settingsPanel.add(caseSensitiveCheckBox, "wrap");

        //Reload duplicates on parameteres of detection change:
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDuplicatesAndStrategies();
            }
        };
        baseColumnComboBox.addActionListener(listener);
        caseSensitiveCheckBox.addActionListener(listener);
    }

    private void loadDeleteMergedNodesCheckBox(JPanel settingsPanel) {
        deleteMergedNodesCheckBox = new JCheckBox(getMessage("MergeNodeDuplicatesUI.deleteMergedNodesText"), manipulator.isDeleteMergedNodes());
        settingsPanel.add(deleteMergedNodesCheckBox, "wrap");
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(MergeNodeDuplicatesUI.class, resName);
    }

    private AttributeRowsMergeStrategy getStrategy(int strategyIndex) {
        if (strategiesComboBoxes[strategyIndex] != null) {
            StrategyWrapper sw = (StrategyWrapper) strategiesComboBoxes[strategyIndex].getSelectedItem();
            if (sw != null) {
                return sw.getStrategy();
            }
        }
        return null;
    }

    class StrategyConfigurationButton extends JButton implements ActionListener {

        private final int strategyIndex;

        public StrategyConfigurationButton(int strategyIndex) {
            this.strategyIndex = strategyIndex;
            setIcon(CONFIG_BUTTONS_ICON);
            setToolTipText(getMessage("MergeNodeDuplicatesUI.configurationText"));
            addActionListener(this);
        }

        public void refreshEnabledState() {
            AttributeRowsMergeStrategy strategy = getStrategy(strategyIndex);
            setEnabled(strategy != null && strategy.getUI() != null);//Has strategy and the strategy has UI
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DataLaboratoryHelper.getDefault().showAttributeRowsMergeStrategyUIDialog(getStrategy(strategyIndex));
        }
    }

    class StrategyComboBox extends JComboBox implements ActionListener {

        private final StrategyConfigurationButton button;
        private final StrategyInfoLabel infoLabel;

        public StrategyComboBox(StrategyConfigurationButton button, StrategyInfoLabel infoLabel) {
            this.button = button;
            this.infoLabel = infoLabel;
            this.addActionListener(this);
        }

        public void refresh() {
            button.refreshEnabledState();
            infoLabel.refreshEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }

    class StrategyInfoLabel extends JLabel {

        private final int strategyIndex;

        public StrategyInfoLabel(int strategyIndex) {
            this.strategyIndex = strategyIndex;
            setIcon(INFO_LABELS_ICON);
            prepareRichTooltip();
        }

        public void refreshEnabledState() {
            AttributeRowsMergeStrategy strategy = getStrategy(strategyIndex);
            setEnabled(strategy != null && strategy.getDescription() != null && !strategy.getDescription().isEmpty());
        }

        private void prepareRichTooltip() {
            addMouseListener(new MouseAdapter() {

                RichTooltip richTooltip;

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        richTooltip = buildTooltip(getStrategy(strategyIndex));
                    }

                    if (richTooltip != null) {
                        richTooltip.showTooltip(StrategyInfoLabel.this);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (richTooltip != null) {
                        richTooltip.hideTooltip();
                        richTooltip = null;
                    }
                }

                private RichTooltip buildTooltip(AttributeRowsMergeStrategy strategy) {
                    if (strategy.getDescription() != null && !strategy.getDescription().isEmpty()) {
                        RichTooltip tooltip = new RichTooltip(strategy.getName(), strategy.getDescription());
                        if (strategy.getIcon() != null) {
                            tooltip.setMainImage(ImageUtilities.icon2Image(strategy.getIcon()));
                        }
                        return tooltip;
                    } else {
                        return null;
                    }
                }
            });
        }
    }

    class StrategyWrapper {

        private final AttributeRowsMergeStrategy strategy;

        public StrategyWrapper(AttributeRowsMergeStrategy strategy) {
            this.strategy = strategy;
        }

        @Override
        public String toString() {
            return strategy.getName();
        }

        public AttributeRowsMergeStrategy getStrategy() {
            return strategy;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollStrategies = new javax.swing.JScrollPane();
        scroll = new javax.swing.JScrollPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollStrategies, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
            .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(scroll, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollStrategies, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scroll;
    private javax.swing.JScrollPane scrollStrategies;
    // End of variables declaration//GEN-END:variables
}
