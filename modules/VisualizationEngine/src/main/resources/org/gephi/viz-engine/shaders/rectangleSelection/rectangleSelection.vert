#version 100

uniform mat4 mvp;
attribute vec2 vert;

void main() {
    gl_Position = mvp * vec4(vert.xy, 0.0, 1.0);
}
