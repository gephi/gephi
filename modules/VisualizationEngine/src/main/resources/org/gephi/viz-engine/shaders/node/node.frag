//#include "../common.frag.glsl"

//#include "common.node.struct.glsl"

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    fragColor = vertexData.color;
}
