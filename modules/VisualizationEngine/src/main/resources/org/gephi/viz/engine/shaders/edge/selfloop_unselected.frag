//#include "../common.frag.glsl"

in vec2 vLocal;
struct VertexData {
    vec4 color;
    float innerRadiusSq; // squared inner radius for ring cutoff
};
flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    // SDF ring inside the [-1, 1] quad: keep the band between the inner radius and the unit circle.
    float r = length(vLocal);
    // Screen-space derivative of r, used as the antialiasing band width.
    float aa = fwidth(r);
    float innerRadius = sqrt(vertexData.innerRadiusSq);
    // Outer rim: 1 inside r < 1, fading to 0 across the antialiased rim at r = 1.
    float outer = 1.0 - smoothstep(1.0 - aa, 1.0, r);
    // Inner rim: 0 inside the hole, fading to 1 past the inner radius (skipped for a filled disk).
    float inner = innerRadius > 0.0 ? smoothstep(innerRadius - aa, innerRadius + aa, r) : 1.0;
    float coverage = outer * inner;
    if (coverage <= 0.0) {
        discard;
    }

    vec4 color = vertexData.color;
    // The rim is antialiased via sample-alpha-to-coverage, enabled while drawing self-loops.
    color.a = coverage;
    fragColor = color;
}
