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
package org.gephi.layout.api;

import org.gephi.layout.spi.Layout;
import org.gephi.workspace.api.Workspace;

/**
 * A LayoutController is the one responsible for controlling the states of
 * the {@link LayoutModel}
 * 
 * @author Mathieu Bastian
 */
public interface LayoutController {

    /**
     * Returns the model of the currently selected {@link Workspace}.
     */
    public LayoutModel getModel();

    /**
     * Sets the Layout to execute.
     * @param layout the layous that is to be selected
     */
    public void setLayout(Layout layout);

    /**
     * Executes the current Layout.
     */
    public void executeLayout();

    /**
     * Determine if the current Layout can be executed.
     * @return <code>true</code> if the layout is executable.
     */
    public boolean canExecute();

    /**
     * Stop the Layout's execution.
     */
    public void stopLayout();

    /**
     * Determine if the current Layout execution can be stopped.
     * If the current Layout is not running, it generally cannot be stopped.
     * @return <code>true</code> if the layout can be stopped.
     */
    public boolean canStop();
}
