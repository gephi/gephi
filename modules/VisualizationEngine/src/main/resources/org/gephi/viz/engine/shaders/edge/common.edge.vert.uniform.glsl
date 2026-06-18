uniform mat4 mvp;

uniform float minWeight;
uniform float weightDifferenceDivisor;
uniform float edgeScaleMin;
uniform float edgeScaleMax;
uniform float nodeScale;
uniform float edgeInset;

// Per-element edge data (sourceStoreId, targetStoreId, weight, colorBits). The shared node data is
// read via fetchNodeData() (common.datatexture.glsl) from the split position/style textures.
uniform sampler2D u_elementTexture;
