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

package gephi.visualization.opengl.compatibility;

import gephi.visualization.NodeInitializer;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.swing.GraphDrawable;
import gephi.visualization.swing.GraphIO;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public class CompatibilityEngine extends AbstractEngine {

    public CompatibilityEngine(GraphDrawable graphDrawable, GraphIO graphIO)
    {
        super(graphDrawable,graphIO);
    }

    @Override
    public void afterDisplay(GL gl, GLU glu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeDisplay(GL gl, GLU glu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cameraHasBeenMoved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void display(GL gl, GLU glu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NodeInitializer getCurrentNodeInitializer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends NodeInitializer> getNodeInitializers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initEngine(GL gl, GLU glu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseClick() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseDrag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refreshGraphLimits() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startDrag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopDrag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
