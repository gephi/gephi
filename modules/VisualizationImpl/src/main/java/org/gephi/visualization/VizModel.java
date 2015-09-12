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
import org.gephi.visualization.text.TextModelImpl;

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
    protected TextModelImpl textModel;
    protected Color backgroundColor;
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
    protected float edgeScale;
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
            @Override
            public void run() {
                if (listeners != null) {
                    for (PropertyChangeListener l : listeners) {
                        l.propertyChange(evt);
                    }
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
        textModel = new TextModelImpl();
        backgroundColor = config.getDefaultBackgroundColor();

        showEdges = config.isDefaultShowEdges();
        lightenNonSelectedAuto = config.isDefaultLightenNonSelectedAuto();
        autoSelectNeighbor = config.isDefaultAutoSelectNeighbor();
        hideNonSelectedEdges = config.isDefaultHideNonSelectedEdges();
        uniColorSelected = config.isDefaultUniColorSelected();
        edgeHasUniColor = config.isDefaultEdgeHasUniColor();
        edgeUniColor = config.getDefaultEdgeUniColor().getRGBComponents(null);
        adjustByText = config.isDefaultAdjustByText();
        edgeSelectionColor = config.isDefaultEdgeSelectionColor();
        edgeInSelectionColor = config.getDefaultEdgeInSelectedColor().getRGBComponents(null);
        edgeOutSelectionColor = config.getDefaultEdgeOutSelectedColor().getRGBComponents(null);
        edgeBothSelectionColor = config.getDefaultEdgeBothSelectedColor().getRGBComponents(null);
        edgeScale = config.getDefaultEdgeScale();
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

    public TextModelImpl getTextModel() {
        return textModel;
    }

    public boolean isUniColorSelected() {
        return uniColorSelected;
    }

    public VizConfig getConfig() {
        return config;
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

    public float getEdgeScale() {
        return edgeScale;
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

    public void setEdgeScale(float edgeScale) {
        this.edgeScale = edgeScale;
        fireProperyChange("edgeScale", null, edgeScale);
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

                    } else if ("showedges".equalsIgnoreCase(name)) {
                        setShowEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("lightennonselectedauto".equalsIgnoreCase(name)) {
                        setLightenNonSelectedAuto(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("autoselectneighbor".equalsIgnoreCase(name)) {
                        setAutoSelectNeighbor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("hidenonselectededges".equalsIgnoreCase(name)) {
                        setHideNonSelectedEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("unicolorselected".equalsIgnoreCase(name)) {
                        setUniColorSelected(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgehasunicolor".equalsIgnoreCase(name)) {
                        setEdgeHasUniColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("adjustbytext".equalsIgnoreCase(name)) {
                        setAdjustByText(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeSelectionColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));

                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        setBackgroundColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeunicolor".equalsIgnoreCase(name)) {
                        setEdgeUniColor(ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeInSelectionColor(ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeOutSelectionColor(ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeBothSelectionColor(ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));

                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        setEdgeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
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

        //Float
        writer.writeStartElement("edgeScale");
        writer.writeAttribute("value", String.valueOf(edgeScale));
        writer.writeEndElement();

        writer.writeEndElement();
    }
}
