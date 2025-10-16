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
import org.gephi.ui.components.JPopupButton;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class EdgeGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    private final EdgeSettingsPanel edgeSettingsPanel = new EdgeSettingsPanel();
    //Toolbar
    private final JToggleButton showEdgeButton;
    private final JSlider edgeScaleSlider;
    private final JPopupButton edgeColorModeButton;

    public EdgeGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Show edges
        showEdgeButton = new JToggleButton();
        showEdgeButton.setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.showEdges"));
        showEdgeButton.setIcon(
            ImageUtilities.loadImageIcon("VisualizationImpl/showEdges.svg", false));
        showEdgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vizController.setShowEdges(showEdgeButton.isSelected());
            }
        });

        //Edge Color mode
        edgeColorModeButton = new JPopupButton();
        for (EdgeColorMode mode : EdgeColorMode.values()) {
            edgeColorModeButton.addItem(mode,
                ImageUtilities.loadImageIcon("VisualizationImpl/EdgeColorMode_" + mode.name() + ".svg", false),
                NbBundle.getMessage(EdgeGroup.class, "EdgeColorMode." + mode.name().toLowerCase() + ".name"));
        }
        edgeColorModeButton.setChangeListener(e -> {
            vizController.setEdgeColorMode((EdgeColorMode) e.getSource());
        });
        edgeColorModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/edgeColorMode.svg", false));
        edgeColorModeButton
            .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Edges.colorMode"));

        //EdgeScale slider
        edgeScaleSlider = new JSlider(0, 100, 0);
        edgeScaleSlider.setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.edgeScale"));
        edgeScaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float value = edgeScaleSlider.getValue() / 10f + 0.1f;
                vizController.setEdgeScale(value);
            }
        });
        edgeScaleSlider.setPreferredSize(new Dimension(100, 20));
        edgeScaleSlider.setMaximumSize(new Dimension(100, 20));
    }

    @Override
    public void setup(VisualisationModel vizModel) {
        edgeSettingsPanel.setup(vizModel);

        edgeColorModeButton.setEnabled(true);
        edgeColorModeButton.setSelectedItem(vizModel.getEdgeColorMode());

        showEdgeButton.setEnabled(true);
        showEdgeButton.setSelected(vizModel.isShowEdges());

        edgeScaleSlider.setEnabled(true);
        edgeScaleSlider.setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));

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
        if (evt.getPropertyName().equals("showEdges")) {
            if (showEdgeButton.isSelected() != model.isShowEdges()) {
                showEdgeButton.setSelected(model.isShowEdges());
            }
        } else if (evt.getPropertyName().equals("edgeScale")) {
            if (model.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                edgeScaleSlider.setValue((int) ((model.getEdgeScale() - 0.1f) * 10));
            }
        } else if (evt.getPropertyName().equals("edgeColorMode")) {
            if (edgeColorModeButton.getSelectedItem() != model.getEdgeColorMode()) {
                edgeColorModeButton.setSelectedItem(model.getEdgeColorMode());
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {
            showEdgeButton,
            edgeScaleSlider,
            edgeColorModeButton
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
