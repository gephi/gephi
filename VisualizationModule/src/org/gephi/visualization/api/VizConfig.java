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

/**
 *
 * @author Mathieu Bastian
 */
public class VizConfig {

    public static enum DisplayConfig {

        DISPLAY_ALL, DISPLAY_NODES_ONLY, DISPLAY_NODES_EDGES, DISPLAY_ALPHA
    }
    private int antialiasing = 4;
    private boolean lineSmooth = true;
    private boolean lineSmoothNicest = true;
    private boolean pointSmooth = true;
    private boolean blending = true;
    private boolean lighting = true;
    private boolean material = true;
    private Color backgroundColor = Color.WHITE;
    private float[] defaultCameraPosition = {0f, 0f, 5000f};
    protected float[] nodeSelectedColor = {1f, 1f, 1f};
    protected boolean selectionEnable = true;
    protected boolean draggingEnable = true;
    protected boolean cameraControlEnable = true;
    protected boolean rotatingEnable = true;
    protected boolean directedEdges = true;
    protected boolean showFPS = true;
    protected float[] edgeInSelectedColor = {1f, 0f, 0f};
    protected float[] edgeOutSelectedColor = {1f, 1f, 0f};
    protected float[] edgeBothSelectedColor = {0f, 0f, 0f};
    protected DisplayConfig displayConfig = DisplayConfig.DISPLAY_ALL;
    protected float[] edgesColor = null;
    protected float edgeAlpha = 1f;
    protected int octreeDepth = 5;
    protected int octreeWidth = 10000;


    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    public float getEdgeAlpha() {
        return edgeAlpha;
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

    public float[] getEdgesColor() {
        return edgesColor;
    }

    public float[] getNodeSelectedColor() {
        return nodeSelectedColor;
    }

    public float[] getDefaultCameraPosition() {
        return defaultCameraPosition;
    }
    private float[] defaultCameraTarget = {0f, 0f, 0f};

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

    public boolean isDirectedEdges() {
        return directedEdges;
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public int getOctreeDepth() {
        return octreeDepth;
    }

    public int getOctreeWidth() {
        return octreeWidth;
    }
}
