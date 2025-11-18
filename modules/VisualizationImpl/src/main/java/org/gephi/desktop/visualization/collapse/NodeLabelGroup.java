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
import org.gephi.visualization.VizModel;
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

public class NodeLabelGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final JToggleButton showLabelsButton;
    private final JPopupButton labelColorModeButton;
    private final JPopupButton labelSizeModeButton;
    private final JToggleButton fitToNodeSizeButton;
    private final JToggleButton avoidOverlapButton;
    private final JButton attributesButton;
    private final JSlider fontSizeSlider;
    private final VisualizationController vizController;
    private final NodeLabelsSettingsPanel nodeLabelsSettingsPanel = new NodeLabelsSettingsPanel();

    public NodeLabelGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Show labels buttons
        showLabelsButton = new JToggleButton();

        showLabelsButton.setToolTipText(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.showLabels"));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showNodeLabels.svg", false));
        showLabelsButton.addActionListener(e -> vizController.setShowNodeLabels(showLabelsButton.isSelected()));

        //Attributes
        attributesButton = new JButton();
        attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));
        attributesButton
            .setToolTipText(NbBundle.getMessage(NodeLabelGroup.class, "VizToolbar.Labels.attributes"));
        attributesButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            LabelAttributesPanel panel = new LabelAttributesPanel(model, false);
            panel.setup();
            DialogDescriptor dd = new DialogDescriptor(panel,
                NbBundle.getMessage(NodeLabelGroup.class, "LabelAttributesPanel.title"), true,
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
                NbBundle.getMessage(NodeLabelGroup.class, "NodeLabelColorMode." + mode.name().toLowerCase() + ".name"));
        }
        labelColorModeButton.setChangeListener(e -> {
            vizController.setNodeLabelColorMode((LabelColorMode) e.getSource());
        });
        labelColorModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelColorMode.svg", false));
        labelColorModeButton
            .setToolTipText(NbBundle.getMessage(NodeLabelGroup.class, "VizToolbar.Labels.colorMode"));

        // Size Mode
        labelSizeModeButton = new JPopupButton();
        for (LabelSizeMode mode : LabelSizeMode.values()) {
            labelSizeModeButton.addItem(mode,
                ImageUtilities.loadImageIcon("VisualizationImpl/LabelSizeMode_" + mode.name() + ".svg", false),
                NbBundle.getMessage(NodeLabelGroup.class, "LabelSizeMode." + mode.name().toLowerCase() + ".name"));
        }
        labelSizeModeButton.setChangeListener(e -> {
            vizController.setNodeLabelSizeMode((LabelSizeMode) e.getSource());
        });
        labelSizeModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelSizeMode.svg", false));
        labelSizeModeButton
            .setToolTipText(NbBundle.getMessage(NodeLabelGroup.class, "VizToolbar.Labels.sizeMode"));

        // Fit to node size
        fitToNodeSizeButton = new JToggleButton();
        fitToNodeSizeButton.setToolTipText(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Labels.fitToNodeSize"));
        fitToNodeSizeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/fitToNodeSize.svg", false));
        fitToNodeSizeButton.addActionListener(
            e -> vizController.setNodeLabelFitToNodeSize(fitToNodeSizeButton.isSelected()));

        // Avoid overlap
        avoidOverlapButton = new JToggleButton();
        avoidOverlapButton.setToolTipText(NbBundle.getMessage(NodeLabelGroup.class, "VizToolbar.Labels.avoidOverlap"));
        avoidOverlapButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/avoidOverlap.svg", false));
        avoidOverlapButton.addActionListener(
            e -> vizController.setAvoidNodeLabelOverlap(avoidOverlapButton.isSelected()));

        //Font size
        fontSizeSlider = new JSlider(1, 100, 1);
        fontSizeSlider.setToolTipText(NbBundle.getMessage(NodeLabelGroup.class, "VizToolbar.Labels.fontScale"));
        fontSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vizController.setNodeLabelScale(fontSizeSlider.getValue() / 100f);
            }
        });
        fontSizeSlider.setPreferredSize(new Dimension(100, 20));
        fontSizeSlider.setMaximumSize(new Dimension(100, 20));
    }

    @Override
    public void setup(VizModel vizModel) {
        nodeLabelsSettingsPanel.setup(vizModel);
        showLabelsButton.setSelected(vizModel.isShowNodeLabels());
        labelColorModeButton.setSelectedItem(vizModel.getNodeLabelColorMode());
        labelSizeModeButton.setSelectedItem(vizModel.getNodeLabelSizeMode());
        fitToNodeSizeButton.setSelected(vizModel.isNodeLabelFitToNodeSize());
        avoidOverlapButton.setSelected(vizModel.isAvoidNodeLabelOverlap());
        fontSizeSlider.setValue((int) (vizModel.getNodeLabelScale() * 100));
        refreshEnable(true);

        vizController.addPropertyChangeListener(this);
    }

    private void refreshEnable(boolean enabled) {
        showLabelsButton.setEnabled(enabled);
        boolean showLabels = enabled && showLabelsButton.isSelected();
        labelColorModeButton.setEnabled(showLabels);
        labelSizeModeButton.setEnabled(showLabels);
        fitToNodeSizeButton.setEnabled(showLabels);
        avoidOverlapButton.setEnabled(showLabels);
        attributesButton.setEnabled(showLabels);
        fontSizeSlider.setEnabled(showLabels);
    }

    @Override
    public void unsetup(VizModel vizModel) {
        vizController.removePropertyChangeListener(this);
        nodeLabelsSettingsPanel.unsetup(vizModel);
    }

    @Override
    public void disable() {
        refreshEnable(false);
        nodeLabelsSettingsPanel.setup(null);
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if ("showNodeLabels".equals(evt.getPropertyName())) {
            showLabelsButton.setSelected((Boolean) evt.getNewValue());
            refreshEnable(true);
        } else if ("nodeLabelColorMode".equals(evt.getPropertyName())) {
            labelColorModeButton.setSelectedItem(model.getNodeLabelColorMode());
        } else if ("nodeLabelSizeMode".equals(evt.getPropertyName())) {
            labelSizeModeButton.setSelectedItem(model.getNodeLabelSizeMode());
        } else if ("nodeLabelFitToNodeSize".equals(evt.getPropertyName())) {
            fitToNodeSizeButton.setSelected((Boolean) evt.getNewValue());
        } else if ("avoidNodeLabelOverlap".equals(evt.getPropertyName())) {
            avoidOverlapButton.setSelected((Boolean) evt.getNewValue());
        } else if ("nodeLabelScale".equals(evt.getPropertyName())) {
            float scale = (Float) evt.getNewValue();
            int sliderValue = (int) (scale * 100);
            if (fontSizeSlider.getValue() != sliderValue) {
                fontSizeSlider.setValue(sliderValue);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NodeGroup.class, "VizToolbar.NodeLabels.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {showLabelsButton, labelColorModeButton, labelSizeModeButton, fitToNodeSizeButton,
            avoidOverlapButton, fontSizeSlider, attributesButton};
    }

    @Override
    public JComponent getExtendedComponent() {
        return nodeLabelsSettingsPanel;
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
