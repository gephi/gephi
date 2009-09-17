/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.opengl.text.TextModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
public class VizModel {

    protected VizConfig config;

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
    protected boolean adjustByText;
    protected String nodeModeler;

    //Listener
    protected List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    public VizModel() {
        defaultValues();
    }

    public List<PropertyChangeListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<PropertyChangeListener> listeners) {
        this.listeners = listeners;
    }

    private void defaultValues() {
        textModel = new TextModel();
        config = VizController.getInstance().getVizConfig();
        use3d = config.isDefaultUse3d();
        lighting = config.isDefaultLighting();
        culling = config.isDefaultCulling();
        material = config.isDefaultMaterial();
        rotatingEnable = config.isDefaultRotatingEnable();
        backgroundColor = config.getDefaultBackgroundColor();

        showEdges = config.isDefaultShowEdges();
        lightenNonSelectedAuto = config.isDefaultLightenNonSelectedAuto();
        autoSelectNeighbor = config.isDefaultAutoSelectNeighbor();
        hideNonSelectedEdges = config.isDefaultHideNonSelectedEdges();
        uniColorSelected = config.isDefaultUniColorSelected();
        edgeHasUniColor = config.isDefaultEdgeHasUniColor();
        edgeUniColor = config.getDefaultEdgeUniColor();
        adjustByText = config.isDefaultAdjustByText();
        nodeModeler = config.getDefaultNodeModeler();
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
        fireProperyChange("use3d", null, use3d);
    }

    public void setNodeModeler(String nodeModeler) {
        this.nodeModeler = nodeModeler;
        fireProperyChange("nodeModeler", null, nodeModeler);
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
        cameraPosition[0] = Float.parseFloat(cameraPostionE.getAttribute("y"));
        cameraPosition[0] = Float.parseFloat(cameraPostionE.getAttribute("z"));
        Element cameraTargetE = (Element) vizModelElement.getElementsByTagName("cameratarget").item(0);
        cameraTarget[0] = Float.parseFloat(cameraTargetE.getAttribute("x"));
        cameraTarget[0] = Float.parseFloat(cameraTargetE.getAttribute("y"));
        cameraTarget[0] = Float.parseFloat(cameraTargetE.getAttribute("z"));

        //Boolean values
        Element use3dE = (Element) vizModelElement.getElementsByTagName("use3d").item(0);
        use3d = Boolean.parseBoolean(use3dE.getAttribute("value"));

        Element lightingE = (Element) vizModelElement.getElementsByTagName("lighting").item(0);
        lighting = Boolean.parseBoolean(lightingE.getAttribute("value"));

        Element cullingE = (Element) vizModelElement.getElementsByTagName("culling").item(0);
        culling = Boolean.parseBoolean(cullingE.getAttribute("value"));

        Element materialE = (Element) vizModelElement.getElementsByTagName("material").item(0);
        material = Boolean.parseBoolean(materialE.getAttribute("value"));

        Element rotatingEnableE = (Element) vizModelElement.getElementsByTagName("rotatingenable").item(0);
        rotatingEnable = Boolean.parseBoolean(rotatingEnableE.getAttribute("value"));

        Element showEdgesE = (Element) vizModelElement.getElementsByTagName("showedges").item(0);
        showEdges = Boolean.parseBoolean(showEdgesE.getAttribute("value"));

        Element lightenNonSelectedAutoE = (Element) vizModelElement.getElementsByTagName("lightennonselectedauto").item(0);
        lightenNonSelectedAuto = Boolean.parseBoolean(lightenNonSelectedAutoE.getAttribute("value"));

        Element autoSelectNeighborE = (Element) vizModelElement.getElementsByTagName("autoselectneighbor").item(0);
        autoSelectNeighbor = Boolean.parseBoolean(autoSelectNeighborE.getAttribute("value"));

        Element hideNonSelectedEdgesE = (Element) vizModelElement.getElementsByTagName("hidenonselectededges").item(0);
        hideNonSelectedEdges = Boolean.parseBoolean(hideNonSelectedEdgesE.getAttribute("value"));

        Element uniColorSelectedE = (Element) vizModelElement.getElementsByTagName("unicolorselected").item(0);
        uniColorSelected = Boolean.parseBoolean(uniColorSelectedE.getAttribute("value"));

        Element edgeHasUniColorE = (Element) vizModelElement.getElementsByTagName("edgehasunicolor").item(0);
        edgeHasUniColor = Boolean.parseBoolean(edgeHasUniColorE.getAttribute("value"));

        Element adjustByTextE = (Element) vizModelElement.getElementsByTagName("adjustbytext").item(0);
        adjustByText = Boolean.parseBoolean(adjustByTextE.getAttribute("value"));

        //Colors
        Element backgroundColorE = (Element) vizModelElement.getElementsByTagName("backgroundcolor").item(0);
        backgroundColor = new Color(Integer.parseInt(backgroundColorE.getAttribute("r")),
                Integer.parseInt(backgroundColorE.getAttribute("g")),
                Integer.parseInt(backgroundColorE.getAttribute("b")));

        Element edgeUniColorE = (Element) vizModelElement.getElementsByTagName("edgeunicolor").item(0);
        edgeUniColor = new float[]{Float.parseFloat(edgeUniColorE.getAttribute("r")),
                    Float.parseFloat(edgeUniColorE.getAttribute("g")),
                    Float.parseFloat(edgeUniColorE.getAttribute("b")),
                    Float.parseFloat(edgeUniColorE.getAttribute("a"))};

        //Misc
        Element nodeModelerE = (Element) vizModelElement.getElementsByTagName("nodemodeler").item(0);
        nodeModeler = nodeModelerE.getAttribute("value");
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

        //Colors
        Element backgroundColorE = document.createElement("backgroundcolor");
        backgroundColorE.setAttribute("r", Integer.toString(backgroundColor.getRed()));
        backgroundColorE.setAttribute("g", Integer.toString(backgroundColor.getGreen()));
        backgroundColorE.setAttribute("b", Integer.toString(backgroundColor.getBlue()));
        vizModelE.appendChild(backgroundColorE);

        Element edgeUniColorE = document.createElement("edgeunicolor");
        edgeUniColorE.setAttribute("r", Float.toString(edgeUniColor[0]));
        edgeUniColorE.setAttribute("g", Float.toString(edgeUniColor[1]));
        edgeUniColorE.setAttribute("b", Float.toString(edgeUniColor[2]));
        edgeUniColorE.setAttribute("a", Float.toString(edgeUniColor[3]));
        vizModelE.appendChild(edgeUniColorE);

        //Misc
        Element nodeModelerE = document.createElement("nodemodeler");
        nodeModelerE.setAttribute("value", nodeModeler);
        vizModelE.appendChild(nodeModelerE);

        return vizModelE;
    }
}
