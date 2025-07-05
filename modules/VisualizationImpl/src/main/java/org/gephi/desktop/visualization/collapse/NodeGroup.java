package org.gephi.desktop.visualization.collapse;

import javax.swing.JComponent;
import org.gephi.visualization.component.VizBarController;
import org.openide.util.NbBundle;

public class NodeGroup implements CollapseGroup {

    JComponent[] components = new JComponent[1];

    @Override
    public String getName() {
        return NbBundle.getMessage(VizBarController.class, "VizToolbar.Nodes.groupBarTitle");
    }

    public void setModelValues() {
//            ((JToggleButton) components[0]).setSelected(vizModel.getTextModel().isShowNodeLabels());
        //TODO
    }

    @Override
    public JComponent[] getToolbarComponents() {
            /*
            //Show labels buttons
            VizModel vizModel = VizController.getInstance().getVizModel();
            final JToggleButton showLabelsButton = new JToggleButton();
            showLabelsButton.setSelected(vizModel.getTextModel().isShowNodeLabels());
            showLabelsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Nodes.showLabels"));
            showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showNodeLabels.svg", false));
            showLabelsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = VizController.getInstance().getVizModel();
                    vizModel.getTextModel().setShowNodeLabels(showLabelsButton.isSelected());
                }
            });
            vizModel.getTextModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl textModel = VizController.getInstance().getVizModel().getTextModel();
                    if (showLabelsButton.isSelected() != textModel.isShowNodeLabels()) {
                        showLabelsButton.setSelected(textModel.isShowNodeLabels());
                    }
                }
            });
            components[0] = showLabelsButton;
            */

        //TODO

        return new JComponent[0];
    }

    @Override
    public JComponent getExtendedComponent() {
//            NodeSettingsPanel panel = new NodeSettingsPanel();
//            panel.setup();
//            return panel;
        return null;
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
