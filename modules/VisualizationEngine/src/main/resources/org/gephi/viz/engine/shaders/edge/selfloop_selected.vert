//#include "../common.vert.glsl"

//#include "common.edge.vert.glsl"

in vec2 vert;
in vec2 position;
in vec4 elementColor;
in float size;
in float nodeSize;

//#include "../common.animation.glsl"

uniform mat4 mvp;
uniform float colorLightenFactor;
uniform float minWeight;
uniform float weightDifferenceDivisor;
uniform float edgeScaleMin;
uniform float edgeScaleMax;
uniform float nodeScale;

struct VertexData {
    vec4 color;
    float innerRadiusSq; // squared inner radius for ring cutoff
};
flat out VertexData vertexData;
out vec2 vLocal;

// Multiplier to make self-loop stroke visually match regular edge thickness
const float STROKE_MULTIPLIER = 1.3;

void main() {
    vLocal = vert;

    float thickness = edge_thickness(edgeScaleMin, edgeScaleMax, size, minWeight, weightDifferenceDivisor);
    float strokeWidth = thickness * STROKE_MULTIPLIER;
    float scaledNodeSize = nodeSize * nodeScale;
    float loopRadius = scaledNodeSize * 0.5 + strokeWidth * 0.33;
    vec2 instancePosition = loopRadius * vert + position + vec2(loopRadius);
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    // Compute inner radius for ring effect (in normalized space)
    float innerRadius = max(0.0, 1.0 - strokeWidth / loopRadius);
    vertexData.innerRadiusSq = innerRadius * innerRadius;

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    vertexData.color = color;
}
