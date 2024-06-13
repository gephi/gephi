/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package org.gephi.layout.plugin.forceAtlas2;

/**
 *
 * @author LEVALLOIS
 */
public class DistanceCalculator {
    
    public static double approximateEuclideanDistance(double dx, double dy) {
        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);
        return 0.70710678118 * (absDx + absDy) + 0.5176380902 * Math.min(absDx, absDy);
    }
    
    public static double trueEuclideanDistance(double dx, double dy) {
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public static double pseudoEuclideanDistance(double dx, double dy) {
        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);
        double max = Math.max(absDx, absDy);
        double min = Math.min(absDx, absDy);
        return max + (min / 2.0);
    }
    
}
