package org.gephi.desktop.visualization.collapse;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class NodeLabelGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final JToggleButton showLabelsButton;
    private final VisualizationController vizController;

    public NodeLabelGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Show labels buttons
        showLabelsButton = new JToggleButton();

        showLabelsButton.setToolTipText(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.showLabels"));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showNodeLabels.svg", false));
        showLabelsButton.addActionListener(e -> vizController.setShowNodeLabels(showLabelsButton.isSelected()));
    }

    @Override
    public void setup(VisualisationModel vizModel) {
        showLabelsButton.setEnabled(true);
        showLabelsButton.setSelected(vizModel.isShowNodeLabels());

        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VisualisationModel vizModel) {
        vizController.removePropertyChangeListener(this);
    }

    @Override
    public void disable() {
        showLabelsButton.setEnabled(false);
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if ("showNodeLabels".equals(evt.getPropertyName())) {
            showLabelsButton.setSelected((Boolean) evt.getNewValue());
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NodeGroup.class, "VizToolbar.NodeLabels.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {showLabelsButton};
    }

    @Override
    public JComponent getExtendedComponent() {
        return null;
    }

    @Override
    public boolean hasToolbar() {
        return true;
    }

    @Override
    public boolean hasExtended() {
        return false;
    }
}
