/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
