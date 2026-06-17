package org.gephi.viz.engine.util.gl;

/**
 *
 * @author Eduardo Ramos
 */
public class Constants {

    public static final String ATTRIB_NAME_VERT = "vert";
    public static final String ATTRIB_NAME_POSITION = "position";
    public static final String ATTRIB_NAME_POSITION_TARGET = "targetPosition";
    public static final String ATTRIB_NAME_COLOR = "elementColor";
    public static final String ATTRIB_NAME_COLOR_BIAS = "colorBias";
    public static final String ATTRIB_NAME_COLOR_MULTIPLIER = "colorMultiplier";
    public static final String ATTRIB_NAME_SIZE = "size";
    public static final String ATTRIB_NAME_SOURCE_COLOR = "sourceColor";
    public static final String ATTRIB_NAME_TARGET_COLOR = "targetColor";
    public static final String ATTRIB_NAME_SOURCE_SIZE = "sourceSize";
    public static final String ATTRIB_NAME_TARGET_SIZE = "targetSize";
    public static final String ATTRIB_NAME_SELFLOOP_NODE_SIZE = "nodeSize";

    // Texture-backed data pipeline: a single per-element index attribute replaces the
    // former per-instance vertex attributes (position/color/size/...). The shaders read the
    // actual per-element data from data textures using this index (see common.datatexture.glsl).
    public static final String ATTRIB_NAME_ELEMENT_INDEX = "elementIndex";

    public static final int SHADER_VERT_LOCATION = 0;
    public static final int SHADER_ELEMENT_INDEX_LOCATION = 1;
    public static final int SHADER_POSITION_LOCATION = 1;
    public static final int SHADER_COLOR_LOCATION = 2;
    public static final int SHADER_SIZE_LOCATION = 3;
    public static final int SHADER_SOURCE_COLOR_LOCATION = 4;
    public static final int SHADER_TARGET_COLOR_LOCATION = 5;
    public static final int SHADER_SOURCE_SIZE_LOCATION = 6;
    public static final int SHADER_TARGET_SIZE_LOCATION = 7;
    public static final int SHADER_POSITION_TARGET_LOCATION = 8;
    public static final int SHADER_SELFLOOP_NODE_SIZE_LOCATION = 9;

    public static final String UNIFORM_NAME_MODEL_VIEW_PROJECTION = "mvp";
    public static final String UNIFORM_NAME_EDGE_SCALE = "edgeScale";
    public static final String UNIFORM_NAME_MIN_WEIGHT = "minWeight";
    public static final String UNIFORM_NAME_MAX_WEIGHT = "maxWeight";
    public static final String UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR = "weightDifferenceDivisor";
    public static final String UNIFORM_NAME_EDGE_SCALE_MIN = "edgeScaleMin";
    public static final String UNIFORM_NAME_EDGE_SCALE_MAX = "edgeScaleMax";
    public static final String UNIFORM_NAME_BACKGROUND_COLOR = "backgroundColor";
    public static final String UNIFORM_NAME_COLOR_LIGHTEN_FACTOR = "colorLightenFactor";

    public static final String UNIFORM_NAME_NODE_SCALE = "nodeScale";

    public static final String UNIFORM_NAME_GLOBAL_TIME = "globalTime";
    public static final String UNIFORM_NAME_SELECTION_TIME = "selectionTime";

    public static final String UNIFORM_NAME_BORDER_SIZE = "borderSize";
    public static final String UNIFORM_NAME_EDGE_INSET = "edgeInset";
    public static final String UNIFORM_NAME_DARKEN_FACTOR = "nodeBorderDarkenFactor";

    // Data textures (RGBA32F) accessed via texelFetch:
    public static final String UNIFORM_NAME_NODE_TEXTURE = "u_nodeTexture";
    public static final String UNIFORM_NAME_ELEMENT_TEXTURE = "u_elementTexture";
    public static final String UNIFORM_NAME_TEXTURE_WIDTH = "u_texWidth";
    // Edge index source: 0 => instanced (use gl_InstanceID), >0 => array-draw (use gl_VertexID / value).
    public static final String UNIFORM_NAME_VERTS_PER_ELEMENT = "u_vertsPerElement";
    // Base element index added to the per-draw element index, so a single element texture holding
    // [unselected | selected] (and split into vertex-buffer-sized batches) can be drawn in ranges.
    public static final String UNIFORM_NAME_ELEMENT_OFFSET = "u_elementOffset";

    // Texture units for the data textures (must match the sampler uniforms set after program link).
    public static final int ELEMENT_TEXTURE_UNIT = 0;
    public static final int NODE_TEXTURE_UNIT = 1;
    //Rendering order:
    public static final int RENDERING_ORDER_LABELS = 200;
    public static final int RENDERING_ORDER_NODES = 100;
    public static final int RENDERING_ORDER_EDGES = 50;

    public static final String SHADERS_ROOT = "/org/gephi/viz/engine/shaders/";

    // Customizable Constants : Might worth considering having a proper static class
    private static final float NODE_BORDER_SIZE = 0.16f;
    private static final float NODE_BORDER_DARKEN_FACTOR = 0.498f;
    private static final float EDGE_INSET = 0.20f;

    public static float getNodeBorderSize() {
        return NODE_BORDER_SIZE;
    }

    public static float getNodeBorderDarkenFactor() {
        return NODE_BORDER_DARKEN_FACTOR;
    }

    public static float getEdgeInset() {
        return EDGE_INSET;
    }
}
