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
package org.gephi.visualization.apiimpl;

import java.awt.Component;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import org.gephi.lib.gleem.linalg.Vec3f;

/**
 *
 * @author Mathieu Bastian
 */
public interface GraphDrawable {

    public Component getGraphComponent();

    public int getViewportHeight();

    public int getViewportWidth();

    public float[] getCameraTarget();

    public float[] getCameraLocation();

    public void setCameraLocation(float[] cameraLocation);

    public void setCameraTarget(float[] cameraTarget);

    public Vec3f getCameraVector();

    public double getDraggingMarkerX();

    public double getDraggingMarkerY();

    public DoubleBuffer getProjectionMatrix();

    public IntBuffer getViewport();
}
