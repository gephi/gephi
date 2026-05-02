float edge_thickness(float edgeScaleMin,
float edgeScaleMax,
float size,
float minWeight,
float weightDifferenceDivisor) {
    if (edgeScaleMin == edgeScaleMax) {
        return size * edgeScaleMin;
    }
    return mix(edgeScaleMin, edgeScaleMax, (size - minWeight) / weightDifferenceDivisor);
}
