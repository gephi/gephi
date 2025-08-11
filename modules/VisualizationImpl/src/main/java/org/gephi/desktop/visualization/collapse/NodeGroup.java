package org.gephi.desktop.visualization.collapse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.VizModelPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class NodeGroup implements CollapseGroup, VizModelPropertyChangeListener {

    private final JToggleButton showLabelsButton;
    private final VizController vizController;

    public NodeGroup() {
        vizController = Lookup.getDefault().lookup(VizController.class);

        //Show labels buttons
        showLabelsButton = new JToggleButton();

        showLabelsButton.setToolTipText(NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.showLabels"));
        showLabelsButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/showNodeLabels.svg", false));
        showLabelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                    vizModel.getTextModel().setShowNodeLabels(showLabelsButton.isSelected());
            }
        });
//            vizModel.getTextModel().addChangeListener(new ChangeListener() {
//                @Override
//                public void stateChanged(ChangeEvent e) {
//                    TextModelImpl textModel = VizController.getInstance().getVizModel().getTextModel();
//                    if (showLabelsButton.isSelected() != textModel.isShowNodeLabels()) {
//                        showLabelsButton.setSelected(textModel.isShowNodeLabels());
//                    }
//                }
//            });
    }

    @Override
    public void setup(VizModel vizModel) {
        showLabelsButton.setEnabled(true);
//        showLabelsButton.setSelected(vizModel.getTextModel().isShowNodeLabels());

        vizModel.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VizModel vizModel) {
        vizModel.removePropertyChangeListener(this);
    }

    @Override
    public void disable() {
        showLabelsButton.setEnabled(false);
    }

    @Override
    public void propertyChange(VizModel model, PropertyChangeEvent evt) {
        // TODO
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NodeGroup.class, "VizToolbar.Nodes.groupBarTitle");
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
