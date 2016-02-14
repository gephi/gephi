/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance.plugin.palette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author mbastian
 */
public class PaletteGenerator {

    private static final float[] DEFAULT_FILTER = new float[]{0, 360, 0, 3, 0, 1.5f};

    public static Color[] generatePalette(int colorsCount, int quality) {
        return generatePalette(colorsCount, quality, false, null, null);
    }

    public static Color[] generatePalette(int colorsCount, int quality, Random random) {
        return generatePalette(colorsCount, quality, false, random, null);
    }

    public static Color[] generatePalette(int colorsCount, int quality, float[] filter) {
        return generatePalette(colorsCount, quality, false, null, filter);
    }

    public static Color[] generatePalette(int colorsCount, int quality, boolean ultraPrecision, Random random, float[] filter) {
        if (filter == null) {
            filter = DEFAULT_FILTER;
        }
        if (random == null) {
            random = new Random();
        }

        double[][] kMeans = generateRandomKmeans(colorsCount, random, filter);

        List<double[]> colorSamples = new ArrayList<>();
        if (ultraPrecision) {
            for (double l = 0; l <= 1; l += 0.01) {
                for (double a = -1; a <= 1; a += 0.05) {
                    for (double b = -1; b <= 1; b += 0.05) {
                        if (checkColor2(l, a, b, filter)) {
                            colorSamples.add(new double[]{l, a, b});
                        }
                    }
                }
            }
        } else {
            for (double l = 0; l <= 1; l += 0.05) {
                for (double a = -1; a <= 1; a += 0.1) {
                    for (double b = -1; b <= 1; b += 0.1) {
                        if (checkColor2(l, a, b, filter)) {
                            colorSamples.add(new double[]{l, a, b});
                        }
                    }
                }
            }
        }

        // Steps
        int[] samplesClosest = new int[colorSamples.size()];
        int steps = quality;
        while (steps-- > 0) {
            // kMeans -> Samples Closest
            for (int i = 0; i < colorSamples.size(); i++) {
                double[] lab = colorSamples.get(i);
                double minDistance = 1000000;
                for (int j = 0; j < kMeans.length; j++) {
                    double[] kMean = kMeans[j];
                    double distance = Math.sqrt(Math.pow(lab[0] - kMean[0], 2) + Math.pow(lab[1] - kMean[1], 2) + Math.pow(lab[2] - kMean[2], 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                        samplesClosest[i] = j;
                    }
                }
            }

            // Samples -> kMeans
            List<double[]> freeColorSamples = colorSamples;
            for (int j = 0; j < kMeans.length; j++) {
                int count = 0;
                double[] candidateKMean = new double[]{0, 0, 0};
                for (int i = 0; i < colorSamples.size(); i++) {
                    if (samplesClosest[i] == j) {
                        count++;
                        double[] colorSample = colorSamples.get(i);
                        candidateKMean[0] += colorSample[0];
                        candidateKMean[1] += colorSample[1];
                        candidateKMean[2] += colorSample[2];
                    }
                }
                if (count != 0) {
                    candidateKMean[0] /= count;
                    candidateKMean[1] /= count;
                    candidateKMean[2] /= count;
                }

                if (count != 0 && checkColor2(candidateKMean[0], candidateKMean[1], candidateKMean[2], filter)) {
                    kMeans[j] = candidateKMean;
                } else // The candidate kMean is out of the boundaries of the color space, or unfound.
                if (freeColorSamples.size() > 0) {
                    // We just search for the closest FREE color of the candidate kMean
                    double minDistance = 10000000000.0;
                    int closest = -1;
                    for (int i = 0; i < freeColorSamples.size(); i++) {
                        double distance = Math.sqrt(Math.pow(freeColorSamples.get(i)[0] - candidateKMean[0], 2) + Math.pow(freeColorSamples.get(i)[1] - candidateKMean[1], 2) + Math.pow(freeColorSamples.get(i)[2] - candidateKMean[2], 2));
                        if (distance < minDistance) {
                            minDistance = distance;
                            closest = i;
                        }
                    }
                    kMeans[j] = colorSamples.get(closest);

                } else {
                    // Then we just search for the closest color of the candidate kMean
                    double minDistance = 10000000000.0;
                    int closest = -1;
                    for (int i = 0; i < colorSamples.size(); i++) {
                        double distance = Math.sqrt(Math.pow(colorSamples.get(i)[0] - candidateKMean[0], 2) + Math.pow(colorSamples.get(i)[1] - candidateKMean[1], 2) + Math.pow(colorSamples.get(i)[2] - candidateKMean[2], 2));
                        if (distance < minDistance) {
                            minDistance = distance;
                            closest = i;
                        }
                    }
                    kMeans[j] = colorSamples.get(closest);
                }
                List<double[]> newFreeColorSamples = new ArrayList<>();
                for (double[] color : freeColorSamples) {
                    double[] kMean = kMeans[j];
                    if (color[0] != kMean[0]
                            || color[1] != kMean[1]
                            || color[2] != kMean[2]) {
                        newFreeColorSamples.add(color);
                    }
                }
                freeColorSamples = newFreeColorSamples;
            }
        }
        kMeans = sortColors(kMeans);
        Color[] res = new Color[kMeans.length];
        for (int i = 0; i < kMeans.length; i++) {
            double[] kmean = kMeans[i];
            int[] rgb = lab2rgb(kmean[0], kmean[1], kmean[2]);
            res[i] = new Color(rgb[0], rgb[1], rgb[2]);
        }
        return res;
    }

    private static double[][] generateRandomKmeans(int colorsCount, Random random, float[] filter) {
        double[][] kMeans = new double[colorsCount][];
        for (int i = 0; i < colorsCount; i++) {
            double[] lab = new double[]{random.nextDouble(), 2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1};
            while (!checkColor2(lab, filter)) {
                lab = new double[]{random.nextDouble(), 2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1};
            }
            kMeans[i] = lab;
        }
        return kMeans;
    }

    private static double[][] sortColors(double[][] colors) {
        LinkedList<double[]> colorsToSort = new LinkedList<>(Arrays.asList(colors));
        List<double[]> diffColors = new ArrayList<>();
        diffColors.add(colorsToSort.pop());
        while (colorsToSort.size() > 0) {
            int index = -1;
            double maxDistance = -1;
            for (int candidate_index = 0; candidate_index < colorsToSort.size(); candidate_index++) {
                double d = 1000000000;
                for (int i = 0; i < diffColors.size(); i++) {
                    double[] colorA = colorsToSort.get(candidate_index);
                    double[] colorB = diffColors.get(i);
                    double dl = colorA[0] - colorB[0];
                    double da = colorA[1] - colorB[1];
                    double db = colorA[2] - colorB[2];
                    d = Math.min(d, Math.sqrt(Math.pow(dl, 2) + Math.pow(da, 2) + Math.pow(db, 2)));
                }
                if (d > maxDistance) {
                    maxDistance = d;
                    index = candidate_index;
                }
            }
            double[] color = colorsToSort.get(index);
            diffColors.add(color);
            colorsToSort.remove(index);
        }
        double[][] res = new double[diffColors.size()][];
        for (int i = 0; i < diffColors.size(); i++) {
            res[i] = diffColors.get(i);
        }
        return res;
    }

    private static boolean checkColor2(double[] lab, float[] filter) {
        return checkColor2(lab[0], lab[1], lab[2], filter);
    }

    private static boolean checkColor2(double l, double a, double b, float[] filter) {
        int[] rgb = lab2rgb(l, a, b);
        double[] hcl = lab2hcl(l, a, b);
        // Check that a color is valid: it must verify our checkColor condition, but also be in the color space
        return !Double.isNaN(rgb[0]) && rgb[0] >= 0 && rgb[1] >= 0
                && rgb[2] >= 0 && rgb[0] < 256 && rgb[1] < 256 && rgb[2] < 256
                && (filter[0] < filter[1] ? (hcl[0] >= filter[0] && hcl[0] <= filter[1]) : (hcl[0] >= filter[0] || hcl[0] <= filter[1]))
                && hcl[1] >= filter[2] && hcl[1] <= filter[3]
                && hcl[2] >= filter[4] && hcl[2] <= filter[5];
    }

    private static int[] lab2rgb(double l, double a, double b) {
        double[] xyz = lab2xyz(l, a, b);
        return xyz2rgb(xyz[0], xyz[1], xyz[2]);
    }

    private static double[] lab2xyz(double l, double a, double b) {
        double sl = (l + 0.16) / 1.16;
        double[] ill = new double[]{0.96421, 1.00000, 0.82519};
        double y = ill[1] * finv(sl);
        double x = ill[0] * finv(sl + (a / 5.0));
        double z = ill[2] * finv(sl - (b / 2.0));
        return new double[]{x, y, z};
    }

    private static int[] xyz2rgb(double x, double y, double z) {
        double rl = 3.2406 * x - 1.5372 * y - 0.4986 * z;
        double gl = -0.9689 * x + 1.8758 * y + 0.0415 * z;
        double bl = 0.0557 * x - 0.2040 * y + 1.0570 * z;
        boolean clip = Math.min(rl, Math.min(gl, bl)) < -0.001 || Math.max(rl, Math.max(gl, bl)) > 1.001;
        if (clip) {
            rl = rl < 0.0 ? 0.0 : rl > 1.0 ? 1.0 : rl;
            gl = gl < 0.0 ? 0.0 : gl > 1.0 ? 1.0 : gl;
            bl = bl < 0.0 ? 0.0 : bl > 1.0 ? 1.0 : bl;
        }
        int r = (int) Math.round(255.0 * correct1(rl));
        int g = (int) Math.round(255.0 * correct1(gl));
        int b = (int) Math.round(255.0 * correct1(bl));
        return new int[]{r, g, b};
    }

    private static double[] rgb2lab(int r, int g, int b) {
        double[] xyz = rgb2xyz(r, g, b);
        return xyz2lab(xyz[0], xyz[1], xyz[2]);
    }

    private static double[] rgb2xyz(int r, int g, int b) {
        double rl = correct2(r / 255.0);
        double gl = correct2(g / 255.0);
        double bl = correct2(b / 255.0);
        double x = 0.4124 * rl + 0.3576 * gl + 0.1805 * bl;
        double y = 0.2126 * rl + 0.7152 * gl + 0.0722 * bl;
        double z = 0.0193 * rl + 0.1192 * gl + 0.9505 * bl;
        return new double[]{x, y, z};
    }

    private static double[] xyz2lab(double x, double y, double z) {
        double[] ill = new double[]{0.96421, 1.00000, 0.82519};
        double l = 1.16 * flab(y / ill[1]) - 0.16;
        double a = 5 * (flab(x / ill[0]) - flab(y / ill[1]));
        double b = 2 * (flab(y / ill[1]) - flab(z / ill[2]));
        return new double[]{l, a, b};
    }

    private static double[] lab2hcl(double l, double a, double b) {
        l = (l - 0.09) / 0.61;
        double r = Math.sqrt(a * a + b * b);
        double s = r / (l * 0.311 + 0.125);
        double TAU = 6.283185307179586476925287;
        double angle = Math.atan2(a, b);
        double c = (TAU / 6.0 - angle) / TAU;
        c *= 360;
        if (c < 0) {
            c += 360;
        }
        return new double[]{c, s, l};
    }

    private static double finv(double t) {
        if (t > (6.0 / 29.0)) {
            return t * t * t;
        } else {
            return 3 * (6.0 / 29.0) * (6.0 / 29.0) * (t - 4.0 / 29.0);
        }
    }

    private static double flab(double t) {
        if (t > Math.pow(6.0 / 29.0, 3)) {
            return Math.pow(t, 1.0 / 3.0);
        } else {
            return (1.0 / 3.0) * (29.0 / 6.0) * (29.0 / 6.0) * t + 4.0 / 29.0;
        }
    }

    private static double correct1(double cl) {
        double a = 0.055;
        if (cl <= 0.0031308) {
            return 12.92 * cl;
        } else {
            return (1 + a) * Math.pow(cl, 1.0 / 2.4) - a;
        }
    }

    private static double correct2(double c) {
        double a = 0.055;
        if (c <= 0.04045) {
            return c / 12.92;
        } else {
            return Math.pow((c + a) / (1.0 + a), 2.4);
        }
    }
}
