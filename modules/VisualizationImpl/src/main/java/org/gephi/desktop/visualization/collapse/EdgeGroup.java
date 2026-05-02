package org.gephi.desktop.visualization.collapse;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.JPopupButton;
import org.gephi.visualization.VizConfig;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationModel;
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
    private final JLabel titleLabel;

    public EdgeGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Title
        titleLabel = new JLabel(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.groupLabel"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

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
            .setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.colorMode"));

        //EdgeScale slider - logarithmic [0, 100] → [EDGE_SCALE_MIN, EDGE_SCALE_MAX],
        // centred (slider=50) at the default value (geometric mean of MIN and MAX).
        edgeScaleSlider = new JSlider(0, 100, 0);
        edgeScaleSlider.setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.edgeScale"));
        edgeScaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float scale = VizConfig.EDGE_SCALE_MIN *
                    (float) Math.pow((double) VizConfig.EDGE_SCALE_MAX / VizConfig.EDGE_SCALE_MIN,
                        edgeScaleSlider.getValue() / 100.0);
                vizController.setEdgeScale(scale);
            }
        });
        edgeScaleSlider.setPreferredSize(new Dimension(100, 20));
        edgeScaleSlider.setMaximumSize(new Dimension(100, 20));
    }

    @Override
    public void setup(VizModel vizModel) {
        edgeSettingsPanel.setup(vizModel);

        titleLabel.setEnabled(true);

        edgeColorModeButton.setEnabled(true);
        edgeColorModeButton.setSelectedItem(vizModel.getEdgeColorMode());

        showEdgeButton.setEnabled(true);
        showEdgeButton.setSelected(vizModel.isShowEdges());

        edgeScaleSlider.setEnabled(true);
        edgeScaleSlider.setValue((int) Math.round(
            Math.log((double) vizModel.getEdgeScale() / VizConfig.EDGE_SCALE_MIN) /
                Math.log((double) VizConfig.EDGE_SCALE_MAX / VizConfig.EDGE_SCALE_MIN) * 100));

        // Listeners
        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VizModel vizModel) {
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
    public void propertyChange(VisualizationModel model, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("showEdges")) {
            if (showEdgeButton.isSelected() != model.isShowEdges()) {
                showEdgeButton.setSelected(model.isShowEdges());
            }
        } else if (evt.getPropertyName().equals("edgeScale")) {
            int targetSlider = (int) Math.round(Math.log((double) model.getEdgeScale() / VizConfig.EDGE_SCALE_MIN) /
                Math.log((double) VizConfig.EDGE_SCALE_MAX / VizConfig.EDGE_SCALE_MIN) * 100);
            if (edgeScaleSlider.getValue() != targetSlider) {
                edgeScaleSlider.setValue(targetSlider);
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
        return new JComponent[] {titleLabel,
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
