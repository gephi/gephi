/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.desktop.statistics.api.StatisticsModelUI;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.api.StatisticsController;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class StatisticsModelUIImpl implements StatisticsModelUI {

    private final Workspace workspace;
    private final List<StatisticsUI> invisibleList;
    private final Map<StatisticsUI, String> resultMap;
    private final List<Statistics> runningList;
    //Listeners
    private final List<ChangeListener> listeners;

    public StatisticsModelUIImpl(Workspace workspace) {
        this.workspace = workspace;
        runningList = Collections.synchronizedList(new ArrayList<Statistics>());
        invisibleList = new ArrayList<StatisticsUI>();
        resultMap = new HashMap<StatisticsUI, String>();
        listeners = new ArrayList<ChangeListener>();
    }

    public void addResult(StatisticsUI ui) {
        if (resultMap.containsKey(ui) && ui.getValue() == null) {
            resultMap.remove(ui);
        } else {
            resultMap.put(ui, ui.getValue());
        }
        fireChangeEvent();
    }

    public String getResult(StatisticsUI statisticsUI) {
        return resultMap.get(statisticsUI);
    }

    public String getReport(Class<? extends Statistics> statistics) {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        return controller.getModel(workspace).getReport(statistics);
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
        writer.writeStartElement("statisticsmodelui");

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

        writer.writeEndElement();
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        Collection<? extends StatisticsUI> uis = Lookup.getDefault().lookupAll(StatisticsUI.class);

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
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("statisticsmodelui".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }
}
