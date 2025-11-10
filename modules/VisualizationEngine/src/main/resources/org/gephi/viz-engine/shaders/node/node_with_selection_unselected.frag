//#include "../common.frag.glsl"

//#include "common.node.frag.uniform.glsl"

uniform vec4 backgroundColor;
uniform float colorLightenFactor;

//#include "../common.animation.glsl"

//#include "common.node.struct.glsl"

//#include "common.node.frag.glsl"
in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    vec4 color = vertexData.color;
    borderColor(color, vLocal);

    // Animation:
    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor * animationCurve);

    fragColor = color;
}
