//#include "../common.vert.glsl"

//#include "common.edge.vert.glsl"
//#include "common.edge.vert.uniform.glsl"
//#include "common.edge.vert.attribute.glsl"

varying vec4 fragColor;

void main() {
    float thickness = edge_thickness(edgeScaleMin, edgeScaleMax, size ,minWeight, weightDifferenceDivisor);

    vec2 direction = targetPosition - position;
    vec2 directionNormalized = normalize(direction);

    vec2 sideVector = vec2(-directionNormalized.y, directionNormalized.x) * thickness * 0.5;

    vec2 lineStart = directionNormalized * (sourceSize * nodeScale);
    vec2 lineLength = (direction - lineStart) - directionNormalized * (targetSize * nodeScale);

    vec2 edgeVert = lineStart + lineLength * vert.x + sideVector * vert.y;

    gl_Position = mvp * vec4(edgeVert + position, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    fragColor = color;
}
