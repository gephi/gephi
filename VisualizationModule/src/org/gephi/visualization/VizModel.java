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
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.opengl.text.TextModel;

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
    protected boolean use3d = false;
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

    public void writeModel() {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        cameraPosition = Arrays.copyOf(drawable.getCameraLocation(), 3);
        cameraTarget = Arrays.copyOf(drawable.getCameraTarget(), 3);
        textModel = VizController.getInstance().getTextManager().getModel();
    }

    public void loadModel() {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        drawable.setCameraLocation(Arrays.copyOf(cameraPosition, 3));
        drawable.setCameraTarget(Arrays.copyOf(cameraTarget, 3));
        VizController.getInstance().getTextManager().setModel(textModel);
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
}
