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
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.text.TextModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public void readXML(Element vizModelElement) {

        //TextModel
        Element textModelE = (Element) vizModelElement.getElementsByTagName("textmodel").item(0);
        textModel.readXML(textModelE);

        //Camera
        Element cameraPostionE = (Element) vizModelElement.getElementsByTagName("cameraposition").item(0);
        cameraPosition[0] = Float.parseFloat(cameraPostionE.getAttribute("x"));
        cameraPosition[1] = Float.parseFloat(cameraPostionE.getAttribute("y"));
        cameraPosition[2] = Float.parseFloat(cameraPostionE.getAttribute("z"));
        Element cameraTargetE = (Element) vizModelElement.getElementsByTagName("cameratarget").item(0);
        cameraTarget[0] = Float.parseFloat(cameraTargetE.getAttribute("x"));
        cameraTarget[1] = Float.parseFloat(cameraTargetE.getAttribute("y"));
        cameraTarget[2] = Float.parseFloat(cameraTargetE.getAttribute("z"));

        //Boolean values
        if (vizModelElement.getElementsByTagName("use3d").getLength() > 0) {
            Element use3dE = (Element) vizModelElement.getElementsByTagName("use3d").item(0);
            use3d = Boolean.parseBoolean(use3dE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("lighting").getLength() > 0) {
            Element lightingE = (Element) vizModelElement.getElementsByTagName("lighting").item(0);
            lighting = Boolean.parseBoolean(lightingE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("culling").getLength() > 0) {
            Element cullingE = (Element) vizModelElement.getElementsByTagName("culling").item(0);
            culling = Boolean.parseBoolean(cullingE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("material").getLength() > 0) {
            Element materialE = (Element) vizModelElement.getElementsByTagName("material").item(0);
            material = Boolean.parseBoolean(materialE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("rotatingenable").getLength() > 0) {
            Element rotatingEnableE = (Element) vizModelElement.getElementsByTagName("rotatingenable").item(0);
            rotatingEnable = Boolean.parseBoolean(rotatingEnableE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("showedges").getLength() > 0) {
            Element showEdgesE = (Element) vizModelElement.getElementsByTagName("showedges").item(0);
            showEdges = Boolean.parseBoolean(showEdgesE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("lightennonselectedauto").getLength() > 0) {
            Element lightenNonSelectedAutoE = (Element) vizModelElement.getElementsByTagName("lightennonselectedauto").item(0);
            lightenNonSelectedAuto = Boolean.parseBoolean(lightenNonSelectedAutoE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("autoselectneighbor").getLength() > 0) {
            Element autoSelectNeighborE = (Element) vizModelElement.getElementsByTagName("autoselectneighbor").item(0);
            autoSelectNeighbor = Boolean.parseBoolean(autoSelectNeighborE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("hidenonselectededges").getLength() > 0) {
            Element hideNonSelectedEdgesE = (Element) vizModelElement.getElementsByTagName("hidenonselectededges").item(0);
            hideNonSelectedEdges = Boolean.parseBoolean(hideNonSelectedEdgesE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("unicolorselected").getLength() > 0) {
            Element uniColorSelectedE = (Element) vizModelElement.getElementsByTagName("unicolorselected").item(0);
            uniColorSelected = Boolean.parseBoolean(uniColorSelectedE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("edgehasunicolor").getLength() > 0) {
            Element edgeHasUniColorE = (Element) vizModelElement.getElementsByTagName("edgehasunicolor").item(0);
            edgeHasUniColor = Boolean.parseBoolean(edgeHasUniColorE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("adjustbytext").getLength() > 0) {
            Element adjustByTextE = (Element) vizModelElement.getElementsByTagName("adjustbytext").item(0);
            adjustByText = Boolean.parseBoolean(adjustByTextE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("edgeSelectionColor").getLength() > 0) {
            Element edgeSelectionColorE = (Element) vizModelElement.getElementsByTagName("edgeSelectionColor").item(0);
            edgeSelectionColor = Boolean.parseBoolean(edgeSelectionColorE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("showHulls").getLength() > 0) {
            Element showHullsE = (Element) vizModelElement.getElementsByTagName("showHulls").item(0);
            showHulls = Boolean.parseBoolean(showHullsE.getAttribute("value"));
        }

        //Colors
        if (vizModelElement.getElementsByTagName("backgroundcolor").getLength() > 0) {
            Element backgroundColorE = (Element) vizModelElement.getElementsByTagName("backgroundcolor").item(0);
            backgroundColor = ColorUtils.decode(backgroundColorE.getAttribute("value"));
        }

        if (vizModelElement.getElementsByTagName("edgeunicolor").getLength() > 0) {
            Element edgeUniColorE = (Element) vizModelElement.getElementsByTagName("edgeunicolor").item(0);
            edgeUniColor = ColorUtils.decode(edgeUniColorE.getAttribute("value")).getRGBComponents(null);
        }

        if (vizModelElement.getElementsByTagName("edgeInSelectionColor").getLength() > 0) {
            Element edgeInSelectionColorE = (Element) vizModelElement.getElementsByTagName("edgeInSelectionColor").item(0);
            edgeInSelectionColor = ColorUtils.decode(edgeInSelectionColorE.getAttribute("value")).getRGBComponents(null);
        }

        if (vizModelElement.getElementsByTagName("edgeOutSelectionColor").getLength() > 0) {
            Element edgeOutSelectionColorE = (Element) vizModelElement.getElementsByTagName("edgeOutSelectionColor").item(0);
            edgeOutSelectionColor = ColorUtils.decode(edgeOutSelectionColorE.getAttribute("value")).getRGBComponents(null);
        }

        if (vizModelElement.getElementsByTagName("edgeBothSelectionColor").getLength() > 0) {
            Element edgeBothSelectionColorE = (Element) vizModelElement.getElementsByTagName("edgeBothSelectionColor").item(0);
            edgeBothSelectionColor = ColorUtils.decode(edgeBothSelectionColorE.getAttribute("value")).getRGBComponents(null);
        }

        //Misc
        if (vizModelElement.getElementsByTagName("nodemodeler").getLength() > 0) {
            Element nodeModelerE = (Element) vizModelElement.getElementsByTagName("nodemodeler").item(0);
            nodeModeler = nodeModelerE.getAttribute("value");
        }

        //Float
        if (vizModelElement.getElementsByTagName("edgeScale").getLength() > 0) {
            Element edgeScaleE = (Element) vizModelElement.getElementsByTagName("edgeScale").item(0);
            edgeScale = Float.parseFloat(edgeScaleE.getAttribute("value"));
        }
    }

    public Element writeXML(Document document) {
        Element vizModelE = document.createElement("vizmodel");

        //Fast refreh
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        cameraPosition = Arrays.copyOf(drawable.getCameraLocation(), 3);
        cameraTarget = Arrays.copyOf(drawable.getCameraTarget(), 3);

        //TextModel
        Element textModelE = textModel.writeXML(document);
        vizModelE.appendChild(textModelE);

        //Camera
        Element cameraPostionE = document.createElement("cameraposition");
        cameraPostionE.setAttribute("x", Float.toString(cameraPosition[0]));
        cameraPostionE.setAttribute("y", Float.toString(cameraPosition[1]));
        cameraPostionE.setAttribute("z", Float.toString(cameraPosition[2]));
        vizModelE.appendChild(cameraPostionE);
        Element cameraTargetE = document.createElement("cameratarget");
        cameraTargetE.setAttribute("x", Float.toString(cameraTarget[0]));
        cameraTargetE.setAttribute("y", Float.toString(cameraTarget[1]));
        cameraTargetE.setAttribute("z", Float.toString(cameraTarget[2]));
        vizModelE.appendChild(cameraTargetE);

        //Boolean values
        Element use3dE = document.createElement("use3d");
        use3dE.setAttribute("value", String.valueOf(use3d));
        vizModelE.appendChild(use3dE);

        Element lightingE = document.createElement("lighting");
        lightingE.setAttribute("value", String.valueOf(lighting));
        vizModelE.appendChild(lightingE);

        Element cullingE = document.createElement("culling");
        cullingE.setAttribute("value", String.valueOf(culling));
        vizModelE.appendChild(cullingE);

        Element materialE = document.createElement("material");
        materialE.setAttribute("value", String.valueOf(material));
        vizModelE.appendChild(materialE);

        Element rotatingEnableE = document.createElement("rotatingenable");
        rotatingEnableE.setAttribute("value", String.valueOf(rotatingEnable));
        vizModelE.appendChild(rotatingEnableE);

        Element showEdgesE = document.createElement("showedges");
        showEdgesE.setAttribute("value", String.valueOf(showEdges));
        vizModelE.appendChild(showEdgesE);

        Element lightenNonSelectedAutoE = document.createElement("lightennonselectedauto");
        lightenNonSelectedAutoE.setAttribute("value", String.valueOf(lightenNonSelectedAuto));
        vizModelE.appendChild(lightenNonSelectedAutoE);

        Element autoSelectNeighborE = document.createElement("autoselectneighbor");
        autoSelectNeighborE.setAttribute("value", String.valueOf(autoSelectNeighbor));
        vizModelE.appendChild(autoSelectNeighborE);

        Element hideNonSelectedEdgesE = document.createElement("hidenonselectededges");
        hideNonSelectedEdgesE.setAttribute("value", String.valueOf(hideNonSelectedEdges));
        vizModelE.appendChild(hideNonSelectedEdgesE);

        Element uniColorSelectedE = document.createElement("unicolorselected");
        uniColorSelectedE.setAttribute("value", String.valueOf(uniColorSelected));
        vizModelE.appendChild(uniColorSelectedE);

        Element edgeHasUniColorE = document.createElement("edgehasunicolor");
        edgeHasUniColorE.setAttribute("value", String.valueOf(edgeHasUniColor));
        vizModelE.appendChild(edgeHasUniColorE);

        Element adjustByTextE = document.createElement("adjustbytext");
        adjustByTextE.setAttribute("value", String.valueOf(adjustByText));
        vizModelE.appendChild(adjustByTextE);

        Element edgeSelectionColorE = document.createElement("edgeSelectionColor");
        edgeSelectionColorE.setAttribute("value", String.valueOf(edgeSelectionColor));
        vizModelE.appendChild(edgeSelectionColorE);

        Element showHullsE = document.createElement("showHulls");
        showHullsE.setAttribute("value", String.valueOf(showHulls));
        vizModelE.appendChild(showHullsE);

        //Colors
        Element backgroundColorE = document.createElement("backgroundcolor");
        backgroundColorE.setAttribute("value", ColorUtils.encode(backgroundColor));
        vizModelE.appendChild(backgroundColorE);

        Element edgeUniColorE = document.createElement("edgeunicolor");
        edgeUniColorE.setAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeUniColor)));
        vizModelE.appendChild(edgeUniColorE);

        Element edgeInSelectionColorE = document.createElement("edgeInSelectionColor");
        edgeInSelectionColorE.setAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeInSelectionColor)));
        vizModelE.appendChild(edgeInSelectionColorE);

        Element edgeOutSelectionColorE = document.createElement("edgeOutSelectionColor");
        edgeOutSelectionColorE.setAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeOutSelectionColor)));
        vizModelE.appendChild(edgeOutSelectionColorE);

        Element edgeBothSelectionColorE = document.createElement("edgeBothSelectionColor");
        edgeBothSelectionColorE.setAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeBothSelectionColor)));
        vizModelE.appendChild(edgeBothSelectionColorE);

        //Misc
        Element nodeModelerE = document.createElement("nodemodeler");
        nodeModelerE.setAttribute("value", nodeModeler);
        vizModelE.appendChild(nodeModelerE);

        //Float
        Element edgeScaleE = document.createElement("edgeScale");
        edgeScaleE.setAttribute("value", String.valueOf(edgeScale));
        vizModelE.appendChild(edgeScaleE);

        return vizModelE;
    }
}
