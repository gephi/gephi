#version 330

#ifdef GL_ES
precision lowp float;
#endif

varying vec4 fragColor;

void main() {
    gl_FragColor = fragColor;
}
