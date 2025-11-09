//#include "../common.vert.glsl"

//#include "common.node.vert.glsl"

//#include "common.node.vert.uniform.glsl"

//#include "common.node.vert.in.glsl"

//#include "common.node.struct.glsl"

flat out VertexData vertexData;

void main() {
    vec2 instancePosition = size * sizeMultiplier * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    color.rgb = color.rgb * colorMultiplier;

    vertexData.color = color;
}
