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

package org.gephi.tools.spi;

/**
 * Tool mouse click listener. Listen to mouse click on visualization window.
 * <p>
 * A tool which declares this listener is notified when user click on the
 * visualizaion window.
 *
 * @author Mathieu Bastian
 * @see Tool
 */
public interface MouseClickEventListener extends ToolEventListener {

    /**
     * Notify a mouse click on the visualization window.
     * @param positionViewport Position in the 2D coordinate system, (0,0) is located
     * top-left.
     * @param position3d Position in the 3D coordinate system, (0,0) is located at the
     * center.
     */
    public void mouseClick(int[] positionViewport, float[] position3d);
}
