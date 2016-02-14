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
package org.gephi.ui.upgrader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author mbastian
 */
public class Upgrader {

    private final static String UPGRADER_LAST_VERSION = "Upgrader_Last_Version";
    private final static List<String> VERSION_TO_CHECK
            = Arrays.asList(new String[]{"0.9.0"});

    public void upgrade() {
        String currentVersion = getCurrentVersion();
        Logger.getLogger("").log(Level.INFO, "Current Version is {0}", currentVersion);

        String lastVersion = NbPreferences.forModule(Upgrader.class).get(UPGRADER_LAST_VERSION, null);
        if (lastVersion == null || !lastVersion.equals(currentVersion)) {
            File latestPreviousVersion = checkPrevious();
            if (latestPreviousVersion != null && !latestPreviousVersion.getName().replace(".", "").equals(currentVersion)) {
                File source = new File(latestPreviousVersion, "dev");
                File dest = new File(System.getProperty("netbeans.user"));
                if (source.exists() && dest.exists()) {
                    NbPreferences.forModule(Upgrader.class).put(UPGRADER_LAST_VERSION, currentVersion);

                    boolean confirm = showUpgradeDialog(latestPreviousVersion);
                    if (confirm) {
                        try {
                            CopyFiles.copyDeep(source, dest);

                            //Restart
                            if (showRestartDialog()) {
                                LifecycleManager.getDefault().markForRestart();
                                LifecycleManager.getDefault().exit();
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }

    private boolean showRestartDialog() {
        String msg = NbBundle.getMessage(Upgrader.class, "Upgrader.restart.message");
        String title = NbBundle.getMessage(Upgrader.class, "Upgrader.restart.title");
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            return true;
        }
        return false;
    }

    private boolean showUpgradeDialog(File source) {
        String msg = NbBundle.getMessage(Upgrader.class, "Upgrader.message", source.getName());
        String title = NbBundle.getMessage(Upgrader.class, "Upgrader.title");
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            return true;
        }
        return false;
    }

    private String getCurrentVersion() {
        File userDir = new File(System.getProperty("netbeans.user"));
        if (userDir.getName().equalsIgnoreCase("testuserdir")) {
            return userDir.getName();
        }
        return userDir.getParentFile().getName().replace(".", "");
    }

    private File checkPrevious() {
        File userDir = new File(System.getProperty("netbeans.user"));
        File sourceFolder = null;

        if (userDir.exists()) {
            File userHomeFile;
            if (userDir.getName().equalsIgnoreCase("userdir")) {
                userHomeFile = userDir.getParentFile();
            } else {
                userHomeFile = userDir.getParentFile().getParentFile();
            }
            Iterator<String> it = VERSION_TO_CHECK.iterator();
            String ver;
            while (it.hasNext() && sourceFolder == null) {
                ver = it.next();
                sourceFolder = new File(userHomeFile.getAbsolutePath(), ver);

                if (sourceFolder.isDirectory()) {
                    break;
                }
                sourceFolder = new File(userHomeFile.getAbsolutePath(), "." + ver);
                if (sourceFolder.isDirectory()) {
                    break;
                }
                sourceFolder = null;
            }
            return sourceFolder;
        } else {
            return null;
        }
    }
}
