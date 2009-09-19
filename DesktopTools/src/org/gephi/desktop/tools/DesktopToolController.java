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
import org.gephi.tools.api.NodeEventListener;
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

    private Tool[] tools;
    private Tool currentTool;
    private HashMap<Tool, VizEventListener> currentListeners;

    public DesktopToolController() {
        //Init tools
        tools = Lookup.getDefault().lookupAll(Tool.class).toArray(new Tool[0]);
        currentListeners = new HashMap<Tool, VizEventListener>();
    }

    public void select(Tool tool) {

        //Connect events
        for (ToolEventListener toolListener : tool.getListeners()) {
            VizEventListener listener = null;
            switch (toolListener.getType()) {
                case NODE_CLICKED:
                    final NodeEventListener nodeListener = (NodeEventListener) toolListener;
                    listener = new VizEventListener() {

                        public void handleEvent(VizEvent event) {
                            nodeListener.handleEvent((Node[]) event.getData());
                        }

                        public Type getType() {
                            return VizEvent.Type.NODE_LEFT_CLICK;
                        }
                    };
                    break;
                case NODE_PRESSED:

                    break;
            }

            if (listener != null) {
                currentListeners.put(tool, listener);
                VizController.getInstance().getVizEventManager().addListener(listener);
            }
        }
    }

    public void unselect() {
        if (currentTool != null) {
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
        for (ToolUI toolUI : toolsUI) {
            final Tool tool = toolMap.get(toolUI);
            JToggleButton btn = new JToggleButton(toolUI.getIcon());
            btn.setToolTipText(toolUI.getName() + " - " + toolUI.getDescription());
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    select(tool);
                }
            });
            toolbar.add(btn);
        }

        return toolbar;
    }

    public JComponent getPropertiesBar() {
        return new JPanel();
    }
}
