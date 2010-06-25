
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
package org.gephi.datalaboratory.spi.attributecolumns;

import java.awt.Image;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;

/**
 * <p>Manipulation action to use for DataLaboratory column manipulator buttons.</p>
 * <p>These are shown as drop down buttons and are able to:</p>
 * <ul>
 *  <li>Execute an action with 1 column</li>
 *  <li>Provide a name, description, type and order of appearance (position in group of its type)</li>
 *  <li>Indicate wether they can be executed on a specific AttributeColumn or not</li>
 *  <li>Provide and UI or not</li>
 *  <li>Provide and icon or not</li>
 * </ul>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsManipulator {

    /**
     * Execute this AttributeColumnsManipulator with the indicated table and column
     * @param table AttributeTable of the column
     * @param column AttributeColumn of the table to manipulate
     */
    void execute(AttributeTable table,AttributeColumn column);

    /**
     * Return name to show for this AttributeColumnsManipulator on the ui.
     * @return Name to show in UI
     */
    String getName();

    /**
     * Description of the AttributeColumnsManipulator.
     * @return Description
     */
    String getDescription();

    /**
     * Indicates if this AttributeColumnsManipulator can manipulate a specific AttributeColumn.
     * @return True if it can manipulate the column, false otherwise
     */
    boolean canManipulateColumn(AttributeTable table,AttributeColumn column);

    /**
     * Returns a ManipulatorUI for this Manipulator if it needs one.
     * @return ManipulatorUI for this Manipulator or null
     */
    AttributeColumnsManipulatorUI getUI();

    /**
     * Type of manipulator. This is used for separating the manipulators
     * in groups when shown. First types to show will be the lesser.
     * @return Type of this manipulator
     */
    int getType();

    /**
     * Returns a position value that indicates the position
     * of this AttributeColumnsManipulator in its type group. Less means upper.
     * @return This AttributeColumnsManipulator position
     */
    int getPosition();

    /**
     * Returns an icon for this AttributeColumnsManipulator if necessary.
     * @return Icon for the manipulator or null
     */
    Image getIcon();
}
