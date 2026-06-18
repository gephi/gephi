//#include "../common.frag.glsl"

//#include "common.node.frag.uniform.glsl"

//#include "common.node.struct.glsl"

//#include "common.node.frag.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    // SDF of the unit disk inside the [-1, 1] quad: distance from the center.
    float r = length(vLocal);
    // Screen-space derivative of r, used as the antialiasing band width.
    float aa = fwidth(r);
    // Coverage: 1 inside the disk, fading to 0 across the antialiased rim at r = 1.
    float coverage = 1.0 - smoothstep(1.0 - aa, 1.0, r);
    if (coverage <= 0.0) {
        discard;
    }

    vec4 color = vertexData.color;
    borderColor(color, vLocal);
    // Opaque interior like the previous geometry; the rim is antialiased via
    // sample-alpha-to-coverage, which is enabled while drawing nodes.
    color.a = coverage;
    fragColor = color;
}
