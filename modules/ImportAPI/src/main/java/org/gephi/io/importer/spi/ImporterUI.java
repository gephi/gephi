/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.io.importer.spi;

import javax.swing.JPanel;
import org.openide.WizardDescriptor;

/**
 * Define importer settings user interface.
 * <p>
 * Declared in the system as services (i.e. singleton), the role of UI classes
 * is to provide user interface to configure importers and remember last used
 * settings if needed.
 * <p>
 * To be recognized by the system, implementations must just add the following
 * annotation:
 * <pre>@ServiceProvider(service=ImporterUI.class)</pre>
 *
 * @author Mathieu Bastian
 * @see Importer
 */
public interface ImporterUI {
    
    /**
     * Optional interface to implement for {@link ImporterUI} classes that need a Wizard
     */
    public interface WithWizard {
        /**
         * Used to retreive the wizard descriptor for the Importer UI.
         * @return Wizard descriptor for the UI
         */
        public WizardDescriptor getWizardDescriptor();
    }

    /**
     * Link the UI to the importers and therefore to settings values. This
     * method is called after <code>getPanel()</code> to push settings.
     *
     * @param importers the importers that settings is to be set
     */
    public void setup(Importer[] importers);

    /**
     * Returns the importer settings panel.
     *
     * @return a settings panel, or <code>null</code>
     */
    public JPanel getPanel();

    /**
     * Notify UI the settings panel has been closed and that new values can be
     * written.
     *
     * @param update    <code>true</code> if user clicked OK or <code>false</code>
     * if CANCEL.
     */
    public void unsetup(boolean update);

    /**
     * Returns the importer display name
     *
     * @return the importer display name
     */
    public String getDisplayName();

    /**
     * Returns <code>true</code> if this UI belongs to the given importer.
     *
     * @param importer the importer that has to be tested
     * @return          <code>true</code> if the UI is matching with
     * <code>importer</code>, <code>false</code> otherwise.
     */
    public boolean isUIForImporter(Importer importer);
}
