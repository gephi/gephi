//#include "../common.vert.glsl"

//#include "common.edge.vert.glsl"

//#include "common.edge.vert.uniform.glsl"
uniform vec4 backgroundColor;
uniform float colorLightenFactor;

//#include "../common.datatexture.glsl"

//#include "common.edge.index.glsl"

//#include "../common.animation.glsl"

//#include "common.edge.vert.in.glsl"

//#include "common.edge.struct.glsl"
flat out VertexData vertexData;

void main() {
    vec4 edgeData = texelFetch(u_elementTexture, dataTexelCoord(edgeElementIndex()), 0);
    int sourceStoreId = int(edgeData.x);
    int targetStoreId = int(edgeData.y);
    float size = edgeData.z;//It's the weight

    vec4 sourceData = texelFetch(u_nodeTexture, dataTexelCoord(sourceStoreId), 0);
    vec4 targetData = texelFetch(u_nodeTexture, dataTexelCoord(targetStoreId), 0);
    vec2 position = sourceData.xy;
    vec2 targetPosition = targetData.xy;
    float sourceSize = sourceData.z;
    float targetSize = targetData.z;

    float thickness = edge_thickness(edgeScaleMin, edgeScaleMax, size, minWeight, weightDifferenceDivisor);

    vec2 direction = targetPosition - position;
    vec2 directionNormalized = normalize(direction);

    vec2 sideVector = vec2(-directionNormalized.y, directionNormalized.x) * thickness * 0.5;

    vec2 lineStart = directionNormalized * (sourceSize * nodeScale);
    vec2 lineLength = (direction - lineStart) - directionNormalized * (targetSize * nodeScale);

    vec2 edgeVert = lineStart + lineLength * vert.x + sideVector * vert.y;

    gl_Position = mvp * vec4(edgeVert + position, 0.0, 1.0);

    vec4 color = unpackColor(edgeData.w);

    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor * animationCurve);

    vertexData.color = color;
}
