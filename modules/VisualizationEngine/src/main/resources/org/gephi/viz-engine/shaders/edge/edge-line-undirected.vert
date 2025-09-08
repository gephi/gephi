//#include "../common.vert.glsl"
//#include "common.edge.vert.glsl"

uniform mat4 mvp;

uniform vec4 backgroundColor;
uniform float colorLightenFactor;
uniform float minWeight;
uniform float weightDifferenceDivisor;
uniform float edgeScaleMin;
uniform float edgeScaleMax;
uniform float nodeScale;

attribute vec2 vert;
attribute vec2 position;
attribute vec2 targetPosition;
attribute float size;//It's the weight
attribute vec4 elementColor;
attribute float sourceSize;
attribute float targetSize;

varying vec4 fragColor;

void main() {
    float thickness = edge_thickness(edgeScaleMin, edgeScaleMax, size ,minWeight, weightDifferenceDivisor);

    vec2 direction = targetPosition - position;
    vec2 directionNormalized = normalize(direction);

    vec2 sideVector = vec2(-directionNormalized.y, directionNormalized.x) * thickness * 0.5;

    vec2 lineStart = directionNormalized * (sourceSize * nodeScale);
    vec2 lineLength = (direction - lineStart) - directionNormalized * (targetSize * nodeScale);

    vec2 edgeVert = lineStart + lineLength * vert.x + sideVector * vert.y;

    gl_Position = mvp * vec4(edgeVert + position, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    color.rgb = mix(color.rgb, backgroundColor.rgb,colorLightenFactor*(1.-animationCurve));

    fragColor = color;
}
