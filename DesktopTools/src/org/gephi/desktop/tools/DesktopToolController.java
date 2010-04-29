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
package org.gephi.desktop.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.NodePressingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.api.ToolController;
import org.gephi.tools.spi.NodePressAndDraggingEventListener;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizEvent;
import org.gephi.visualization.apiimpl.VizEvent.Type;
import org.gephi.visualization.apiimpl.VizEventListener;
import org.gephi.visualization.api.selection.SelectionManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ToolController.class)
public class DesktopToolController implements ToolController {

    //Architecture
    private Tool[] tools;
    private PropertiesBar propertiesBar;
    //Current tool
    private Tool currentTool;
    private ToolEventHandler[] currentHandlers;

    public DesktopToolController() {
        //Init tools
        tools = Lookup.getDefault().lookupAll(Tool.class).toArray(new Tool[0]);
    }

    public void select(Tool tool) {
        unselect();
        if (tool == null) {
            return;
        }

        //Connect events
        ArrayList<ToolEventHandler> handlers = new ArrayList<ToolEventHandler>();
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
                throw new RuntimeException("The ToolEventListener " + toolListener.getClass().getSimpleName() + " cannot be recognized");
            }
        }
        currentHandlers = handlers.toArray(new ToolEventHandler[0]);
        switch (tool.getSelectionType()) {
            case NONE:
                VizController.getInstance().getSelectionManager().disableSelection();
                break;
            case SELECTION:
                VizController.getInstance().getSelectionManager().blockSelection(true);
                VizController.getInstance().getSelectionManager().setDraggingEnable(false);
                break;
            case SELECTION_AND_DRAGGING:
                VizController.getInstance().getSelectionManager().blockSelection(true);
                VizController.getInstance().getSelectionManager().setDraggingEnable(true);
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
            if (propertiesBar != null) {
                propertiesBar.unselect();
            }
        }
    }

    public JComponent getToolbar() {

        //Get tools ui
        HashMap<ToolUI, Tool> toolMap = new HashMap<ToolUI, Tool>();
        List<ToolUI> toolsUI = new ArrayList<ToolUI>();
        for (Tool tool : tools) {
            ToolUI ui = tool.getUI();
            if (ui != null) {
                toolsUI.add(ui);
                toolMap.put(ui, tool);
            }

        }
        //Sort by priority
        Collections.sort(toolsUI, new Comparator() {

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
                btn = new JToggleButton(new ImageIcon(getClass().getResource("/org/gephi/desktop/tools/tool.png")));
            }
            btn.setToolTipText(toolUI.getName() + " - " + toolUI.getDescription());
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    //Let the user unselect a tool (by clicking on it again) without having to select other tool:
                    if (tool == currentTool) {
                        toolbar.clearSelection();
                        unselect();
                    } else {
                        select(tool);
                        propertiesBar.select(toolUI.getPropertiesBar(tool));
                    }
                }
            });
            toolbar.add(btn);
        }

        //SelectionManager events
        VizController.getInstance().getSelectionManager().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                SelectionManager selectionManager = VizController.getInstance().getSelectionManager();

                if (selectionManager.isRectangleSelection() && currentTool != null) {
                    toolbar.clearSelection();
                    unselect();
                } else if (selectionManager.isSelectionEnabled() && currentTool != null && currentTool.getSelectionType() == ToolSelectionType.NONE) {
                    toolbar.clearSelection();
                    unselect();
                } else if (selectionManager.isDraggingEnabled() && currentTool != null) {
                    toolbar.clearSelection();
                    unselect();
                }
            }
        });

        return toolbar;
    }

    public JComponent getPropertiesBar() {
        propertiesBar = new PropertiesBar();
        return propertiesBar;
    }

    //Event handlers classes
    private static interface ToolEventHandler {

        public void select();

        public void unselect();
    }

    //HANDLERS
    private static class NodeClickEventHandler implements ToolEventHandler {

        private NodeClickEventListener toolEventListener;
        private VizEventListener currentListener;

        public NodeClickEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (NodeClickEventListener) toolListener;
        }

        public void select() {
            currentListener = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    toolEventListener.clickNodes((Node[]) event.getData());
                }

                public Type getType() {
                    return VizEvent.Type.NODE_LEFT_CLICK;
                }
            };
            VizController.getInstance().getVizEventManager().addListener(currentListener);
        }

        public void unselect() {
            VizController.getInstance().getVizEventManager().removeListener(currentListener);
            currentListener = null;
            toolEventListener = null;
        }
    }

    private static class NodePressingEventHandler implements ToolEventHandler {

        private NodePressingEventListener toolEventListener;
        private VizEventListener[] currentListeners;

        public NodePressingEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (NodePressingEventListener) toolListener;
        }

        public void select() {
            currentListeners = new VizEventListener[2];
            currentListeners[0] = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    toolEventListener.pressingNodes((Node[]) event.getData());
                }

                public Type getType() {
                    return VizEvent.Type.NODE_LEFT_PRESSING;
                }
            };
            currentListeners[1] = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    toolEventListener.released();
                }

                public Type getType() {
                    return VizEvent.Type.MOUSE_RELEASED;
                }
            };
            VizController.getInstance().getVizEventManager().addListener(currentListeners);
        }

        public void unselect() {
            VizController.getInstance().getVizEventManager().removeListener(currentListeners);
            toolEventListener = null;
            currentListeners = null;
        }
    }

    private static class NodePressAndDraggingEventHandler implements ToolEventHandler {

        private NodePressAndDraggingEventListener toolEventListener;
        private VizEventListener[] currentListeners;

        public NodePressAndDraggingEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (NodePressAndDraggingEventListener) toolListener;
        }

        public void select() {
            currentListeners = new VizEventListener[3];
            currentListeners[0] = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    toolEventListener.pressNodes((Node[]) event.getData());
                }

                public Type getType() {
                    return VizEvent.Type.NODE_LEFT_PRESS;
                }
            };
            currentListeners[1] = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    float[] mouseDrag = (float[]) event.getData();
                    toolEventListener.drag(mouseDrag[0], mouseDrag[1]);
                }

                public Type getType() {
                    return VizEvent.Type.DRAG;
                }
            };
            currentListeners[2] = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    toolEventListener.released();
                }

                public Type getType() {
                    return VizEvent.Type.MOUSE_RELEASED;
                }
            };
            VizController.getInstance().getVizEventManager().addListener(currentListeners);
        }

        public void unselect() {
            VizController.getInstance().getVizEventManager().removeListener(currentListeners);
            toolEventListener = null;
            currentListeners = null;
        }
    }

    private static class MouseClickEventHandler implements ToolEventHandler {

        private MouseClickEventListener toolEventListener;
        private VizEventListener currentListener;

        public MouseClickEventHandler(ToolEventListener toolListener) {
            this.toolEventListener = (MouseClickEventListener) toolListener;
        }

        public void select() {
            currentListener = new VizEventListener() {

                public void handleEvent(VizEvent event) {
                    float[] data = (float[]) event.getData();
                    int[] viewport = new int[]{(int) data[0], (int) data[1]};
                    float[] threed = new float[]{data[2], data[3]};
                    toolEventListener.mouseClick(viewport, threed);
                }

                public Type getType() {
                    return VizEvent.Type.MOUSE_LEFT_CLICK;
                }
            };
            VizController.getInstance().getVizEventManager().addListener(currentListener);
        }

        public void unselect() {
            VizController.getInstance().getVizEventManager().removeListener(currentListener);
            toolEventListener = null;
            currentListener = null;
        }
    }
}
