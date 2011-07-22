/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.ranking.plugin;

import java.awt.Color;
import java.beans.XMLEncoder;
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

    /** Return the backing store Preferences
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
