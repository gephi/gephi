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

import java.util.Observer;

/**
 * A LayoutController is the one responsible for controlling the run states of
 * a Layout and provide the states information to its user (i.e. the UI).
 * 
 * The Observer pattern is used to inform the user of the LayoutController of
 * a change in the execution state of the Layout (e.g. the current Layout may
 * now be executed or stopped).
 * 
 * @author Mathieu Bastian
 */
public interface LayoutController {

    public LayoutModel getModel();

    /**
     * Sets the Layout this Controller will run.
     * @param layout
     */
    public void setLayout(Layout layout);

    /**
     * Executes the current Layout.
     */
    public void executeLayout();

    /**
     * Determine if the current Layout can be executed.
     * @return
     */
    public boolean canExecute();

    /**
     * Stop the Layout's execution.
     */
    public void stopLayout();

    /**
     * Determine if the current Layout execution can be stopped.
     * If the current Layout is not running, it generally cannot be stopped.
     * @return
     */
    public boolean canStop();
}
