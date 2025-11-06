#version 330

uniform mat4 MVPMatrix;

in vec4 MCVertex;
in vec2 TexCoord0;

out vec2 Coord0;

void main() {
   gl_Position = MVPMatrix * MCVertex;
   Coord0 = TexCoord0;
}