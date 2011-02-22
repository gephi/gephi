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
package org.gephi.visualization;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.text.TextModel;

/**
 *
 * @author Mathieu Bastian
 */
public class VizModel {

    protected VizConfig config;
    protected GraphLimits limits;
    //Variable
    protected float[] cameraPosition;
    protected float[] cameraTarget;
    protected TextModel textModel;
    protected boolean use3d;
    protected boolean lighting;
    protected boolean culling;
    protected boolean material;
    protected Color backgroundColor;
    protected boolean rotatingEnable;
    protected boolean showEdges;
    protected boolean lightenNonSelectedAuto;
    protected boolean autoSelectNeighbor;
    protected boolean hideNonSelectedEdges;
    protected boolean uniColorSelected;
    protected boolean edgeHasUniColor;
    protected float[] edgeUniColor;
    protected boolean edgeSelectionColor;
    protected float[] edgeInSelectionColor;
    protected float[] edgeOutSelectionColor;
    protected float[] edgeBothSelectionColor;
    protected boolean adjustByText;
    protected String nodeModeler;
    protected boolean showHulls;
    protected float edgeScale;
    protected float metaEdgeScale;
    //Listener
    protected List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private boolean defaultModel = false;

    public VizModel() {
        defaultValues();
        limits = VizController.getInstance().getLimits();
    }

    public VizModel(boolean defaultModel) {
        this.defaultModel = defaultModel;
        defaultValues();
        limits = VizController.getInstance().getLimits();
    }

    public void init() {
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, "init", null, null);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(evt);
                }
            }
        });
    }

    public boolean isDefaultModel() {
        return defaultModel;
    }

    public List<PropertyChangeListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<PropertyChangeListener> listeners) {
        this.listeners = listeners;
    }

    private void defaultValues() {
        config = VizController.getInstance().getVizConfig();
        cameraPosition = Arrays.copyOf(config.getDefaultCameraPosition(), 3);
        cameraTarget = Arrays.copyOf(config.getDefaultCameraTarget(), 3);
        textModel = new TextModel();
        use3d = config.isDefaultUse3d();
        lighting = use3d;
        culling = use3d;
        material = use3d;
        rotatingEnable = use3d;
        backgroundColor = config.getDefaultBackgroundColor();

        showEdges = config.isDefaultShowEdges();
        lightenNonSelectedAuto = config.isDefaultLightenNonSelectedAuto();
        autoSelectNeighbor = config.isDefaultAutoSelectNeighbor();
        hideNonSelectedEdges = config.isDefaultHideNonSelectedEdges();
        uniColorSelected = config.isDefaultUniColorSelected();
        edgeHasUniColor = config.isDefaultEdgeHasUniColor();
        edgeUniColor = config.getDefaultEdgeUniColor().getRGBComponents(null);
        adjustByText = config.isDefaultAdjustByText();
        nodeModeler = use3d ? "CompatibilityNodeSphereModeler" : "CompatibilityNodeDiskModeler";
        edgeSelectionColor = config.isDefaultEdgeSelectionColor();
        edgeInSelectionColor = config.getDefaultEdgeInSelectedColor().getRGBComponents(null);
        edgeOutSelectionColor = config.getDefaultEdgeOutSelectedColor().getRGBComponents(null);
        edgeBothSelectionColor = config.getDefaultEdgeBothSelectedColor().getRGBComponents(null);
        showHulls = config.isDefaultShowHulls();
        edgeScale = config.getDefaultEdgeScale();
        metaEdgeScale = config.getDefaultMetaEdgeScale();
    }

    //GETTERS
    public boolean isAdjustByText() {
        return adjustByText;
    }

    public boolean isAutoSelectNeighbor() {
        return autoSelectNeighbor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public float[] getCameraPosition() {
        return cameraPosition;
    }

    public float[] getCameraTarget() {
        return cameraTarget;
    }

    public boolean isCulling() {
        return culling;
    }

    public boolean isShowEdges() {
        return showEdges;
    }

    public boolean isEdgeHasUniColor() {
        return edgeHasUniColor;
    }

    public float[] getEdgeUniColor() {
        return edgeUniColor;
    }

    public boolean isHideNonSelectedEdges() {
        return hideNonSelectedEdges;
    }

    public boolean isLightenNonSelectedAuto() {
        return lightenNonSelectedAuto;
    }

    public boolean isLighting() {
        return lighting;
    }

    public boolean isMaterial() {
        return material;
    }

    public boolean isRotatingEnable() {
        return rotatingEnable;
    }

    public TextModel getTextModel() {
        return textModel;
    }

    public boolean isUniColorSelected() {
        return uniColorSelected;
    }

    public boolean isUse3d() {
        return use3d;
    }

    public VizConfig getConfig() {
        return config;
    }

    public String getNodeModeler() {
        return nodeModeler;
    }

    public boolean isEdgeSelectionColor() {
        return edgeSelectionColor;
    }

    public float[] getEdgeInSelectionColor() {
        return edgeInSelectionColor;
    }

    public float[] getEdgeOutSelectionColor() {
        return edgeOutSelectionColor;
    }

    public float[] getEdgeBothSelectionColor() {
        return edgeBothSelectionColor;
    }

    public boolean isShowHulls() {
        return showHulls;
    }

    public float getEdgeScale() {
        return edgeScale;
    }

    public float getMetaEdgeScale() {
        return metaEdgeScale;
    }

    //SETTERS
    public void setAdjustByText(boolean adjustByText) {
        this.adjustByText = adjustByText;
        fireProperyChange("adjustByText", null, adjustByText);
    }

    public void setAutoSelectNeighbor(boolean autoSelectNeighbor) {
        this.autoSelectNeighbor = autoSelectNeighbor;
        fireProperyChange("autoSelectNeighbor", null, autoSelectNeighbor);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        fireProperyChange("backgroundColor", null, backgroundColor);
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
        fireProperyChange("showEdges", null, showEdges);
    }

    public void setEdgeHasUniColor(boolean edgeHasUniColor) {
        this.edgeHasUniColor = edgeHasUniColor;
        fireProperyChange("edgeHasUniColor", null, edgeHasUniColor);
    }

    public void setEdgeUniColor(float[] edgeUniColor) {
        this.edgeUniColor = edgeUniColor;
        fireProperyChange("edgeUniColor", null, edgeUniColor);
    }

    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        this.hideNonSelectedEdges = hideNonSelectedEdges;
        fireProperyChange("hideNonSelectedEdges", null, hideNonSelectedEdges);
    }

    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        this.lightenNonSelectedAuto = lightenNonSelectedAuto;
        fireProperyChange("lightenNonSelectedAuto", null, lightenNonSelectedAuto);
    }

    public void setUniColorSelected(boolean uniColorSelected) {
        this.uniColorSelected = uniColorSelected;
        fireProperyChange("uniColorSelected", null, uniColorSelected);
    }

    public void setUse3d(boolean use3d) {
        this.use3d = use3d;
        //Additional
        this.lighting = use3d;
        this.culling = use3d;
        this.rotatingEnable = use3d;
        this.material = use3d;
        fireProperyChange("use3d", null, use3d);
    }

    public void setNodeModeler(String nodeModeler) {
        this.nodeModeler = nodeModeler;
        fireProperyChange("nodeModeler", null, nodeModeler);
    }

    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        this.edgeSelectionColor = edgeSelectionColor;
        fireProperyChange("edgeSelectionColor", null, edgeSelectionColor);
    }

    public void setEdgeInSelectionColor(float[] edgeInSelectionColor) {
        this.edgeInSelectionColor = edgeInSelectionColor;
        fireProperyChange("edgeInSelectionColor", null, edgeInSelectionColor);
    }

    public void setEdgeOutSelectionColor(float[] edgeOutSelectionColor) {
        this.edgeOutSelectionColor = edgeOutSelectionColor;
        fireProperyChange("edgeOutSelectionColor", null, edgeOutSelectionColor);
    }

    public void setEdgeBothSelectionColor(float[] edgeBothSelectionColor) {
        this.edgeBothSelectionColor = edgeBothSelectionColor;
        fireProperyChange("edgeBothSelectionColor", null, edgeBothSelectionColor);
    }

    public void setShowHulls(boolean showHulls) {
        this.showHulls = showHulls;
        fireProperyChange("showHulls", null, showHulls);
    }

    public void setEdgeScale(float edgeScale) {
        this.edgeScale = edgeScale;
        fireProperyChange("edgeScale", null, edgeScale);
    }

    public void setMetaEdgeScale(float metaEdgeScale) {
        this.metaEdgeScale = metaEdgeScale;
        fireProperyChange("metaEdgeScale", null, metaEdgeScale);
    }

    public GraphLimits getLimits() {
        return limits;
    }

    public float getCameraDistance() {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        return drawable.getCameraVector().length();
    }

    public void setCameraDistance(float distance) {
    }

    //EVENTS
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireProperyChange(String propertyName, Object oldvalue, Object newValue) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldvalue, newValue);
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }

    //XML
    public void readXML(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("textmodel".equalsIgnoreCase(name)) {
                        textModel.readXML(reader, workspace);
                    } else if ("cameraposition".equalsIgnoreCase(name)) {
                        cameraPosition[0] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                        cameraPosition[1] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                        cameraPosition[2] = Float.parseFloat(reader.getAttributeValue(null, "z"));
                    } else if ("cameratarget".equalsIgnoreCase(name)) {
                        cameraTarget[0] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                        cameraTarget[1] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                        cameraTarget[2] = Float.parseFloat(reader.getAttributeValue(null, "z"));
                    } else if ("use3d".equalsIgnoreCase(name)) {
                        use3d = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("lighting".equalsIgnoreCase(name)) {
                        lighting = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("culling".equalsIgnoreCase(name)) {
                        culling = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("material".equalsIgnoreCase(name)) {
                        material = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("rotatingenable".equalsIgnoreCase(name)) {
                        rotatingEnable = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("showedges".equalsIgnoreCase(name)) {
                        showEdges = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("lightennonselectedauto".equalsIgnoreCase(name)) {
                        lightenNonSelectedAuto = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("autoselectneighbor".equalsIgnoreCase(name)) {
                        autoSelectNeighbor = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("hidenonselectededges".equalsIgnoreCase(name)) {
                        hideNonSelectedEdges = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("unicolorselected".equalsIgnoreCase(name)) {
                        uniColorSelected = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("edgehasunicolor".equalsIgnoreCase(name)) {
                        edgeHasUniColor = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("adjustbytext".equalsIgnoreCase(name)) {
                        adjustByText = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("edgeSelectionColor".equalsIgnoreCase(name)) {
                        edgeSelectionColor = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("showHulls".equalsIgnoreCase(name)) {
                        showHulls = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        backgroundColor = ColorUtils.decode(reader.getAttributeValue(null, "value"));
                    } else if ("edgeunicolor".equalsIgnoreCase(name)) {
                        edgeUniColor = ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null);
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        edgeInSelectionColor = ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null);
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        edgeOutSelectionColor = ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null);
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        edgeBothSelectionColor = ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null);
                    } else if ("nodemodeler".equalsIgnoreCase(name)) {
                        nodeModeler = reader.getAttributeValue(null, "value");
                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        edgeScale = Float.parseFloat(reader.getAttributeValue(null, "value"));
                    } else if ("metaEdgeScale".equalsIgnoreCase(name)) {
                        metaEdgeScale = Float.parseFloat(reader.getAttributeValue(null, "value"));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("vizmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {

        writer.writeStartElement("vizmodel");

        //Fast refreh
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        cameraPosition = Arrays.copyOf(drawable.getCameraLocation(), 3);
        cameraTarget = Arrays.copyOf(drawable.getCameraTarget(), 3);

        //TextModel
        textModel.writeXML(writer);
        writer.writeEndElement();

        //Camera
        writer.writeStartElement("cameraposition");
        writer.writeAttribute("x", Float.toString(cameraPosition[0]));
        writer.writeAttribute("y", Float.toString(cameraPosition[1]));
        writer.writeAttribute("z", Float.toString(cameraPosition[2]));
        writer.writeEndElement();
        writer.writeStartElement("cameratarget");
        writer.writeAttribute("x", Float.toString(cameraTarget[0]));
        writer.writeAttribute("y", Float.toString(cameraTarget[1]));
        writer.writeAttribute("z", Float.toString(cameraTarget[2]));
        writer.writeEndElement();

        //Boolean values
        writer.writeStartElement("use3d");
        writer.writeAttribute("value", String.valueOf(use3d));
        writer.writeEndElement();

        writer.writeStartElement("lighting");
        writer.writeAttribute("value", String.valueOf(lighting));
        writer.writeEndElement();

        writer.writeStartElement("culling");
        writer.writeAttribute("value", String.valueOf(culling));
        writer.writeEndElement();

        writer.writeStartElement("material");
        writer.writeAttribute("value", String.valueOf(material));
        writer.writeEndElement();

        writer.writeStartElement("rotatingenable");
        writer.writeAttribute("value", String.valueOf(rotatingEnable));
        writer.writeEndElement();

        writer.writeStartElement("showedges");
        writer.writeAttribute("value", String.valueOf(showEdges));
        writer.writeEndElement();

        writer.writeStartElement("lightennonselectedauto");
        writer.writeAttribute("value", String.valueOf(lightenNonSelectedAuto));
        writer.writeEndElement();

        writer.writeStartElement("autoselectneighbor");
        writer.writeAttribute("value", String.valueOf(autoSelectNeighbor));
        writer.writeEndElement();

        writer.writeStartElement("hidenonselectededges");
        writer.writeAttribute("value", String.valueOf(hideNonSelectedEdges));
        writer.writeEndElement();

        writer.writeStartElement("unicolorselected");
        writer.writeAttribute("value", String.valueOf(uniColorSelected));
        writer.writeEndElement();

        writer.writeStartElement("edgehasunicolor");
        writer.writeAttribute("value", String.valueOf(edgeHasUniColor));
        writer.writeEndElement();

        writer.writeStartElement("adjustbytext");
        writer.writeAttribute("value", String.valueOf(adjustByText));
        writer.writeEndElement();

        writer.writeStartElement("edgeSelectionColor");
        writer.writeAttribute("value", String.valueOf(edgeSelectionColor));
        writer.writeEndElement();

        writer.writeStartElement("showHulls");
        writer.writeAttribute("value", String.valueOf(showHulls));
        writer.writeEndElement();

        //Colors
        writer.writeStartElement("backgroundcolor");
        writer.writeAttribute("value", ColorUtils.encode(backgroundColor));
        writer.writeEndElement();

        writer.writeStartElement("edgeunicolor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeUniColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgeInSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeInSelectionColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgeOutSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeOutSelectionColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgeBothSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeBothSelectionColor)));
        writer.writeEndElement();

        //Misc
        writer.writeStartElement("nodemodeler");
        writer.writeAttribute("value", nodeModeler);
        writer.writeEndElement();

        //Float
        writer.writeStartElement("edgeScale");
        writer.writeAttribute("value", String.valueOf(edgeScale));
        writer.writeEndElement();

        writer.writeStartElement("metaEdgeScale");
        writer.writeAttribute("value", String.valueOf(metaEdgeScale));
        writer.writeEndElement();

        writer.writeEndElement();
    }
}
