/*
Copyright 2008-2010 Gephi
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
package org.gephi.datalab.plugin.manipulators.nodes.ui;

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
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.plugin.manipulators.nodes.MergeNodes;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Node;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public final class MergeNodesUI extends JPanel implements ManipulatorUI {

    private static final ImageIcon CONFIG_BUTTONS_ICON = ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/gear.png", true);
    private static final ImageIcon INFO_LABELS_ICON = ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/information.png", true);
    private MergeNodes manipulator;
    private JCheckBox deleteMergedNodesCheckBox;
    private JComboBox nodesComboBox;
    private Node[] nodes;
    private StrategyComboBox[] strategiesComboBoxes;
    private StrategyConfigurationButton[] strategiesConfigurationButtons;

    /** Creates new form MergeNodesUI */
    public MergeNodesUI() {
        initComponents();
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        manipulator = (MergeNodes) m;
        loadSettings();
    }

    @Override
    public void unSetup() {
        manipulator.setDeleteMergedNodes(deleteMergedNodesCheckBox.isSelected());
        manipulator.setSelectedNode(nodes[nodesComboBox.getSelectedIndex()]);
        AttributeRowsMergeStrategy[] chosenStrategies = new AttributeRowsMergeStrategy[strategiesComboBoxes.length];
        for (int i = 0; i < strategiesComboBoxes.length; i++) {
            chosenStrategies[i] = strategiesComboBoxes[i].getSelectedItem() != null ? ((StrategyWrapper) strategiesComboBoxes[i].getSelectedItem()).getStrategy() : null;
        }
        manipulator.setMergeStrategies(chosenStrategies);
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

    public void loadSettings() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new MigLayout("fillx"));
        loadDescription(settingsPanel);
        loadDeleteMergedNodesCheckBox(settingsPanel);
        loadSelectedRow(settingsPanel);
        loadColumnsStrategies(settingsPanel);
        scroll.setViewportView(settingsPanel);
    }

    private void loadColumnsStrategies(JPanel settingsPanel) {
        Column[] columns = manipulator.getColumns();

        strategiesConfigurationButtons = new StrategyConfigurationButton[columns.length];
        strategiesComboBoxes = new StrategyComboBox[columns.length];
        for (int i = 0; i < columns.length; i++) {
            //Strategy information label:
            StrategyInfoLabel infoLabel = new StrategyInfoLabel(i);

            //Strategy configuration button:
            strategiesConfigurationButtons[i] = new StrategyConfigurationButton(i);

            //Strategy selection:
            StrategyComboBox strategyComboBox = new StrategyComboBox(strategiesConfigurationButtons[i],infoLabel);
            strategiesComboBoxes[i] = strategyComboBox;
            for (AttributeRowsMergeStrategy strategy : getColumnAvailableStrategies(columns[i])) {
                strategyComboBox.addItem(new StrategyWrapper(strategy));
            }
            strategyComboBox.refresh();

            settingsPanel.add(new JLabel(columns[i].getTitle() + ": "), "wrap");

            settingsPanel.add(infoLabel, "split 3");
            settingsPanel.add(strategiesConfigurationButtons[i]);
            settingsPanel.add(strategyComboBox, "growx, wrap 15px");

        }
    }

    private List<AttributeRowsMergeStrategy> getColumnAvailableStrategies(Column column) {
        ArrayList<AttributeRowsMergeStrategy> availableStrategies = new ArrayList<>();
        for (AttributeRowsMergeStrategy strategy : DataLaboratoryHelper.getDefault().getAttributeRowsMergeStrategies()) {
            strategy.setup(nodes, manipulator.getSelectedNode(), column);
            if (strategy.canExecute()) {
                availableStrategies.add(strategy);
            }
        }
        return availableStrategies;
    }

    private void loadDescription(JPanel settingsPanel) {
        JLabel descriptionLabel = new JLabel();
        descriptionLabel.setText(getMessage("MergeNodesUI.description"));
        settingsPanel.add(descriptionLabel, "wrap 25px");
    }

    private void loadDeleteMergedNodesCheckBox(JPanel settingsPanel) {
        deleteMergedNodesCheckBox = new JCheckBox(getMessage("MergeNodesUI.deleteMergedNodesText"), manipulator.isDeleteMergedNodes());
        settingsPanel.add(deleteMergedNodesCheckBox, "wrap 25px");
    }

    private void loadSelectedRow(JPanel settingsPanel) {
        JLabel selectedRowLabel = new JLabel();
        selectedRowLabel.setText(getMessage("MergeNodesUI.selectedRowText"));
        settingsPanel.add(selectedRowLabel, "wrap");
        nodesComboBox = new JComboBox();

        //Prepare selected node combo box with nodes data:
        nodes = manipulator.getNodes();
        Node selectedNode = manipulator.getSelectedNode();

        for (int i = 0; i < nodes.length; i++) {
            nodesComboBox.addItem(nodes[i].getId() + " - " + nodes[i].getLabel());
            if (nodes[i] == selectedNode) {
                nodesComboBox.setSelectedIndex(i);
            }
        }
        settingsPanel.add(nodesComboBox, "growx, wrap 25px");
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(MergeNodesUI.class, resName);
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

        private int strategyIndex;

        public StrategyConfigurationButton(int strategyIndex) {
            this.strategyIndex = strategyIndex;
            setIcon(CONFIG_BUTTONS_ICON);
            setToolTipText(getMessage("MergeNodesUI.configurationText"));
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
        private StrategyConfigurationButton button;
        private StrategyInfoLabel infoLabel;

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

        private int strategyIndex;

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

        private AttributeRowsMergeStrategy strategy;

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

        scroll = new javax.swing.JScrollPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scroll;
    // End of variables declaration//GEN-END:variables
}
