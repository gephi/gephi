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
package org.gephi.datalab.spi;

import javax.swing.JPanel;

/**
 * <p>UI Manipulators can provide.</p>
 * <p>Must provide a JPanel, a window name/title and indicate if it is modal.</p>
 * <p>The panel will be shown in a dialog with Ok/Cancel options only.</p>
 * <p>The ok button can be enabled/disabled with the <code>DialogControls</code> instance passed at setup</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface ManipulatorUI {

    /**
     * Prepare this UI to be able to interact with its Manipulator.
     * @param gem Manipulator for the UI
     * @param dialogControls Used to enable/disable the dialog controls
     */
    void setup(Manipulator m, DialogControls dialogControls);

    /**
     * Called when the dialog is closed, canceled or accepted.
     */
    void unSetup();

    /**
     * Returns name/title for the window
     * @return Name/title for the window
     */
    String getDisplayName();

    /**
     * Returns a settings panel instance for this Manipulator.
     * @return Settings panel instance
     */
    public JPanel getSettingsPanel();

    /**
     * Indicates if the created dialog has to be modal
     * @return True if modal, false otherwise
     */
    public boolean isModal();
}
