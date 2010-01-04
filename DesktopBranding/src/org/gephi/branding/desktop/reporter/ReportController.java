/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.branding.desktop.reporter;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ReportController {

    public void sendReport(Report report) {
        Thread thread = new Thread(new Runnable() {

            public void run() {

                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ReportController.class, "ReportController.status.sent"));
            }
        }, "Exception Reporter");
        thread.start();
    }

    private void logScreenSize(Report report) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        report.setScreenSize(screenSize);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        report.setScreenDevices(ge.getScreenDevices().length);
    }

    private void logCPUInfo(Report report) {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        report.setNumberOfProcessors(bean.getAvailableProcessors());
        report.setArch(bean.getArch());
        report.setOs(bean.getName());
        report.setOsVersion(bean.getVersion());
    }

    private void logMemoryInfo(Report report) {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        report.setHeapMemoryUsage(bean.getHeapMemoryUsage().toString());
        report.setNonHeapMemoryUsage(bean.getNonHeapMemoryUsage().toString());
    }

    private void logJavaInfo(Report report) {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        report.setVmName(bean.getVmName());
        report.setVmVendor(bean.getVmVendor());
        report.setVmVendor(bean.getVmVendor());
    }
}
