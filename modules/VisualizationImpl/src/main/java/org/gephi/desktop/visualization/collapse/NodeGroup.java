package org.gephi.desktop.visualization.collapse;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import org.gephi.visualization.VizConfig;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationModel;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class NodeGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    private final NodeSettingsPanel nodeSettingsPanel = new NodeSettingsPanel();
    private final JSlider nodeScaleSlider;
    private final JLabel titleLabel;

    public NodeGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        // Label
        titleLabel = new JLabel(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.groupLabel"));

        // NodeScale slider - logarithmic [0, 100] → [NODE_SCALE_MIN, NODE_SCALE_MAX],
        // centred (slider=50) at the default value (geometric mean of MIN and MAX).
        nodeScaleSlider = new JSlider(0, 100, 0);
        nodeScaleSlider.setToolTipText(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.nodeScale"));
        nodeScaleSlider.addChangeListener(e -> {
            float scale = VizConfig.NODE_SCALE_MIN *
                (float) Math.pow((double) VizConfig.NODE_SCALE_MAX / VizConfig.NODE_SCALE_MIN,
                    nodeScaleSlider.getValue() / 100.0);
            vizController.setNodeScale(scale);
        });
        nodeScaleSlider.setPreferredSize(new Dimension(100, 20));
        nodeScaleSlider.setMaximumSize(new Dimension(100, 20));
    }

    @Override
    public void setup(VizModel vizModel) {
        nodeSettingsPanel.setup(vizModel);

        titleLabel.setEnabled(true);

        nodeScaleSlider.setEnabled(true);
        nodeScaleSlider.setValue((int) Math.round(
            Math.log((double) vizModel.getNodeScale() / VizConfig.NODE_SCALE_MIN) /
                Math.log((double) VizConfig.NODE_SCALE_MAX / VizConfig.NODE_SCALE_MIN) * 100));

        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VizModel vizModel) {
        nodeSettingsPanel.unsetup(vizModel);
        vizController.removePropertyChangeListener(this);
    }

    @Override
    public void disable() {
        nodeSettingsPanel.setup(null);
        nodeScaleSlider.setEnabled(false);
        titleLabel.setEnabled(false);
    }

    @Override
    public void propertyChange(VisualizationModel model, PropertyChangeEvent evt) {
        if ("nodeScale".equals(evt.getPropertyName())) {
            int targetSlider = (int) Math.round(Math.log((double) model.getNodeScale() / VizConfig.NODE_SCALE_MIN) /
                Math.log((double) VizConfig.NODE_SCALE_MAX / VizConfig.NODE_SCALE_MIN) * 100);
            if (nodeScaleSlider.getValue() != targetSlider) {
                nodeScaleSlider.setValue(targetSlider);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {titleLabel, nodeScaleSlider};
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
