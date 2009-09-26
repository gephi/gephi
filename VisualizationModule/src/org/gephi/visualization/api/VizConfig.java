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

/**
 *
 * @author Mathieu Bastian
 */
public class VizConfig {

    //Default config - loaded in the VizModel
    protected boolean defaultUse3d = false;
    protected boolean defaultLighting = false;
    protected boolean defaultCulling = false;
    protected boolean defaultMaterial = false;
    protected Color defaultBackgroundColor = Color.WHITE;
    protected float[] defaultCameraTarget = {0f, 0f, 0f};
    protected float[] defaultCameraPosition = {0f, 0f, 5000f};
    protected boolean defaultRotatingEnable = false;
    protected boolean defaultShowNodeLabels = false;
    protected boolean defaultShowEdgeLabels = false;
    protected boolean defaultShowEdges = true;
    protected boolean defaultLightenNonSelectedAuto = true;
    protected boolean defaultAutoSelectNeighbor = true;
    protected boolean defaultHideNonSelectedEdges = false;
    protected boolean defaultUniColorSelected = false;
    protected boolean defaultEdgeHasUniColor = false;
    protected float[] defaultEdgeUniColor = {0.5f, 0.5f, 0.5f, 0.5f};
    protected float[] defaultNodeLabelColor = {0f, 0f, 0f, 1f};
    protected float[] defaultEdgeLabelColor = {0.5f, 0.5f, 0.5f, 1f};
    protected Font defaultNodeLabelFont = new Font("Arial", Font.BOLD, 20);
    protected Font defaultEdgeLabelFont = new Font("Arial", Font.BOLD, 20);
    protected boolean defaultAdjustByText = false;
    protected boolean defaultShowLabelOnSelectedOnly = false;
    protected String defaultNodeModeler = "CompatibilityNodeDiskModeler";

    //Preferences
    protected int antialiasing = 4;
    protected boolean lineSmooth = false;
    protected boolean lineSmoothNicest = false;
    protected boolean pointSmooth = false;
    protected boolean blending = true;
    protected boolean blendCinema = false;
    protected boolean wireFrame = false;
    protected boolean useGLJPanel = false;
    protected float[] nodeSelectedColor = {1f, 1f, 1f};
    protected boolean selectionEnable = true;
    protected boolean rectangleSelection = false;
    protected float[] rectangleSelectionColor = {0.16f, 0.48f, 0.81f, 0.2f};
    protected boolean draggingEnable = false;
    protected boolean cameraControlEnable = true;
    protected boolean showFPS = true;
    protected boolean showArrows = true;
    protected boolean lightenNonSelected = true;
    protected float[] lightenNonSelectedColor = {0.95f, 0.95f, 0.95f, 1f};
    protected boolean lightenNonSelectedAnimation = true;
    protected float lightenNonSelectedFactor = 0.5f;
    protected float[] uniColorSelectedColor = {0.8f, 0.2f, 0.2f};
    protected float[] uniColorSelectedNeigborColor = {0.2f, 1f, 0.3f};
    protected float[] edgeInSelectedColor = {1f, 0f, 0f};
    protected float[] edgeOutSelectedColor = {1f, 1f, 0f};
    protected float[] edgeBothSelectedColor = {0f, 0f, 0f};
    protected int octreeDepth = 5;
    protected int octreeWidth = 100000;
    protected boolean cleanDeletedModels = false;
    protected boolean labelMipMap = true;
    protected boolean labelAntialiased = true;
    protected boolean labelFractionalMetrics = true;
    protected boolean useLabelRenderer3d = false;//no working   
    protected boolean showVizVar = true;
    protected boolean visualizeTree = false;
    protected boolean contextMenu = true;
    protected boolean toolbar = true;
    protected boolean propertiesbar = true;
    protected int mouseSelectionDiameter = 1;
    protected boolean mouseSelectionZoomProportionnal = false;
    protected boolean disableLOD = false;

    public int getAntialiasing() {
        return antialiasing;
    }

    public boolean isBlendCinema() {
        return blendCinema;
    }

    public boolean isBlending() {
        return blending;
    }

    public boolean isCameraControlEnable() {
        return cameraControlEnable;
    }

    public boolean isCleanDeletedModels() {
        return cleanDeletedModels;
    }

    public boolean isContextMenu() {
        return contextMenu;
    }

    public boolean isDefaultAdjustByText() {
        return defaultAdjustByText;
    }

    public boolean isDefaultAutoSelectNeighbor() {
        return defaultAutoSelectNeighbor;
    }

    public Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public float[] getDefaultCameraPosition() {
        return defaultCameraPosition;
    }

    public float[] getDefaultCameraTarget() {
        return defaultCameraTarget;
    }

    public boolean isDefaultCulling() {
        return defaultCulling;
    }

    public boolean isDefaultEdgeHasUniColor() {
        return defaultEdgeHasUniColor;
    }

    public float[] getDefaultEdgeLabelColor() {
        return defaultEdgeLabelColor;
    }

    public Font getDefaultEdgeLabelFont() {
        return defaultEdgeLabelFont;
    }

    public float[] getDefaultEdgeUniColor() {
        return defaultEdgeUniColor;
    }

    public boolean isDefaultHideNonSelectedEdges() {
        return defaultHideNonSelectedEdges;
    }

    public boolean isDefaultLightenNonSelectedAuto() {
        return defaultLightenNonSelectedAuto;
    }

    public boolean isDefaultLighting() {
        return defaultLighting;
    }

    public boolean isDefaultMaterial() {
        return defaultMaterial;
    }

    public float[] getDefaultNodeLabelColor() {
        return defaultNodeLabelColor;
    }

    public Font getDefaultNodeLabelFont() {
        return defaultNodeLabelFont;
    }

    public boolean isDefaultRotatingEnable() {
        return defaultRotatingEnable;
    }

    public boolean isDefaultShowEdgeLabels() {
        return defaultShowEdgeLabels;
    }

    public boolean isDefaultShowLabelOnSelectedOnly() {
        return defaultShowLabelOnSelectedOnly;
    }

    public boolean isDefaultShowNodeLabels() {
        return defaultShowNodeLabels;
    }

    public boolean isDefaultUniColorSelected() {
        return defaultUniColorSelected;
    }

    public boolean isDefaultUse3d() {
        return defaultUse3d;
    }

    public boolean isDefaultShowEdges() {
        return defaultShowEdges;
    }

    public boolean isDraggingEnable() {
        return draggingEnable;
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

    public boolean isLabelAntialiased() {
        return labelAntialiased;
    }

    public boolean isLabelFractionalMetrics() {
        return labelFractionalMetrics;
    }

    public boolean isLabelMipMap() {
        return labelMipMap;
    }

    public boolean isLightenNonSelected() {
        return lightenNonSelected;
    }

    public boolean isLightenNonSelectedAnimation() {
        return lightenNonSelectedAnimation;
    }

    public float[] getLightenNonSelectedColor() {
        return lightenNonSelectedColor;
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public boolean isLineSmooth() {
        return lineSmooth;
    }

    public boolean isLineSmoothNicest() {
        return lineSmoothNicest;
    }

    public float[] getNodeSelectedColor() {
        return nodeSelectedColor;
    }

    public int getOctreeDepth() {
        return octreeDepth;
    }

    public int getOctreeWidth() {
        return octreeWidth;
    }

    public boolean isPointSmooth() {
        return pointSmooth;
    }

    public boolean isRectangleSelection() {
        return rectangleSelection;
    }

    public float[] getRectangleSelectionColor() {
        return rectangleSelectionColor;
    }

    public boolean isSelectionEnable() {
        return selectionEnable;
    }

    public boolean isShowArrows() {
        return showArrows;
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public boolean isShowVizVar() {
        return showVizVar;
    }

    public float[] getUniColorSelectedColor() {
        return uniColorSelectedColor;
    }

    public float[] getUniColorSelectedNeigborColor() {
        return uniColorSelectedNeigborColor;
    }

    public boolean isUseGLJPanel() {
        return useGLJPanel;
    }

    public boolean isUseLabelRenderer3d() {
        return useLabelRenderer3d;
    }

    public boolean isVisualizeTree() {
        return visualizeTree;
    }

    public boolean isWireFrame() {
        return wireFrame;
    }

    public String getDefaultNodeModeler() {
        return defaultNodeModeler;
    }

    public boolean isToolbar() {
        return toolbar;
    }

    public boolean isPropertiesbar() {
        return propertiesbar;
    }

    public int getMouseSelectionDiameter() {
        return mouseSelectionDiameter;
    }

    public boolean isMouseSelectionZoomProportionnal() {
        return mouseSelectionZoomProportionnal;
    }

    //Setters
    public void setLightenNonSelectedFactor(float lightenNonSelectedFactor) {
        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
    }

    public void setLightenNonSelected(boolean lightenNonSelected) {
        this.lightenNonSelected = lightenNonSelected;
    }

    public void setRectangleSelection(boolean rectangleSelection) {
        this.rectangleSelection = rectangleSelection;
    }

    public void setDraggingEnable(boolean draggingEnable) {
        this.draggingEnable = draggingEnable;
    }

    public void setSelectionEnable(boolean selectionEnable) {
        this.selectionEnable = selectionEnable;
    }

    public boolean isDisableLOD() {
        return disableLOD;
    }

    public void setDisableLOD(boolean disableLOD) {
        this.disableLOD = disableLOD;
    }
}
