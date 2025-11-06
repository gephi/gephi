#version 330

//#include "../common.animation.glsl"

uniform sampler2D Texture;
uniform vec4 Color=vec4(1,1,1,1);

in vec2 Coord0;

out vec4 FragColor;

void main() {
   float tsample = texture(Texture,Coord0).r;
   FragColor = Color * tsample;
}