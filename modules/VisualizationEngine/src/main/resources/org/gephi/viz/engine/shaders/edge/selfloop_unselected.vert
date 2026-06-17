//#include "../common.vert.glsl"

//#include "common.edge.vert.glsl"

//#include "../common.datatexture.glsl"

//#include "common.edge.index.glsl"

in vec2 vert;

//#include "../common.animation.glsl"

uniform mat4 mvp;
uniform vec4 backgroundColor;
uniform float colorLightenFactor;
uniform float minWeight;
uniform float weightDifferenceDivisor;
uniform float edgeScaleMin;
uniform float edgeScaleMax;
uniform float nodeScale;
uniform sampler2D u_elementTexture;
uniform sampler2D u_nodeTexture;

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

    vec4 edgeData = texelFetch(u_elementTexture, dataTexelCoord(edgeElementIndex()), 0);
    int nodeStoreId = int(edgeData.x);
    float size = edgeData.y;//It's the weight

    vec4 nodeData = texelFetch(u_nodeTexture, dataTexelCoord(nodeStoreId), 0);
    vec2 position = nodeData.xy;
    float nodeSize = nodeData.z;

    float thickness = edge_thickness(edgeScaleMin, edgeScaleMax, size, minWeight, weightDifferenceDivisor);
    float strokeWidth = thickness * STROKE_MULTIPLIER;
    float scaledNodeSize = nodeSize * nodeScale;
    float loopRadius = scaledNodeSize * 0.5 + strokeWidth * 0.33;
    vec2 instancePosition = loopRadius * vert + position + vec2(loopRadius);
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    // Compute inner radius for ring effect (in normalized space)
    float innerRadius = max(0.0, 1.0 - strokeWidth / loopRadius);
    vertexData.innerRadiusSq = innerRadius * innerRadius;

    vec4 color = unpackColor(edgeData.z);

    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor * animationCurve);
    vertexData.color = color;
}
