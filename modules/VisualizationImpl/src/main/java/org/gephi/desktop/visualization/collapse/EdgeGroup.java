package org.gephi.desktop.visualization.collapse;

import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.VizModelPropertyChangeListener;
import org.openide.util.NbBundle;

public class EdgeGroup implements CollapseGroup, VizModelPropertyChangeListener {

    JComponent[] components = new JComponent[4];

    public EdgeGroup() {
        //TODO
    }

    @Override
    public void propertyChange(VizModel model, PropertyChangeEvent evt) {

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EdgeGroup.class, "VizToolbar.Edges.groupBarTitle");
    }

    public void setModelValues() {
//            ((JToggleButton) components[2]).setSelected(vizModel.getTextModel().isShowEdgeLabels());
//            ((JSlider) components[3]).setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));

        //TODO
    }

    @Override
    public JComponent[] getToolbarComponents() {
            /*//Show edges buttons
            VizModel vizModel = VizController.getInstance().getVizModel();
            final JToggleButton showEdgeButton = new JToggleButton();
            showEdgeButton.setSelected(vizModel.isShowEdges());
            showEdgeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showEdges"));
            showEdgeButton.setIcon(
                ImageUtilities.loadImageIcon("VisualizationImpl/showEdges.png", false));
            showEdgeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = VizController.getInstance().getVizModel();
                    vizModel.setShowEdges(showEdgeButton.isSelected());
                }
            });
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("showEdges")) {
                        VizModel vizModel = VizController.getInstance().getVizModel();
                        if (showEdgeButton.isSelected() != vizModel.isShowEdges()) {
                            showEdgeButton.setSelected(vizModel.isShowEdges());
                        }
                    }
                }
            });
            components[0] = showEdgeButton;

            //Edge color mode
            final JToggleButton edgeHasNodeColorButton = new JToggleButton();
            edgeHasNodeColorButton.setSelected(!vizModel.isEdgeHasUniColor());
            edgeHasNodeColorButton
                .setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.edgeNodeColor"));
            edgeHasNodeColorButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/edgeNodeColor.png", false));
            edgeHasNodeColorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = VizController.getInstance().getVizModel();
                    vizModel.setEdgeHasUniColor(!edgeHasNodeColorButton.isSelected());
                }
            });
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("edgeHasUniColor")) {
                        VizModel vizModel = VizController.getInstance().getVizModel();
                        if (edgeHasNodeColorButton.isSelected() != !vizModel.isEdgeHasUniColor()) {
                            edgeHasNodeColorButton.setSelected(!vizModel.isEdgeHasUniColor());
                        }
                    }
                }
            });
            components[1] = edgeHasNodeColorButton;

            //Show labels buttons
            final JToggleButton showLabelsButton = new JToggleButton();
            showLabelsButton.setSelected(vizModel.getTextModel().isShowEdgeLabels());
            showLabelsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showLabels"));
            showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showEdgeLabels.svg", false));
            showLabelsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = VizController.getInstance().getVizModel();
                    vizModel.getTextModel().setShowEdgeLabels(showLabelsButton.isSelected());
                }
            });
            vizModel.getTextModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl textModel = VizController.getInstance().getVizModel().getTextModel();
                    if (showLabelsButton.isSelected() != textModel.isShowEdgeLabels()) {
                        showLabelsButton.setSelected(textModel.isShowEdgeLabels());
                    }
                }
            });
            components[2] = showLabelsButton;

            //EdgeScale slider
            final JSlider edgeScaleSlider = new JSlider(0, 100, (int) ((vizModel.getEdgeScale() - 0.1f) * 10));
            edgeScaleSlider.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.edgeScale"));
            edgeScaleSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    VizModel vizModel = VizController.getInstance().getVizModel();
                    if (vizModel.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                        vizModel.setEdgeScale(edgeScaleSlider.getValue() / 10f + 0.1f);
                    }
                }
            });
            edgeScaleSlider.setPreferredSize(new Dimension(100, 20));
            edgeScaleSlider.setMaximumSize(new Dimension(100, 20));
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("edgeScale")) {
                        VizModel vizModel = VizController.getInstance().getVizModel();
                        if (vizModel.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                            edgeScaleSlider.setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));
                        }
                    }
                }
            });
            components[3] = edgeScaleSlider;
            return components;*/

        //TODO

        return new JComponent[0];
    }

    @Override
    public JComponent getExtendedComponent() {
        EdgeSettingsPanel panel = new EdgeSettingsPanel();
        panel.setup();
        return panel;
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
