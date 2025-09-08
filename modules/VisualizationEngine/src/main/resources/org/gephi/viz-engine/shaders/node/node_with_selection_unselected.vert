//#include "../common.vert.glsl"

uniform mat4 mvp;
uniform float sizeMultiplier;
uniform float colorMultiplier;
uniform vec4 backgroundColor;
uniform float colorLightenFactor;

//#include "common.node.attribute.vert.glsl"

varying vec4 fragColor;

void main() {	
    vec2 instancePosition = size * sizeMultiplier * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    color.rgb = color.rgb * colorMultiplier;
    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor*animationCurve*1.75);

    fragColor = color;
}
