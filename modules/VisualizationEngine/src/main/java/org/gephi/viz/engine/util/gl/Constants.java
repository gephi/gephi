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

    public static final int SHADER_VERT_LOCATION = 0;
    public static final int SHADER_POSITION_LOCATION = 1;
    public static final int SHADER_COLOR_LOCATION = 2;
    public static final int SHADER_SIZE_LOCATION = 3;
    public static final int SHADER_SOURCE_COLOR_LOCATION = 4;
    public static final int SHADER_TARGET_COLOR_LOCATION = 5;
    public static final int SHADER_SOURCE_SIZE_LOCATION = 6;
    public static final int SHADER_TARGET_SIZE_LOCATION = 7;
    public static final int SHADER_POSITION_TARGET_LOCATION = 8;

    public static final String UNIFORM_NAME_MODEL_VIEW_PROJECTION = "mvp";
    public static final String UNIFORM_NAME_EDGE_SCALE = "edgeScale";
    public static final String UNIFORM_NAME_MIN_WEIGHT = "minWeight";
    public static final String UNIFORM_NAME_MAX_WEIGHT = "maxWeight";
    public static final String UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR = "weightDifferenceDivisor";
    public static final String UNIFORM_NAME_EDGE_SCALE_MIN = "edgeScaleMin";
    public static final String UNIFORM_NAME_EDGE_SCALE_MAX = "edgeScaleMax";
    
    public static final String UNIFORM_NAME_BACKGROUND_COLOR = "backgroundColor";
    public static final String UNIFORM_NAME_SIZE_MULTIPLIER = "sizeMultiplier";
    public static final String UNIFORM_NAME_COLOR_LIGHTEN_FACTOR = "colorLightenFactor";
    public static final String UNIFORM_NAME_COLOR_MULTIPLIER = "colorMultiplier";

    //Rendering order:
    public static final int RENDERING_ORDER_NODES = 100;
    public static final int RENDERING_ORDER_EDGES = 50;

    public static final float NODER_BORDER_DARKEN_FACTOR = 0.498f;//Configurable?

    public static final float EDGE_SCALE_MIN = 0.4f;
    public static final float EDGE_SCALE_MAX = 8.0f;
    
    public static final String SHADERS_ROOT = "/org/gephi/viz-engine/shaders/";
}
