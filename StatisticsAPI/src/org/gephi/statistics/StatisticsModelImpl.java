/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.api.StatisticsModel;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
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
        reportMap = new HashMap<Class, String>();
    }

    public void addReport(Statistics statistics) {
        reportMap.put(statistics.getClass(), statistics.getReport());
    }

    public String getReport(Class<? extends Statistics> statisticsClass) {
        return reportMap.get(statisticsClass);
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("statisticsmodel");

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
            ex.printStackTrace();
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
                    e.printStackTrace();
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
