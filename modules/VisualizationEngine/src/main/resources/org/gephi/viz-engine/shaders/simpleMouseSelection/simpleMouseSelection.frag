#version 100

#ifdef GL_ES
precision lowp float;
#endif

void main() {
    gl_FragColor = vec4(0.3, 0.3, 0.3, 0.2);
}
