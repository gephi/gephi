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
        invisibleList = new ArrayList<>();
        resultMap = new HashMap<>();
        listeners = new ArrayList<>();
    }

    public void addResult(StatisticsUI ui) {
        if (resultMap.containsKey(ui) && ui.getValue() == null) {
            resultMap.remove(ui);
        } else {
            resultMap.put(ui, ui.getValue());
        }
        fireChangeEvent();
    }

    @Override
    public String getResult(StatisticsUI statisticsUI) {
        return resultMap.get(statisticsUI);
    }

    @Override
    public String getReport(Class<? extends Statistics> statistics) {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        return controller.getModel(workspace).getReport(statistics);
    }

    @Override
    public boolean isStatisticsUIVisible(StatisticsUI statisticsUI) {
        return !invisibleList.contains(statisticsUI);
    }

    @Override
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

    @Override
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

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    //PERSISTENCE
    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {

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
