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
package org.gephi.branding.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.gephi.branding.desktop.reporter.ReporterHandler;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.project.api.ProjectController;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
        try {
            //Fix old preview loading - bug 873148
            doDisable("org.gephi.preview");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Init
        initGephi();

        //TopTab
        UIManager.put("ViewTabDisplayerUI", "org.gephi.branding.desktop.NoTabsTabDisplayerUI");

        //GTK Slider issue #529913
        UIManager.put("Slider.paintValue", Boolean.FALSE);

        //Handler
        if (System.getProperty("org.gephi.crashReporter.enabled", "true").equals("true")) {
            Logger.getLogger("").addHandler(new ReporterHandler());
        }

        //Memory Starvation Manager
        if (System.getProperty("org.gephiMemoryStarvationManager.enabled", "true").equals("true")) {
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

    public void doDisable(String codeName) throws Exception {
        FileObject confFileObject = FileUtil.getConfigFile("Modules/" + codeName.replace('.', '-') + ".xml");

        if (confFileObject != null && confFileObject.isValid()) {
            StringBuilder outputBuilder = new StringBuilder();

            String matchOptionsLine = "<param name=\"enabled\">true";

            //In
            File confFile = FileUtil.toFile(confFileObject);
            BufferedReader reader = new BufferedReader(new FileReader(confFile));
            String strLine;
            boolean matched = false;
            while ((strLine = reader.readLine()) != null) {
                if (strLine.indexOf(matchOptionsLine) != -1) {
                    matched = true;
                    strLine = strLine.replaceAll("<param name=\"enabled\">true</param>", "<param name=\"enabled\">false</param>");
                }
                outputBuilder.append(strLine);
                outputBuilder.append("\n");
            }
            reader.close();

            //Out
            FileWriter writer = new FileWriter(confFile);
            writer.write(outputBuilder.toString());
            writer.close();

            if (matched) {
                JOptionPane.showMessageDialog(null, "A custom patch to import your 0.8 alpha settings has been applied and Gephi needs to restart now. Sorry for the inconvenience.");

                //Restart
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            }
        }
    }
}
