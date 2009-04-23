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
package org.gephi.visualization.selection;

import org.gephi.visualization.api.selection.SelectionArea;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.gleem.linalg.Vecf;

/**
 *
 * @author Mathieu Bastian
 */
public class Point implements SelectionArea {

    private static float[] rectangle = {1, 1};

    @Override
    public float[] getSelectionAreaRectancle() {
        return rectangle;
    }

    @Override
    public boolean select(Renderable object) {
        return true;
    }

    @Override
    public boolean unselect(Renderable object) {
        return true;
    }

    @Override
    public boolean mouseTest(Vecf distanceFromMouse, Object3dImpl object) {
        return object.selectionTest(distanceFromMouse, 0);
    }

    public void drawArea(GL gl, GLU glu) {
        // TODO Auto-generated method stub
    }
}
