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
import org.gephi.ui.utils.ColorUtils;
import org.gephi.ui.utils.FontUtils;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class VizConfig {

    //Const Default Config
    public static final String USE_3D = "VizConfig.defaultUse3d";
    public static final String BACKGROUND_COLOR = "VizConfig.defaultBackgroundColor";
    public static final String NODE_LABELS = "VizConfig.defaultShowNodeLabels";
    public static final String EDGE_LABELS = "VizConfig.defaultShowEdgeLabels";
    public static final String SHOW_EDGES = "VizConfig.defaultShowEdges";
    public static final String HIGHLIGHT = "VizConfig.defaultLightenNonSelectedAuto";
    public static final String NEIGHBOUR_SELECT = "VizConfig.defaultAutoSelectNeighbor";
    public static final String HIDE_NONSELECTED_EDGES = "VizConfig.defaultHideNonSelectedEdges";
    public static final String SELECTEDNODE_UNIQUE_COLOR = "VizConfig.defaultHideNonSelectedEdges";
    public static final String EDGE_HAS_UNIQUE_COLOR = "VizConfig.defaultEdgeHasUniColor";
    public static final String EDGE_UNIQUE_COLOR = "VizConfig.defaultEdgeUniColor";
    public static final String NODE_LABEL_COLOR = "VizConfig.defaultNodeLabelColor";
    public static final String EDGE_LABEL_COLOR = "VizConfig.defaultEdgeLabelColor";
    public static final String NODE_LABEL_FONT = "VizConfig.defaultNodeLabelFont";
    public static final String EDGE_LABEL_FONT = "VizConfig.defaultEdgeLabelFont";
    public static final String LABEL_SELECTION_ONLY = "VizConfig.defaultShowLabelOnSelectedOnly";
    public static final String SELECTEDEDGE_HAS_COLOR = "VizConfig.defaultEdgeSelectionColor";
    public static final String SELECTEDEDGE_IN_COLOR = "VizConfig.defaultEdgeInSelectedColor";
    public static final String SELECTEDEDGE_OUT_COLOR = "VizConfig.defaultEdgeOutSelectedColor";
    public static final String SELECTEDEDGE_BOTH_COLOR = "VizConfig.defaultEdgeBothSelectedColor";
    public static final String SHOW_HULLS = "VizConfig.defaultShowHulls";
    //Const Prefs
    public static final String ANTIALIASING = "VizConfig.antialiasing";
    public static final String BLENDING = "VizConfig.blending";
    public static final String WIREFRAME = "VizConfig.wireFrame";
    public static final String GLJPANEL = "VizConfig.useGLJPanel";
    public static final String SELECTION = "VizConfig.selectionEnable";
    public static final String RECTANGLE_SELECTION = "VizConfig.rectangleSelection";
    public static final String RECTANGLE_SELECTION_COLOR = "VizConfig.rectangleSelectionColor";
    public static final String DRAGGING = "VizConfig.draggingEnable";
    public static final String CAMERA_CONTROL = "VizConfig.cameraControlEnable";
    public static final String SHOW_FPS = "VizConfig.showFPS";
    public static final String REDUCE_FPS_MOUSE_OUT = "VizConfig.reduceFpsWhenMouseOut";
    public static final String REDUCE_FPS_MOUSE_OUT_VALUE = "VizConfig.reduceFpsWhenMouseOutValue";
    public static final String HIGHTLIGHT_COLOR = "VizConfig.lightenNonSelectedColor";
    public static final String HIGHTLIGHT_ANIMATION = "VizConfig.lightenNonSelectedAnimation";
    public static final String NODE_SELECTED_UNIQUE_COLOR = "VizConfig.uniColorSelectedColor";
    public static final String NODE_NEIGHBOR_SELECTED_UNIQUE_COLOR = "VizConfig.uniColorSelectedNeigborColor";
    public static final String OCTREE_DEPTH = "VizConfig.octreeDepth";
    public static final String OCTREE_WIDTH = "VizConfig.octreeWidth";
    public static final String CLEAN_DELETED_MODELS = "VizConfig.cleanDeletedModels";
    public static final String LABEL_MIPMAP = "VizConfig.labelMipMap";
    public static final String LABEL_ANTIALIASED = "VizConfig.labelAntialiased";
    public static final String LABEL_FRACTIONAL_METRICS = "VizConfig.labelFractionalMetrics";
    public static final String VIZBAR = "VizConfig.showVizVar";
    public static final String CONTEXT_MENU = "VizConfig.contextMenu";
    public static final String TOOLBAR = "VizConfig.toolbar";
    public static final String MOUSE_SELECTION_DIAMETER = "VizConfig.mouseSelectionDiameter";
    public static final String MOUSE_SELECTION_ZOOM_PROPORTIONAL = "VizConfig.mouseSelectionZoomProportionnal";
    public static final String DISABLE_LOD = "VizConfig.disableLOD";
    //Default values
    public static final boolean DEFAULT_USE_3D = false;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final boolean DEFAULT_NODE_LABELS = false;
    public static final boolean DEFAULT_EDGE_LABELS = false;
    public static final boolean DEFAULT_SHOW_EDGES = true;
    public static final boolean DEFAULT_HIGHLIGHT = true;
    public static final boolean DEFAULT_NEIGHBOUR_SELECT = true;
    public static final boolean DEFAULT_HIDE_NONSELECTED_EDGES = false;
    public static final boolean DEFAULT_SELECTEDNODE_UNIQUE_COLOR = false;
    public static final boolean DEFAULT_EDGE_HAS_UNIQUE_COLOR = false;
    public static final Color DEFAULT_EDGE_UNIQUE_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f);
    public static final Color DEFAULT_NODE_LABEL_COLOR = new Color(0f, 0f, 0f, 1f);
    public static final Color DEFAULT_EDGE_LABEL_COLOR = new Color(0.5f, 0.5f, 0.5f, 1f);
    public static final Font DEFAULT_NODE_LABEL_FONT = new Font("Arial", Font.BOLD, 20);
    public static final Font DEFAULT_EDGE_LABEL_FONT = new Font("Arial", Font.BOLD, 20);
    public static final boolean DEFAULT_LABEL_SELECTION_ONLY = false;
    public static final boolean DEFAULT_SELECTEDEDGE_HAS_COLOR = false;
    public static final Color DEFAULT_SELECTEDEDGE_IN_COLOR = new Color(32, 95, 154, 255);
    public static final Color DEFAULT_SELECTEDEDGE_OUT_COLOR = new Color(196, 66, 79, 255);
    public static final Color DEFAULT_SELECTEDEDGE_BOTH_COLOR = new Color(248, 215, 83, 255);
    public static final int DEFAULT_ANTIALIASING = 4;
    public static final boolean DEFAULT_BLENDING = true;
    public static final boolean DEFAULT_WIREFRAME = false;
    public static final boolean DEFAULT_GLJPANEL = false;
    public static final boolean DEFAULT_SELECTION = true;
    public static final boolean DEFAULT_RECTANGLE_SELECTION = false;
    public static final Color DEFAULT_RECTANGLE_SELECTION_COLOR = new Color(0.16f, 0.48f, 0.81f, 0.2f);
    public static final boolean DEFAULT_DRAGGING = true;
    public static final boolean DEFAULT_CAMERA_CONTROL = true;
    public static final boolean DEFAULT_SHOW_FPS = true;
    public static final boolean DEFAULT_REDUCE_FPS_MOUSE_OUT = true;
    public static final int DEFAULT_REDUCE_FPS_MOUSE_OUT_VALUE = 20;
    public static final Color DEFAULT_HIGHTLIGHT_COLOR = new Color(0.95f, 0.95f, 0.95f, 1f);
    public static final boolean DEFAULT_HIGHTLIGHT_ANIMATION = true;
    public static final Color DEFAULT_NODE_SELECTED_UNIQUE_COLOR = new Color(0.8f, 0.2f, 0.2f);
    public static final Color DEFAULT_NODE_NEIGHBOR_SELECTED_UNIQUE_COLOR = new Color(0.2f, 1f, 0.3f);
    public static final int DEFAULT_OCTREE_DEPTH = 5;
    public static final int DEFAULT_OCTREE_WIDTH = 10000;
    public static final boolean DEFAULT_CLEAN_DELETED_MODELS = true;
    public static final boolean DEFAULT_LABEL_MIPMAP = true;
    public static final boolean DEFAULT_LABEL_ANTIALIASED = true;
    public static final boolean DEFAULT_LABEL_FRACTIONAL_METRICS = true;
    public static final boolean DEFAULT_VIZBAR = true;
    public static final boolean DEFAULT_CONTEXT_MENU = true;
    public static final boolean DEFAULT_TOOLBAR = true;
    public static final int DEFAULT_MOUSE_SELECTION_DIAMETER = 1;
    public static final boolean DEFAULT_MOUSE_SELECTION_ZOOM_PROPORTIONAL = false;
    public static final boolean DEFAULT_DISABLE_LOD = false;
    public static final boolean DEFAULT_SHOW_HULLS = true;
    //Default config - loaded in the VizModel
    protected boolean defaultUse3d = NbPreferences.forModule(VizConfig.class).getBoolean(USE_3D, DEFAULT_USE_3D);
    protected boolean defaultLighting = false;  //Overriden by use3d
    protected boolean defaultCulling = false;   //Overriden by use3d
    protected boolean defaultMaterial = false;  //Overriden by use3d
    protected Color defaultBackgroundColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(BACKGROUND_COLOR, ColorUtils.encode(DEFAULT_BACKGROUND_COLOR)));
    protected float[] defaultCameraTarget = {0f, 0f, 0f};
    protected float[] defaultCameraPosition = {0f, 0f, 5000f};
    protected boolean defaultRotatingEnable = false;    //Overriden by use3d
    protected boolean defaultShowNodeLabels = NbPreferences.forModule(VizConfig.class).getBoolean(NODE_LABELS, DEFAULT_NODE_LABELS);
    protected boolean defaultShowEdgeLabels = NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_LABELS, DEFAULT_EDGE_LABELS);
    protected boolean defaultShowEdges = NbPreferences.forModule(VizConfig.class).getBoolean(SHOW_EDGES, DEFAULT_SHOW_EDGES);
    protected boolean defaultLightenNonSelectedAuto = NbPreferences.forModule(VizConfig.class).getBoolean(HIGHLIGHT, DEFAULT_HIGHLIGHT);
    protected boolean defaultAutoSelectNeighbor = NbPreferences.forModule(VizConfig.class).getBoolean(NEIGHBOUR_SELECT, DEFAULT_NEIGHBOUR_SELECT);
    protected boolean defaultHideNonSelectedEdges = NbPreferences.forModule(VizConfig.class).getBoolean(HIDE_NONSELECTED_EDGES, DEFAULT_HIDE_NONSELECTED_EDGES);
    protected boolean defaultUniColorSelected = NbPreferences.forModule(VizConfig.class).getBoolean(SELECTEDNODE_UNIQUE_COLOR, DEFAULT_SELECTEDNODE_UNIQUE_COLOR);
    protected boolean defaultEdgeHasUniColor = NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_HAS_UNIQUE_COLOR, DEFAULT_EDGE_HAS_UNIQUE_COLOR);
    protected Color defaultEdgeUniColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(EDGE_UNIQUE_COLOR, ColorUtils.encode(DEFAULT_EDGE_UNIQUE_COLOR)));
    protected Color defaultNodeLabelColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_COLOR, ColorUtils.encode(DEFAULT_NODE_LABEL_COLOR)));
    protected Color defaultEdgeLabelColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_COLOR, ColorUtils.encode(DEFAULT_EDGE_LABEL_COLOR)));
    protected Font defaultNodeLabelFont = Font.decode(NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_FONT, FontUtils.encode(DEFAULT_NODE_LABEL_FONT)));
    protected Font defaultEdgeLabelFont = Font.decode(NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_FONT, FontUtils.encode(DEFAULT_EDGE_LABEL_FONT)));
    protected boolean defaultAdjustByText = false;    //Overriden in Engine
    protected boolean defaultShowLabelOnSelectedOnly = NbPreferences.forModule(VizConfig.class).getBoolean(LABEL_SELECTION_ONLY, DEFAULT_LABEL_SELECTION_ONLY);
    protected String defaultNodeModeler = "CompatibilityNodeDiskModeler";       //Overriden by use3d
    protected boolean defaultEdgeSelectionColor = NbPreferences.forModule(VizConfig.class).getBoolean(SELECTEDEDGE_HAS_COLOR, DEFAULT_SELECTEDEDGE_HAS_COLOR);
    protected Color defaultEdgeInSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(SELECTEDEDGE_IN_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_IN_COLOR)));
    protected Color defaultEdgeOutSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(SELECTEDEDGE_OUT_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_OUT_COLOR)));
    protected Color defaultEdgeBothSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(SELECTEDEDGE_BOTH_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_BOTH_COLOR)));
    protected boolean defaultShowHulls = NbPreferences.forModule(VizConfig.class).getBoolean(SHOW_HULLS, DEFAULT_SHOW_HULLS);
    //Preferences
    protected int antialiasing = NbPreferences.forModule(VizConfig.class).getInt(ANTIALIASING, DEFAULT_ANTIALIASING);
    protected boolean lineSmooth = false;       //Not useful, GL_LINES
    protected boolean lineSmoothNicest = false;     //Not useful, GL_LINES
    protected boolean pointSmooth = false;          //Not useful, GL_POINTS
    protected boolean blending = NbPreferences.forModule(VizConfig.class).getBoolean(BLENDING, DEFAULT_BLENDING);
    protected boolean blendCinema = false;      //Not working
    protected boolean wireFrame = NbPreferences.forModule(VizConfig.class).getBoolean(WIREFRAME, DEFAULT_WIREFRAME);
    protected boolean useGLJPanel = NbPreferences.forModule(VizConfig.class).getBoolean(GLJPANEL, DEFAULT_GLJPANEL);
    protected float[] nodeSelectedColor = {1f, 1f, 1f};     //Not used
    protected boolean selectionEnable = NbPreferences.forModule(VizConfig.class).getBoolean(SELECTION, DEFAULT_SELECTION);
    protected boolean rectangleSelection = NbPreferences.forModule(VizConfig.class).getBoolean(RECTANGLE_SELECTION, DEFAULT_RECTANGLE_SELECTION);
    protected Color rectangleSelectionColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(RECTANGLE_SELECTION_COLOR, ColorUtils.encode(DEFAULT_RECTANGLE_SELECTION_COLOR)));
    protected boolean customSelection = false;      //Overriden in Engine
    protected boolean draggingEnable = NbPreferences.forModule(VizConfig.class).getBoolean(DRAGGING, DEFAULT_DRAGGING);
    protected boolean cameraControlEnable = NbPreferences.forModule(VizConfig.class).getBoolean(CAMERA_CONTROL, DEFAULT_CAMERA_CONTROL);
    protected boolean showFPS = NbPreferences.forModule(VizConfig.class).getBoolean(SHOW_FPS, DEFAULT_SHOW_FPS);
    protected boolean reduceFpsWhenMouseOut = NbPreferences.forModule(VizConfig.class).getBoolean(REDUCE_FPS_MOUSE_OUT, DEFAULT_REDUCE_FPS_MOUSE_OUT);
    protected int reduceFpsWhenMouseOutValue = NbPreferences.forModule(VizConfig.class).getInt(REDUCE_FPS_MOUSE_OUT_VALUE, DEFAULT_REDUCE_FPS_MOUSE_OUT_VALUE);
    protected boolean showArrows = true;        //Overriden in Engine
    protected boolean lightenNonSelected = true;        //Overriden in Engine
    protected float[] lightenNonSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(HIGHTLIGHT_COLOR, ColorUtils.encode(DEFAULT_HIGHTLIGHT_COLOR))).getRGBColorComponents(null);
    protected boolean lightenNonSelectedAnimation = NbPreferences.forModule(VizConfig.class).getBoolean(HIGHTLIGHT_ANIMATION, DEFAULT_HIGHTLIGHT_ANIMATION);
    protected float lightenNonSelectedFactor = 0.5f;        //Overriden in Engine
    protected float[] uniColorSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(NODE_SELECTED_UNIQUE_COLOR, ColorUtils.encode(DEFAULT_NODE_SELECTED_UNIQUE_COLOR))).getRGBColorComponents(null);
    protected float[] uniColorSelectedNeigborColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class).get(NODE_NEIGHBOR_SELECTED_UNIQUE_COLOR, ColorUtils.encode(DEFAULT_NODE_NEIGHBOR_SELECTED_UNIQUE_COLOR))).getRGBColorComponents(null);
    protected int octreeDepth = NbPreferences.forModule(VizConfig.class).getInt(OCTREE_DEPTH, DEFAULT_OCTREE_DEPTH);
    protected int octreeWidth = NbPreferences.forModule(VizConfig.class).getInt(OCTREE_WIDTH, DEFAULT_OCTREE_WIDTH);
    protected boolean cleanDeletedModels = NbPreferences.forModule(VizConfig.class).getBoolean(CLEAN_DELETED_MODELS, DEFAULT_CLEAN_DELETED_MODELS);
    protected boolean labelMipMap = NbPreferences.forModule(VizConfig.class).getBoolean(LABEL_MIPMAP, DEFAULT_LABEL_MIPMAP);
    protected boolean labelAntialiased = NbPreferences.forModule(VizConfig.class).getBoolean(LABEL_ANTIALIASED, DEFAULT_LABEL_ANTIALIASED);
    protected boolean labelFractionalMetrics = NbPreferences.forModule(VizConfig.class).getBoolean(LABEL_FRACTIONAL_METRICS, DEFAULT_LABEL_FRACTIONAL_METRICS);
    protected boolean useLabelRenderer3d = false;//no working   
    protected boolean showVizVar = NbPreferences.forModule(VizConfig.class).getBoolean(VIZBAR, DEFAULT_VIZBAR);
    protected boolean contextMenu = NbPreferences.forModule(VizConfig.class).getBoolean(CONTEXT_MENU, DEFAULT_CONTEXT_MENU);
    protected boolean toolbar = NbPreferences.forModule(VizConfig.class).getBoolean(TOOLBAR, DEFAULT_TOOLBAR);
    protected boolean propertiesbar = NbPreferences.forModule(VizConfig.class).getBoolean(HIGHTLIGHT_ANIMATION, DEFAULT_HIGHTLIGHT_ANIMATION);
    protected int mouseSelectionDiameter = NbPreferences.forModule(VizConfig.class).getInt(MOUSE_SELECTION_DIAMETER, DEFAULT_MOUSE_SELECTION_DIAMETER);
    protected boolean mouseSelectionZoomProportionnal = NbPreferences.forModule(VizConfig.class).getBoolean(MOUSE_SELECTION_ZOOM_PROPORTIONAL, DEFAULT_MOUSE_SELECTION_ZOOM_PROPORTIONAL);
    protected boolean disableLOD = NbPreferences.forModule(VizConfig.class).getBoolean(DISABLE_LOD, DEFAULT_DISABLE_LOD);

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

    public Color getDefaultEdgeLabelColor() {
        return defaultEdgeLabelColor;
    }

    public Font getDefaultEdgeLabelFont() {
        return defaultEdgeLabelFont;
    }

    public Color getDefaultEdgeUniColor() {
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

    public Color getDefaultNodeLabelColor() {
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

    public boolean isDefaultEdgeSelectionColor() {
        return defaultEdgeSelectionColor;
    }

    public Color getDefaultEdgeBothSelectedColor() {
        return defaultEdgeBothSelectedColor;
    }

    public Color getDefaultEdgeInSelectedColor() {
        return defaultEdgeInSelectedColor;
    }

    public Color getDefaultEdgeOutSelectedColor() {
        return defaultEdgeOutSelectedColor;
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

    public Color getRectangleSelectionColor() {
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

    public boolean isCustomSelection() {
        return customSelection;
    }

    public boolean isReduceFpsWhenMouseOut() {
        return reduceFpsWhenMouseOut;
    }

    public int getReduceFpsWhenMouseOutValue() {
        return reduceFpsWhenMouseOutValue;
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

    public void setCustomSelection(boolean customSelection) {
        this.customSelection = customSelection;
    }

    public boolean isDisableLOD() {
        return disableLOD;
    }

    public void setDisableLOD(boolean disableLOD) {
        this.disableLOD = disableLOD;
    }

    public boolean isDefaultShowHulls() {
        return defaultShowHulls;
    }
}
