// Texture-backed per-element data access (float 2D textures + texelFetch).
// Requires GLSL 330 (GL3 / GLES3).

uniform int u_texWidth;

// Node data is split across two RG32F textures (indexed by node store id): a position texture that
// streams during layout and a style texture re-uploaded only when size/color changes.
uniform sampler2D u_nodePosTexture;   // (x, y)
uniform sampler2D u_nodeStyleTexture; // (rawSize, colorBits)

// Maps a linear element index to 2D texel coordinates for a data texture of width u_texWidth.
ivec2 dataTexelCoord(int index) {
    return ivec2(index % u_texWidth, index / u_texWidth);
}

// Reassembles the full per-node datum (x, y, rawSize, colorBits) from the split position/style
// textures, so callers can keep treating node data as a single vec4 (.xy=pos, .z=size, .w=color).
vec4 fetchNodeData(int storeId) {
    ivec2 c = dataTexelCoord(storeId);
    vec2 pos = texelFetch(u_nodePosTexture, c, 0).xy;
    vec2 style = texelFetch(u_nodeStyleTexture, c, 0).xy;
    return vec4(pos, style.x, style.y);
}

// Unpacks a Java ARGB color (stored as the raw int bits of a float) into a normalized RGBA vec4.
// Mirrors the legacy attribute path: bytes are (B,G,R,A) little-endian and were swizzled .bgra.
vec4 unpackColor(float colorBits) {
    int packed = floatBitsToInt(colorBits);
    float b = float(packed & 0xFF);
    float g = float((packed >> 8) & 0xFF);
    float r = float((packed >> 16) & 0xFF);
    float a = float((packed >> 24) & 0xFF);
    return vec4(r, g, b, a) / 255.0;
}
