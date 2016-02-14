/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.Set;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.visualization.VizController;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Utility class that tracks memory consumption and informs users when the free memory
 * is under 20mb.
 * <p>
 * The user has the choice to cancel or let this class increase the maximum memory and
 * reboot. It will also try to save the project.
 * 
 * @author Mathieu Bastian
 */
public class MemoryStarvationManager implements NotificationListener {

    private static final String APPNAME = "gephi";
    private static double reservedMemory = 20971520;
    private static String IMPORTER_THREAD = "Importer";
    private static String EXPORTER_THREAD = "Exporter";
    private static String GENERATOR_THREAD = "Generator";
    private static String PROJECT_THREAD = "Project IO";
    private static String STATISTICS_THREAD = "Statistics";
    private static String PREVIEW_THREAD = "Refresh Preview";
    private static boolean messageDelivered = false;

    public void startup() {
        ((NotificationEmitter) ManagementFactory.getMemoryMXBean()).addNotificationListener(this, null, null);
        List<MemoryPoolMXBean> mpbeans = ManagementFactory.getMemoryPoolMXBeans();
        MemoryPoolMXBean biggestHeap = null;
        long biggestSize = 0;
        for (MemoryPoolMXBean b : mpbeans) {
            if (b.getType() == MemoryType.HEAP) {
                /* Here we are making the leap of faith that the biggest
                 * heap is the tenured heap
                 */
                long size = b.getUsage().getMax();
                if (size > biggestSize) {
                    biggestSize = size;
                    biggestHeap = b;
                }
            }
        }
        if (biggestHeap != null) {
            long usageThreshold = (long) (biggestSize - reservedMemory);
            biggestHeap.setUsageThreshold(usageThreshold);
            biggestHeap.setCollectionUsageThreshold(usageThreshold);
        }
    }

    @Override
    public void handleNotification(Notification n, Object o) {
        if (messageDelivered) {
            return;
        }
        CompositeData cd = (CompositeData) n.getUserData();
        MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);

        suspendThreads();

        messageDelivered = true;

        //Dialog
        if (canIncreaseMemory()) {
            String messageBundle = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.canIncreaseMemory.message", getMb(Runtime.getRuntime().maxMemory()) + " mb", getMb(getMaximumXmx()) + " mb");
            String titleBundle = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.title");
            String increaseAndRestart = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.canIncreaseMemory.button");
            String cancelBundle = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.cancel");
            NotifyDescriptor msg = new NotifyDescriptor(messageBundle, titleBundle,
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{increaseAndRestart, cancelBundle}, increaseAndRestart);

            if (DialogDisplayer.getDefault().notify(msg) != increaseAndRestart) {
                resumeThreads();
                return;

            }

            String xmx = getMb(getMaximumXmx()) + "m";

            try {
                updateConfiguration(xmx);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            String messageBundle = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.canIncreaseMemory.message", getMb(Runtime.getRuntime().maxMemory()) + " mb");
            String titleBundle = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.title");
            String saveAndRestart = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.canIncreaseMemory.button");
            String cancelBundle = NbBundle.getMessage(MemoryStarvationManager.class, "OutOfMemoryError.cancel");
            NotifyDescriptor msg = new NotifyDescriptor(messageBundle, titleBundle,
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{saveAndRestart, cancelBundle}, saveAndRestart);

            if (DialogDisplayer.getDefault().notify(msg) != saveAndRestart) {
                resumeThreads();
                return;
            }
        }

        interruptThreads();

        freeSomeMemory();

        saveProject();

        restart();
    }

    private boolean isDebug() {
        String homePath;
        if (Utilities.isMac() || Utilities.isUnix()) {
            homePath = System.getProperty("netbeans.home");
        } else {
            homePath = System.getProperty("user.dir");
        }

        //When launched within Netbeans
        if (homePath.contains("NetBeans")) {
            return true;
        }
        return false;
    }

    private void updateConfiguration(String newXmx) throws IOException {
        if (isDebug()) {
            return;
        }
        String homePath;
        if (Utilities.isMac() || Utilities.isUnix()) {
            homePath = System.getProperty("netbeans.home");
        } else {
            homePath = System.getProperty("user.dir");
        }


        File etc = new File(homePath, "etc");
        if (!etc.exists()) {
            File base = new File(homePath).getParentFile();
            etc = new File(base, "etc");
        }

        File confFile = new File(etc, APPNAME + ".conf");
        StringBuilder outputBuilder = new StringBuilder();
        String match = "-J-Xmx";

        //In
        BufferedReader reader = new BufferedReader(new FileReader(confFile));
        String strLine;
        while ((strLine = reader.readLine()) != null) {
            int i = 0;
            if ((i = strLine.indexOf(match)) != -1) {
                String xmx = strLine.substring(i + match.length());
                xmx = xmx.substring(0, xmx.indexOf(" "));
                String before = strLine.substring(0, i + match.length());
                String after = strLine.substring(i + match.length() + xmx.length());
                outputBuilder.append(before);
                outputBuilder.append(newXmx);
                outputBuilder.append(after);
            } else {
                outputBuilder.append(strLine);
            }
            outputBuilder.append("\n");
        }
        reader.close();

        //Out
        FileWriter writer = new FileWriter(confFile);
        writer.write(outputBuilder.toString());
        writer.close();
    }

    private void saveProject() {
        ProjectControllerUI pui = Lookup.getDefault().lookup(ProjectControllerUI.class);
        if (pui.canSave()) {
            pui.saveProject();
        }
    }

    private boolean canIncreaseMemory() {
        long maxXmx = getMb(getMaximumXmx());
        long currentXmx = getMb(Runtime.getRuntime().maxMemory());
        return currentXmx < maxXmx;
    }

    private long getMaximumXmx() {
        OperatingSystemMXBean mxbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = mxbean.getTotalPhysicalMemorySize();
        String arch = System.getProperty("sun.arch.data.model");
        if (getMb(totalMemory) < 2100 || arch.equals("32")) {
            return getBytes(1300);
        } else {
            return (long) (totalMemory * 0.7);
        }
    }

    private long getMb(long bytes) {
        return bytes / 1024 / 1024;
    }

    private long getBytes(long mb) {
        return mb * 1024 * 1024;
    }

    private void suspendThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        // List every thread in the group
        for (Thread t : threadSet) {
            if (t.getName().startsWith(GENERATOR_THREAD)
                    || t.getName().startsWith(IMPORTER_THREAD)
                    || t.getName().startsWith(EXPORTER_THREAD)
                    || t.getName().startsWith(PROJECT_THREAD)
                    || t.getName().startsWith(STATISTICS_THREAD)
                    || t.getName().startsWith(PREVIEW_THREAD)) {
                if (t.isAlive()) {
                    t.suspend();
                }
            }
        }
    }

    private void resumeThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        // List every thread in the group
        for (Thread t : threadSet) {
            if (t.getName().startsWith(GENERATOR_THREAD)
                    || t.getName().startsWith(IMPORTER_THREAD)
                    || t.getName().startsWith(EXPORTER_THREAD)
                    || t.getName().startsWith(PROJECT_THREAD)
                    || t.getName().startsWith(STATISTICS_THREAD)
                    || t.getName().startsWith(PREVIEW_THREAD)) {
                if (t.isAlive()) {
                    t.resume();
                }
            }
        }
    }

    private void interruptThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        // List every thread in the group
        for (Thread t : threadSet) {
            if (t.getName().startsWith(GENERATOR_THREAD)
                    || t.getName().startsWith(IMPORTER_THREAD)
                    || t.getName().startsWith(EXPORTER_THREAD)
                    || t.getName().startsWith(STATISTICS_THREAD)
                    || t.getName().startsWith(PREVIEW_THREAD)) {
                t.interrupt();
            }
        }
    }

    private void freeSomeMemory() {
        //Stop Graphics engine and free all elements models. Should be enough
        VizController.getInstance().destroy();
    }

    private boolean canRestart() {
        return !Utilities.isMac() && !Utilities.isUnix();
    }

    private void restart() {
        if (isDebug()) {
            return;
        }
        //Restart
        if (canRestart()) {
            //On Mac the change is applied only if restarted manually
            LifecycleManager.getDefault().markForRestart();
        }
        LifecycleManager.getDefault().exit();
    }
}
