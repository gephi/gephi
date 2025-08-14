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

package org.gephi.desktop.visualization.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.ToolController;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.NodePressAndDraggingEventListener;
import org.gephi.tools.spi.NodePressingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolUI;
import org.gephi.tools.spi.UnselectToolException;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationEvent;
import org.gephi.visualization.api.VisualizationEventListener;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ToolController.class)
public class DesktopToolController implements ToolController {

    //Architecture
    private final Tool[] tools;
    private final VisualizationController visualizationController;
    private ToolsPropertiesBar toolsPropertiesBar;
    //Current tool
    private Tool currentTool;
    private ToolEventHandler[] currentHandlers;

    public DesktopToolController() {
        //Init tools
        tools = Lookup.getDefault().lookupAll(Tool.class).toArray(new Tool[0]);
        visualizationController = Lookup.getDefault().lookup(VisualizationController.class);
    }

    @Override
    public void select(Tool tool) {
        unselect();
        if (tool == null) {
            return;
        }

        //Connect events
        ArrayList<ToolEventHandler> handlers = new ArrayList<>();
        for (ToolEventListener toolListener : tool.getListeners()) {
            if (toolListener instanceof NodeClickEventListener) {
                NodeClickEventHandler h = new NodeClickEventHandler(toolListener);
                h.select();
                handlers.add(h);
            } else if (toolListener instanceof NodePressingEventListener) {
                NodePressingEventHandler h = new NodePressingEventHandler(toolListener);
                h.select();
                handlers.add(h);
            } else if (toolListener instanceof MouseClickEventListener) {
                MouseClickEventHandler h = new MouseClickEventHandler(toolListener);
                h.select();
                handlers.add(h);
            } else if (toolListener instanceof NodePressAndDraggingEventListener) {
                NodePressAndDraggingEventHandler h = new NodePressAndDraggingEventHandler(toolListener);
                h.select();
                handlers.add(h);

            } else {
                throw new RuntimeException(
                        "The ToolEventListener " + toolListener.getClass().getSimpleName() + " cannot be recognized");
            }
        }
        currentHandlers = handlers.toArray(new ToolEventHandler[0]);
        switch (tool.getSelectionType()) {
            case NONE:
                visualizationController.disableSelection();
                break;
            case SELECTION:
            case SELECTION_AND_DRAGGING:
                visualizationController.setDirectMouseSelection(true);
                break;
        }
        currentTool = tool;
        currentTool.select();
    }

    public void unselect() {
        if (currentTool != null) {
            //Disconnect events
            for (ToolEventHandler handler : currentHandlers) {
                handler.unselect();
            }
            currentTool.unselect();
            currentHandlers = null;
            currentTool = null;
            if (toolsPropertiesBar != null) {
                toolsPropertiesBar.unselect();
            }
        }
    }

    public JComponent getToolbar() {

        //Get tools ui
        HashMap<ToolUI, Tool> toolMap = new HashMap<>();
        List<ToolUI> toolsUI = new ArrayList<>();
        for (Tool tool : tools) {
            ToolUI ui = tool.getUI();
            if (ui != null) {
                toolsUI.add(ui);
                toolMap.put(ui, tool);
            }

        }
        //Sort by priority
        Collections.sort(toolsUI, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                Integer p1 = ((ToolUI) o1).getPosition();
                Integer p2 = ((ToolUI) o2).getPosition();
                return p1.compareTo(p2);
            }
        });

        //Create toolbar
        final Toolbar toolbar = new Toolbar();
        for (final ToolUI toolUI : toolsUI) {
            final Tool tool = toolMap.get(toolUI);
            JToggleButton btn;
            if (toolUI.getIcon() != null) {
                btn = new JToggleButton(toolUI.getIcon());
            } else {
                btn = new JToggleButton(ImageUtilities.loadImageIcon("VisualizationImpl/tool.png", false));
            }
            btn.setFocusPainted(false);
            btn.setToolTipText(toolUI.getName() + " - " + toolUI.getDescription());
            btn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //Let the user unselect a tool (by clicking on it again) without having to select other tool:
                    if (tool == currentTool) {
                        toolbar.clearSelection();
                        unselect();

                        // Go back to selection
                        visualizationController.setDirectMouseSelection(false);
                    } else {
                        try {
                            select(tool);
                            toolsPropertiesBar.select(toolUI.getPropertiesBar(tool));
                        } catch (UnselectToolException unselectToolException) {
                            toolbar.clearSelection();
                            unselect();
                        }
                    }
                }
            });
            toolbar.add(btn);
        }

        //SelectionManager events
        visualizationController.addPropertyChangeListener(new VisualizationPropertyChangeListener() {

            @Override
            public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("selection")) {
                    if (currentTool != null && !model.isCustomSelection() && !model.isSingleNodeSelection()) {
                        toolbar.clearSelection();
                        unselect();
                    }
                }
            }
        });

        return toolbar;
    }

    public JComponent getPropertiesBar() {
        toolsPropertiesBar = new ToolsPropertiesBar();
        return toolsPropertiesBar;
    }

    //Event handlers classes
    private interface ToolEventHandler {

        void select();

        void unselect();
    }

    //HANDLERS
    private static class NodeClickEventHandler implements ToolEventHandler {

        private NodeClickEventListener toolEventListener;
        private VisualizationEventListener currentListener;

        public NodeClickEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (NodeClickEventListener) toolListener;
        }

        @Override
        public void select() {
            currentListener = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    return toolEventListener.clickNodes((Node[]) event.getData());
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.NODE_LEFT_CLICK;
                }
            };
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.addListener(currentListener);
        }

        @Override
        public void unselect() {
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.removeListener(currentListener);
            currentListener = null;
            toolEventListener = null;
        }
    }

    private static class NodePressingEventHandler implements ToolEventHandler {

        private NodePressingEventListener toolEventListener;
        private VisualizationEventListener[] currentListeners;

        public NodePressingEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (NodePressingEventListener) toolListener;
        }

        @Override
        public void select() {
            currentListeners = new VisualizationEventListener[2];
            currentListeners[0] = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    return toolEventListener.pressingNodes((Node[]) event.getData());
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.NODE_LEFT_PRESSING;
                }
            };
            currentListeners[1] = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    return toolEventListener.released();
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.MOUSE_RELEASED;
                }
            };
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.addListener(currentListeners[0]);
            vizController.addListener(currentListeners[1]);
        }

        @Override
        public void unselect() {
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.removeListener(currentListeners[0]);
            vizController.removeListener(currentListeners[1]);
            toolEventListener = null;
            currentListeners = null;
        }
    }

    private static class NodePressAndDraggingEventHandler implements ToolEventHandler {

        private NodePressAndDraggingEventListener toolEventListener;
        private VisualizationEventListener[] currentListeners;

        public NodePressAndDraggingEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (NodePressAndDraggingEventListener) toolListener;
        }

        @Override
        public void select() {
            currentListeners = new VisualizationEventListener[3];
            currentListeners[0] = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    return toolEventListener.pressNodes((Node[]) event.getData());
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.NODE_LEFT_PRESS;
                }
            };
            currentListeners[1] = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    float[] mouseDrag = (float[]) event.getData();
                    return toolEventListener.drag(
                            // Screen coordinates displacement:
                            mouseDrag[0], mouseDrag[1],
                            // World coordinates displacement:
                            mouseDrag[2], mouseDrag[3]);
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.DRAG;
                }
            };
            currentListeners[2] = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    toolEventListener.released();

                    return false;//Never consume release events
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.STOP_DRAG;
                }
            };
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.addListener(currentListeners[0]);
            vizController.addListener(currentListeners[1]);
            vizController.addListener(currentListeners[2]);
        }

        @Override
        public void unselect() {
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.removeListener(currentListeners[0]);
            vizController.removeListener(currentListeners[1]);
            vizController.removeListener(currentListeners[2]);
            toolEventListener = null;
            currentListeners = null;
        }
    }

    private static class MouseClickEventHandler implements ToolEventHandler {

        private MouseClickEventListener toolEventListener;
        private VisualizationEventListener currentListener;

        public MouseClickEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (MouseClickEventListener) toolListener;
        }

        @Override
        public void select() {
            currentListener = new VisualizationEventListener() {

                @Override
                public boolean handleEvent(VisualizationEvent event) {
                    float[] data = (float[]) event.getData();
                    int[] viewport = new int[]{(int) data[0], (int) data[1]};
                    float[] worldPosition = new float[]{data[2], data[3]};

                    return toolEventListener.mouseClick(viewport, worldPosition);
                }

                @Override
                public VisualizationEvent.Type getType() {
                    return VisualizationEvent.Type.MOUSE_LEFT_CLICK;
                }
            };
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.addListener(currentListener);
        }

        @Override
        public void unselect() {
            VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
            vizController.removeListener(currentListener);
            toolEventListener = null;
            currentListener = null;
        }
    }
}
