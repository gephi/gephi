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
 *
 * @author Mathieu Bastian
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
     *
     * @return panels of the current importer
     */
    public WizardDescriptor.Panel[] getPanels();

    public void setup(SpigotImporter importer);

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
