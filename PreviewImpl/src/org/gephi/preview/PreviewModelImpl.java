/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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
import org.gephi.workspace.api.Workspace;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    private PreviewPreset currentPreset;

    public PreviewModelImpl() {
        nodeSupervisor = new NodeSupervisorImpl();
        globalEdgeSupervisor = new GlobalEdgeSupervisorImpl();
        selfLoopSupervisor = new SelfLoopSupervisorImpl();
        uniEdgeSupervisor = new UnidirectionalEdgeSupervisorImpl();
        biEdgeSupervisor = new BidirectionalEdgeSupervisorImpl();
        undirectedEdgeSupervisor = new UndirectedEdgeSupervisorImpl();
        currentPreset = new DefaultPreset();
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
                        editor.setAsText(valueStr);
                        Object value = editor.getValue();
                        if (value != null) {
                            p.setValue(value);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //PERSISTENCE
    public Element writeXML(Document document) {
        Element previewModelE = document.createElement("previewmodel");

        //Ratio
        Element visibilityRatioE = document.createElement("ratio");
        visibilityRatioE.setTextContent(String.valueOf(visibilityRatio));
        previewModelE.appendChild(visibilityRatioE);

        //Properties
        writeProperties(document, previewModelE, nodeSupervisor);
        writeProperties(document, previewModelE, globalEdgeSupervisor);
        writeProperties(document, previewModelE, selfLoopSupervisor);
        writeProperties(document, previewModelE, uniEdgeSupervisor);
        writeProperties(document, previewModelE, biEdgeSupervisor);
        writeProperties(document, previewModelE, undirectedEdgeSupervisor);

        return previewModelE;
    }

    private void writeProperties(Document document, Element parent, Supervisor supervisor) {
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
                        Element propertyE = document.createElement("previewproperty");
                        propertyE.setAttribute("name", propertyName);
                        editor.setValue(propertyValue);
                        propertyE.setTextContent(editor.getAsText());
                        parent.appendChild(propertyE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void readXML(Element previewModelE) {
        Map<String, Property> propertiesMap = getPropertiesMap();

        NodeList childList = previewModelE.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node n = childList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element childE = (Element) n;
                if (childE.getNodeName().equals("ratio")) {
                    visibilityRatio = Float.parseFloat(childE.getTextContent());
                } else if (childE.getNodeName().equals("previewproperty")) {
                    String name = childE.getAttribute("name");
                    Property p = propertiesMap.get(name);
                    if (p != null) {
                        PropertyEditor editor = p.getPropertyEditor();
                        if (editor == null) {
                            editor = PropertyEditorManager.findEditor(p.getValueType());
                        }
                        if (editor != null) {
                            editor.setAsText(childE.getTextContent());
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
