#version 100

#ifdef GL_ES
precision lowp float;
#endif
uniform float globalTime;
uniform int selectionMode;
uniform float selectionTime;
varying vec4 fragColor;

void main() {

    gl_FragColor = fragColor;//*sin(globalTime+length(fragColor)*4.0)*.5+.5;
}
