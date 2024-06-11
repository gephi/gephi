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
import java.util.List;
import java.util.Objects;
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
import org.gephi.viz.engine.status.GraphRenderingOptions;
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
    private VizEngine<?, ?> engine;

    protected VizConfig config;
    //Variable
    protected float[] backgroundColorComponents = new float[4];

    // TODO: these must be either removed or moved to viz-engine:
    protected boolean hideNonSelectedEdges;
    protected boolean uniColorSelected;
    protected boolean edgeHasUniColor;
    protected float[] edgeUniColor;
    protected boolean edgeSelectionColor;
    protected boolean adjustByText;
    protected float edgeScale;
    //Listener
    protected List<PropertyChangeListener> listeners = new ArrayList<>();
    private GraphRenderingOptions renderingOptions;

    public VizModel(Workspace workspace) {
        Objects.requireNonNull(workspace, "workspace");
        this.workspace = workspace;

        final GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
//        textModel.setTextColumns(new Column[] {gm.getNodeTable().getColumn("label")},
//            new Column[] {gm.getEdgeTable().getColumn("label")});
        //TODO
    }

    private boolean loadEngine() {
        engine = workspace.getLookup().lookup(VizEngine.class);
        if (engine == null) {
            return false; // Engine still not ready in the workspace
        }
        this.renderingOptions = engine.getLookup().lookup(GraphRenderingOptions.class);

        defaultValues();

        return true;
    }

    /**
     * Getters and setters should only be called if the model is ready (has a backing viz-engine setup).
     */
    public boolean isReady() {
        if (engine != null) {
           return true;
        }

        return loadEngine();
    }

    private boolean initialized = false;
    public boolean init() {
        if (initialized) {
            return true;
        }

        if (!loadEngine()) {
            return false;
        }

        initialized = true;

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

        return initialized;
    }

    public List<PropertyChangeListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<PropertyChangeListener> listeners) {
        this.listeners = listeners;
    }

    private void defaultValues() {
        if (!isReady()) {
            return;
        }

        config = VizController.getInstance().getVizConfig();

        //textModel = new TextModelImpl();
        //TODO
        if (UIUtils.isDarkLookAndFeel()) {
            setBackgroundColor(config.getDefaultDarkBackgroundColor());
        } else {
            setBackgroundColor(config.getDefaultBackgroundColor());
        }

        setShowEdges(config.isDefaultShowEdges());
        setLightenNonSelectedAuto(config.isDefaultLightenNonSelectedAuto());
        setAutoSelectNeighbors(config.isDefaultAutoSelectNeighbor());
        setHideNonSelectedEdges(config.isDefaultHideNonSelectedEdges());
        setUniColorSelected(config.isDefaultUniColorSelected());
        setEdgeHasUniColor(config.isDefaultEdgeHasUniColor());
        setEdgeUniColor(config.getDefaultEdgeUniColor().getRGBComponents(null));
        setAdjustByText(config.isDefaultAdjustByText());
        setEdgeSelectionColor(config.isDefaultEdgeSelectionColor());
        setEdgeInSelectionColor(config.getDefaultEdgeInSelectedColor().getRGBComponents(null));
        setEdgeOutSelectionColor(config.getDefaultEdgeOutSelectedColor().getRGBComponents(null));
        setEdgeBothSelectionColor(config.getDefaultEdgeBothSelectedColor().getRGBComponents(null));
        setEdgeScale(config.getDefaultEdgeScale());
    }

    public float getZoom() {
        return engine.getZoom();
    }

    public void setZoom(float zoom) {
        engine.setZoom(zoom);
    }

    public boolean isAdjustByText() {
        //TODO
        return adjustByText;
    }

    public void setAdjustByText(boolean adjustByText) {
        //TODO
        this.adjustByText = adjustByText;
        fireProperyChange("adjustByText", null, adjustByText);
    }

    public boolean isAutoSelectNeighbors() {
        return renderingOptions.isAutoSelectNeighbours();
    }

    public void setAutoSelectNeighbors(boolean autoSelectNeighbor) {
        renderingOptions.setAutoSelectNeighbours(autoSelectNeighbor);
        fireProperyChange("autoSelectNeighbor", null, autoSelectNeighbor);
    }

    public Color getBackgroundColor() {
        engine.getBackgroundColor(backgroundColorComponents);

        return new Color(backgroundColorComponents[0], backgroundColorComponents[1],
                backgroundColorComponents[2], backgroundColorComponents[3]);
    }

    public void setBackgroundColor(Color backgroundColor) {
        engine.setBackgroundColor(backgroundColor);

        fireProperyChange("backgroundColor", null, backgroundColor);
    }

    public float[] getBackgroundColorComponents() {
        engine.getBackgroundColor(backgroundColorComponents);

        return backgroundColorComponents;
    }

    public boolean isShowEdges() {
        return renderingOptions.isShowEdges();
    }

    public void setShowEdges(boolean showEdges) {
        renderingOptions.setShowEdges(showEdges);
        fireProperyChange("showEdges", null, showEdges);
    }

    public boolean isEdgeHasUniColor() {
        //TODO
        return edgeHasUniColor;
    }

    public void setEdgeHasUniColor(boolean edgeHasUniColor) {
        //TODO
        this.edgeHasUniColor = edgeHasUniColor;
        fireProperyChange("edgeHasUniColor", null, edgeHasUniColor);
    }

    public float[] getEdgeUniColor() {
        //TODO
        return edgeUniColor;
    }

    public void setEdgeUniColor(float[] edgeUniColor) {
        //TODO
        this.edgeUniColor = edgeUniColor;
        fireProperyChange("edgeUniColor", null, edgeUniColor);
    }

    public boolean isHideNonSelectedEdges() {
        //TODO
        return hideNonSelectedEdges;
    }

    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        //TODO
        this.hideNonSelectedEdges = hideNonSelectedEdges;
        fireProperyChange("hideNonSelectedEdges", null, hideNonSelectedEdges);
    }

    public boolean isLightenNonSelectedAuto() {
        return renderingOptions.isLightenNonSelected();
    }

    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        renderingOptions.setLightenNonSelected(lightenNonSelectedAuto);
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
        //TODO
        return edgeSelectionColor;
    }

    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        //TODO
        this.edgeSelectionColor = edgeSelectionColor;
        fireProperyChange("edgeSelectionColor", null, edgeSelectionColor);
    }

    public float[] getEdgeInSelectionColor() {
        return renderingOptions.getEdgeInSelectionColor().getRGBComponents(null);
    }

    public void setEdgeInSelectionColor(float[] edgeInSelectionColor) {
        Color color = new Color(
                edgeInSelectionColor[0], edgeInSelectionColor[1],
                edgeInSelectionColor[2], edgeInSelectionColor[3]);
        renderingOptions.setEdgeInSelectionColor(color);

        fireProperyChange("edgeInSelectionColor", null, edgeInSelectionColor);
    }

    public float[] getEdgeOutSelectionColor() {
        return renderingOptions.getEdgeOutSelectionColor().getRGBComponents(null);
    }

    public void setEdgeOutSelectionColor(float[] edgeOutSelectionColor) {
        Color color = new Color(
                edgeOutSelectionColor[0], edgeOutSelectionColor[1],
                edgeOutSelectionColor[2], edgeOutSelectionColor[3]);
        renderingOptions.setEdgeOutSelectionColor(color);

        fireProperyChange("edgeOutSelectionColor", null, edgeOutSelectionColor);
    }

    public float[] getEdgeBothSelectionColor() {
        return renderingOptions.getEdgeBothSelectionColor().getRGBComponents(null);
    }

    public void setEdgeBothSelectionColor(float[] edgeBothSelectionColor) {
        Color color = new Color(
                edgeBothSelectionColor[0], edgeBothSelectionColor[1],
                edgeBothSelectionColor[2], edgeBothSelectionColor[3]);
        renderingOptions.setEdgeBothSelectionColor(color);
        fireProperyChange("edgeBothSelectionColor", null, edgeBothSelectionColor);
    }

    public float getEdgeScale() {
        return renderingOptions.getEdgeScale();
    }

    public void setEdgeScale(float edgeScale) {
        renderingOptions.setEdgeScale(edgeScale);
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
                        engine.setTranslate(
                                Float.parseFloat(reader.getAttributeValue(null, "x")),
                                Float.parseFloat(reader.getAttributeValue(null, "y"))
                        );
                    } else if ("cameratarget".equalsIgnoreCase(name)) {
                        // No longer necessary
                    } else if ("showedges".equalsIgnoreCase(name)) {
                        setShowEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("lightennonselectedauto".equalsIgnoreCase(name)) {
                        setLightenNonSelectedAuto(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("autoselectneighbor".equalsIgnoreCase(name)) {
                        setAutoSelectNeighbors(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
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
        writer.writeAttribute("value", String.valueOf(isShowEdges()));
        writer.writeEndElement();

        writer.writeStartElement("lightennonselectedauto");
        writer.writeAttribute("value", String.valueOf(isLightenNonSelectedAuto()));
        writer.writeEndElement();

        writer.writeStartElement("autoselectneighbor");
        writer.writeAttribute("value", String.valueOf(isAutoSelectNeighbors()));
        writer.writeEndElement();

        writer.writeStartElement("hidenonselectededges");
        writer.writeAttribute("value", String.valueOf(isHideNonSelectedEdges()));
        writer.writeEndElement();

        writer.writeStartElement("unicolorselected");
        writer.writeAttribute("value", String.valueOf(isUniColorSelected()));
        writer.writeEndElement();

        writer.writeStartElement("edgehasunicolor");
        writer.writeAttribute("value", String.valueOf(isEdgeHasUniColor()));
        writer.writeEndElement();

        writer.writeStartElement("adjustbytext");
        writer.writeAttribute("value", String.valueOf(isAdjustByText()));
        writer.writeEndElement();

        writer.writeStartElement("edgeSelectionColor");
        writer.writeAttribute("value", String.valueOf(isEdgeSelectionColor()));
        writer.writeEndElement();

        //Colors
        writer.writeStartElement("backgroundcolor");
        writer.writeAttribute("value", ColorUtils.encode(getBackgroundColor()));
        writer.writeEndElement();

        writer.writeStartElement("edgeunicolor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeUniColor())));
        writer.writeEndElement();

        writer.writeStartElement("edgeInSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeInSelectionColor())));
        writer.writeEndElement();

        writer.writeStartElement("edgeOutSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeOutSelectionColor())));
        writer.writeEndElement();

        writer.writeStartElement("edgeBothSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeBothSelectionColor())));
        writer.writeEndElement();

        //Float
        writer.writeStartElement("edgeScale");
        writer.writeAttribute("value", String.valueOf(getEdgeScale()));
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
