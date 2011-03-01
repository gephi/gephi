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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private final List<StatisticsUI> invisibleList;
    private final List<Statistics> runningList;
    private final Map<StatisticsUI, String> resultMap;
    private final Map<Class, String> reportMap;
    //Listeners
    private final List<ChangeListener> listeners;

    public StatisticsModelImpl() {
        invisibleList = new ArrayList<StatisticsUI>();
        runningList = Collections.synchronizedList(new ArrayList<Statistics>());
        listeners = new ArrayList<ChangeListener>();
        resultMap = new HashMap<StatisticsUI, String>();
        reportMap = new HashMap<Class, String>();
    }

    public void addReport(Statistics statistics) {
        reportMap.put(statistics.getClass(), statistics.getReport());
        fireChangeEvent();
    }

    public void addResult(StatisticsUI ui) {
        if (resultMap.containsKey(ui) && ui.getValue() == null) {
            resultMap.remove(ui);
        } else {
            resultMap.put(ui, ui.getValue());
        }
        fireChangeEvent();
    }

    public String getReport(Class<? extends Statistics> statisticsClass) {
        return reportMap.get(statisticsClass);
    }

    public String getResult(StatisticsUI statisticsUI) {
        return resultMap.get(statisticsUI);
    }

    public boolean isStatisticsUIVisible(StatisticsUI statisticsUI) {
        return !invisibleList.contains(statisticsUI);
    }

    public boolean isRunning(StatisticsUI statisticsUI) {
        for (Statistics s : runningList.toArray(new Statistics[0])) {
            if (statisticsUI.getStatisticsClass().equals(s.getClass())) {
                return true;
            }
        }
        return false;
    }

    public void setRunning(Statistics statistics, boolean running) {
        if (!running) {
            if (runningList.remove(statistics)) {
                fireChangeEvent();
            }
        } else if (!runningList.contains(statistics)) {
            runningList.add(statistics);
            fireChangeEvent();
        }
    }

    public Statistics getRunning(StatisticsUI statisticsUI) {
        for (Statistics s : runningList.toArray(new Statistics[0])) {
            if (statisticsUI.getStatisticsClass().equals(s)) {
                return s;
            }
        }
        return null;
    }

    public void setVisible(StatisticsUI statisticsUI, boolean visible) {
        if (visible) {
            if (invisibleList.remove(statisticsUI)) {
                fireChangeEvent();
            }
        } else if (!invisibleList.contains(statisticsUI)) {
            invisibleList.add(statisticsUI);
            fireChangeEvent();
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("statisticsmodel");

        writer.writeStartElement("results");
        for (Map.Entry<StatisticsUI, String> entry : resultMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                writer.writeStartElement("result");
                writer.writeAttribute("class", entry.getKey().getClass().getName());
                writer.writeAttribute("value", entry.getValue());
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();

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
                    if ("result".equalsIgnoreCase(name)) {
                        String classStr = reader.getAttributeValue(null, "class");
                        StatisticsUI resultUI = null;
                        for (StatisticsUI ui : uis) {
                            if (ui.getClass().getName().equals(classStr)) {
                                resultUI = ui;
                            }
                        }
                        if (resultUI != null) {
                            String value = reader.getAttributeValue(null, "value");
                            resultMap.put(resultUI, value);
                        }
                    } else if ("report".equalsIgnoreCase(name)) {
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
