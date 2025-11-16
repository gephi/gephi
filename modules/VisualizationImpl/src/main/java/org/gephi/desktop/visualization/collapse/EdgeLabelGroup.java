package org.gephi.desktop.visualization.collapse;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.JPopupButton;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class EdgeLabelGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final JToggleButton showLabelsButton;
    private final JPopupButton labelColorModeButton;
    private final JPopupButton labelSizeModeButton;
    private final JButton attributesButton;
    private final JSlider fontSizeSlider;
    private final VisualizationController vizController;
    private final EdgeLabelsSettingsPanel edgeLabelsSettingsPanel = new EdgeLabelsSettingsPanel();

    public EdgeLabelGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Show labels buttons
        showLabelsButton = new JToggleButton();
        showLabelsButton.setToolTipText(NbBundle.getMessage(EdgeLabelGroup.class, "VizToolbar.Edges.showLabels"));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showEdgeLabels.svg", false));
        showLabelsButton.addActionListener(e -> vizController.setShowEdgeLabels(showLabelsButton.isSelected()));

        //Attributes
        attributesButton = new JButton();
        attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));
        attributesButton
            .setToolTipText(NbBundle.getMessage(EdgeLabelGroup.class, "VizToolbar.Labels.attributes"));
        attributesButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            LabelAttributesPanel panel = new LabelAttributesPanel(model, true);
            panel.setup();
            DialogDescriptor dd = new DialogDescriptor(panel,
                NbBundle.getMessage(EdgeLabelGroup.class, "LabelAttributesPanel.title"), true,
                NotifyDescriptor.OK_CANCEL_OPTION, null, null);
            if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                panel.unsetup();
            }
        });

        // Color mode
        labelColorModeButton = new JPopupButton();
        for (LabelColorMode mode : LabelColorMode.values()) {
            labelColorModeButton.addItem(mode,
                ImageUtilities.loadImageIcon("VisualizationImpl/LabelColorMode_" + mode.name() + ".svg", false),
                NbBundle.getMessage(EdgeLabelGroup.class, "EdgeLabelColorMode." + mode.name().toLowerCase() + ".name"));
        }
        labelColorModeButton.setChangeListener(e -> {
            vizController.setEdgeLabelColorMode((LabelColorMode) e.getSource());
        });
        labelColorModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelColorMode.svg", false));
        labelColorModeButton
            .setToolTipText(NbBundle.getMessage(EdgeLabelGroup.class, "VizToolbar.Labels.colorMode"));

        // Size Mode
        labelSizeModeButton = new JPopupButton();
        for (LabelSizeMode mode : LabelSizeMode.values()) {
            labelSizeModeButton.addItem(mode,
                ImageUtilities.loadImageIcon("VisualizationImpl/LabelSizeMode_" + mode.name() + ".svg", false),
                NbBundle.getMessage(EdgeLabelGroup.class, "LabelSizeMode." + mode.name().toLowerCase() + ".name"));
        }
        labelSizeModeButton.setChangeListener(e -> {
            vizController.setEdgeLabelSizeMode((LabelSizeMode) e.getSource());
        });
        labelSizeModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelSizeMode.svg", false));
        labelSizeModeButton
            .setToolTipText(NbBundle.getMessage(EdgeLabelGroup.class, "VizToolbar.Labels.sizeMode"));

        //Font size
        fontSizeSlider = new JSlider(1, 100, 1);
        fontSizeSlider.setToolTipText(NbBundle.getMessage(EdgeLabelGroup.class, "VizToolbar.Labels.fontScale"));
        fontSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vizController.setEdgeLabelScale(fontSizeSlider.getValue() / 100f);
            }
        });
        fontSizeSlider.setPreferredSize(new Dimension(100, 20));
        fontSizeSlider.setMaximumSize(new Dimension(100, 20));
    }

    @Override
    public void setup(VisualisationModel vizModel) {
        edgeLabelsSettingsPanel.setup(vizModel);
        showLabelsButton.setSelected(vizModel.isShowEdgeLabels());
        labelColorModeButton.setSelectedItem(vizModel.getEdgeLabelColorMode());
        labelSizeModeButton.setSelectedItem(vizModel.getEdgeLabelSizeMode());
        fontSizeSlider.setValue((int) (vizModel.getEdgeLabelScale() * 100));
        refreshEnable(true);

        vizController.addPropertyChangeListener(this);
    }

    private void refreshEnable(boolean enabled) {
        showLabelsButton.setEnabled(enabled);
        boolean showLabels = enabled && showLabelsButton.isSelected();
        labelColorModeButton.setEnabled(showLabels);
        labelSizeModeButton.setEnabled(showLabels);
        attributesButton.setEnabled(showLabels);
        fontSizeSlider.setEnabled(showLabels);
    }

    @Override
    public void unsetup(VisualisationModel vizModel) {
        vizController.removePropertyChangeListener(this);
        edgeLabelsSettingsPanel.unsetup(vizModel);
    }

    @Override
    public void disable() {
        refreshEnable(false);
        edgeLabelsSettingsPanel.setup(null);
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if ("showEdgeLabels".equals(evt.getPropertyName())) {
            showLabelsButton.setSelected((Boolean) evt.getNewValue());
            refreshEnable(true);
        } else if ("edgeLabelColorMode".equals(evt.getPropertyName())) {
            labelColorModeButton.setSelectedItem(model.getEdgeLabelColorMode());
        } else if ("edgeLabelSizeMode".equals(evt.getPropertyName())) {
            labelSizeModeButton.setSelectedItem(model.getEdgeLabelSizeMode());
        } else if ("edgeLabelScale".equals(evt.getPropertyName())) {
            float scale = (Float) evt.getNewValue();
            int sliderValue = (int) (scale * 100);
            if (fontSizeSlider.getValue() != sliderValue) {
                fontSizeSlider.setValue(sliderValue);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EdgeLabelGroup.class, "VizToolbar.EdgeLabels.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {showLabelsButton, labelColorModeButton, labelSizeModeButton,
            fontSizeSlider};
    }

    @Override
    public JComponent getExtendedComponent() {
        return edgeLabelsSettingsPanel;
    }

    @Override
    public boolean hasToolbar() {
        return true;
    }

    @Override
    public boolean hasExtended() {
        return true;
    }

    @Override
    public boolean drawSeparator() {
        return false;
    }
}
