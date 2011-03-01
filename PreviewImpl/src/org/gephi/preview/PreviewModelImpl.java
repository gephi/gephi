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
package org.gephi.preview;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.preview.api.supervisors.Supervisor;
import org.gephi.preview.api.supervisors.UndirectedEdgeSupervisor;
import org.gephi.preview.presets.DefaultPreset;
import org.gephi.preview.supervisors.BidirectionalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.GlobalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.NodeSupervisorImpl;
import org.gephi.preview.supervisors.SelfLoopSupervisorImpl;
import org.gephi.preview.supervisors.UndirectedEdgeSupervisorImpl;
import org.gephi.preview.supervisors.UnidirectionalEdgeSupervisorImpl;
import org.gephi.project.api.Workspace;
import org.openide.nodes.Node.Property;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewModelImpl implements PreviewModel, GraphListener {

    //Supervisors
    private final NodeSupervisorImpl nodeSupervisor;
    private final GlobalEdgeSupervisorImpl globalEdgeSupervisor;
    private final SelfLoopSupervisorImpl selfLoopSupervisor;
    private final UnidirectionalEdgeSupervisorImpl uniEdgeSupervisor;
    private final BidirectionalEdgeSupervisorImpl biEdgeSupervisor;
    private final UndirectedEdgeSupervisorImpl undirectedEdgeSupervisor;
    //States
    private boolean updateFlag = true;
    private float visibilityRatio = 1;
    private Color backgroundColor = Color.WHITE;
    private PreviewPreset currentPreset;

    public PreviewModelImpl() {
        nodeSupervisor = new NodeSupervisorImpl();
        globalEdgeSupervisor = new GlobalEdgeSupervisorImpl();
        selfLoopSupervisor = new SelfLoopSupervisorImpl();
        uniEdgeSupervisor = new UnidirectionalEdgeSupervisorImpl();
        biEdgeSupervisor = new BidirectionalEdgeSupervisorImpl();
        undirectedEdgeSupervisor = new UndirectedEdgeSupervisorImpl();
        currentPreset = new DefaultPreset();
        applyPreset(currentPreset);
    }

    public void select(Workspace workspace) {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        graphModel.addGraphListener(this);
    }

    public void unselect(Workspace workspace) {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        graphModel.removeGraphListener(this);
    }

    /**
     * Sets the update flag when the structure of the workspace graph has
     * changed.
     *
     * @see GraphListener#graphChanged(org.gephi.graph.api.GraphEvent)
     */
    public void graphChanged(GraphEvent event) {
        updateFlag = true;
    }

    /**
     * Clears the supervisors' lists of supervised elements.
     */
    public void clearSupervisors() {
        nodeSupervisor.clearSupervised();
        globalEdgeSupervisor.clearSupervised();
        selfLoopSupervisor.clearSupervised();
        uniEdgeSupervisor.clearSupervised();
        biEdgeSupervisor.clearSupervised();
    }

    public NodeSupervisor getNodeSupervisor() {
        return nodeSupervisor;
    }

    public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
        return globalEdgeSupervisor;
    }

    public SelfLoopSupervisor getSelfLoopSupervisor() {
        return selfLoopSupervisor;
    }

    public DirectedEdgeSupervisor getUniEdgeSupervisor() {
        return uniEdgeSupervisor;
    }

    public DirectedEdgeSupervisor getBiEdgeSupervisor() {
        return biEdgeSupervisor;
    }

    public UndirectedEdgeSupervisor getUndirectedEdgeSupervisor() {
        return undirectedEdgeSupervisor;
    }

    public boolean isUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(boolean updateFlag) {
        this.updateFlag = updateFlag;
    }

    public float getVisibilityRatio() {
        return visibilityRatio;
    }

    public void setVisibilityRatio(float visibilityRatio) {
        this.visibilityRatio = visibilityRatio;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public PreviewPreset getCurrentPreset() {
        return currentPreset;
    }

    public void setCurrentPreset(PreviewPreset currentPreset) {
        this.currentPreset = currentPreset;
    }

    public PreviewPreset wrapPreset(String name) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        for (Property p : getPropertiesMap().values()) {
            try {
                Object propertyValue = p.getValue();
                if (propertyValue != null) {
                    PropertyEditor editor = p.getPropertyEditor();
                    if (editor == null) {
                        editor = PropertyEditorManager.findEditor(p.getValueType());
                    }
                    if (editor != null) {
                        editor.setValue(propertyValue);
                        String val = editor.getAsText();
                        if (!val.isEmpty()) {
                            propertiesMap.put(p.getName(), val);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new PreviewPreset(name, propertiesMap);
    }

    public void applyPreset(PreviewPreset preset) {
        Map<String, String> propertiesMap = preset.getProperties();
        for (Property p : getPropertiesMap().values()) {
            try {
                PropertyEditor editor = p.getPropertyEditor();
                if (editor == null) {
                    editor = PropertyEditorManager.findEditor(p.getValueType());
                }
                if (editor != null) {
                    String valueStr = propertiesMap.get(p.getName());
                    if (valueStr != null && !valueStr.isEmpty()) {
                        if (p.getValueType().equals(Font.class)) { //bug 551877
                            p.setValue(Font.decode(valueStr));
                        } else {
                            editor.setAsText(valueStr);
                            Object value = editor.getValue();
                            if (value != null) {
                                p.setValue(value);
                            }
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //PERSISTENCE
    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("previewmodel");

        //Ratio
        writer.writeStartElement("ratio");
        writer.writeCharacters(String.valueOf(visibilityRatio));
        writer.writeEndElement();

        //Color
        writer.writeStartElement("backgroundcolor");
        writer.writeCharacters("" + backgroundColor.getRGB());
        writer.writeEndElement();

        //Properties
        writeProperties(writer, nodeSupervisor);
        writeProperties(writer, globalEdgeSupervisor);
        writeProperties(writer, selfLoopSupervisor);
        writeProperties(writer, uniEdgeSupervisor);
        writeProperties(writer, biEdgeSupervisor);
        writeProperties(writer, undirectedEdgeSupervisor);

        writer.writeEndElement();
    }

    private void writeProperties(XMLStreamWriter writer, Supervisor supervisor) {
        for (SupervisorPropery p : supervisor.getProperties()) {
            String propertyName = p.getProperty().getName();
            try {
                Object propertyValue = p.getProperty().getValue();
                if (propertyValue != null) {
                    PropertyEditor editor = p.getProperty().getPropertyEditor();
                    if (editor == null) {
                        editor = PropertyEditorManager.findEditor(p.getProperty().getValueType());
                    }
                    if (editor != null) {
                        writer.writeStartElement("previewproperty");
                        writer.writeAttribute("name", propertyName);
                        editor.setValue(propertyValue);
                        writer.writeCharacters(editor.getAsText());
                        writer.writeEndElement();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        Map<String, Property> propertiesMap = getPropertiesMap();

        boolean ratio = false;
        boolean bgColor = false;
        String propName = null;

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("ratio".equalsIgnoreCase(name)) {
                        ratio = true;
                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        bgColor = true;
                    } else if ("previewproperty".equalsIgnoreCase(name)) {
                        propName = reader.getAttributeValue(null, "name");
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        if (ratio) {
                            visibilityRatio = Float.parseFloat(reader.getText());
                        } else if (bgColor) {
                            backgroundColor = new Color(Integer.parseInt(reader.getText()));
                        } else if (propName != null) {
                            Property p = propertiesMap.get(propName);
                            if (p != null) {
                                PropertyEditor editor = p.getPropertyEditor();
                                if (editor == null) {
                                    editor = PropertyEditorManager.findEditor(p.getValueType());
                                }
                                if (editor != null) {
                                    editor.setAsText(reader.getText());
                                    if (editor.getValue() != null) {
                                        try {
                                            p.setValue(editor.getValue());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("previewmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    bgColor = false;
                    ratio = false;
                    name = null;
                    break;
            }
        }
    }

    private Map<String, Property> getPropertiesMap() {
        Map<String, Property> propertiesMap = new HashMap<String, Property>();
        Supervisor[] supervisors = new Supervisor[]{nodeSupervisor, globalEdgeSupervisor, selfLoopSupervisor, uniEdgeSupervisor, biEdgeSupervisor, undirectedEdgeSupervisor};
        for (Supervisor s : supervisors) {
            for (SupervisorPropery p : s.getProperties()) {
                String propertyName = p.getProperty().getName();
                propertiesMap.put(propertyName, p.getProperty());
            }
        }
        return propertiesMap;
    }
}
