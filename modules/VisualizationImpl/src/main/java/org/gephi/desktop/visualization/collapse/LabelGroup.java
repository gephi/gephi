package org.gephi.desktop.visualization.collapse;

import com.connectina.swing.fontchooser.JFontChooser;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.dev.colorchooser.ColorChooser;
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
import org.openide.windows.WindowManager;

public class LabelGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    private final LabelSettingsPanel labelSettingsPanel = new LabelSettingsPanel();
    //Toolbar
    private final JPopupButton labelSizeModeButton;
    private final JPopupButton labelColorModeButton;
    private final JButton fontButton;
    private final JSlider fontSizeSlider;
    private final ColorChooser colorChooser;
    private final JButton attributesButton;


    public LabelGroup() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        //Size Mode
        labelSizeModeButton = new JPopupButton();
        labelSizeModeButton.addItem(LabelSizeMode.FIXED,
            ImageUtilities.loadImageIcon("VisualizationImpl/FixedSizeMode.svg", false),
            NbBundle.getMessage(LabelGroup.class, "FixedSizeMode.name"));
        labelSizeModeButton.addItem(LabelSizeMode.SCALED,
            ImageUtilities.loadImageIcon("VisualizationImpl/ScaledSizeMode.svg", false),
            NbBundle.getMessage(LabelGroup.class, "ScaledSizeMode.name"));
        labelSizeModeButton.addItem(LabelSizeMode.PROPORTIONAL,
            ImageUtilities.loadImageIcon("VisualizationImpl/ProportionalSizeMode.svg", false),
            NbBundle.getMessage(LabelGroup.class, "ProportionalSizeMode.name"));
        labelSizeModeButton.setChangeListener(e -> {
            vizController.setNodeLabelSizeMode((LabelSizeMode) e.getSource());
        });
        labelSizeModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelSizeMode.svg", false));
        labelSizeModeButton
            .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.sizeMode"));

        //Color mode
        labelColorModeButton = new JPopupButton();
        labelColorModeButton.addItem(LabelColorMode.UNIQUE,
            ImageUtilities.loadImageIcon("VisualizationImpl/UniqueColorMode.png", false),
            NbBundle.getMessage(LabelGroup.class, "UniqueColorMode.name"));
        labelColorModeButton.addItem(LabelColorMode.OBJECT,
            ImageUtilities.loadImageIcon("VisualizationImpl/ObjectColorMode.png", false),
            NbBundle.getMessage(LabelGroup.class, "ObjectColorMode.name"));
        labelColorModeButton.addItem(LabelColorMode.TEXT,
            ImageUtilities.loadImageIcon("VisualizationImpl/TextColorMode.png", false),
            NbBundle.getMessage(LabelGroup.class, "TextColorMode.name"));
        labelColorModeButton.setChangeListener(e -> {
            vizController.setNodeLabelColorMode((LabelColorMode) e.getSource());
        });
        labelColorModeButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/labelColorMode.svg", false));
        labelColorModeButton
            .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.colorMode"));

        //Font
        fontButton = new JButton("");
        fontButton.setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.font"));
        fontButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeLabelFont());
            if (font != null && font != model.getNodeLabelFont()) {
                vizController.setNodeLabelFont(font);
            }
        });

        //Font size
        fontSizeSlider = new JSlider(0, 100, 0);
        fontSizeSlider.setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.fontScale"));
        fontSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                vizController.setNodeLabelScale(fontSizeSlider.getValue() / 100f);
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
                    vizController.setNodeLabelColor(colorChooser.getColor());
                }
            }
        });

        //Attributes
        attributesButton = new JButton();
        attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));
        attributesButton
            .setToolTipText(NbBundle.getMessage(LabelGroup.class, "VizToolbar.Labels.attributes"));
        attributesButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            LabelAttributesPanel panel = new LabelAttributesPanel(model);
            panel.setup();
            DialogDescriptor dd = new DialogDescriptor(panel,
                NbBundle.getMessage(LabelGroup.class, "LabelAttributesPanel.title"), true,
                NotifyDescriptor.OK_CANCEL_OPTION, null, null);
            if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                panel.unsetup();
            }
        });
    }

    @Override
    public void setup(VisualisationModel vizModel) {
        labelSettingsPanel.setup(vizModel);
        //Toolbar
        labelSizeModeButton.setSelectedItem(vizModel.getNodeLabelSizeMode());
        labelColorModeButton.setSelectedItem(vizModel.getNodeLabelColorMode());
        fontButton.setText(vizModel.getNodeLabelFont().getFontName() + ", " + vizModel.getNodeLabelFont().getSize());
        fontSizeSlider.setValue((int) (vizModel.getNodeLabelScale() * 100));
        colorChooser.setColor(vizModel.getNodeLabelColor());

        if (vizModel.isShowNodeLabels()) {
            // Toolbar
            for (JComponent component : getToolbarComponents()) {
                component.setEnabled(true);
            }
        }

        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VisualisationModel vizModel) {
        vizController.removePropertyChangeListener(this);
        labelSettingsPanel.unsetup(vizModel);
    }

    @Override
    public void propertyChange(VisualisationModel vizModel, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("showNodeLabels")) {
            boolean showLabels = vizModel.isShowNodeLabels();
            // Toolbar
            for (JComponent component : getToolbarComponents()) {
                component.setEnabled(showLabels);
            }
        } else if (evt.getPropertyName().equals("nodeLabelSizeMode")) {
            labelSizeModeButton.setSelectedItem(vizModel.getNodeLabelSizeMode());
        } else if (evt.getPropertyName().equals("nodeLabelColorMode")) {
            labelColorModeButton.setSelectedItem(vizModel.getNodeLabelColorMode());
        } else if (evt.getPropertyName().equals("nodeLabelFont")) {
            Font font = vizModel.getNodeLabelFont();
            fontButton.setText(font.getFontName() + ", " + font.getSize());
        } else if (evt.getPropertyName().equals("nodeLabelSize")) {
            if (((int) (vizModel.getNodeLabelScale() * 100f)) != fontSizeSlider.getValue()) {
                fontSizeSlider.setValue((int) (vizModel.getNodeLabelScale() * 100f));
            }
        } else if (evt.getPropertyName().equals("nodeLabelColor")) {
            if (!vizModel.getNodeLabelColor().equals(colorChooser.getColor())) {
                colorChooser.setColor(vizModel.getNodeLabelColor());
            }
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
        return new JComponent[] {
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
