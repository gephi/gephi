
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

import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;

/**
 * UI AttributeColumnsManipulators can provide.
 * This itself provides a JPanel and a window name/title.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsManipulatorUI {

    /**
     * Prepare this UI to be able to interact with its AttributeColumnsManipulator.
     * @param m Manipulator for the UI
     * @param table Table of the column to manipulate
     * @param column Column to manipulate
     */
    void setup(AttributeColumnsManipulator m, AttributeTable table, AttributeColumn column);

    /**
     * Called when the window is closed or accepted.
     */
    void unSetup();

    /**
     * Returns name/title for the window
     * @return Name/title for the window
     */
    String getDisplayName();

    /**
     * Returns a settings panel instance for this AttributeColumnsManipulator.
     * @return Settings panel instance
     */
    public JPanel getSettingsPanel();

    /**
     * Indicates if the created dialog has to be modal
     * @return True if modal, false otherwise
     */
    public boolean isModal();
}
