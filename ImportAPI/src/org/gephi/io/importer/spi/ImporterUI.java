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

import javax.swing.JPanel;

/**
 * Define importer settings user interface.
 * <p>
 * Declared in the system as services (i.e. singleton), the role of UI classes
 * is to provide user interface to configure importers and remember last used
 * settings if needed.
 * <p>
 * To be recognized by the system, implementations must just add the following annotation:
 * <pre>@ServiceProvider(service=ImporterUI.class)</pre>
 *
 * @author Mathieu Bastian
 * @see Importer
 */
public interface ImporterUI {

    /**
     * Link the UI to the importer and therefore to settings values. This method
     * is called after <code>getPanel()</code> to push settings.
     *
     * @param importer  the importer that settings is to be set
     */
    public void setup(Importer importer);

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
     *                  if CANCEL.
     */
    public void unsetup(boolean update);

    /**
     * Returns the importer display name
     * @return          the importer display name
     */
    public String getDisplayName();

    /**
     * Returns <code>true</code> if this UI belongs to the given importer.
     *
     * @param importer  the importer that has to be tested
     * @return          <code>true</code> if the UI is matching with <code>importer</code>,
     *                  <code>false</code> otherwise.
     */
    public boolean isUIForImporter(Importer importer);
}
