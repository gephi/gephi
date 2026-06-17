//#include "../common.vert.glsl"

//#include "common.node.vert.glsl"

//#include "common.node.vert.uniform.glsl"

//#include "../common.animation.glsl"

//#include "../common.datatexture.glsl"

//#include "common.node.vert.in.glsl"

//#include "common.node.struct.glsl"

flat out VertexData vertexData;
out vec2 vLocal;

void main() {
    vLocal = vert;

    vec4 nodeData = texelFetch(u_nodeTexture, dataTexelCoord(int(elementIndex)), 0);
    vec2 position = nodeData.xy;
    float size = nodeData.z * nodeScale;

    vec2 instancePosition = size * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    vec4 color = unpackColor(nodeData.w);
    color = mix(color, color * 1.1, animationCurve);

    vertexData.color = color;
}
