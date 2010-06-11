
/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.spi;

import org.gephi.datalaboratory.spi.nodes.NodesManipulator;

/**
 * <p>General and abstract manipulation action to use for DataLaboratory tables.</p>
 * <p>These are shown on right click on one or more elements of a table and are able to:</p>
 * <ul>
 *  <li>Execute an action</li>
 *  <li>Provide a name, description and order of appearance (position)</li>
 *  <li>Indicate wether they have to be shown or not</li>
 *  <li>Provide and UI or not</li>
 * </ul>
 * <p>Used for NodesManipulator and EdgesManipulator.</p>
 * @see NodesManipulator
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface GraphElementsManipulator {

    /**
     * Execute this GraphElementsManipulator.
     * It will operate with data like nodes and edge previously set up.
     */
    void execute();

    /**
     * Return name to show for this GraphElementsManipulator on the context menu.
     * Implementations should provide different names depending on the data this
     * GraphElementsManipulator has (for example depending on the number of nodes in a NodeManipulator).
     * @return Name to show at current time and conditions
     */
    String getName();

    /**
     * Description of the GraphElementsManipulator.
     * TODO: Maybe show as tooltip?
     * @return Description
     */
    String getDescription();

    /**
     * Indicates if this GraphElementsManipulator has to be shown.
     * Implementations should evaluate the current data and conditions.
     * @return True if it has to be shown, false otherwise
     */
    boolean show();

    /**
     * Returns a GraphElementsManipulatorUI for this GraphElementsManipulator if it needs one.
     * @return GraphElementsManipulatorUI for this GraphElementsManipulator or null
     */
    GraphElementsManipulatorUI getUI();

    /**
     * Returns a position value that indicates the position
     * of this GraphElementsManipulator in the context menu. Less means upper.
     * @return This GraphElementsManipulator position
     */
    int getPosition();
}
