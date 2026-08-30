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
import io.sentry.Hint;
import io.sentry.UserFeedback;
import io.sentry.protocol.SentryId;
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
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import io.sentry.Sentry;
import org.gephi.branding.desktop.SentryIdentity;
import org.openide.util.NbPreferences;
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

    public static final String SEND_CRASH_REPORTS = "send_crash_reports";
    public static final boolean DEFAULT_SEND_CRASH_REPORTS = false;

    public static final String TRACK_USAGE = "track_usage";
    public static final boolean DEFAULT_TRACK_USAGE = false;

    public static final String DISABLE_ALL_TRACKING = "disable_all_tracking";

    /** Set to true once the user has interacted with the consent popup (any button, including close). */
    public static final String ANALYTICS_CONSENT_SHOWN = "analytics_consent_shown";

    public void sendReport(final Report report) {
        boolean autoSend = NbPreferences.forModule(ReportController.class)
            .getBoolean(SEND_CRASH_REPORTS, DEFAULT_SEND_CRASH_REPORTS);
        String sendingKey = autoSend ? "ReportController.autoSend.status.sending" : "ReportController.manual.status.sending";
        String sentKey = autoSend ? "ReportController.autoSend.status.sent" : "ReportController.manual.status.sent";

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandleFactory
                    .createHandle(NbBundle.getMessage(ReportController.class, sendingKey));
                try {
                    handle.start();
                    sendSentryReport(report, autoSend);
                    handle.finish();
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(ReportController.class, sentKey),
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

    private void sendSentryReport(Report report, boolean autoSend) {
        String username = report.getUserGitHubUsername();
        if (!username.isEmpty()) {
            NbPreferences.forModule(ReportController.class).put("github_username", username);
        }

        if (!autoSend) {
            // Manual mode: capture the full exception first to obtain a SentryId.
            captureExceptionToSentry(report);
        }

        // Both modes: attach user feedback (username + description) to the Sentry event.
        SentryId eventId = report.getSentryEventId();
        if (eventId == null || SentryId.EMPTY_ID.equals(eventId)) {
            throw new RuntimeException("No Sentry event ID — capture may have failed");
        }
        if (!username.isEmpty() || !report.getUserDescription().isEmpty()) {
            UserFeedback feedback = new UserFeedback(eventId);
            if (!username.isEmpty()) {
                feedback.setName("@" + username.replace("@", ""));
            }
            if (!report.getUserDescription().isEmpty()) {
                feedback.setComments(report.getUserDescription());
            }
            Sentry.captureUserFeedback(feedback);
        }
    }

    /**
     * Captures the exception to Sentry with all available system context and the log attachment.
     * In auto-send mode, called from {@link ReporterHandler#publish} immediately when the
     * exception is logged. In manual mode, called from {@link #sendSentryReport} when the user
     * clicks "Send Report". GitHub username and description are included if already set on the
     * report.
     */
    void captureExceptionToSentry(Report report) {
        Attachment log =
            new Attachment(anonymizeLog(report.getLog()).getBytes(StandardCharsets.UTF_8), "messages.log",
                "text/plain");
        Hint hint = Hint.withAttachment(log);

        final User user = new User();
        user.setId(SentryIdentity.getOrCreateDistinctId());

        final SentryId[] eventId = {SentryId.EMPTY_ID};
        Sentry.withIsolationScope(scope -> {
            scope.setUser(user);
            scope.setTag("OS", report.getOs());
            scope.setContexts("Heap memory usage", report.getHeapMemoryUsage());
            scope.setContexts("Non heap memory usage", report.getNonHeapMemoryUsage());
            scope.setContexts("Processors", report.getNumberOfProcessors());
            scope.setContexts("Screen devices", report.getScreenDevices());
            scope.setContexts("Screen size", report.getScreenSize());
            scope.setContexts("VM", report.getVm());
            scope.setContexts("OpenGL Profile", report.getGlProfile());
            scope.setContexts("OpenGL Vendor", report.getGlVendor());
            scope.setContexts("OpenGL Renderer", report.getGlRenderer());
            scope.setContexts("OpenGL Version", report.getGlVersion());

            eventId[0] = Sentry.captureException(report.getThrowable(), hint);
        });
        report.setSentryEventId(eventId[0]);
    }

    /**
     * Populates the report with system information. Called from {@link ReporterHandler#publish}
     * immediately when the exception is logged, before the Sentry capture, so all context is
     * available at capture time regardless of whether the user opens the dialog.
     */
    void populateSystemInfo(Report report) {
        logMessageLog(report);
        logVersion(report);
        logScreenSize(report);
        logCPU(report);
        logMemoryInfo(report);
        logJavaInfo(report);
        logGLInfo(report);
    }

    public Document buildReportDocument(Report report) {
        populateSystemInfo(report);
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
                if (line.contains("Chosen GL Profile: ")) {
                    report.setGlProfile(line.replaceFirst(".*Chosen GL Profile: ", "").trim());
                } else if (line.contains("OpenGL Vendor: ")) {
                    String rest = line.replaceFirst(".*OpenGL Vendor: ", "");
                    int rendererIdx = rest.indexOf(", Renderer: ");
                    if (rendererIdx >= 0) {
                        report.setGlVendor(rest.substring(0, rendererIdx).trim());
                        rest = rest.substring(rendererIdx + ", Renderer: ".length());
                        int versionIdx = rest.indexOf(", Version: ");
                        if (versionIdx >= 0) {
                            report.setGlRenderer(rest.substring(0, versionIdx).trim());
                            report.setGlVersion(rest.substring(versionIdx + ", Version: ".length()).trim());
                        } else {
                            report.setGlRenderer(rest.trim());
                        }
                    } else {
                        report.setGlVendor(rest.trim());
                    }
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
