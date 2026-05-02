//#include "../common.frag.glsl"

//#include "common.node.frag.uniform.glsl"

//#include "common.node.struct.glsl"

//#include "common.node.frag.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    vec4 color = vertexData.color;
    borderColor(color, vLocal);
    fragColor = color;
}
