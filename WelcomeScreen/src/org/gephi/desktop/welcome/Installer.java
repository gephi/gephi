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
package org.gephi.desktop.welcome;

import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        if (NbPreferences.forModule(WelcomeTopComponent.class).getBoolean(WelcomeTopComponent.STARTUP_PREF, Boolean.TRUE)) {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                public void run() {
                    WelcomeTopComponent welcomeTC = WelcomeTopComponent.findInstance();
                    Mode mode = WindowManager.getDefault().findMode("welcomemode");
                    if (mode != null) {
                        mode.dockInto(welcomeTC);
                        welcomeTC.open();
                    }
                }
            });
        }
    }
}
