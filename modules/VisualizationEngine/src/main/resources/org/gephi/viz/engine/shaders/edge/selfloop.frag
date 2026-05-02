//#include "../common.frag.glsl"

in vec2 vLocal;
struct VertexData {
    vec4 color;
    float innerRadiusSq; // squared inner radius for ring cutoff
};
flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    float distSq = dot(vLocal, vLocal);
    
    // Discard pixels inside the inner radius (creates the ring/stroke effect)
    if (distSq <= vertexData.innerRadiusSq) discard;
    
    // Discard pixels outside the outer radius (circle edge)
    if (distSq > 1.0) discard;
    
    fragColor = vertexData.color;
}
