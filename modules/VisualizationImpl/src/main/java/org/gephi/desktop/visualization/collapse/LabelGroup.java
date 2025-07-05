package org.gephi.desktop.visualization.collapse;

import javax.swing.JComponent;
import org.gephi.visualization.component.VizBarController;
import org.openide.util.NbBundle;

public class LabelGroup implements CollapseGroup {

    JComponent[] components = new JComponent[6];

    @Override
    public String getName() {
        return NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.groupBarTitle");
    }

    public void setModelValues() {
//            TextModelImpl model = vizModel.getTextModel();
//            ((JPopupButton) components[0]).setSelectedItem(model.getSizeMode());
//            ((JPopupButton) components[1]).setSelectedItem(model.getColorMode());
//            ((JButton) components[2]).setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
//            ((JSlider) components[3]).setValue((int) (model.getNodeSizeFactor() * 100f));

        //TODO
    }

    @Override
    public JComponent[] getToolbarComponents() {
            /*TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();

            //Mode
            final JPopupButton labelSizeModeButton = new JPopupButton();
            TextManager textManager = VizController.getInstance().getTextManager();
            for (final SizeMode sm : textManager.getSizeModes()) {
                labelSizeModeButton.addItem(sm, sm.getIcon());
            }
            labelSizeModeButton.setSelectedItem(model.getSizeMode());
            labelSizeModeButton.setChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    SizeMode sm = (SizeMode) e.getSource();
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    model.setSizeMode(sm);
                }
            });
            labelSizeModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelSizeMode.svg", false));
            labelSizeModeButton
                .setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.sizeMode"));
            model.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    if (model.getSizeMode() != labelSizeModeButton.getSelectedItem()) {
                        labelSizeModeButton.setSelectedItem(model.getSizeMode());
                    }
                }
            });
            components[0] = labelSizeModeButton;

            //Color mode
            final JPopupButton labelColorModeButton = new JPopupButton();
            for (final ColorMode cm : textManager.getColorModes()) {
                labelColorModeButton.addItem(cm, cm.getIcon());
            }
            labelColorModeButton.setSelectedItem(textManager.getModel().getColorMode());
            labelColorModeButton.setChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    ColorMode cm = (ColorMode) e.getSource();
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    model.setColorMode(cm);
                }
            });
            labelColorModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelColorMode.svg", false));
            labelColorModeButton
                .setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.colorMode"));
            model.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    if (model.getColorMode() != labelColorModeButton.getSelectedItem()) {
                        labelColorModeButton.setSelectedItem(model.getColorMode());
                    }
                }
            });
            components[1] = labelColorModeButton;

            //Font
            final JButton fontButton =
                new JButton(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
            fontButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.font"));
            fontButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    Font font =
                        JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeFont());
                    if (font != null && font != model.getNodeFont()) {
                        model.setNodeFont(font);
                    }
                }
            });
            model.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    fontButton.setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
                }
            });
            components[2] = fontButton;

            //Font size
            final JSlider fontSizeSlider = new JSlider(0, 100, (int) (model.getNodeSizeFactor() * 100f));
            fontSizeSlider.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.fontScale"));
            fontSizeSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    model.setNodeSizeFactor(fontSizeSlider.getValue() / 100f);
                }
            });
            fontSizeSlider.setPreferredSize(new Dimension(100, 20));
            fontSizeSlider.setMaximumSize(new Dimension(100, 20));
            model.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    if (((int) (model.getNodeSizeFactor() * 100f)) != fontSizeSlider.getValue()) {
                        fontSizeSlider.setValue((int) (model.getNodeSizeFactor() * 100f));
                    }
                }
            });
            components[3] = fontSizeSlider;

            //Color
            final ColorChooser colorChooser = new ColorChooser(model.getNodeColor());
            colorChooser.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.defaultColor"));
            colorChooser.setPreferredSize(new Dimension(16, 16));
            colorChooser.setMaximumSize(new Dimension(16, 16));
            colorChooser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
                        TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                        model.setNodeColor(colorChooser.getColor());
                    }
                }
            });
            model.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    if (!model.getNodeColor().equals(colorChooser.getColor())) {
                        colorChooser.setColor(model.getNodeColor());
                    }
                }
            });
            components[4] = colorChooser;

            //Attributes
            final JButton attributesButton = new JButton();
            attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));
            attributesButton
                .setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.attributes"));
            attributesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TextModelImpl model = VizController.getInstance().getVizModel().getTextModel();
                    LabelAttributesPanel panel = new LabelAttributesPanel();
                    panel.setup(model);
                    DialogDescriptor dd = new DialogDescriptor(panel,
                        NbBundle.getMessage(VizBarController.class, "LabelAttributesPanel.title"), true,
                        NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                    if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                        panel.unsetup();
                        return;
                    }
                }
            });
            components[5] = attributesButton;

            return components;*/

        //TODO
        return new JComponent[0];
    }

    @Override
    public JComponent getExtendedComponent() {
        LabelSettingsPanel panel = new LabelSettingsPanel();
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
