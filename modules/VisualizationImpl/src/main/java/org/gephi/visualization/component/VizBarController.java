/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.visualization.component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.gephi.project.api.Workspace;
import org.gephi.ui.components.JColorBlackWhiteSwitcher;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.components.JDropDownButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class VizBarController {

    private VizToolbarGroup[] groups;
    private VizToolbar toolbar;
    private VizExtendedBar extendedBar;

    public VizBarController() {
        createDefaultGroups();
    }

    private void createDefaultGroups() {
        groups = new VizToolbarGroup[4];

        groups[0] = new GlobalGroupBar();
        groups[1] = new NodeGroupBar();
        groups[2] = new EdgeGroupBar();
        groups[3] = new LabelGroupBar();
    }

    public VizToolbar getToolbar() {
        toolbar = new VizToolbar(groups);
        return toolbar;
    }

    public VizExtendedBar getExtendedBar() {
        extendedBar = new VizExtendedBar(groups);
        return extendedBar;
    }

    public void workspaceSelected(Workspace workspace) {
        if (workspace == null) {
            return;
        }

        final VizModel vizModel = workspace.getLookup().lookup(VizModel.class);

        ((GlobalGroupBar) groups[0]).setVizModel(vizModel);
        ((NodeGroupBar) groups[1]).setModelValues();
        ((EdgeGroupBar) groups[2]).setModelValues();
        ((LabelGroupBar) groups[3]).setModelValues();
    }

    private static class GlobalGroupBar implements VizToolbarGroup {

        private final JColorBlackWhiteSwitcher backgroundColorButton;
        private final JDropDownButton screenshotButton;
        private final PropertyChangeListener vizModelListener;

        private final GlobalSettingsPanel globalSettingsPanel = new GlobalSettingsPanel();

        private VizModel vizModel;

        public GlobalGroupBar() {
            backgroundColorButton = new JColorBlackWhiteSwitcher(Color.WHITE);
            backgroundColorButton
                    .setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.background"));
            backgroundColorButton.addPropertyChangeListener(JColorButton.EVENT_COLOR, evt -> {
                if (vizModel != null && vizModel.isReady()) {
                    vizModel.setBackgroundColor(backgroundColorButton.getColor());
                }

//                    TextModelImpl textModel = VizController.getInstance().getVizModel().getTextModel();
//                    boolean isDarkBackground =
//                        (backgroundColor.getRed() + backgroundColor.getGreen() + backgroundColor.getBlue()) / 3 < 128;
//                    textModel.setNodeColor(isDarkBackground ? Color.WHITE : Color.BLACK);
                //TODO
            });

            //Screenshots
            JPopupMenu screenshotPopup = new JPopupMenu();
            JMenuItem configureScreenshotItem =
                    new JMenuItem(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.screenshot.configure"));
            configureScreenshotItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VizController.getInstance().getScreenshotMaker().configure();
                }
            });
            screenshotPopup.add(configureScreenshotItem);
            screenshotButton = new JDropDownButton(
                ImageUtilities.loadImageIcon("VisualizationImpl/screenshot.svg", false),
                screenshotPopup);
            screenshotButton
                    .setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.screenshot"));
            screenshotButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VizController.getInstance().getScreenshotMaker().takeScreenshot();
                }
            });

            vizModelListener = evt -> {
                if (evt.getPropertyName().equals("backgroundColor")) {
                    backgroundColorButton.setColor(vizModel.getBackgroundColor());
                }
            };
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.groupBarTitle");
        }

        public void setVizModel(VizModel vizModel) {
            if (vizModel == this.vizModel) {
                return;
            }

            if (this.vizModel != null) {
                this.vizModel.removePropertyChangeListener(vizModelListener);
            }

            this.vizModel = vizModel;
            if (vizModel != null) {
                vizModel.addPropertyChangeListener(vizModelListener);
                backgroundColorButton.setColor(vizModel.getBackgroundColor());
            }

            globalSettingsPanel.setVizModel(vizModel);
        }

        @Override
        public JComponent[] getToolbarComponents() {
            return new JComponent[]{backgroundColorButton, screenshotButton};
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
    }

    private static class NodeGroupBar implements VizToolbarGroup {

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

    private static class EdgeGroupBar implements VizToolbarGroup {

        JComponent[] components = new JComponent[4];

        @Override
        public String getName() {
            return NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.groupBarTitle");
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

    private static class LabelGroupBar implements VizToolbarGroup {

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
}
