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
package org.gephi.visualization.opengl;

import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import org.gephi.ui.utils.PrefsUtils;
import org.openide.util.NbPreferences;

public class Lighting {

    //Const preferences
    public static final String AMBIANT_ENABLED = "Lighting.ambiant.enabled";
    public static final String AMBIANT_AMBIANT = "Lighting.ambiant.ambiant";
    public static final String AMBIANT_SPECULAR = "Lighting.ambiant.specular";
    public static final String AMBIANT_DIFFUSE = "Lighting.ambiant.diffuse";
    public static final String LIGHT1_ENABLED = "Lighting.light1.enabled";
    public static final String LIGHT1_AMBIANT = "Lighting.light1.ambiant";
    public static final String LIGHT1_SPECULAR = "Lighting.light1.specular";
    public static final String LIGHT1_DIFFUSE = "Lighting.light1.diffuse";
    public static final String LIGHT1_POSITION = "Lighting.light1.position";
    public static final String LIGHT2_ENABLED = "Lighting.light2.enabled";
    public static final String LIGHT2_AMBIANT = "Lighting.light2.ambiant";
    public static final String LIGHT2_SPECULAR = "Lighting.light2.specular";
    public static final String LIGHT2_DIFFUSE = "Lighting.light2.diffuse";
    public static final String LIGHT2_POSITION = "Lighting.light2.position";
    public static final String LIGHT3_ENABLED = "Lighting.light3.enabled";
    public static final String LIGHT3_AMBIANT = "Lighting.light3.ambiant";
    public static final String LIGHT3_SPECULAR = "Lighting.light3.specular";
    public static final String LIGHT3_DIFFUSE = "Lighting.light3.diffuse";
    public static final String LIGHT3_POSITION = "Lighting.light3.position";

    //Data
    private List<Light> lights;

    public Lighting() {
        createLights();
    }

    private void createLights() {
        lights = new ArrayList<Light>();

        Light ambiant = new Light(GL.GL_LIGHT0);
        ambiant.setEnabled(NbPreferences.forModule(Lighting.class).getBoolean(AMBIANT_ENABLED, true));
        ambiant.setAmbiant(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(AMBIANT_AMBIANT, "0.30, 0.33, 0.33, 1.0")));
        ambiant.setDiffuse(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(AMBIANT_DIFFUSE, "0.15, 0.10, 0.39, 1.0")));
        ambiant.setSpecular(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(AMBIANT_SPECULAR, "0, 0, 0, 1")));

        Light light1 = new Light(GL.GL_LIGHT1);
        light1.setEnabled(NbPreferences.forModule(Lighting.class).getBoolean(LIGHT1_ENABLED, true));
        light1.setAmbiant(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT1_AMBIANT, "0.0, 0.0, 0.0, 1.0")));
        light1.setSpecular(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT1_SPECULAR, "0.91, 0.31, 0.31, 1.0")));
        light1.setDiffuse(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT1_DIFFUSE, "0.61, 0.28, 0.20, 1.0")));
        light1.setDirection(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT1_POSITION, "-1.0, -1.2, -0.5, 0.0")));

        Light light2 = new Light(GL.GL_LIGHT2);
        light2.setEnabled(NbPreferences.forModule(Lighting.class).getBoolean(LIGHT2_ENABLED, true));
        light2.setAmbiant(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT2_AMBIANT, "0.0, 0.0, 0.0, 1.0")));
        light2.setSpecular(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT2_SPECULAR, "0.96, 0.89, 0.22, 1.0")));
        light2.setDiffuse(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT2_DIFFUSE, "0.40, 0.39, 0.18, 1.0")));
        light2.setDirection(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT2_POSITION, "2.0, 0.0, 1.0, 0.0")));

        Light light3 = new Light(GL.GL_LIGHT3);
        light3.setEnabled(NbPreferences.forModule(Lighting.class).getBoolean(LIGHT3_ENABLED, true));
        light3.setAmbiant(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT3_AMBIANT, "0.0, 0.0, 0.0, 1.0")));
        light3.setSpecular(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT3_SPECULAR, "0.37, 0.37, 0.94, 1.0")));
        light3.setDiffuse(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT3_DIFFUSE, "0.31, 0.44, 0.54, 1.0")));
        light3.setDirection(PrefsUtils.stringToFloatArray(NbPreferences.forModule(Lighting.class).get(LIGHT3_POSITION, "0.0, 2.0, 0.0, 0.0f")));

        lights.add(ambiant);
        lights.add(light1);
        lights.add(light2);
        lights.add(light3);
    }

    public List<Light> getLights() {
        return lights;
    }

    public void glInit(GL gl) {
        for (int i = 0; i < lights.size(); i++) {
            Light l = lights.get(i);
            l.glInit(gl);
        }
    }

    public static class Light {

        private int id;
        private float[] direction;
        private float[] ambiant;
        private float[] specular;
        private float[] diffuse;
        private boolean enabled;

        public Light(int id) {
            this.id = id;
        }

        public void glInit(GL gl) {
            if (enabled) {
                gl.glEnable(id);
                if (ambiant != null) {
                    gl.glLightfv(id, GL.GL_AMBIENT, ambiant, 0);   // color of the reflected light
                }
                if (diffuse != null) {
                    gl.glLightfv(id, GL.GL_DIFFUSE, diffuse, 0);   // color of the direct illumination
                }
                if (specular != null) {
                    gl.glLightfv(id, GL.GL_SPECULAR, specular, 0);  // color of the highlight
                }
                if (direction != null) {
                    gl.glLightfv(id, GL.GL_POSITION, direction, 0);
                }
            } else {
                gl.glDisable(id);
            }
        }

        public float[] getAmbiant() {
            return ambiant;
        }

        public void setAmbiant(float[] ambiant) {
            this.ambiant = ambiant;
        }

        public float[] getDiffuse() {
            return diffuse;
        }

        public void setDiffuse(float[] diffuse) {
            this.diffuse = diffuse;
        }

        public float[] getDirection() {
            return direction;
        }

        public void setDirection(float[] direction) {
            this.direction = direction;
        }

        public float[] getSpecular() {
            return specular;
        }

        public void setSpecular(float[] specular) {
            this.specular = specular;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

