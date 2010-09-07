
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
package org.gephi.datalab.spi.columns;

import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.spi.DialogControls;

/**
 * <p>UI AttributeColumnsManipulators can provide.</p>
 * <p>Must provide a JPanel, a window name/title and indictate if it is modal.</p>
 * <p>The panel will be shown in a dialog with Ok/Cancel options only.</p>
 * <p>The ok button can be enabled/disabled with the DialogControls instance passed at setup</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsManipulatorUI {

    /**
     * Prepare this UI to be able to interact with its AttributeColumnsManipulator.
     * @param m Manipulator for the UI
     * @param table Table of the column to manipulate
     * @param column Column to manipulate
     * @param dialogControls Used to enable/disable the dialog controls
     */
    void setup(AttributeColumnsManipulator m, AttributeTable table, AttributeColumn column, DialogControls dialogControls);

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
