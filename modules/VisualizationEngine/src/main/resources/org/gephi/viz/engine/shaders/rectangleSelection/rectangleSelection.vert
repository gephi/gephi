//#include "../common.vert.glsl"

uniform mat4 mvp;
in vec2 vert;

void main() {
    gl_Position = mvp * vec4(vert.xy, 0.0, 1.0);
}
