package org.gephi.desktop.visualization.collapse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.gephi.ui.components.JColorBlackWhiteSwitcher;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.components.JDropDownButton;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.VisualizationModel;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.gephi.visualization.VizConfig;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class GlobalGroup implements CollapseGroup, VisualizationPropertyChangeListener {

    private final JColorBlackWhiteSwitcher backgroundColorButton;
    private final JDropDownButton screenshotButton;

    private final GlobalSettingsPanel globalSettingsPanel = new GlobalSettingsPanel();

    private final VizController vizController;

    public GlobalGroup(VizController vizController) {
        this.vizController = vizController;
        backgroundColorButton = new JColorBlackWhiteSwitcher(
            UIUtils.isDarkLookAndFeel() ? VizConfig.DEFAULT_DARK_BACKGROUND_COLOR : VizConfig.DEFAULT_BACKGROUND_COLOR);
        backgroundColorButton.setLightColor(VizConfig.DEFAULT_BACKGROUND_COLOR);
        backgroundColorButton.setDarkColor(VizConfig.DEFAULT_DARK_BACKGROUND_COLOR);
        backgroundColorButton
            .setToolTipText(NbBundle.getMessage(GlobalGroup.class, "VizToolbar.Global.background"));
        backgroundColorButton.addPropertyChangeListener(JColorButton.EVENT_COLOR, evt -> {
            vizController.setBackgroundColor(backgroundColorButton.getColor());
        });

        //Screenshots
        JPopupMenu screenshotPopup = new JPopupMenu();
        JMenuItem configureScreenshotItem =
            new JMenuItem(NbBundle.getMessage(GlobalGroup.class, "VizToolbar.Global.screenshot.configure"));
        configureScreenshotItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vizController.getScreenshotController().configure();
            }
        });
        screenshotPopup.add(configureScreenshotItem);
        screenshotButton = new JDropDownButton(
            ImageUtilities.loadImageIcon("VisualizationImpl/screenshot.svg", false),
            screenshotPopup);
        screenshotButton
            .setToolTipText(NbBundle.getMessage(GlobalGroup.class, "VizToolbar.Global.screenshot"));
        screenshotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vizController.getScreenshotController().takeScreenshot();
            }
        });
    }

    @Override
    public void propertyChange(VisualizationModel vizModel, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("backgroundColor")) {
            backgroundColorButton.setColor(vizModel.getBackgroundColor());
        }
    }

    @Override
    public void setup(VizModel vizModel) {
        for (JComponent component : getToolbarComponents()) {
            component.setEnabled(true);
        }
        backgroundColorButton.setColor(vizModel.getBackgroundColor());
        globalSettingsPanel.setup(vizModel);
        vizController.addPropertyChangeListener(this);
    }

    @Override
    public void unsetup(VizModel vizModel) {
        vizController.removePropertyChangeListener(this);
        globalSettingsPanel.unsetup(vizModel);
    }

    @Override
    public void disable() {
        globalSettingsPanel.setup(null);
        for (JComponent component : getToolbarComponents()) {
            component.setEnabled(false);
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GlobalGroup.class, "VizToolbar.Global.groupBarTitle");
    }

    @Override
    public JComponent[] getToolbarComponents() {
        return new JComponent[] {backgroundColorButton, screenshotButton};
    }

    @Override
    public JComponent getExtendedComponent() {
        return globalSettingsPanel;
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
