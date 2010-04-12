/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.generator.plugin.RandomGraphUI;
import org.gephi.io.generator.plugin.WattsStrogatz;
import org.gephi.io.generator.plugin.WattsStrogatzUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WattsStrogatzUI.class)
public class WattsStrogatzUIImpl implements WattsStrogatzUI {

    private WattsStrogatzPanel panel;
    private WattsStrogatz wattsStrogatz;

    public WattsStrogatzUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new WattsStrogatzPanel();
        }
        return WattsStrogatzPanel.createValidationPanel(panel);
    }

    public void setup(Generator generator) {
        this.wattsStrogatz = (WattsStrogatz) generator;

        //Set UI
        if (panel == null) {
            panel = new WattsStrogatzPanel();
        }
        panel.nodeField.setText(String.valueOf(wattsStrogatz.getNumberOfNodes()));
        panel.neighborField.setText(String.valueOf(wattsStrogatz.getNumberOfNeighbors()));
        panel.probabilityField.setText(String.valueOf(wattsStrogatz.getRewiringProbability()));
    }

    public void unsetup() {
        //Set params
        wattsStrogatz.setNumberOfNodes(Integer.parseInt(panel.nodeField.getText()));
        wattsStrogatz.setNumberOfNeighbors(Integer.parseInt(panel.neighborField.getText()));
        wattsStrogatz.setRewiringProbability(Double.parseDouble(panel.probabilityField.getText()));
        panel = null;
    }
}