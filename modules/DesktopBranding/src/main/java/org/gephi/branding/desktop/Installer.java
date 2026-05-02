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

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.gephi.branding.desktop.reporter.ReportController;
import org.gephi.branding.desktop.reporter.ReporterHandler;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private static final String POST_URL =
        "https://d007fbbdeb6241b5b2c542a6bc548cf3@o43889.ingest.us.sentry.io/85815";
    private static final String LATEST_GEPHI_VERSION_URL =
        "https://raw.githubusercontent.com/gephi/gephi/gh-pages/latest";

    protected static boolean isNewVersion(String latest, String current) {
        String gephiVersionTst = current.replaceAll("[0-9]{12}", "").replaceAll("[a-zA-Z -]", "");
        latest = latest.replaceAll("[a-zA-Z -]", "");
        int res = ModuleDescriptor.Version.parse(gephiVersionTst)
            .compareTo(ModuleDescriptor.Version.parse(latest));
        return res < 0;
    }

    @Override
    public void restored() {
        //Init
        initGephi();

        //Init Sentry
        initSession();

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

        //Check for new major release:
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        checkForNewMajorRelease();
                    }
                }.start();
            }
        });

        //Analytics consent notification (30s after startup):
        WindowManager.getDefault().invokeWhenUIReady(() -> new Thread(() -> {
            try {
                Thread.sleep(15_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            showAnalyticsConsentNotification();
        }, "Analytics Consent Notification").start());

        //Output logger
        installOutputLogger();
    }

    @Override
    public void close() {
        closeSession();
    }

    private void initGephi() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                DragNDropFrameAdapter.register();
            }
        });

        if (Utilities.isMac()) {
            try {
                Desktop.getDesktop().setOpenFileHandler(new ProjectOpenFilesHandler());
            } catch (Exception e) {
                Logger.getLogger(Installer.class.getName())
                    .log(Level.WARNING, "Can't setup OpenFilesHandler", e);
            }
        }
    }

    private void initSession() {
        Sentry.init(options -> {
            String gephiVersion = System.getProperty("netbeans.productversion");
            if (!gephiVersion.contains("SNAPSHOT")) {
                // Strip build
                gephiVersion = gephiVersion.substring(0, gephiVersion.length() - 13);
            }

            options.setDsn(POST_URL);
            options.setRelease(gephiVersion);
            options.setDistinctId(SentryIdentity.getOrCreateDistinctId());
            options.setDiagnosticLevel(SentryLevel.ERROR);
            options.setServerName("Gephi Desktop");
            options.setEnvironment(gephiVersion.contains("SNAPSHOT") ? "development" : "production");
            // Gephi has its own ReporterHandler/ReportController for user-initiated exception
            // reporting with full context. Disabling automatic capture prevents Sentry from
            // sending a context-free event that would then deduplicate (and drop) the enriched
            // manual report submitted by the user.
            options.setEnableUncaughtExceptionHandler(false);
            options.setEnableDeduplication(false);
        });
        boolean trackUsage = NbPreferences.forModule(ReportController.class)
            .getBoolean(ReportController.TRACK_USAGE, ReportController.DEFAULT_TRACK_USAGE);
        if (trackUsage) {
            try {
                Sentry.startSession();
            } catch (Exception e) {
                Logger.getLogger(Installer.class.getName())
                    .log(Level.WARNING, "Can't start Sentry session", e);
            }
            try {
                Sentry.metrics().count("start", 1.0);
            } catch (Exception e) {
                Logger.getLogger(Installer.class.getName())
                    .log(Level.WARNING, "Can't count Sentry metric", e);
            }
        }
    }

    private void showAnalyticsConsentNotification() {
        java.util.prefs.Preferences prefs = NbPreferences.forModule(ReportController.class);
        boolean doNotRemind = prefs.getBoolean(ReportController.DO_NOT_REMIND_ANALYTICS, false);
        boolean bothEnabled = prefs.getBoolean(ReportController.SEND_CRASH_REPORTS, ReportController.DEFAULT_SEND_CRASH_REPORTS)
            && prefs.getBoolean(ReportController.TRACK_USAGE, ReportController.DEFAULT_TRACK_USAGE);
        if (doNotRemind || bothEnabled) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Frame mainFrame = WindowManager.getDefault().getMainWindow();

            JDialog dialog = new JDialog(mainFrame);
            dialog.setUndecorated(true);
            dialog.setAlwaysOnTop(true);
            dialog.setFocusableWindowState(false);

            JLabel titleLabel = new JLabel(NbBundle.getMessage(Installer.class, "AnalyticsConsent.notification.title"));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

            JLabel msgLabel = new JLabel("<html><body style='width:220px'>" +
                NbBundle.getMessage(Installer.class, "AnalyticsConsent.notification.details") +
                "</body></html>");
            msgLabel.setFont(msgLabel.getFont().deriveFont(msgLabel.getFont().getSize() - 1f));

            JButton enableBtn = new JButton(NbBundle.getMessage(Installer.class, "AnalyticsConsent.notification.enable"));
            enableBtn.addActionListener(e -> {
                prefs.putBoolean(ReportController.SEND_CRASH_REPORTS, true);
                prefs.putBoolean(ReportController.TRACK_USAGE, true);
                dialog.dispose();
            });

            JButton settingsBtn = new JButton(NbBundle.getMessage(Installer.class, "AnalyticsConsent.notification.button"));
            settingsBtn.addActionListener(e -> {
                dialog.dispose();
                OptionsDisplayer.getDefault().open("Gephi/Analytics");
            });

            JButton dismissBtn = new JButton("✕");
            dismissBtn.setMargin(new Insets(0, 4, 0, 4));
            dismissBtn.setFont(dismissBtn.getFont().deriveFont(dismissBtn.getFont().getSize() - 1f));
            dismissBtn.addActionListener(e -> dialog.dispose());

            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setOpaque(false);
            header.add(titleLabel, BorderLayout.CENTER);
            header.add(dismissBtn, BorderLayout.EAST);

            JPanel buttons = new JPanel(new BorderLayout(6, 0));
            buttons.setOpaque(false);
            buttons.add(enableBtn, BorderLayout.CENTER);
            buttons.add(settingsBtn, BorderLayout.EAST);

            JPanel content = new JPanel(new BorderLayout(0, 8));
            content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("controlShadow"), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            content.add(header, BorderLayout.NORTH);
            content.add(msgLabel, BorderLayout.CENTER);
            content.add(buttons, BorderLayout.SOUTH);

            dialog.add(content);
            dialog.pack();

            // Position at bottom-right of the main frame
            Rectangle frameBounds = mainFrame.getBounds();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = Math.min(frameBounds.x + frameBounds.width - dialog.getWidth() - 16,
                screenSize.width - dialog.getWidth() - 8);
            int y = Math.min(frameBounds.y + frameBounds.height - dialog.getHeight() - 48,
                screenSize.height - dialog.getHeight() - 48);
            dialog.setLocation(x, y);
            dialog.setVisible(true);

            Timer timer = new Timer(15_000, e -> dialog.dispose());
            timer.setRepeats(false);
            timer.start();
        });
    }

    private void closeSession() {
        try {
            boolean trackUsage = NbPreferences.forModule(ReportController.class)
                .getBoolean(ReportController.TRACK_USAGE, ReportController.DEFAULT_TRACK_USAGE);
            if (trackUsage) {
                Sentry.endSession();
            }
            Sentry.flush(500);
        } finally {
            Sentry.close();
        }
    }

    private void checkForNewMajorRelease() {
        boolean doCheck =
            NbPreferences.forModule(Installer.class).getBoolean("check_latest_version", true);
        if (doCheck) {
            InputStream stream = null;
            BufferedReader reader = null;
            try {
                String gephiVersion = System.getProperty("netbeans.productversion");
                if (gephiVersion.contains("SNAPSHOT")) {
                    return;
                }

                URL url = new URL(LATEST_GEPHI_VERSION_URL);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                connection.connect();
                stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                String latest = reader.readLine();

                if (isNewVersion(latest, gephiVersion)) {
                    //Show update dialog
                    JCheckBox checkbox =
                        new JCheckBox(NbBundle.getMessage(Installer.class, "MajorReleaseCheck.dontShowAgain"),
                            false);
                    String message =
                        NbBundle
                            .getMessage(Installer.class, "MajorReleaseCheck.message", latest, gephiVersion);
                    int option = JOptionPane.showConfirmDialog(null, new Object[] {message, checkbox},
                        NbBundle.getMessage(Installer.class, "MajorReleaseCheck.newVersion"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    NbPreferences.forModule(Installer.class)
                        .putBoolean("check_latest_version", !checkbox.isSelected());
                    if (option == JOptionPane.OK_OPTION) {
                        Desktop.getDesktop().browse(new URI("http://gephi.org/users/download/"));
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger("").warning("Error while checking latest Gephi version");
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {

                }
            }
        }
    }

    private void installOutputLogger() {
        Logger.getLogger("").addHandler(new OutputHandler());
    }

    private static class OutputHandler extends Handler {

        private final InputOutput io;
        private final OutputWriter outputWriter;
        private final MsgFormatter formatter;

        public OutputHandler() {
            io = IOProvider.getDefault().getIO("Log", true);
            outputWriter = io.getOut();
            formatter = new MsgFormatter();
        }

        @Override
        public void publish(LogRecord record) {
            if ((record.getMessage() == null || record.getMessage().isEmpty()) &&
                record.getThrown() == null) {
                //Nothing to log
                return;
            }

            Color color = Color.BLACK;
            if (record.getLevel().equals(Level.WARNING)) {
                color = Color.ORANGE;
            } else if (record.getLevel().equals(Level.SEVERE)) {
                color = Color.RED;
            }

            String msg = formatter.format(record);
            if (IOColorLines.isSupported(io)) {
                try {
                    IOColorLines.println(io, msg, color);
                } catch (IOException ex) {
                    outputWriter.println(msg);
                }
            } else {
                outputWriter.println(msg);
            }
        }

        @Override
        public void flush() {
            outputWriter.flush();
        }

        @Override
        public void close() throws SecurityException {
            outputWriter.close();
        }

        public class MsgFormatter extends Formatter {

            @Override
            public synchronized String format(LogRecord record) {
                String formattedMessage = formatMessage(record);
                String throwable = "";
                String outputFormat = "[%1$s] %2$s %3$s"; //Also adding for logging exceptions
                if (record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    pw.println();
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    throwable = sw.toString();
                }
                return String
                    .format(outputFormat, record.getLevel().getName(), formattedMessage, throwable);
            }
        }
    }
}
