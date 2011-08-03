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
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.gephi.branding.desktop.reporter.ReporterHandler;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.project.api.ProjectController;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
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
        if (NbPreferences.forModule(Installer.class).getBoolean("CrashReporter.enabled", true)) {
            Logger.getLogger("").addHandler(new ReporterHandler());
        }

        //Memory Starvation Manager
        if (NbPreferences.forModule(Installer.class).getBoolean("MemoryStarvationManager.enabled", true)) {
            MemoryStarvationManager memoryStarvationManager = new MemoryStarvationManager();
            memoryStarvationManager.startup();
        }
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

    @Override
    public boolean closing() {
        if (Lookup.getDefault().lookup(ProjectController.class).getCurrentProject() == null) {
            //Close directly if no project open
            return true;
        }

        int option = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(Installer.class, "CloseConfirmation.message"), NbBundle.getMessage(Installer.class, "CloseConfirmation.message"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            Lookup.getDefault().lookup(ProjectControllerUI.class).saveProject();
        } else if (option == JOptionPane.CANCEL_OPTION) {
            return false;//Exit canceled
        }
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
        return true;
    }
}
