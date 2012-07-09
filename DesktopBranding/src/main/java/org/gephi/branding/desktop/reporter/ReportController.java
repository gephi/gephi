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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
public class ReportController {

    private static final String POST_URL = "http://gephi.org/crashreporter/report.php";

    public void sendReport(final Report report) {
        Thread thread = new Thread(new Runnable() {

            public void run() {
                ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ReportController.class, "ReportController.status.sending"));
                try {
                    handle.start();
                    Document doc = buildReportDocument(report);
                    if (doc != null) {
                        if (sendDocument(doc)) {
                            handle.finish();
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(NbBundle.getMessage(ReportController.class, "ReportController.status.sent"),
                                    NotifyDescriptor.INFORMATION_MESSAGE));
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handle.finish();
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(ReportController.class, "ReportController.status.failed"),
                        NotifyDescriptor.WARNING_MESSAGE));
            }
        }, "Exception Reporter");
        thread.start();
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

    public boolean sendDocument(Document document) {
        try {
            //Get String from Document
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            String xmlString = "report=" + URLEncoder.encode(sw.toString(), "UTF-8");

            URL url = new URL(POST_URL);
            URLConnection con = url.openConnection();

            // specify that we will send output and accept input
            con.setDoInput(true);
            con.setDoOutput(true);

            con.setUseCaches(false);
            con.setDefaultUseCaches(false);

            // tell the web server what we are sending
            //con.setRequestProperty("Content-Type", "text/xml");
            //con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(xmlString);
            writer.flush();
            writer.close();

            // reading the response
            InputStreamReader reader = new InputStreamReader(con.getInputStream());

            StringBuilder buf = new StringBuilder();
            char[] cbuf = new char[2048];
            int num;

            while (-1 != (num = reader.read(cbuf))) {
                buf.append(cbuf, 0, num);
            }

            String serverResult = buf.toString();
            System.err.println("\nResponse from server:\n" + serverResult);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
            e.printStackTrace();
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
        String str = ""; // NOI18N
        try {
            str = MessageFormat.format(
                    NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
                    new Object[]{System.getProperty("netbeans.buildnumber")} // NOI18N
                    );
            report.setVersion(str);
        } catch (MissingResourceException ex) {
        }
    }

    private void logGLInfo(Report report) {
        String output = report.getLog();
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(output));
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                if (line.startsWith("GL_VENDOR:")) {
                    report.setGlVendor(line.substring(11));
                } else if (line.startsWith("GL_RENDERER:")) {
                    report.setGlRenderer(line.substring(13));
                } else if (line.startsWith("GL_VERSION:")) {
                    report.setGlVersion(line.substring(12));
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
                moduleStr = m.getCodeName() + " [" + specVersion.toString() + "]";
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
        System.err.flush();
        System.out.flush();
        String ud = System.getProperty("netbeans.user"); // NOI18N
        if (ud == null || "memory".equals(ud)) { // NOI18N
            return;
        }
        Handler[] handlers = Logger.getLogger("").getHandlers();
        handlers[0].flush();
        File userDir = new File(ud); // NOI18N
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
}
