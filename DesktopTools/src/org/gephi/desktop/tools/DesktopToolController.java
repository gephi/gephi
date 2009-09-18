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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.gephi.tools.api.Tool;
import org.gephi.tools.api.ToolController;
import org.gephi.ui.tools.ToolUI;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopToolController implements ToolController {

    private Tool[] tools;

    public DesktopToolController() {
        //Init tools
        tools = Lookup.getDefault().lookupAll(Tool.class).toArray(new Tool[0]);
    }

    public JComponent getToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        List<ToolUI> toolsUI = new ArrayList<ToolUI>();
        for (Tool tool : tools) {
            ToolUI ui = tool.getUI();
            if (ui != null) {
                toolsUI.add(ui);
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

        //Create toolbar buttons
        for (ToolUI toolUI : toolsUI) {
        }
        return toolbar;
    }

    public JComponent getPropertiesBar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
