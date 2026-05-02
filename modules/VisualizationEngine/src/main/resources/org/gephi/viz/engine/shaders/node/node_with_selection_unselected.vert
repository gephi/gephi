//#include "../common.vert.glsl"

//#include "common.node.vert.glsl"

//#include "common.node.vert.uniform.glsl"

//#include "common.node.vert.in.glsl"

//#include "common.node.struct.glsl"

flat out VertexData vertexData;
out vec2 vLocal;

void main() {
    vLocal = vert;

    vec2 instancePosition = size * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    vertexData.color = color;
}
