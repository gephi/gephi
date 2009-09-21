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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.NodeClickEventListener;
import org.gephi.tools.api.NodePressingEventListener;
import org.gephi.tools.api.Tool;
import org.gephi.tools.api.ToolController;
import org.gephi.tools.api.ToolEventListener;
import org.gephi.ui.tools.ToolUI;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VizEvent;
import org.gephi.visualization.api.VizEvent.Type;
import org.gephi.visualization.api.VizEventListener;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
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
            } else {
                throw new RuntimeException("The ToolEventListener " + toolListener.getClass().getSimpleName() + " cannot be recognized");
            }
        }
        currentHandlers = handlers.toArray(new ToolEventHandler[0]);
        currentTool = tool;
    }

    public void unselect() {

        if (currentTool != null) {
            //Disconnect events
            for (ToolEventHandler handler : currentHandlers) {
                handler.unselect();
            }
            currentHandlers = null;
            currentTool = null;
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
        Toolbar toolbar = new Toolbar();
        for (final ToolUI toolUI : toolsUI) {
            final Tool tool = toolMap.get(toolUI);
            JToggleButton btn = new JToggleButton(toolUI.getIcon());
            btn.setToolTipText(toolUI.getName() + " - " + toolUI.getDescription());
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    propertiesBar.unselect();
                    select(tool);
                    propertiesBar.select(toolUI.getPropertiesBar(tool));
                }
            });
            toolbar.add(btn);
        }

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
}
