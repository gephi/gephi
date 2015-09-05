/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.datalab.spi;

import javax.swing.JPanel;

/**
 * <p>UI Manipulators can provide.</p>
 * <p>Must provide a JPanel, a window name/title and indicate if it is modal.</p>
 * <p>The panel will be shown in a dialog with Ok/Cancel options only.</p>
 * <p>The ok button can be enabled/disabled with the <code>DialogControls</code> instance passed at setup</p>
 * @author Eduardo Ramos
 */
public interface ManipulatorUI {

    /**
     * Prepare this UI to be able to interact with its Manipulator.
     * @param m Manipulator for the UI
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
