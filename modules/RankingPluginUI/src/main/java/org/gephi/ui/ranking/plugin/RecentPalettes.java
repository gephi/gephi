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
package org.gephi.ui.ranking.plugin;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer.LinearGradient;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class RecentPalettes {

    protected static String DEFAULT_NODE_NAME = "prefs";
    public static final String COLORS = "PaletteColors";
    public static final String POSITIONS = "PalettePositions";
    private List<LinearGradient> gradients;
    private int maxSize;
    protected String nodeName = null;

    public RecentPalettes() {
        nodeName = "recentpalettes";
        maxSize = 14;
        gradients = new ArrayList<LinearGradient>(maxSize);
        retrieve();
    }

    public void add(LinearGradient gradient) {
        //Remove the old
        gradients.remove(gradient);

        // add to the top
        gradients.add(0, gradient);
        while (gradients.size() > maxSize) {
            gradients.remove(gradients.size() - 1);
        }

        store();
    }

    public LinearGradient[] getPalettes() {
        return gradients.toArray(new LinearGradient[0]);
    }

    protected void store() {
        Preferences prefs = getPreferences();

        // clear the backing store
        try {
            prefs.clear();
        } catch (BackingStoreException ex) {
        }

        for (int i = 0; i < gradients.size(); i++) {
            LinearGradient gradient = gradients.get(i);
            try {
                prefs.putByteArray(COLORS + i, serializeColors(gradient.getColors()));
                prefs.putByteArray(POSITIONS + i, serializePositions(gradient.getPositions()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void retrieve() {
        gradients.clear();
        Preferences prefs = getPreferences();

        for (int i = 0; i < maxSize; i++) {
            byte[] cols = prefs.getByteArray(COLORS + i, null);
            byte[] poss = prefs.getByteArray(POSITIONS + i, null);
            if (cols != null && poss != null) {
                try {
                    Color[] colors = deserializeColors(cols);
                    float[] posisitons = deserializePositions(poss);
                    LinearGradient linearGradient = new LinearGradient(colors, posisitons);
                    gradients.add(linearGradient);
                } catch (Exception e) {
                    e.printStackTrace();
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
        String name = DEFAULT_NODE_NAME;
        if (nodeName != null) {
            name = nodeName;
        }

        Preferences prefs = NbPreferences.forModule(this.getClass()).node("options").node(name);

        return prefs;
    }

    private byte[] serializePositions(float[] positions) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(positions);
        out.close();
        return bos.toByteArray();
    }

    private float[] deserializePositions(byte[] positions) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(positions);
        ObjectInputStream in = new ObjectInputStream(bis);
        float[] array = (float[]) in.readObject();
        in.close();
        return array;
    }

    private byte[] serializeColors(Color[] colors) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(colors);
        out.close();
        return bos.toByteArray();
    }

    private Color[] deserializeColors(byte[] colors) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(colors);
        ObjectInputStream in = new ObjectInputStream(bis);
        Color[] array = (Color[]) in.readObject();
        in.close();
        return array;
    }
}
