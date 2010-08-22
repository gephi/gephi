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
package org.gephi.ui.spigot.plugin;

import javax.swing.JPanel;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.spigot.plugin.SampleSpigot;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ImporterUI.class)
public class SampleSpigotUI implements ImporterUI {

    private SampleSpigotPanel panel;

    public void setup(Importer importer) {
    }

    public JPanel getPanel() {
        panel = new SampleSpigotPanel();
        return panel;
    }

    public void unsetup(boolean update) {
        panel = null;
    }

    public String getDisplayName() {
        return "Sample";
    }

    public boolean isUIForImporter(Importer importer) {
        return importer instanceof SampleSpigot;
    }
}
