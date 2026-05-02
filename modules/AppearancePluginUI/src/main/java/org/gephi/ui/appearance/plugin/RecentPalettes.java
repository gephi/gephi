/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.ui.appearance.plugin;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import org.gephi.appearance.plugin.RankingElementColorTransformer.LinearGradient;
import org.gephi.utils.ColorUtils;
import org.openide.util.NbPreferences;

/**
 * @author Mathieu Bastian
 */
public class RecentPalettes {

    public static final String COLORS = "PaletteColors";
    public static final String POSITIONS = "PalettePositions";
    private static final int MAX_SIZE = 14;
    protected static final String NODE_NAME = "recentrankingpalettes";
    private final LinkedList<LinearGradient> gradients;

    public RecentPalettes() {
        gradients = new LinkedList<>();
        retrieve();
    }

    public void add(LinearGradient gradient) {
        if (!gradients.isEmpty() && gradients.getFirst().equals(gradient)) {
            return;
        }
        //Remove the old
        gradients.remove(gradient);

        // add to the top
        gradients.push(new LinearGradient(
                Arrays.copyOf(gradient.getColors(), gradient.getColors().length),
                Arrays.copyOf(gradient.getPositions(), gradient.getPositions().length)));
        while (gradients.size() > MAX_SIZE) {
            gradients.removeLast();
        }

        store();
    }

    public LinearGradient[] getPalettes() {
        return gradients.toArray(new LinearGradient[0]);
    }

    private void store() {
        Preferences prefs = getPreferences();

        int i = 0;
        for (LinearGradient gradient : gradients) {
            prefs.putByteArray(COLORS + i, ColorUtils.serializeColors(gradient.getColors()));
            prefs.putByteArray(POSITIONS + i, ColorUtils.serializeFloats(gradient.getPositions()));
            i++;
        }
        // Remove stale entries beyond the current list size
        for (; i < MAX_SIZE; i++) {
            prefs.remove(COLORS + i);
            prefs.remove(POSITIONS + i);
        }
    }

    private void retrieve() {
        gradients.clear();
        Preferences prefs = getPreferences();

        for (int i = 0; i < MAX_SIZE; i++) {
            byte[] cols = prefs.getByteArray(COLORS + i, null);
            byte[] poss = prefs.getByteArray(POSITIONS + i, null);
            if (cols != null && poss != null) {
                Color[] colors = ColorUtils.deserializeColors(cols);
                float[] positions = ColorUtils.deserializeFloats(poss);
                if (colors != null && positions != null) {
                    gradients.addLast(new LinearGradient(colors, positions));
                }
            } else {
                break;
            }
        }
    }

    /**
     * Return the backing store Preferences
     *
     * @return Preferences
     */
    protected final Preferences getPreferences() {
        return NbPreferences.forModule(this.getClass()).node("options").node(NODE_NAME);
    }
}
