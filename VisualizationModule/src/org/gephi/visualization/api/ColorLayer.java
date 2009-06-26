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
package org.gephi.visualization.api;

import org.gephi.graph.api.Renderable;

/**
 *
 * @author Mathieu Bastian
 */
public class ColorLayer {

    private final float r;
    private final float g;
    private final float b;

    private ColorLayer(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static void layerColor(ModelImpl model, float r, float g, float b) {
        Renderable obj = model.getObj();
        ColorLayer cl = new ColorLayer(obj.r(), obj.g(), obj.b());
        model.setColorLayer(cl);
        obj.setR(r);
        obj.setG(g);
        obj.setB(b);
    }

    public static void restituteColor(ModelImpl model) {
        ColorLayer cl = model.getColorLayer();
        if (cl != null) {
            model.getObj().setR(cl.r);
            model.getObj().setG(cl.g);
            model.getObj().setB(cl.b);
        }
    }
}
