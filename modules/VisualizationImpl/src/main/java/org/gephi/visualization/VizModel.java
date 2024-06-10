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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.joml.Vector2fc;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.screenshot.ScreenshotMaker;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class VizModel {

    private final Workspace workspace;

    protected VizConfig config;
    //Variable
    protected float[] cameraPosition;
    protected float[] cameraTarget;
    protected Color backgroundColor;
    protected float[] backgroundColorComponents = new float[4];
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
    protected List<PropertyChangeListener> listeners = new ArrayList<>();
    private boolean defaultModel = false;

    public VizModel(Workspace workspace) {
        defaultValues();

        final GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.workspace = workspace;
//        textModel.setTextColumns(new Column[] {gm.getNodeTable().getColumn("label")},
//            new Column[] {gm.getEdgeTable().getColumn("label")});
        //TODO
    }

    public VizModel(boolean defaultModel) {
        this.defaultModel = defaultModel;
        this.workspace = null;
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
        //textModel = new TextModelImpl();
        //TODO
        if (UIUtils.isDarkLookAndFeel()) {
            backgroundColor = config.getDefaultDarkBackgroundColor();
        } else {
            backgroundColor = config.getDefaultBackgroundColor();
        }
        backgroundColorComponents = backgroundColor.getRGBComponents(backgroundColorComponents);

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

    //SETTERS
    public void setAdjustByText(boolean adjustByText) {
        this.adjustByText = adjustByText;
        fireProperyChange("adjustByText", null, adjustByText);
    }

    public boolean isAutoSelectNeighbor() {
        return autoSelectNeighbor;
    }

    public void setAutoSelectNeighbor(boolean autoSelectNeighbor) {
        this.autoSelectNeighbor = autoSelectNeighbor;
        fireProperyChange("autoSelectNeighbor", null, autoSelectNeighbor);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.backgroundColorComponents = backgroundColor.getRGBComponents(this.backgroundColorComponents);
        fireProperyChange("backgroundColor", null, backgroundColor);
    }

    public float[] getBackgroundColorComponents() {
        return backgroundColorComponents;
    }

    public float[] getCameraPosition() {
        return cameraPosition;
    }

    public boolean isShowEdges() {
        return showEdges;
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
        fireProperyChange("showEdges", null, showEdges);
    }

    public boolean isEdgeHasUniColor() {
        return edgeHasUniColor;
    }

    public void setEdgeHasUniColor(boolean edgeHasUniColor) {
        this.edgeHasUniColor = edgeHasUniColor;
        fireProperyChange("edgeHasUniColor", null, edgeHasUniColor);
    }

    public float[] getEdgeUniColor() {
        return edgeUniColor;
    }

    public void setEdgeUniColor(float[] edgeUniColor) {
        this.edgeUniColor = edgeUniColor;
        fireProperyChange("edgeUniColor", null, edgeUniColor);
    }

    public boolean isHideNonSelectedEdges() {
        return hideNonSelectedEdges;
    }

    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        this.hideNonSelectedEdges = hideNonSelectedEdges;
        fireProperyChange("hideNonSelectedEdges", null, hideNonSelectedEdges);
    }

    public boolean isLightenNonSelectedAuto() {
        return lightenNonSelectedAuto;
    }

    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        this.lightenNonSelectedAuto = lightenNonSelectedAuto;
        fireProperyChange("lightenNonSelectedAuto", null, lightenNonSelectedAuto);
    }

    public boolean isUniColorSelected() {
        return uniColorSelected;
    }

    public void setUniColorSelected(boolean uniColorSelected) {
        this.uniColorSelected = uniColorSelected;
        fireProperyChange("uniColorSelected", null, uniColorSelected);
    }

    public VizConfig getConfig() {
        return config;
    }

    public boolean isEdgeSelectionColor() {
        return edgeSelectionColor;
    }

    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        this.edgeSelectionColor = edgeSelectionColor;
        fireProperyChange("edgeSelectionColor", null, edgeSelectionColor);
    }

    public float[] getEdgeInSelectionColor() {
        return edgeInSelectionColor;
    }

    public void setEdgeInSelectionColor(float[] edgeInSelectionColor) {
        this.edgeInSelectionColor = edgeInSelectionColor;
        fireProperyChange("edgeInSelectionColor", null, edgeInSelectionColor);
    }

    public float[] getEdgeOutSelectionColor() {
        return edgeOutSelectionColor;
    }

    public void setEdgeOutSelectionColor(float[] edgeOutSelectionColor) {
        this.edgeOutSelectionColor = edgeOutSelectionColor;
        fireProperyChange("edgeOutSelectionColor", null, edgeOutSelectionColor);
    }

    public float[] getEdgeBothSelectionColor() {
        return edgeBothSelectionColor;
    }

    public void setEdgeBothSelectionColor(float[] edgeBothSelectionColor) {
        this.edgeBothSelectionColor = edgeBothSelectionColor;
        fireProperyChange("edgeBothSelectionColor", null, edgeBothSelectionColor);
    }

    public float getEdgeScale() {
        return edgeScale;
    }

    public void setEdgeScale(float edgeScale) {
        this.edgeScale = edgeScale;
        fireProperyChange("edgeScale", null, edgeScale);
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
//                        textModel.readXML(reader, workspace);
                        //TODO
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
                        setEdgeUniColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeInSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeOutSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeBothSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));

                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        setEdgeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if("screenshotMaker".equalsIgnoreCase(name)) {
                        ScreenshotMaker screenshotMaker = VizController.getInstance().getScreenshotMaker();
                        if (screenshotMaker != null) {
                            screenshotMaker.setWidth(Integer.parseInt(reader.getAttributeValue(null, "width")));
                            screenshotMaker.setHeight(Integer.parseInt(reader.getAttributeValue(null, "height")));
                            screenshotMaker.setTransparentBackground(Boolean.parseBoolean(reader.getAttributeValue(null, "transparent")));
                            screenshotMaker.setAutoSave(Boolean.parseBoolean(reader.getAttributeValue(null, "autosave")));
                            screenshotMaker.setAntiAliasing(Integer.parseInt(reader.getAttributeValue(null, "antialiasing")));
                            String path = reader.getAttributeValue(null, "path");
                            if (path != null && !path.isEmpty()) {
                                File file = new File(reader.getAttributeValue(null, "path"));
                                if (file.exists()) {
                                    screenshotMaker.setDefaultDirectory(file);
                                }
                            }
                        }
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
        final VizEngine<JOGLRenderingTarget, NEWTEvent> engine =
            Lookup.getDefault().lookup(CurrentWorkspaceVizEngine.class)
                .getEngine(workspace).orElse(null);

        if (engine == null) {
            return;
        }

        //Fast refresh
        final Vector2fc cameraPosition = engine.getTranslate();

        //TextModel
        //        textModel.writeXML(writer);
        //TODO

        //Camera
        writer.writeStartElement("cameraposition");
        writer.writeAttribute("x", Float.toString(cameraPosition.x()));
        writer.writeAttribute("y", Float.toString(cameraPosition.y()));
        writer.writeAttribute("z", Float.toString(0));
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

        //Screenshot settings
        ScreenshotMaker screenshotMaker = VizController.getInstance().getScreenshotMaker();
        if (screenshotMaker != null) {
            writer.writeStartElement("screenshotMaker");
            writer.writeAttribute("width", String.valueOf(screenshotMaker.getWidth()));
            writer.writeAttribute("height", String.valueOf(screenshotMaker.getHeight()));
            writer.writeAttribute("antialiasing", String.valueOf(screenshotMaker.getAntiAliasing()));
            writer.writeAttribute("transparent", String.valueOf(screenshotMaker.isTransparentBackground()));
            writer.writeAttribute("autosave", String.valueOf(screenshotMaker.isAutoSave()));
            if (screenshotMaker.getDefaultDirectory() != null) {
                writer.writeAttribute("path", screenshotMaker.getDefaultDirectory());
            }
            writer.writeEndElement();
        }
    }
}
