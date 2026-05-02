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
import java.awt.Font;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.ui.utils.FontUtils;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.openide.util.NbPreferences;

/**
 * @author Mathieu Bastian
 */
public class VizConfig {

    private VizConfig() {
        // Only static methods and fields
    }

    //Const Default Config
    public static final String BACKGROUND_COLOR = "VizConfig.defaultBackgroundColor";
    public static final String BACKGROUND_COLOR_DARK = "VizConfig.defaultDarkBackgroundColor";
    public static final String NODE_LABELS = "VizConfig.defaultShowNodeLabels";
    public static final String EDGE_LABELS = "VizConfig.defaultShowEdgeLabels";
    public static final String SHOW_EDGES = "VizConfig.defaultShowEdges";
    public static final String HIGHLIGHT = "VizConfig.defaultLightenNonSelectedAuto";
    public static final String HIGHLIGHT_FACTOR = "VizConfig.defaultLightenNonSelectedFactor";
    public static final String NEIGHBOUR_SELECT = "VizConfig.defaultAutoSelectNeighbor";
    public static final String HIDE_NONSELECTED_EDGES = "VizConfig.defaultHideNonSelectedEdges";
    public static final String EDGE_COLOR_MODE = "VizConfig.defaultEdgeColorMode";
    public static final String NODE_LABEL_FONT = "VizConfig.defaultNodeLabelFont";
    public static final String EDGE_LABEL_FONT = "VizConfig.defaultEdgeLabelFont";
    public static final String SELECTEDEDGE_HAS_COLOR = "VizConfig.defaultEdgeSelectionColor";
    public static final String SELECTEDEDGE_IN_COLOR = "VizConfig.defaultEdgeInSelectedColor";
    public static final String SELECTEDEDGE_OUT_COLOR = "VizConfig.defaultEdgeOutSelectedColor";
    public static final String SELECTEDEDGE_BOTH_COLOR = "VizConfig.defaultEdgeBothSelectedColor";
    public static final String EDGE_SCALE = "VizConfig.defaultEdgeScale";
    public static final String NODE_SCALE = "VizConfig.defaultNodeScale";
    public static final String EDGE_WEIGHTED = "VizConfig.defaultUseEdgeWeight";
    public static final String EDGE_RESCALE_WEIGHT = "VizConfig.defaultRescaleEdgeWeight";
    public static final String NODE_LABEL_SIZE_MODE = "VizConfig.defaultNodeLabelSizeMode";
    public static final String NODE_LABEL_COLOR_MODE = "VizConfig.defaultNodeLabelColorMode";
    public static final String NODE_LABEL_SCALE = "VizConfig.defaultNodeLabelScale";
    public static final String EDGE_LABEL_SCALE = "VizConfig.defaultEdgeLabelScale";
    public static final String EDGE_LABEL_SIZE_MODE = "VizConfig.defaultEdgeLabelSizeMode";
    public static final String EDGE_LABEL_COLOR_MODE = "VizConfig.defaultEdgeLabelColorMode";
    public static final String ZOOM = "VizConfig.defaultZoom";
    public static final String HIDE_NONSELECTED_NODE_LABELS = "VizConfig.hideNonSelectedNodeLabels";
    public static final String HIDE_NONSELECTED_EDGE_LABELS = "VizConfig.hideNonSelectedEdgeLabels";
    public static final String FIT_NODE_LABELS_TO_NODE_SIZE = "VizConfig.fitNodeLabelsToNodeSize";
    public static final String AVOID_NODE_LABEL_OVERLAP = "VizConfig.avoidNodeLabelOverlap";
    public static final String MOUSE_SELECTION_DIAMETER = "VizConfig.mouseSelectionDiameter";
    public static final String SCREENSHOT_SCALE_FACTOR = "VizConfig.screenshotScaleFactor";
    public static final String SCREENSHOT_TRANSPARENT_BACKGROUND = "VizConfig.screenshotTransparentBackground";
    public static final String SCREENSHOT_AUTO_SAVE = "VizConfig.screenshotAutoSave";
    //Const Prefs
    public static final String ANTIALIASING = "VizConfig.antialiasing";
    public static final String SHOW_FPS = "VizConfig.showFPS";
    public static final String CONTEXT_MENU = "VizConfig.contextMenu";
    public static final String ENGINE_DISABLE_INDIRECT_RENDERING = "VizConfig.engineDisableIndirectRendering";
    public static final String ENGINE_DISABLE_INSTANCED_RENDERING = "VizConfig.engineDisableInstancedRendering";
    public static final String ENGINE_DISABLE_VAOS = "VizConfig.engineDisableVAOs";
    public static final String ENGINE_DISABLE_VERTEX_ARRAY_DRAWING = "VizConfig.engineDisableVertexArrayDrawing";
    public static final String ENGINE_OPENGL_DEBUG = "VizConfig.engineOpenGLDebug";
    //Default values
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final Color DEFAULT_DARK_BACKGROUND_COLOR = new Color(52, 55, 57, 255);
    public static final boolean DEFAULT_NODE_LABELS = false;
    public static final boolean DEFAULT_EDGE_LABELS = false;
    public static final boolean DEFAULT_SHOW_EDGES = true;
    public static final boolean DEFAULT_HIGHLIGHT = true;
    public static final float DEFAULT_HIGHLIGHT_FACTOR = 0.9f;
    public static final boolean DEFAULT_NEIGHBOUR_SELECT = true;
    public static final EdgeColorMode DEFAULT_EDGE_COLOR_MODE = EdgeColorMode.SOURCE;
    public static final boolean DEFAULT_HIDE_NONSELECTED_EDGES = false;
    public static final Font DEFAULT_NODE_LABEL_FONT = new Font("Arial", Font.BOLD, 32);
    public static final Font DEFAULT_EDGE_LABEL_FONT = new Font("Arial", Font.BOLD, 32);
    public static final float DEFAULT_ZOOM = 0.3f;
    public static final boolean DEFAULT_HIDE_NONSELECTED_NODE_LABELS = false;
    public static final boolean DEFAULT_HIDE_NONSELECTED_EDGE_LABELS = false;
    public static final boolean DEFAULT_FIT_NODE_LABELS_TO_NODE_SIZE = true;
    public static final boolean DEFAULT_AVOID_NODE_LABEL_OVERLAP = true;
    public static final boolean DEFAULT_SELECTEDEDGE_HAS_COLOR = false;
    public static final Color DEFAULT_SELECTEDEDGE_IN_COLOR = new Color(32, 95, 154, 255);
    public static final Color DEFAULT_SELECTEDEDGE_OUT_COLOR = new Color(196, 66, 79, 255);
    public static final Color DEFAULT_SELECTEDEDGE_BOTH_COLOR = new Color(248, 215, 83, 255);
    public static final int DEFAULT_ANTIALIASING = 4;
    public static final boolean DEFAULT_SHOW_FPS = false;
    public static final boolean DEFAULT_CONTEXT_MENU = true;
    public static final int DEFAULT_MOUSE_SELECTION_DIAMETER = 1;
    public static final float DEFAULT_EDGE_SCALE = 2f;
    public static final float DEFAULT_NODE_SCALE = 1f;
    public static final float DEFAULT_NODE_LABEL_SCALE = 0.5f;
    public static final float DEFAULT_EDGE_LABEL_SCALE = 0.5f;
    public static final String DEFAULT_NODE_LABEL_SIZE_MODE = LabelSizeMode.ZOOM.name();
    public static final String DEFAULT_NODE_LABEL_COLOR_MODE = LabelColorMode.SELF.name();
    public static final String DEFAULT_EDGE_LABEL_SIZE_MODE = LabelSizeMode.ZOOM.name();
    public static final String DEFAULT_EDGE_LABEL_COLOR_MODE = LabelColorMode.SELF.name();
    public static final boolean DEFAULT_EDGE_WEIGHTED = true;
    public static final boolean DEFAULT_EDGE_RESCALE_WEIGHTED = true;
    public static final int DEFAULT_SCREENSHOT_SCALE_FACTOR = 1;
    public static final boolean DEFAULT_SCREENSHOT_TRANSPARENT_BACKGROUND = false;
    public static final boolean DEFAULT_SCREENSHOT_AUTO_SAVE = false;
    public static final boolean DEFAULT_ENGINE_DISABLE_INDIRECT_RENDERING = false;
    public static final boolean DEFAULT_ENGINE_DISABLE_INSTANCED_RENDERING = false;
    public static final boolean DEFAULT_ENGINE_DISABLE_VAOS = false;
    public static final boolean DEFAULT_ENGINE_DISABLE_VERTEX_ARRAY_DRAWING = false;
    public static final boolean DEFAULT_ENGINE_OPENGL_DEBUG = false;
    // Scale property bounds (float range displayed by the sliders).
    // Node/edge scale sliders are logarithmic: the geometric mean of MIN and MAX equals the
    // respective default value, so the slider is centred there (5× factor in each direction).
    public static final float NODE_SCALE_MIN = 0.2f;   // DEFAULT_NODE_SCALE / 5
    public static final float NODE_SCALE_MAX = 5.0f;   // DEFAULT_NODE_SCALE * 5
    public static final float EDGE_SCALE_MIN = 0.4f;   // DEFAULT_EDGE_SCALE / 5
    public static final float EDGE_SCALE_MAX = 10.0f;  // DEFAULT_EDGE_SCALE * 5
    // Label scale sliders are linear; min > 0 prevents invisible labels.
    public static final float NODE_LABEL_SCALE_MIN = 0.01f;
    public static final float NODE_LABEL_SCALE_MAX = 1.0f;
    public static final float EDGE_LABEL_SCALE_MIN = 0.01f;
    public static final float EDGE_LABEL_SCALE_MAX = 1.0f;

    //Default config - loaded in the VizModel
    protected static final Color defaultBackgroundColor = ColorUtils.decode(
        NbPreferences.forModule(VizConfig.class).get(BACKGROUND_COLOR, ColorUtils.encode(DEFAULT_BACKGROUND_COLOR)));
    protected static final Color defaultDarkBackgroundColor = ColorUtils.decode(
        NbPreferences.forModule(VizConfig.class)
            .get(BACKGROUND_COLOR_DARK, ColorUtils.encode(DEFAULT_DARK_BACKGROUND_COLOR)));
    protected static final boolean defaultShowNodeLabels =
        NbPreferences.forModule(VizConfig.class).getBoolean(NODE_LABELS, DEFAULT_NODE_LABELS);
    protected static final boolean defaultShowEdgeLabels =
        NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_LABELS, DEFAULT_EDGE_LABELS);
    protected static final boolean defaultShowEdges =
        NbPreferences.forModule(VizConfig.class).getBoolean(SHOW_EDGES, DEFAULT_SHOW_EDGES);
    protected static final EdgeColorMode defaultEdgeColorMode =
        EdgeColorMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(EDGE_COLOR_MODE, DEFAULT_EDGE_COLOR_MODE.name()));
    protected static final boolean defaultLightenNonSelectedAuto =
        NbPreferences.forModule(VizConfig.class).getBoolean(HIGHLIGHT, DEFAULT_HIGHLIGHT);
    protected static final float defaultLightenNonSelectedFactor =
        NbPreferences.forModule(VizConfig.class).getFloat(HIGHLIGHT_FACTOR, DEFAULT_HIGHLIGHT_FACTOR);
    protected static final boolean defaultAutoSelectNeighbor =
        NbPreferences.forModule(VizConfig.class).getBoolean(NEIGHBOUR_SELECT, DEFAULT_NEIGHBOUR_SELECT);
    protected static final boolean defaultHideNonSelectedEdges =
        NbPreferences.forModule(VizConfig.class).getBoolean(HIDE_NONSELECTED_EDGES, DEFAULT_HIDE_NONSELECTED_EDGES);
    protected static final Font defaultNodeLabelFont = Font.decode(
        NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_FONT, FontUtils.encode(DEFAULT_NODE_LABEL_FONT)));
    protected static final Font defaultEdgeLabelFont = Font.decode(
        NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_FONT, FontUtils.encode(DEFAULT_EDGE_LABEL_FONT)));
    protected static final boolean defaultHideNonSelectedNodeLabels =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(HIDE_NONSELECTED_NODE_LABELS, DEFAULT_HIDE_NONSELECTED_NODE_LABELS);
    protected static final boolean defaultHideNonSelectedEdgeLabels =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(HIDE_NONSELECTED_EDGE_LABELS, DEFAULT_HIDE_NONSELECTED_EDGE_LABELS);
    protected static final boolean defaultFitNodeLabelsToNodeSize =
        NbPreferences.forModule(VizConfig.class).getBoolean(FIT_NODE_LABELS_TO_NODE_SIZE,
            DEFAULT_FIT_NODE_LABELS_TO_NODE_SIZE);
    protected static final boolean defaultAvoidNodeLabelOverlap =
        NbPreferences.forModule(VizConfig.class).getBoolean(AVOID_NODE_LABEL_OVERLAP,
            DEFAULT_AVOID_NODE_LABEL_OVERLAP);
    protected static final boolean defaultEdgeSelectionColor =
        NbPreferences.forModule(VizConfig.class).getBoolean(SELECTEDEDGE_HAS_COLOR, DEFAULT_SELECTEDEDGE_HAS_COLOR);
    protected static final Color defaultEdgeInSelectedColor = ColorUtils.decode(NbPreferences.forModule(VizConfig.class)
        .get(SELECTEDEDGE_IN_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_IN_COLOR)));
    protected static final Color defaultEdgeOutSelectedColor =
        ColorUtils.decode(NbPreferences.forModule(VizConfig.class)
            .get(SELECTEDEDGE_OUT_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_OUT_COLOR)));
    protected static final Color defaultEdgeBothSelectedColor =
        ColorUtils.decode(NbPreferences.forModule(VizConfig.class)
            .get(SELECTEDEDGE_BOTH_COLOR, ColorUtils.encode(DEFAULT_SELECTEDEDGE_BOTH_COLOR)));
    protected static final float defaultEdgeScale =
        NbPreferences.forModule(VizConfig.class).getFloat(EDGE_SCALE, DEFAULT_EDGE_SCALE);
    protected static final float defaultNodeScale =
        NbPreferences.forModule(VizConfig.class).getFloat(NODE_SCALE, DEFAULT_NODE_SCALE);
    protected static final LabelSizeMode defaultNodeLabelSizeMode =
        LabelSizeMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_SIZE_MODE, DEFAULT_NODE_LABEL_SIZE_MODE));
    protected static final LabelColorMode defaultNodeLabelColorMode =
        LabelColorMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(NODE_LABEL_COLOR_MODE, DEFAULT_NODE_LABEL_COLOR_MODE));
    protected static final boolean defaultUseEdgeWeight =
        NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_WEIGHTED, DEFAULT_EDGE_WEIGHTED);
    protected static final boolean defaultRescaleEdgeWeight =
        NbPreferences.forModule(VizConfig.class).getBoolean(EDGE_RESCALE_WEIGHT, DEFAULT_EDGE_RESCALE_WEIGHTED);
    protected static final float defaultNodeLabelScale =
        NbPreferences.forModule(VizConfig.class).getFloat(NODE_LABEL_SCALE, DEFAULT_NODE_LABEL_SCALE);
    protected static final float defaultEdgeLabelScale =
        NbPreferences.forModule(VizConfig.class).getFloat(EDGE_LABEL_SCALE, DEFAULT_EDGE_LABEL_SCALE);
    protected static final LabelSizeMode defaultEdgeLabelSizeMode =
        LabelSizeMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_SIZE_MODE, DEFAULT_EDGE_LABEL_SIZE_MODE));
    protected static final LabelColorMode defaultEdgeLabelColorMode =
        LabelColorMode.valueOf(
            NbPreferences.forModule(VizConfig.class).get(EDGE_LABEL_COLOR_MODE, DEFAULT_EDGE_LABEL_COLOR_MODE));
    protected static final float defaultZoom = NbPreferences.forModule(VizConfig.class).getFloat(ZOOM, DEFAULT_ZOOM);
    protected static final int defaultMouseSelectionDiameter =
        NbPreferences.forModule(VizConfig.class).getInt(MOUSE_SELECTION_DIAMETER, DEFAULT_MOUSE_SELECTION_DIAMETER);
    protected static final int screenshotScaleFactor =
        NbPreferences.forModule(VizConfig.class)
            .getInt(SCREENSHOT_SCALE_FACTOR, DEFAULT_SCREENSHOT_SCALE_FACTOR);
    protected static final boolean screenshotTransparentBackground =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(SCREENSHOT_TRANSPARENT_BACKGROUND, DEFAULT_SCREENSHOT_TRANSPARENT_BACKGROUND);
    protected static final boolean screenshotAutoSave =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(SCREENSHOT_AUTO_SAVE, DEFAULT_SCREENSHOT_AUTO_SAVE);
    protected static final boolean engineDisableIndirectRendering =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(ENGINE_DISABLE_INDIRECT_RENDERING, DEFAULT_ENGINE_DISABLE_INDIRECT_RENDERING);
    protected static final boolean engineDisableInstancedRendering =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(ENGINE_DISABLE_INSTANCED_RENDERING, DEFAULT_ENGINE_DISABLE_INSTANCED_RENDERING);
    protected static final boolean engineDisableVAOs =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(ENGINE_DISABLE_VAOS, DEFAULT_ENGINE_DISABLE_VAOS);
    protected static final boolean engineDisableVertexArrayDrawing =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(ENGINE_DISABLE_VERTEX_ARRAY_DRAWING, DEFAULT_ENGINE_DISABLE_VERTEX_ARRAY_DRAWING);
    protected static final boolean engineOpenGLDebug =
        NbPreferences.forModule(VizConfig.class)
            .getBoolean(ENGINE_OPENGL_DEBUG, DEFAULT_ENGINE_OPENGL_DEBUG);
    //Preferences
    protected static final int antialiasing =
        NbPreferences.forModule(VizConfig.class).getInt(ANTIALIASING, DEFAULT_ANTIALIASING);
    protected static final boolean enableContextMenu =
        NbPreferences.forModule(VizConfig.class).getBoolean(CONTEXT_MENU, DEFAULT_CONTEXT_MENU);
    protected static final boolean showFps =
        NbPreferences.forModule(VizConfig.class).getBoolean(SHOW_FPS, DEFAULT_SHOW_FPS);

    public static int getAntialiasing() {
        return antialiasing;
    }

    public static boolean isEnableContextMenu() {
        return enableContextMenu;
    }

    public static boolean isShowFps() {
        return showFps;
    }

    public static float getDefaultZoom() {
        return defaultZoom;
    }

    public static Vector2fc getDefaultPan() {
        return new Vector2f(0.0f, 0.0f);
    }

    public static boolean isDefaultAutoSelectNeighbor() {
        return defaultAutoSelectNeighbor;
    }

    public static Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public static Color getDefaultDarkBackgroundColor() {
        return defaultDarkBackgroundColor;
    }

    public static Font getDefaultEdgeLabelFont() {
        return defaultEdgeLabelFont;
    }

    public static boolean isDefaultHideNonSelectedEdges() {
        return defaultHideNonSelectedEdges;
    }

    public static boolean isDefaultLightenNonSelectedAuto() {
        return defaultLightenNonSelectedAuto;
    }

    public static float getDefaultLightenNonSelectedFactor() {
        return defaultLightenNonSelectedFactor;
    }

    public static Font getDefaultNodeLabelFont() {
        return defaultNodeLabelFont;
    }

    public static boolean isDefaultShowEdgeLabels() {
        return defaultShowEdgeLabels;
    }

    public static boolean isDefaultHideNonSelectedNodeLabels() {
        return defaultHideNonSelectedNodeLabels;
    }

    public static boolean isDefaultHideNonSelectedEdgeLabels() {
        return defaultHideNonSelectedEdgeLabels;
    }

    public static boolean isDefaultFitNodeLabelsToNodeSize() {
        return defaultFitNodeLabelsToNodeSize;
    }

    public static boolean isDefaultAvoidNodeLabelOverlap() {
        return defaultAvoidNodeLabelOverlap;
    }

    public static boolean isDefaultShowNodeLabels() {
        return defaultShowNodeLabels;
    }

    public static boolean isDefaultShowEdges() {
        return defaultShowEdges;
    }

    public static boolean isDefaultEdgeSelectionColor() {
        return defaultEdgeSelectionColor;
    }

    public static Color getDefaultEdgeBothSelectedColor() {
        return defaultEdgeBothSelectedColor;
    }

    public static Color getDefaultEdgeInSelectedColor() {
        return defaultEdgeInSelectedColor;
    }

    public static Color getDefaultEdgeOutSelectedColor() {
        return defaultEdgeOutSelectedColor;
    }

    public static LabelSizeMode getDefaultNodeLabelSizeMode() {
        return defaultNodeLabelSizeMode;
    }

    public static LabelColorMode getDefaultNodeLabelColorMode() {
        return defaultNodeLabelColorMode;
    }

    public static boolean isDefaultUseEdgeWeight() {
        return defaultUseEdgeWeight;
    }

    public static boolean isDefaultRescaleEdgeWeight() {
        return defaultRescaleEdgeWeight;
    }

    public static float getDefaultNodeLabelScale() {
        return defaultNodeLabelScale;
    }

    public static float getDefaultEdgeLabelScale() {
        return defaultEdgeLabelScale;
    }

    public static LabelSizeMode getDefaultEdgeLabelSizeMode() {
        return defaultEdgeLabelSizeMode;
    }

    public static LabelColorMode getDefaultEdgeLabelColorMode() {
        return defaultEdgeLabelColorMode;
    }

    public static int getDefaultMouseSelectionDiameter() {
        return defaultMouseSelectionDiameter;
    }

    public static float getDefaultEdgeScale() {
        return defaultEdgeScale;
    }

    public static float getDefaultNodeScale() {
        return defaultNodeScale;
    }

    public static EdgeColorMode getDefaultEdgeColorMode() {
        return defaultEdgeColorMode;
    }

    public static int getDefaultScreenshotScaleFactor() {
        return screenshotScaleFactor;
    }

    public static boolean isDefaultScreenshotTransparentBackground() {
        return screenshotTransparentBackground;
    }

    public static boolean isDefaultScreenshotAutoSave() {
        return screenshotAutoSave;
    }

    public static boolean isEngineDisableIndirectRendering() {
        return engineDisableIndirectRendering;
    }

    public static boolean isEngineDisableInstancedRendering() {
        return engineDisableInstancedRendering;
    }

    public static boolean isEngineDisableVAOs() {
        return engineDisableVAOs;
    }

    public static boolean isEngineDisableVertexArrayDrawing() {
        return engineDisableVertexArrayDrawing;
    }

    public static boolean isEngineOpenGLDebug() {
        return engineOpenGLDebug;
    }

}
