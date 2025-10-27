float edge_thickness(float edgeScaleMin,
                     float edgeScaleMax,
                     float size,
                     float minWeight,
                     float weightDifferenceDivisor) {
    return mix(edgeScaleMin, edgeScaleMax, (size - minWeight) / weightDifferenceDivisor);
}
