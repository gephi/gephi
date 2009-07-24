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
package org.gephi.visualization.api;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathieu Bastian
 */
public class VizConfig {

    public static enum DisplayConfig {

        DISPLAY_ALL, DISPLAY_NODES_ONLY, DISPLAY_NODES_EDGES, DISPLAY_ALPHA
    }
    private int antialiasing = 4;
    private boolean use3d = false;
    private boolean lineSmooth = false;
    private boolean lineSmoothNicest = false;
    private boolean pointSmooth = false;
    private boolean blending = true;
    private boolean blendCinema = false;
    private boolean lighting = false;
    private boolean culling = false;
    private boolean material = false;
    private boolean wireFrame = false;
    private boolean useGLJPanel = false;
    private Color backgroundColor = Color.WHITE;
    private float[] defaultCameraTarget = {0f, 0f, 0f};
    private float[] defaultCameraPosition = {0f, 0f, 5000f};
    protected float[] nodeSelectedColor = {1f, 1f, 1f};
    protected boolean selectionEnable = true;
    protected boolean rectangleSelection = true;
    protected float[] rectangleSelectionColor = {0.16f, 0.48f, 0.81f, 0.2f};
    protected boolean draggingEnable = true;
    protected boolean cameraControlEnable = true;
    protected boolean rotatingEnable = true;
    protected boolean showFPS = true;
    protected boolean showEdges = true;
    protected boolean showArrows = true;
    protected boolean showLabels = false;
    protected boolean showEdgeLabels = true;
    protected boolean lightenNonSelectedAuto = true;
    protected boolean lightenNonSelected = true;
    protected float[] lightenNonSelectedColor = {0.95f, 0.95f, 0.95f, 1f};
    protected boolean lightenNonSelectedAnimation = true;
    protected float lightenNonSelectedFactor = 0.5f;
    protected boolean autoSelectNeighbor = true;
    protected boolean hideNonSelectedEdges = false;
    protected boolean uniColorSelected = false;
    protected float[] uniColorSelectedColor = {0.8f, 0.2f, 0.2f};
    protected float[] uniColorSelectedNeigborColor = {0.2f, 1f, 0.3f};
    protected float[] edgeInSelectedColor = {1f, 0f, 0f};
    protected float[] edgeOutSelectedColor = {1f, 1f, 0f};
    protected float[] edgeBothSelectedColor = {0f, 0f, 0f};
    protected DisplayConfig displayConfig = DisplayConfig.DISPLAY_ALL;
    protected boolean edgeUniColor = false;
    protected float[] edgeUniColorValue = {0.5f, 0.5f, 0.5f, 0.5f};
    protected int octreeDepth = 5;
    protected int octreeWidth = 100000;
    protected float[] defaultNodeLabelColor = {0f, 0f, 0f, 1f};
    protected float[] defaultEdgeLabelColor = {0.5f, 0.5f, 0.5f, 1f};
    protected Font defaultLabelFont = new Font("Arial", Font.BOLD, 20);
    protected boolean showVizVar = false;
    protected boolean visualizeTree = false;
    protected boolean contextMenu = true;
    protected boolean adjustByText = false;

    //Listener
    protected List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    public float[] getEdgeBothSelectedColor() {
        return edgeBothSelectedColor;
    }

    public float[] getEdgeInSelectedColor() {
        return edgeInSelectedColor;
    }

    public float[] getEdgeOutSelectedColor() {
        return edgeOutSelectedColor;
    }

    public float[] getNodeSelectedColor() {
        return nodeSelectedColor;
    }

    public float[] getDefaultCameraPosition() {
        return defaultCameraPosition;
    }

    public float[] getDefaultCameraTarget() {
        return defaultCameraTarget;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isLighting() {
        return lighting;
    }

    public boolean isBlending() {
        return blending;
    }

    public boolean isBlendingCinema() {
        return blendCinema;
    }

    public boolean isLineSmoothNicest() {
        return lineSmoothNicest;
    }

    public boolean isLineSmooth() {
        return lineSmooth;
    }

    public boolean isPointSmooth() {
        return pointSmooth;
    }

    public int getAntialiasing() {
        return antialiasing;
    }

    public boolean isMaterial() {
        return material;
    }

    public boolean isCameraControlEnable() {
        return cameraControlEnable;
    }

    public boolean isDraggingEnable() {
        return draggingEnable;
    }

    public boolean isRotatingEnable() {
        return rotatingEnable;
    }

    public boolean isSelectionEnable() {
        return selectionEnable;
    }

    public boolean isRectangleSelection() {
        return rectangleSelection;
    }

    public float[] getRectangleSelectionColor() {
        return rectangleSelectionColor;
    }

    public boolean isShowEdges() {
        return showEdges;
    }

    public boolean isShowArrows() {
        return showArrows;
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public boolean isShowLabels() {
        return showLabels;
    }

    public boolean isShowEdgeLabels() {
        return showEdgeLabels;
    }

    public boolean isAdjustByText() {
        return adjustByText;
    }

    public boolean isShowVizVar() {
        return showVizVar;
    }

    public boolean isVisualizeTree() {
        return visualizeTree;
    }

    public boolean isContextMenu() {
        return contextMenu;
    }

    public boolean isAutoSelectNeighbor() {
        return autoSelectNeighbor;
    }

    public boolean isLightenNonSelected() {
        return lightenNonSelected;
    }

    public boolean isLightenNonSelectedAuto() {
        return lightenNonSelectedAuto;
    }

    public boolean isLightenNonSelectedAnimation() {
        return lightenNonSelectedAnimation;
    }

    public float[] getLightenNonSelectedColor() {
        return lightenNonSelectedColor;
    }

    public void setLightenNonSelected(boolean lightenNonSelected) {
        this.lightenNonSelected = lightenNonSelected;
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public void setLightenNonSelectedFactor(float lightenNonSelectedFactor) {
        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
    }

    public int getOctreeDepth() {
        return octreeDepth;
    }

    public int getOctreeWidth() {
        return octreeWidth;
    }

    public boolean isWireFrame() {
        return wireFrame;
    }

    public boolean useGLJPanel() {
        return useGLJPanel;
    }

    public boolean use3d() {
        return use3d;
    }

    public boolean isCulling() {
        return culling;
    }

    public boolean isUniColorSelected() {
        return uniColorSelected;
    }

    public float[] getUniColorSelectedColor() {
        return uniColorSelectedColor;
    }

    public float[] getUniColorSelectedNeigborColor() {
        return uniColorSelectedNeigborColor;
    }

    public boolean isEdgeUniColor() {
        return edgeUniColor;
    }

    public float[] getEdgeUniColorValue() {
        return edgeUniColorValue;
    }

    public boolean isHideNonSelectedEdges() {
        return hideNonSelectedEdges;
    }

    public float[] getDefaultEdgeLabelColor() {
        return defaultEdgeLabelColor;
    }

    public float[] getDefaultNodeLabelColor() {
        return defaultNodeLabelColor;
    }

    public Font getDefaultLabelFont() {
        return defaultLabelFont;
    }

    public void setAntialiasing(int antialiasing) {
        this.antialiasing = antialiasing;
        fireProperyChange("antialiasing", null, antialiasing);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        fireProperyChange("backgroundColor", null, backgroundColor);
    }

    public void setDisplayConfig(DisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
        fireProperyChange("displayConfig", null, displayConfig);
    }

    public void setShowFPS(boolean showFPS) {
        this.showFPS = showFPS;
        fireProperyChange("fps", null, showFPS);
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
        fireProperyChange("showEdges", null, showEdges);
    }

    public void setShowArrows(boolean showArrows) {
        this.showArrows = showArrows;
        fireProperyChange("showArrows", null, showArrows);
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
