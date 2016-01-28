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

import org.openide.WizardDescriptor;

/**
 * Define importer settings wizard user interface.
 * <p>
 * Declared in the system as services (i.e. singleton), the role of UI classes
 * is to provide user interface to configure importers and remember last used
 * settings if needed. This service is designed to provide the different panels
 * part of an import wizard.
 * <p>
 * To be recognized by the system, implementations must just add the following
 * annotation:
 * <pre>@ServiceProvider(service=ImporterWizardUI.class)</pre>
 *
 * @author Mathieu Bastian
 * @see WizardImporter
 */
public interface ImporterWizardUI {

    /**
     * Returns the importer display name
     *
     * @return the importer display name
     */
    public String getDisplayName();

    /**
     * There are two levels for wizard UIs, the category and then the display
     * name. Returns the importer category.
     *
     * @return the importer category
     */
    public String getCategory();

    /**
     * Returns the description for this importer
     *
     * @return the description test
     */
    public String getDescription();

    /**
     * Returns wizard panels.
     *
     * @return panels of the current importer
     */
    public WizardDescriptor.Panel[] getPanels();

    /**
     * Configure <code>panel</code> with previously remembered settings. This
     * method is called after <code>getPanels()</code> to push settings.
     *
     * @param panel the panel that settings are to be set
     */
    public void setup(WizardDescriptor.Panel panel);

    /**
     * Notify UI the settings panel has been closed and that new values can be
     * written. Settings can be read in <code>panel</code> and written
     * <code>importer</code>.
     *
     * @param importer the importer that settings are to be written
     * @param panel the panel that settings are read
     */
    public void unsetup(WizardImporter importer, WizardDescriptor.Panel panel);

    /**
     * Returns <code>true</code> if this UI belongs to the given importer.
     *
     * @param importer the importer that has to be tested
     * @return          <code>true</code> if the UI is matching with
     * <code>importer</code>, <code>false</code> otherwise.
     */
    public boolean isUIForImporter(Importer importer);
}
