/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.io.importer.spi;

import org.openide.WizardDescriptor;

/**
 * Define importer settings wizard user interface.
 * <p>
 * Declared in the system as services (i.e. singleton), the role of UI classes
 * is to provide user interface to configure importers and remember last used
 * settings if needed. This service is designed to provide the different panels
 * part of a spigot import wizard.
 * <p>
 * To be recognized by the system, implementations must just add the following annotation:
 * <pre>@ServiceProvider(service=ImporterWizardUI.class)</pre>
 *
 * @author Mathieu Bastian
 * @see SpigotImporter
 */
public interface ImporterWizardUI {

    /**
     * Returns the importer display name
     * @return          the importer display name
     */
    public String getDisplayName();

    /**
     * There are two levels for wizard UIs, the category and then the display name.
     * Returns the importer category.
     * @return          the importer category
     */
    public String getCategory();

    /**
     * Returns the description for this importer
     * @return          the description test
     */
    public String getDescription();

    /**
     * Returns wizard panels.
     * @return          panels of the current importer
     */
    public WizardDescriptor.Panel[] getPanels();

    /**
     * Configure <code>panel</code> with previously remembered settings. This method
     * is called after <code>getPanels()</code> to push settings.
     *
     * @param panel     the panel that settings are to be set
     */
    public void setup(WizardDescriptor.Panel panel);

    /**
     * Notify UI the settings panel has been closed and that new values can be
     * written. Settings can be read in <code>panel</code> and written
     * <code>importer</code>.
     * @param importer  the importer that settings are to be written
     * @param panel     the panel that settings are read
     */
    public void unsetup(SpigotImporter importer, WizardDescriptor.Panel panel);

    /**
     * Returns <code>true</code> if this UI belongs to the given importer.
     *
     * @param importer  the importer that has to be tested
     * @return          <code>true</code> if the UI is matching with <code>importer</code>,
     *                  <code>false</code> otherwise.
     */
    public boolean isUIForImporter(Importer importer);
}
