//#include "../common.vert.glsl"

//#include "common.node.vert.uniform.glsl"

//#include "../common.datatexture.glsl"

//#include "common.node.vert.in.glsl"

// Flat per-node color encoding the node store id, read back from an offscreen buffer to identify the
// node under the cursor (GPU picking). vLocal carries the quad-local position for the SDF disk test.
flat out vec4 vPickColor;
out vec2 vLocal;

void main() {
    vLocal = vert;

    vec4 nodeData = fetchNodeData(int(elementIndex));
    vec2 position = nodeData.xy;
    float size = nodeData.z * nodeScale;

    vec2 instancePosition = size * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    // Encode (store id + 1) as a 24-bit RGB color so a cleared (0) pixel decodes to "no node".
    int id = int(elementIndex) + 1;
    vPickColor = vec4(
        float((id >> 16) & 0xFF) / 255.0,
        float((id >> 8) & 0xFF) / 255.0,
        float(id & 0xFF) / 255.0,
        1.0
    );
}
