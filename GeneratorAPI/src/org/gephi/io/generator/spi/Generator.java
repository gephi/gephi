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
package org.gephi.io.generator.spi;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.utils.longtask.LongTask;

/**
 * Define a generator, that is generating graph structure from a bunch of parameters.
 * <p>
 * Extends <code>LongTask</code> to support tasks progress and cancellation.
 * @author Mathieu Bastian
 */
public interface Generator extends LongTask {

    /**
     * Main method that generates the graph and put it in the container.
     * <p>
     * From the container content, the controller makes verification and append
     * the graph to <code>GraphAPI</code>.
     * @param container the container the graph is to be pushed
     */
    public void generate(ContainerLoader container);

    /**
     * Returns the generator display name.
     * @return          returns the generator name
     */
    public String getName();

    /**
     * Returns the UI that belongs to this generator, or <code>null</code> if UI
     * is not needed.
     * @return          the UI thet belongs to this generator, or <code>null</code>
     */
    public GeneratorUI getUI();
}
