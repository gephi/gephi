/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.opengl.text;

import org.gephi.graph.api.Object3d;

/**
 *
 * @author Mathieu Bastian
 */
public class FixedSizeMode implements SizeMode {

    private static float GLOBAL_FACTOR = 65f;
    private static float FACTOR_LIMIT = 0.1f;

    public void setSizeFactor(TextData text) {
        Object3d object3d = text.getObject3d();
        float factor = GLOBAL_FACTOR / object3d.getCameraDistance();
        if (factor < FACTOR_LIMIT) {
            factor = 0f;
        }
        text.setSizeFactor(factor);
    }
}
