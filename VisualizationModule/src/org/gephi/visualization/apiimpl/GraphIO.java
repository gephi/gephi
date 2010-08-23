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
package org.gephi.visualization.apiimpl;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 *
 * @author Mathieu Bastian
 */
public interface GraphIO extends MouseListener, MouseWheelListener, MouseMotionListener, KeyListener {

    public float[] getMousePosition();

    public float[] getMousePosition3d();

    public float[] getMouseDrag();

    public float[] getMouseDrag3d();

    public void startMouseListening();

    public void stopMouseListening();

    public void trigger();

    public void setCameraDistance(float distance);

    public void centerOnZero();

    public void centerOnGraph();

    public void centerOnCoordinate(float x, float y, float z);
}
