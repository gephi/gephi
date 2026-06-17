uniform mat4 mvp;

uniform float minWeight;
uniform float weightDifferenceDivisor;
uniform float edgeScaleMin;
uniform float edgeScaleMax;
uniform float nodeScale;
uniform float edgeInset;

// Per-element edge data (sourceStoreId, targetStoreId, weight, colorBits) and the shared node data
// texture (x, y, rawSize, colorBits) indexed by node store id.
uniform sampler2D u_elementTexture;
uniform sampler2D u_nodeTexture;
