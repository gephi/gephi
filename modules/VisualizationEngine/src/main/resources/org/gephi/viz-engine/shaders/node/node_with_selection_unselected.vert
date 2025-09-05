//#include "../common.glsl"

uniform mat4 mvp;
uniform float sizeMultiplier;
uniform float colorMultiplier;
uniform vec4 backgroundColor;
uniform float colorLightenFactor;

uniform float globalTime;
uniform float selectionTime;

attribute vec2 vert;
attribute vec2 position;
attribute vec4 elementColor;
attribute float size;

varying vec4 fragColor;

void main() {	
    vec2 instancePosition = size * sizeMultiplier * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    float animationTime = globalTime-selectionTime;
    float animationCurve = _animationSlope(animationTime); // Going from 0. to 1. https://graphtoy.com/?f1(x,t)=1.-exp(-5.*x)&v1=true&f2(x,t)=&v2=false&f3(x,t)=&v3=false&f4(x,t)=&v4=false&f5(x,t)=&v5=false&f6(x,t)=&v6=false&grid=1&coords=0,0,2.3741360268016223
    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    color.rgb = color.rgb * colorMultiplier;
    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor*animationCurve*1.1);

    fragColor = color;
}
