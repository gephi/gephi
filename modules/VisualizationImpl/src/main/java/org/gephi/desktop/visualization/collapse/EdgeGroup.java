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

public class EdgeGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    private final EdgeSettingsPanel edgeSettingsPanel = new EdgeSettingsPanel();
    //Toolbar
    private final JToggleButton showEdgeButton;
    private final JToggleButton showLabelsButton;
    private final JSlider edgeScaleSlider;

    public EdgeGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Toolbar
        showEdgeButton = new JToggleButton();

        showEdgeButton.setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.showEdges"));
        showEdgeButton.setIcon(
            ImageUtilities.loadImageIcon("VisualizationImpl/showEdges.png", false));
        showEdgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vizController.setShowEdges(showEdgeButton.isSelected());
            }
        });

        //Edge color mode
//        edgeHasNodeColorButton = new JToggleButton();
//        edgeHasNodeColorButton
//            .setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.edgeNodeColor"));
//        edgeHasNodeColorButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/edgeNodeColor.png", false));
//        edgeHasNodeColorButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                vizController.setEdgeHasUniColor(!edgeHasNodeColorButton.isSelected());
//            }
//        });

        //Show labels buttons
        showLabelsButton = new JToggleButton();
        showLabelsButton.setToolTipText(NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.showLabels"));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showEdgeLabels.svg", false));
        showLabelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vizController.setShowEdgeLabels(showLabelsButton.isSelected());
            }
        });

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

        // Toolbar
        for (JComponent component : getToolbarComponents()) {
            component.setEnabled(true);
        }
        showEdgeButton.setSelected(vizModel.isShowEdges());
//        edgeHasNodeColorButton.setSelected(!vizModel.isEdgeHasUniColor());
        showLabelsButton.setSelected(vizModel.isShowEdgeLabels());
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
        } else if (evt.getPropertyName().equals("showEdgeLabels")) {
            if (showLabelsButton.isSelected() != model.isShowEdgeLabels()) {
                showLabelsButton.setSelected(model.isShowEdgeLabels());
            }
        } else if (evt.getPropertyName().equals("edgeScale")) {
            if (model.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                edgeScaleSlider.setValue((int) ((model.getEdgeScale() - 0.1f) * 10));
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
            showLabelsButton,
            edgeScaleSlider
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
