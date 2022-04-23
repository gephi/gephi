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

package org.gephi.branding.desktop.reporter;

import io.sentry.Attachment;
import io.sentry.protocol.User;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.SentryException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;

/**
 * @author Mathieu Bastian
 */
public class ReportController {

    private static final String POST_URL =
        "https://d007fbbdeb6241b5b2c542a6bc548cf3@o43889.ingest.sentry.io/85815";

    public ReportController() {
        Sentry.init(options -> {
            String gephiVersion = System.getProperty("netbeans.productversion");
            if (!gephiVersion.contains("SNAPSHOT")) {
                // Strip build
                gephiVersion = gephiVersion.substring(0, gephiVersion.length() - 13);
            }

            options.setDsn(POST_URL);
            options.setRelease(gephiVersion);
            options.setDiagnosticLevel(SentryLevel.ERROR);
            options.setServerName("Gephi Desktop");
            options.setEnvironment(gephiVersion.contains("SNAPSHOT") ? "development" : "production");
        });
    }

    public void sendReport(final Report report) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                ProgressHandle handle = ProgressHandleFactory
                    .createHandle(NbBundle.getMessage(ReportController.class, "ReportController.status.sending"));
                try {
                    handle.start();

                    sendSentryReport(report);

                    handle.finish();
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(ReportController.class, "ReportController.status.sent"),
                            NotifyDescriptor.INFORMATION_MESSAGE));
                    return;
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
                handle.finish();
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(ReportController.class, "ReportController.status.failed"),
                        NotifyDescriptor.WARNING_MESSAGE));
            }

        }, "Exception Reporter");
        thread.start();
    }

    private void sendSentryReport(Report report) {
        final User user = !report.getUserEmail().isEmpty() ? new User() : null;
        if (user != null) {
            user.setEmail(report.getUserEmail());
        }

        Sentry.withScope(scope -> {
            // Main
            scope.setLevel(SentryLevel.ERROR);

            // Extra
            scope.setContexts("OS", report.getOs());
            scope.setContexts("Heap memory usage", report.getHeapMemoryUsage());
            scope.setContexts("Non heap memory usage", report.getNonHeapMemoryUsage());
            scope.setContexts("Processors", report.getNumberOfProcessors());
            scope.setContexts("Screen devices", report.getScreenDevices());
            scope.setContexts("Screen size", report.getScreenSize());
            scope.setContexts("VM", report.getVm());
            scope.setContexts("OpenGL Vendor", report.getGlVendor());
            scope.setContexts("OpenGL Renderer", report.getGlRenderer());
            scope.setContexts("OpenGL Version", report.getGlVersion());
            scope.setContexts("Description", report.getUserDescription());

            //User
            scope.setUser(user);

            // Log
            Attachment log =
                new Attachment(anonymizeLog(report.getLog()).getBytes(StandardCharsets.UTF_8), "messages.log",
                    "text/plain");
            scope.addAttachment(log);

            // Send
            Sentry.captureException(report.getThrowable());
        });
    }

    public Document buildReportDocument(Report report) {
        logMessageLog(report);
        logVersion(report);
        logScreenSize(report);
        logCPU(report);
        logMemoryInfo(report);
        logJavaInfo(report);
        logGLInfo(report);
        //logModules(report);
        return buildXMLDocument(report);
    }

    private Document buildXMLDocument(Report report) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            report.writeXml(document);
            return document;
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private void logScreenSize(Report report) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        report.setScreenSize(screenSize);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        report.setScreenDevices(ge.getScreenDevices().length);
    }

    private void logCPU(Report report) {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        report.setNumberOfProcessors(bean.getAvailableProcessors());
        String unknown = "unknown";                                   // NOI18N
        String str = System.getProperty("os.name", unknown) + ", " + // NOI18N
            System.getProperty("os.version", unknown) + ", " + // NOI18N
            System.getProperty("os.arch", unknown);               // NOI18N

        report.setOs(str);
    }

    private void logMemoryInfo(Report report) {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        report.setHeapMemoryUsage(bean.getHeapMemoryUsage().toString());
        report.setNonHeapMemoryUsage(bean.getNonHeapMemoryUsage().toString());
    }

    private void logJavaInfo(Report report) {
        String str = System.getProperty("java.vm.name", "unknown") + ", " // NOI18N
            + System.getProperty("java.vm.version", "") + ", " // NOI18N
            + System.getProperty("java.runtime.name", "unknown") + ", " // NOI18N
            + System.getProperty("java.runtime.version", ""); // NOI18N
        report.setVm(str);
    }

    private void logVersion(Report report) {
        String str = System.getProperty("netbeans.productversion");
        report.setVersion(str);
    }

    private void logGLInfo(Report report) {
        String output = report.getLog();
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(output));
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                if (line.contains("GL_VENDOR:")) {
                    report.setGlVendor(line.replaceFirst(".*GL_VENDOR:", ""));
                } else if (line.contains("GL_RENDERER:")) {
                    report.setGlRenderer(line.replaceFirst(".*GL_RENDERER:", ""));
                } else if (line.contains("GL_VERSION:")) {
                    report.setGlVersion(line.replaceFirst(".*GL_VERSION:", ""));
                    break;
                }
            }
            lineNumberReader.close();
        } catch (Exception e) {
        }
    }

    private void logModules(Report report) {
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            String moduleStr = "";
            SpecificationVersion specVersion = m.getSpecificationVersion();
            if (specVersion != null) {
                moduleStr = m.getCodeName() + " [" + specVersion + "]";
            } else {
                moduleStr = m.getCodeName();
            }
            if (m.isEnabled()) {
                report.addEnabledModule(moduleStr);
            } else {
                report.addDisabledModule(moduleStr);
            }
        }
    }

    private void logMessageLog(Report report) {
        System.out.flush();
        Handler[] handlers = Logger.getLogger("").getHandlers();
        handlers[0].flush();
        File userDir = Places.getUserDirectory();
        File directory = new File(new File(userDir, "var"), "log");
        File messagesLog = new File(directory, "messages.log");
        String log = "";
        try {
            byte[] buffer = new byte[(int) messagesLog.length()];
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(messagesLog));
            f.read(buffer);
            log = new String(buffer);
        } catch (Exception e) {
        }
        report.setLog(log);
    }

    /**
     * Removes usernames from log files
     */
    protected static String anonymizeLog(String log) {
        return log.replaceAll(
            "(/((home)|(Users))/[^/\n]*)|(\\\\Users\\\\[^\\\\\n]*)",
            "/ANONYMIZED_HOME_DIR"); // NOI18N
    }
}
