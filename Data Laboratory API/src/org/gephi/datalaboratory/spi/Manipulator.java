
/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.spi;

import javax.swing.Icon;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;

/**
 * <p>General and abstract manipulation action to use for Data Laboratory table UI.</p>
 * <p>Different subtypes of manipulators are defined for every type of action in the UI.</p>
 * <p>All manipulator types are able to:</p>
 * <ul>
 *  <li>Execute an action</li>
 *  <li>Provide a name, description, type and order of appearance (position in group of its type)</li>
 *  <li>Indicate wether they have to be executable or not</li>
 *  <li>Provide and UI or not</li>
 *  <li>Provide and icon or not</li>
 * </ul>
 * <p>Used for different manipulators such as NodesManipulator, EdgesManipulator and GeneralActionsManipulator.</p>
 * <p>The only methods that are called before setting up a manipulator (subtypes have special setup methods) with the data are getType and getPosition.
 * This way, the other methods behaviour can depend on the data that has been setup before</p>
 * @see NodesManipulator
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface Manipulator {

    /**
     * Execute this Manipulator.
     * It will operate with data like nodes and edges previously setup for the type of manipulator.
     */
    void execute();

    /**
     * <p>Return name to show for this Manipulator on the ui.</p>
     * <p>Implementations can provide different names depending on the data this
     * Manipulator has (for example depending on the number of nodes in a NodesManipulator).</p>
     * @return Name to show at current time and conditions
     */
    String getName();

    /**
     * Description of the Manipulator.
     * @return Description
     */
    String getDescription();

    /**
     * Indicates if this Manipulator has to be shown.
     * Implementations should evaluate the current data and conditions.
     * @return True if it has to be shown, false otherwise
     */
    boolean canExecute();

    /**
     * Returns a ManipulatorUI for this Manipulator if it needs one.
     * @return ManipulatorUI for this Manipulator or null
     */
    ManipulatorUI getUI();

    /**
     * Type of manipulator. This is used for separating the manipulators
     * in groups when shown, using popup separators. First types to show will be the lesser.
     * @return Type of this manipulator
     */
    int getType();

    /**
     * Returns a position value that indicates the position
     * of this Manipulator in its type group. Less means upper.
     * @return This Manipulator position
     */
    int getPosition();

    /**
     * Returns an icon for this manipulator if necessary.
     * @return Icon for the manipulator or null
     */
    Icon getIcon();
}
