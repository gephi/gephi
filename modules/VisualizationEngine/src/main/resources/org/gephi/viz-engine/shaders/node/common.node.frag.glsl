void borderColor(inout vec4 color, vec2 position) {
    float r2 = dot(position, position);
    float t = 1.0 - borderSize;// inner edge of border
    float t2 = t * t;

    color.rgb = r2 < t2 ? color.rgb : clamp(color.rgb * nodeBorderDarkenFactor, vec3(.1), vec3(.9));
}