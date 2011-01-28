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
package org.gephi.visualization.api.initializer;

import org.gephi.graph.api.Model;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 * Interface for classes which wants to create graphic node {@link Model}. Because node models may be too
 * various to only propose a Node model interface it is possible to create different modelers.
 * <p>
 * The modeler is responsible of the model creation and maintenance. Different functionalities
 * like LOD or texturing can be done in modelers, with the cooperation of the engine.
 *
 * @author Mathieu Bastian
 * @see AbstractEngine
 */
public interface NodeModeler extends Modeler {

    public boolean is3d();
}
