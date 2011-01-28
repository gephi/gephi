/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.tools.spi;

/**
 * Tools are functions for interacting with user inputs on the visualization window.
 * <p>
 * A tool receive events from visualization window when it is currently the selected
 * tool. The visualization window toolbar presents all available tools implementations.
 * <p><b>Example:</b> A <i>Brush</i> tool colors clicked nodes.
 * <h3>How-to create a tool implementation</h3>
 * <ol><li>Create a class which implement <code>Tool</code> interface</li>
 * <li>Add the following annotation to your class to be declared as a new
 * implementation <code>@ServiceProvider(service=Tool.class)</code></li>
 * <li>Declare {@link ToolEventListener} instances for specifying how the
 * tool is interacting with user input like node click or mouse drag.</li>
 * <li>Provide a {@link ToolUI} instance for giving a name and an icon to your
 * tool.</li></ol>
 * @author Mathieu Bastian
 */
public interface Tool {

    /**
     * Notify when this tool is selected.
     */
    public void select();

    /**
     * Notify when this tool is unselected.
     */
    public void unselect();

    /**
     * Returns the declared tool listeners for this tool. Tool listeners says how
     * the tool is interacting with user input on the visualization window.
     * @return tool listeners declared for this tool implementation
     */
    public ToolEventListener[] getListeners();

    /**
     * Returns <code>ToolUI</code> instance for this tool.
     * @return the user interface attributes for this tool
     */
    public ToolUI getUI();

    /**
     * Returns the tool type of selection interaction.
     * @return the tool type of selection interaction
     */
    public ToolSelectionType getSelectionType();
}
