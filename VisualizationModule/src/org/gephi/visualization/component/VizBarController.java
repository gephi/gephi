/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.component;

import com.connectina.swing.fontchooser.JFontChooser;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.dev.colorchooser.ColorChooser;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.components.JDropDownButton;
import org.gephi.ui.components.JPopupButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.opengl.text.ColorMode;
import org.gephi.visualization.opengl.text.SizeMode;
import org.gephi.visualization.opengl.text.TextManager;
import org.gephi.visualization.opengl.text.TextModel;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class VizBarController {

    private VizToolbarGroup[] groups;

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
        VizToolbar toolbar = new VizToolbar(groups);
        return toolbar;
    }

    public VizExtendedBar getExtendedBar() {
        VizExtendedBar extendedBar = new VizExtendedBar(groups);
        return extendedBar;
    }

    private static class GlobalGroupBar implements VizToolbarGroup {

        public String getName() {
            return "Global";
        }

        public JComponent[] getToolbarComponents() {
            JComponent[] components = new JComponent[4];

            //Background color
            final VizConfig vizConfig = VizController.getInstance().getVizConfig();
            final JButton backgroundColorButton = new JColorButton(vizConfig.getBackgroundColor());
            backgroundColorButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.background"));
            backgroundColorButton.addPropertyChangeListener("color", new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    vizConfig.setBackgroundColor(((JColorButton) backgroundColorButton).getColor());
                }
            });
            components[0] = backgroundColorButton;

            //Center on graph
            final JButton centerOnGraphButton = new JButton();
            centerOnGraphButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.centerOnGraph"));
            centerOnGraphButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnGraph.png")));
            centerOnGraphButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizController.getInstance().getGraphIO().centerOnGraph();
                }
            });
            components[1] = centerOnGraphButton;

            //Center on zero
            final JButton centerOnZeroButton = new JButton();
            centerOnZeroButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.centerOnZero"));
            centerOnZeroButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnZero.png")));
            centerOnZeroButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizController.getInstance().getGraphIO().centerOnZero();
                }
            });
            components[2] = centerOnZeroButton;

            //Screenshots
            JPopupMenu screenshotPopup = new JPopupMenu();
            JMenuItem configureScreenshotItem = new JMenuItem(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.screenshot.configure"));
            configureScreenshotItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizController.getInstance().getScreenshotMaker().configure();
                }
            });
            screenshotPopup.add(configureScreenshotItem);
            final JButton screenshotButton = new JDropDownButton(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/screenshot.png")), screenshotPopup);
            screenshotButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.screenshot"));
            screenshotButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizController.getInstance().getScreenshotMaker().takeScreenshot();
                }
            });
            components[3] = screenshotButton;

            return components;
        }

        public JComponent getExtendedComponent() {
            GlobalSettingsPanel panel = new GlobalSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }

    private static class NodeGroupBar implements VizToolbarGroup {

        public String getName() {
            return "Nodes";
        }

        public JComponent[] getToolbarComponents() {
            JComponent[] components = new JComponent[1];

            //Show labels buttons
            final VizConfig vizConfig = VizController.getInstance().getVizConfig();
            final JToggleButton showLabelsButton = new JToggleButton();
            showLabelsButton.setSelected(vizConfig.isShowNodeLabels());
            showLabelsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showLabels"));
            showLabelsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/showLabels.png")));
            showLabelsButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    vizConfig.setShowNodeLabels(showLabelsButton.isSelected());
                }
            });
            components[0] = showLabelsButton;

            return components;
        }

        public JComponent getExtendedComponent() {
            NodeSettingsPanel panel = new NodeSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }

    private static class EdgeGroupBar implements VizToolbarGroup {

        public String getName() {
            return "Edges";
        }

        public JComponent[] getToolbarComponents() {
            JComponent[] components = new JComponent[3];

            //Show edges buttons
            final VizConfig vizConfig = VizController.getInstance().getVizConfig();
            final JToggleButton showEdgeButton = new JToggleButton();
            showEdgeButton.setSelected(vizConfig.isShowEdges());
            showEdgeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showEdges"));
            showEdgeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/showEdges.png")));
            showEdgeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    vizConfig.setShowEdges(showEdgeButton.isSelected());
                }
            });
            vizConfig.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("showEdges")) {
                        if (showEdgeButton.isSelected() != vizConfig.isShowEdges()) {
                            showEdgeButton.setSelected(vizConfig.isShowEdges());
                        }
                    }
                }
            });
            components[0] = showEdgeButton;

            //Edge color mode
            final JToggleButton edgeHasNodeColorButton = new JToggleButton();
            edgeHasNodeColorButton.setSelected(!vizConfig.isEdgeUniColor());
            edgeHasNodeColorButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.edgeNodeColor"));
            edgeHasNodeColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/edgeNodeColor.png")));
            edgeHasNodeColorButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    vizConfig.setEdgeUniColor(!edgeHasNodeColorButton.isSelected());
                }
            });
            vizConfig.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("edgeUniColor")) {
                        if (edgeHasNodeColorButton.isSelected() != !vizConfig.isEdgeUniColor()) {
                            edgeHasNodeColorButton.setSelected(!vizConfig.isEdgeUniColor());
                        }
                    }
                }
            });
            components[1] = edgeHasNodeColorButton;


            //Show labels buttons
            final JToggleButton showLabelsButton = new JToggleButton();
            showLabelsButton.setSelected(vizConfig.isShowEdgeLabels());
            showLabelsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showLabels"));
            showLabelsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/showLabels.png")));
            showLabelsButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    vizConfig.setShowEdgeLabels(showLabelsButton.isSelected());
                }
            });
            vizConfig.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("showEdgeLabels")) {
                        if (showLabelsButton.isSelected() != vizConfig.isShowEdgeLabels()) {
                            showLabelsButton.setSelected(vizConfig.isShowEdgeLabels());
                        }
                    }
                }
            });
            components[2] = showLabelsButton;

            return components;
        }

        public JComponent getExtendedComponent() {
            EdgeSettingsPanel panel = new EdgeSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }

    private static class LabelGroupBar implements VizToolbarGroup {

        public String getName() {
            return "Labels";
        }

        public JComponent[] getToolbarComponents() {
            JComponent[] components = new JComponent[5];

            //Mode
            JPopupButton labelSizeModeButton = new JPopupButton() {

                @Override
                public void createPopup(JPopupMenu popupMenu) {
                    final TextManager textManager = VizController.getInstance().getTextManager();
                    for (final SizeMode sm : textManager.getSizeModes()) {
                        JRadioButtonMenuItem item = new JRadioButtonMenuItem(sm.getName(), sm.getIcon(), sm == textManager.getModel().getSizeMode());
                        item.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                textManager.getModel().setSizeMode(sm);
                            }
                        });
                        popupMenu.add(item);
                    }
                }
            };
            labelSizeModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/labelSizeMode.png")));
            labelSizeModeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.sizeMode"));
            components[0] = labelSizeModeButton;

            //Color mode
            JPopupButton labelColorModeButton = new JPopupButton() {

                @Override
                public void createPopup(JPopupMenu popupMenu) {
                    final TextManager textManager = VizController.getInstance().getTextManager();
                    for (final ColorMode cm : textManager.getColorModes()) {
                        JRadioButtonMenuItem item = new JRadioButtonMenuItem(cm.getName(), cm.getIcon(), cm == textManager.getModel().getColorMode());
                        item.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                textManager.getModel().setColorMode(cm);
                            }
                        });
                        popupMenu.add(item);
                    }
                }
            };
            labelColorModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/labelColorMode.png")));
            labelColorModeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.colorMode"));
            components[1] = labelColorModeButton;

            //Font
            final TextModel model = VizController.getInstance().getTextManager().getModel();
            final JButton fontButton = new JButton(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
            fontButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.font"));
            fontButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeFont());
                    if (font != null && font != model.getNodeFont()) {
                        model.setNodeFont(font);
                    }
                }
            });
            model.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    fontButton.setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
                }
            });
            components[2] = fontButton;

            //Font size
            final JSlider fontSizeSlider = new JSlider(0, 100, (int) (model.getNodeSizeFactor() * 100f));
            fontSizeSlider.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    model.setNodeSizeFactor(fontSizeSlider.getValue() / 100f);
                }
            });
            fontSizeSlider.setPreferredSize(new Dimension(100, 20));
            fontSizeSlider.setMaximumSize(new Dimension(100, 20));
            components[3] = fontSizeSlider;

            //Color
            final ColorChooser colorChooser = new ColorChooser(model.getNodeColor());
            colorChooser.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.defaultColor"));
            colorChooser.setPreferredSize(new Dimension(16, 16));
            colorChooser.setMaximumSize(new Dimension(16, 16));
            colorChooser.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    model.setNodeColor(colorChooser.getColor());
                }
            });

            components[4] = colorChooser;

            return components;
        }

        public JComponent getExtendedComponent() {
            return new JPanel();
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }
}
