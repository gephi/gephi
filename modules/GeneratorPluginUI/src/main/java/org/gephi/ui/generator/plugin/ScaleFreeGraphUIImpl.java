package org.gephi.ui.generator.plugin;

import javax.swing.JPanel;
import org.gephi.io.generator.plugin.ScaleFreeGraph;
import org.gephi.io.generator.plugin.ScaleFreeGraphUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ScaleFreeGraphUI.class)
public class ScaleFreeGraphUIImpl implements ScaleFreeGraphUI {

    private ScaleFreeGraphPanel panel;
    private ScaleFreeGraph scaleFreeGraph;

    @Override
    public JPanel getPanel() {
        if (panel == null) {
            panel = new ScaleFreeGraphPanel();
        }
        return ScaleFreeGraphPanel.createValidationPanel(panel);
    }

    @Override
    public void setup(Generator generator) {
        this.scaleFreeGraph = (ScaleFreeGraph) generator;

        //Set UI
        if (panel == null) {
            panel = new ScaleFreeGraphPanel();
        }
        panel.nodeField.setText(String.valueOf(scaleFreeGraph.getNumberOfNodes()));
        panel.edgeField.setText(String.valueOf(scaleFreeGraph.getEdgesPerNode()));
        panel.seedField.setText(scaleFreeGraph.getSeed() != null ? String.valueOf(scaleFreeGraph.getSeed()) : "");
    }

    @Override
    public void unsetup() {
        //Set params
        scaleFreeGraph.setNumberOfNodes(Integer.parseInt(panel.nodeField.getText()));
        scaleFreeGraph.setEdgesPerNode(Integer.parseInt(panel.edgeField.getText()));
        String seed = panel.seedField.getText().trim();
        scaleFreeGraph.setSeed(seed.isEmpty() ? null : Long.parseLong(seed));
        panel = null;
    }
}
