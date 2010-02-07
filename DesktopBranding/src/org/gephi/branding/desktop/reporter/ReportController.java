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
import java.text.MessageFormat;
import java.util.MissingResourceException;
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
            String xmlString = "report=" + sw.toString();

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
