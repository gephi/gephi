/*
 Copyright 2008-2025 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2025 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2025 Gephi Consortium.
 */

package org.gephi.desktop.appearance.options;

import java.awt.Color;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Interpolator;
import org.openide.util.NbPreferences;

/**
 * Appearance module preferences, storing default values for transformers.
 */
public final class AppearancePreferences {

    // Size keys
    public static final String NODE_RANKING_SIZE_MIN = "Appearance.nodeRankingSizeMin";
    public static final String NODE_RANKING_SIZE_MAX = "Appearance.nodeRankingSizeMax";
    public static final String LABEL_RANKING_SIZE_MIN = "Appearance.labelRankingSizeMin";
    public static final String LABEL_RANKING_SIZE_MAX = "Appearance.labelRankingSizeMax";

    // Color keys — values encoded as comma-separated hex RGB / float strings
    public static final String ELEMENT_RANKING_COLORS = "Appearance.elementRankingColors";
    public static final String ELEMENT_RANKING_COLOR_POSITIONS = "Appearance.elementRankingColorPositions";
    public static final String LABEL_RANKING_COLORS = "Appearance.labelRankingColors";
    public static final String LABEL_RANKING_COLOR_POSITIONS = "Appearance.labelRankingColorPositions";

    // Size defaults
    public static final float DEFAULT_NODE_RANKING_SIZE_MIN = 1f;
    public static final float DEFAULT_NODE_RANKING_SIZE_MAX = 4f;
    public static final float DEFAULT_LABEL_RANKING_SIZE_MIN = 1f;
    public static final float DEFAULT_LABEL_RANKING_SIZE_MAX = 4f;

    // Default appearance settings keys
    public static final String RANKING_LOCAL_SCALE = "Appearance.rankingLocalScale";
    public static final String PARTITION_LOCAL_SCALE = "Appearance.partitionLocalScale";
    public static final String TRANSFORM_NULL_VALUES = "Appearance.transformNullValues";

    // Default appearance settings defaults
    public static final boolean DEFAULT_RANKING_LOCAL_SCALE = false;
    public static final boolean DEFAULT_PARTITION_LOCAL_SCALE = false;
    public static final boolean DEFAULT_TRANSFORM_NULL_VALUES = false;

    // Interpolator key
    public static final String DEFAULT_INTERPOLATOR = "Appearance.defaultInterpolator";

    // Color defaults — match RankingElementColorTransformer's hardcoded initial gradient
    public static final Color[] DEFAULT_ELEMENT_RANKING_COLORS =
        {new Color(0xEDF8FB), new Color(0x66C2A4), new Color(0x006D2C)};
    public static final float[] DEFAULT_COLOR_POSITIONS = {0f, 0.5f, 1f};

    private AppearancePreferences() {
    }

    // ---- Default appearance settings accessors ----

    public static boolean isRankingLocalScale() {
        return NbPreferences.forModule(AppearanceModel.class)
            .getBoolean(RANKING_LOCAL_SCALE, DEFAULT_RANKING_LOCAL_SCALE);
    }

    public static boolean isPartitionLocalScale() {
        return NbPreferences.forModule(AppearanceModel.class)
            .getBoolean(PARTITION_LOCAL_SCALE, DEFAULT_PARTITION_LOCAL_SCALE);
    }

    public static boolean isTransformNullValues() {
        return NbPreferences.forModule(AppearanceModel.class)
            .getBoolean(TRANSFORM_NULL_VALUES, DEFAULT_TRANSFORM_NULL_VALUES);
    }

    // ---- Size accessors ----

    public static float getNodeRankingSizeMin() {
        return NbPreferences.forModule(AppearancePreferences.class)
            .getFloat(NODE_RANKING_SIZE_MIN, DEFAULT_NODE_RANKING_SIZE_MIN);
    }

    public static float getNodeRankingSizeMax() {
        return NbPreferences.forModule(AppearancePreferences.class)
            .getFloat(NODE_RANKING_SIZE_MAX, DEFAULT_NODE_RANKING_SIZE_MAX);
    }

    public static float getLabelRankingSizeMin() {
        return NbPreferences.forModule(AppearancePreferences.class)
            .getFloat(LABEL_RANKING_SIZE_MIN, DEFAULT_LABEL_RANKING_SIZE_MIN);
    }

    public static float getLabelRankingSizeMax() {
        return NbPreferences.forModule(AppearancePreferences.class)
            .getFloat(LABEL_RANKING_SIZE_MAX, DEFAULT_LABEL_RANKING_SIZE_MAX);
    }

    // ---- Color accessors ----

    public static Color[] getElementRankingColors() {
        String s = NbPreferences.forModule(AppearancePreferences.class).get(ELEMENT_RANKING_COLORS, null);
        return decodeColors(s, DEFAULT_ELEMENT_RANKING_COLORS);
    }

    public static float[] getElementRankingColorPositions() {
        String s =
            NbPreferences.forModule(AppearancePreferences.class).get(ELEMENT_RANKING_COLOR_POSITIONS, null);
        return decodePositions(s, DEFAULT_COLOR_POSITIONS);
    }

    public static Color[] getLabelRankingColors() {
        String s = NbPreferences.forModule(AppearancePreferences.class).get(LABEL_RANKING_COLORS, null);
        return decodeColors(s, DEFAULT_ELEMENT_RANKING_COLORS);
    }

    public static float[] getLabelRankingColorPositions() {
        String s =
            NbPreferences.forModule(AppearancePreferences.class).get(LABEL_RANKING_COLOR_POSITIONS, null);
        return decodePositions(s, DEFAULT_COLOR_POSITIONS);
    }

    // ---- Interpolator accessors ----

    public static Interpolator getDefaultInterpolator() {
        String s = NbPreferences.forModule(Interpolator.class).get(DEFAULT_INTERPOLATOR, null);
        return Interpolator.fromString(s);
    }

    // ---- Encoding helpers ----

    public static String encodeColors(Color[] colors) {
        return Arrays.stream(colors)
            .map(c -> String.format("#%06X", c.getRGB() & 0xFFFFFF))
            .collect(Collectors.joining(","));
    }

    public static String encodePositions(float[] positions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(positions[i]);
        }
        return sb.toString();
    }

    private static Color[] decodeColors(String s, Color[] defaultValue) {
        if (s == null || s.isEmpty()) {
            return defaultValue;
        }
        try {
            String[] parts = s.split(",");
            Color[] colors = new Color[parts.length];
            for (int i = 0; i < parts.length; i++) {
                colors[i] = Color.decode(parts[i].trim());
            }
            return colors;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static float[] decodePositions(String s, float[] defaultValue) {
        if (s == null || s.isEmpty()) {
            return defaultValue;
        }
        try {
            String[] parts = s.split(",");
            float[] positions = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                positions[i] = Float.parseFloat(parts[i].trim());
            }
            return positions;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
