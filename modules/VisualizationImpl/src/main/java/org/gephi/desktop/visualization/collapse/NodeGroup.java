package org.gephi.desktop.visualization.collapse;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JSlider;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class NodeGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    private final NodeSettingsPanel nodeSettingsPanel = new NodeSettingsPanel();
    private final JSlider nodeScaleSlider;

    public NodeGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //NodeScale slider
        nodeScaleSlider = new JSlider(0, 100, 0);
        nodeScaleSlider.setToolTipText(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.nodeScale"));
        nodeScaleSlider.addChangeListener(e -> {
            float value = nodeScaleSlider.getValue() / 10f + 0.1f;
            vizController.setNodeScale(value);
        });
        nodeScaleSlider.setPreferredSize(new Dimension(100, 20));
        nodeScaleSlider.setMaximumSize(new Dimension(100, 20));
    }

    @Override
    public void setup(VisualisationModel vizModel) {
        nodeSettingsPanel.setup(vizModel);

        nodeScaleSlider.setEnabled(true);
        nodeScaleSlider.setValue((int) ((vizModel.getNodeScale() - 0.1f) * 10));

        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VisualisationModel vizModel) {
        nodeSettingsPanel.unsetup(vizModel);
        vizController.removePropertyChangeListener(this);
    }

    @Override
    public void disable() {
        nodeSettingsPanel.setup(null);
        nodeScaleSlider.setEnabled(false);
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if ("nodeScale".equals(evt.getPropertyName())) {
            if (model.getNodeScale() != (nodeScaleSlider.getValue() / 10f + 0.1f)) {
                nodeScaleSlider.setValue((int) ((model.getNodeScale() - 0.1f) * 10));
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {nodeScaleSlider};
    }

    @Override
    public JComponent getExtendedComponent() {
        return nodeSettingsPanel;
    }

    @Override
    public boolean hasToolbar() {
        return true;
    }

    @Override
    public boolean hasExtended() {
        return true;
    }
}
