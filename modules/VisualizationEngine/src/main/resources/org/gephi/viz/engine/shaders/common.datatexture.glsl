// Texture-backed per-element data access (RGBA32F 2D textures + texelFetch).
// Requires GLSL 330 (GL3 / GLES3).

uniform int u_texWidth;

// Maps a linear element index to 2D texel coordinates for a data texture of width u_texWidth.
ivec2 dataTexelCoord(int index) {
    return ivec2(index % u_texWidth, index / u_texWidth);
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
