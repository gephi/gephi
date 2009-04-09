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
package org.gephi.visualization.api.initializer;

import org.gephi.visualization.api.initializer.Object3dInitializer;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.api.Object3dImpl;

/**
 * Interface for classes which wants to create graphic node {@link Object3d}. Because node objects may be too
 * various to only propose a Node object interface it is possible to create different initializers.
 * <p>
 * The initializer is responsible of the object creation and maintenance. Different functionalities
 * like LOD or texturing can be done in initializers, with the cooperation of the engine.
 * <p>
 * This interface is also designed in order to be implemented in plugins.
 *
 * @author Mathieu Bastian
 * @see AbstractEngine
 */
public interface NodeInitializer extends Object3dInitializer {
}
