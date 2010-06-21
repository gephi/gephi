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
package org.gephi.tools.api;

import javax.swing.JComponent;
import org.gephi.tools.spi.Tool;

/**
 * Controller for visualization toolbar.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>ToolController tc = Lookup.getDefault().lookup(ToolController.class);</pre>
 * @author Mathieu Bastian
 */
public interface ToolController {

    /**
     * Selects <code>tool</code> as the active tool and therefore unselect the
     * current tool, if exists.
     * @param tool  the tool that is to be selected
     */
    public void select(Tool tool);

    /**
     * Returns the toolbar component, build from tools implementations.
     * @return      the toolbar component
     */
    public JComponent getToolbar();

    /**
     * Returns the properties bar component, that display tools settings.
     * @return      the properties bar component
     */
    public JComponent getPropertiesBar();


}
