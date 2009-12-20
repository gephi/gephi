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
package org.gephi.tools.spi;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Tool's user interface attributes: name, description, icon and a properties bar.
 *
 * @author Mathieu Bastian
 */
public interface ToolUI {

    /**
     * Returns the tool's properties bar. The properties bar is used for tool's
     * settings.
     * @param tool the tool instance
     * @return a <code>JPanel</code> for the the tool's properties bar
     */
    public JPanel getPropertiesBar(Tool tool);

    /**
     * Returns the tool icon, for the toobar.
     */
    public Icon getIcon();

    /**
     * Returns the tool's name.
     */
    public String getName();

    /**
     * Returns the tool's description.
     */
    public String getDescription();

    /**
     * Returns the tool relative position. Smaller is the position, higher is
     * the position in the toolbar.
     * @return A number between 0 and 200
     */
    public int getPosition();
}
