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

import org.gephi.graph.api.GraphModel;

/**
 * A Layout algorithm should implement the <code>Layout</code> interface to allow the
 * <code>LayoutController</code> to run it properly.
 * <p>
 * See the <code>LayoutBuilder</code> documentation to know how layout should
 * be instanciated.
 * <p>
 * To have fully integrated properties that can be changed in real-time by users,
 * properly define the various <code>LayoutProperty</code> returned by the
 * {@link #getProperties()} method and provide getter and setter for each.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 * @see LayoutBuilder
 */
public interface Layout {

    /**
     * initAlgo() is called to initialize the algorithm (prepare to run).
     */
    public void initAlgo();

    /**
     * Injects the graph model for the graph this Layout should operate on.
     * <p>
     * It's preferable to get <b>visible</b> graph to perform on visualization.
     * @param graphModel    the graph model that the layout is to be working on
     */
    public void setGraphModel(GraphModel graphModel);

    /**
     * Run a step in the algorithm, should be called only if canAlgo() returns
     * true.
     */
    public void goAlgo();

    /**
     * Tests if the algorithm can run, called before each pass.
     * @return              <code>true</code> if the algorithm can run, <code>
     *                      false</code> otherwise
     */
    public boolean canAlgo();

    /**
     * Called when the algorithm is finished (canAlgo() returns false).
     */
    public void endAlgo();

    /**
     * The properties for this layout.
     * @return              the layout properties
     * @throws NoSuchMethodException 
     */
    public LayoutProperty[] getProperties();

    /**
     * Resets the properties values to the default values.
     */
    public void resetPropertiesValues();

    /**
     * The reference to the LayoutBuilder that instanciated this Layout.
     * @return              the reference to the builder that builts this instance
     */
    public LayoutBuilder getBuilder();
}
