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
package org.gephi.branding.desktop;

import java.util.logging.Logger;
import javax.swing.UIManager;
import org.gephi.branding.desktop.reporter.ReporterHandler;
import org.gephi.project.api.ProjectController;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        initGephi();

        //TopTab
        UIManager.put("ViewTabDisplayerUI", "org.gephi.branding.desktop.NoTabsTabDisplayerUI");

        //GTK Slider issue #529913
        UIManager.put("Slider.paintValue", Boolean.FALSE);

        //Handler
        Logger.getLogger("").addHandler(new ReporterHandler());
    }

    private void initGephi() {
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            public void run() {
                pc.startup();
                DragNDropFrameAdapter.register();
            }
        });
    }
}
