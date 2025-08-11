package org.gephi.desktop.visualization.collapse;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.dev.colorchooser.ColorChooser;
import org.gephi.ui.components.JPopupButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.VizModelPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class LabelGroup implements CollapseGroup, VizModelPropertyChangeListener {

    private final VizController vizController;
    private final LabelSettingsPanel labelSettingsPanel = new LabelSettingsPanel();
    //Toolbar
    private final JPopupButton labelSizeModeButton;
    private final JPopupButton labelColorModeButton;
    private final JButton fontButton;
    private final JSlider fontSizeSlider;
    private final ColorChooser colorChooser;
    private final JButton attributesButton;


    public LabelGroup() {
        vizController = Lookup.getDefault().lookup(VizController.class);

        //Toolbar

            //Mode
            labelSizeModeButton = new JPopupButton();
            //TODO
//            TextManager textManager = VizController.getInstance().getTextManager();
//            for (final SizeMode sm : textManager.getSizeModes()) {
//                labelSizeModeButton.addItem(sm, sm.getIcon());
//            }

            labelSizeModeButton.setChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    //TODO
//                    SizeMode sm = (SizeMode) e.getSource();
//                    vizController.setSizeMode(sm);
                }
            });
            labelSizeModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelSizeMode.svg", false));
            labelSizeModeButton
                .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.sizeMode"));

            //Color mode
            labelColorModeButton = new JPopupButton();
            //TODO
//            for (final ColorMode cm : textManager.getColorModes()) {
//                labelColorModeButton.addItem(cm, cm.getIcon());
//            }
            labelColorModeButton.setChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
//                    ColorMode cm = (ColorMode) e.getSource();
//                    model.setColorMode(cm);
                }
            });
            labelColorModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelColorMode.svg", false));
            labelColorModeButton
                .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.colorMode"));

            //Font
            fontButton = new JButton("");
            fontButton.setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.font"));
            fontButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//                    Font font =
//                        JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeFont());
//                    if (font != null && font != model.getNodeFont()) {
//                        model.setNodeFont(font);
//                    }
                }
            });

            //Font size
            fontSizeSlider = new JSlider(0, 100, 0);
            fontSizeSlider.setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.fontScale"));
            fontSizeSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
//                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//                    model.setNodeSizeFactor(fontSizeSlider.getValue() / 100f);
                }
            });
            fontSizeSlider.setPreferredSize(new Dimension(100, 20));
            fontSizeSlider.setMaximumSize(new Dimension(100, 20));

            //Color
            colorChooser = new ColorChooser();
            colorChooser.setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.defaultColor"));
            colorChooser.setPreferredSize(new Dimension(16, 16));
            colorChooser.setMaximumSize(new Dimension(16, 16));
            colorChooser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
//                        TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//                        model.setNodeColor(colorChooser.getColor());
                    }
                }
            });

            //Attributes
            attributesButton = new JButton();
            attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));
            attributesButton
                .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.attributes"));
            attributesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//                    LabelAttributesPanel panel = new LabelAttributesPanel();
//                    panel.setup(model);
//                    DialogDescriptor dd = new DialogDescriptor(panel,
//                        NbBundle.getMessage(VizBarController.class, "LabelAttributesPanel.title"), true,
//                        NotifyDescriptor.OK_CANCEL_OPTION, null, null);
//                    if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
//                        panel.unsetup();
//                        return;
//                    }
                }
            });
    }

    @Override
    public void setup(VizModel vizModel) {
        labelSettingsPanel.setup(vizModel);
        //Toolbar
//        labelSizeModeButton.setSelectedItem(model.getSizeMode());
//        labelColorModeButton.setSelectedItem(textManager.getModel().getColorMode();
//        fontButton.setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
//        fontSizeSlider.setValue((int) (model.getNodeSizeFactor() * 100
//        colorChooser.setColor(model. text color());
    }

    @Override
    public void unsetup(VizModel vizModel) {

    }

    @Override
    public void propertyChange(VizModel vizModel, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("something about the label size mode")) {
//            if (model.getSizeMode() != labelSizeModeButton.getSelectedItem()) {
//                labelSizeModeButton.setSelectedItem(model.getSizeMode());
//            }
        } else if (evt.getPropertyName().equals("something about the label color mode")) {
//            if (model.getColorMode() != labelColorModeButton.getSelectedItem()) {
//                labelColorModeButton.setSelectedItem(model.getColorMode());
//            }
        } else if (evt.getPropertyName().equals("something about the label font")) {
//            TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//            fontButton.setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
        } else if (evt.getPropertyName().equals("something about the label size factor")) {
//            TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//            if (((int) (model.getNodeSizeFactor() * 100f)) != fontSizeSlider.getValue()) {
//                fontSizeSlider.setValue((int) (model.getNodeSizeFactor() * 100f));
//            }
        } else if (evt.getPropertyName().equals("something about the label color")) {
//            TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
//            if (!model.getNodeColor().equals(colorChooser.getColor())) {
//                colorChooser.setColor(model.getNodeColor());
//            }
        }
    }

    @Override
    public void disable() {
        for (JComponent component : getToolbarComponents()) {
            component.setEnabled(false);
        }
        labelSettingsPanel.setup(null);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[]{
            labelSizeModeButton,
            labelColorModeButton,
            fontButton,
            fontSizeSlider,
            colorChooser,
            attributesButton
        };
    }

    @Override
    public JComponent getExtendedComponent() {
        return labelSettingsPanel;
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
