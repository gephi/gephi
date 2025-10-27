//#include "../common.vert.glsl"

//#include "common.node.vert.glsl"
//#include "common.node.vert.attribute.glsl"
//#include "common.node.vert.uniform.glsl"

uniform vec4 backgroundColor;
uniform float colorLightenFactor;




varying vec4 fragColor;

void main() {
    vec2 instancePosition = size * sizeMultiplier * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    color.rgb = color.rgb * colorMultiplier;
    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor);

    fragColor = color;
}
