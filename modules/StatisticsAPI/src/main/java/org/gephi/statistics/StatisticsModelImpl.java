/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.statistics;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import org.gephi.statistics.api.StatisticsModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Patrick J. McSweeney
 */
public class StatisticsModelImpl implements StatisticsModel {

    //Model  
    private final Map<Class, String> reportMap;

    public StatisticsModelImpl() {
        reportMap = new HashMap<>();
    }

    public void addReport(Statistics statistics) {
        reportMap.put(statistics.getClass(), statistics.getReport());
    }

    @Override
    public String getReport(Class<? extends Statistics> statisticsClass) {
        return reportMap.get(statisticsClass);
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("reports");
        for (Map.Entry<Class, String> entry : reportMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                writer.writeStartElement("report");
                String report = entry.getValue();
                report = embedImages(report);
                writer.writeAttribute("class", entry.getKey().getName());
                writer.writeAttribute("value", report);
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        Collection<? extends StatisticsUI> uis = Lookup.getDefault().lookupAll(StatisticsUI.class);
        Collection<? extends StatisticsBuilder> builders = Lookup.getDefault().lookupAll(StatisticsBuilder.class);

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("report".equalsIgnoreCase(name)) {
                        String classStr = reader.getAttributeValue(null, "class");
                        Class reportClass = null;
                        for (StatisticsBuilder builder : builders) {
                            if (builder.getStatisticsClass().getName().equals(classStr)) {
                                reportClass = builder.getStatisticsClass();
                            }
                        }
                        if (reportClass != null) {
                            String report = reader.getAttributeValue(null, "value");
                            report = unembedImages(report);
                            reportMap.put(reportClass, report);
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("statisticsmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    private String unembedImages(String report) {
        StringBuilder builder = new StringBuilder();
        String[] result = report.split("data:image/png;base64");
        if (result.length == 0) {
            return report;
        }
        try {
            TempDir tempDir = TempDirUtils.createTempDir();

            for (int i = 0; i < result.length; i++) {
                if (result[i].contains("</IMG>")) {
                    String next = result[i];
                    int endIndex = next.indexOf('\"');
                    String pngStr = next.substring(0, endIndex);
                    byte[] imageBytes = Base64.decodeBase64(pngStr);
                    String fileName = "image" + i + ".png";
                    File file = tempDir.createFile(fileName);

                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(imageBytes);

                    String path = "file:" + file.getAbsolutePath();
                    builder.append(path);

                    builder.append(next.substring(endIndex, next.length()));
                } else {
                    builder.append(result[i]);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return builder.toString();
    }

    private String embedImages(String report) {
        StringBuilder builder = new StringBuilder();
        String[] result = report.split("file:");
        boolean first = true;
        for (int i = 0; i < result.length; i++) {
            if (result[i].contains("</IMG>")) {
                String next = result[i];
                String[] elements = next.split("\"");
                String filename = elements[0];

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                File file = new File(filename);
                try {
                    BufferedImage image = ImageIO.read(file);
                    ImageIO.write((RenderedImage) image, "PNG", out);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
                byte[] imageBytes = out.toByteArray();
                String base64String = Base64.encodeBase64String(imageBytes);
                if (!first) {

                    builder.append("\"");
                }
                first = false;
                builder.append("data:image/png;base64,");
                builder.append(base64String);
                for (int j = 1; j < elements.length; j++) {
                    builder.append("\"");
                    builder.append(elements[j]);
                }
            } else {
                builder.append(result[i]);
            }
        }
        return builder.toString();
    }
}
