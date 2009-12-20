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
package org.gephi.layout.spi;

import org.gephi.graph.api.GraphController;

/**
 * A Layout algorithm should implement the Layout interface to allow the
 * LayoutController to run it properly.
 *
 * Note that a using a LayoutBuilder is the preferred way any Layout implementor
 * should be instanciated. See the LayoutBuilder interface for a more detailed
 * description.
 *
 * @author Mathieu Bastian
 */
public interface Layout {

    /**
     * initAlgo() is called to initialize the algorithm (prepare to run).
     */
    public void initAlgo();

    /**
     * Injects the graphController for the graph this Layout should operate on.
     * @param graphController
     */
    public void setGraphController(GraphController graphController);

    /**
     * Run a step in the algorithm, should be called only if canAlgo() returns
     * true.
     */
    public void goAlgo();

    /**
     * Tests if the algorithm can run.
     * @return
     */
    public boolean canAlgo();

    /**
     * Called when the algorithm is finished (canAlgo() returns false).
     */
    public void endAlgo();

    /**
     * The property sets of the layout.
     * @return
     * @throws NoSuchMethodException 
     */
    public LayoutProperty[] getProperties();

    /**
     * Resets the properties values to the default values.
     */
    public void resetPropertiesValues();

    /**
     * The reference to the LayoutBuilder that instanciated this Layout.
     * @return
     */
    public LayoutBuilder getBuilder();
}
