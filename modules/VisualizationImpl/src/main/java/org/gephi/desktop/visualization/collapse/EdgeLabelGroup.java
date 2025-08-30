package org.gephi.desktop.visualization.collapse;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class EdgeLabelGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    private final EdgeSettingsPanel edgeSettingsPanel = new EdgeSettingsPanel();
    //Toolbar
    private final JToggleButton showLabelsButton;

    public EdgeLabelGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Show labels buttons
        showLabelsButton = new JToggleButton();
        showLabelsButton.setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.showLabels"));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showEdgeLabels.svg", false));
        showLabelsButton.addActionListener(e -> vizController.setShowEdgeLabels(showLabelsButton.isSelected()));
    }

    @Override
    public void setup(VisualisationModel vizModel) {
        edgeSettingsPanel.setup(vizModel);
        showLabelsButton.setSelected(vizModel.isShowEdgeLabels());

        // Listeners
        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VisualisationModel vizModel) {
        vizController.removePropertyChangeListener(this);
        edgeSettingsPanel.unsetup(vizModel);
    }

    @Override
    public void disable() {
        edgeSettingsPanel.setup(null);
        for (JComponent component : getToolbarComponents()) {
            component.setEnabled(false);
        }
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("showEdgeLabels")) {
            if (showLabelsButton.isSelected() != model.isShowEdgeLabels()) {
                showLabelsButton.setSelected(model.isShowEdgeLabels());
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EdgeGroup.class, "VizToolbar.EdgeLabels.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {
            showLabelsButton,
        };
    }

    @Override
    public JComponent getExtendedComponent() {
        return edgeSettingsPanel;
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
