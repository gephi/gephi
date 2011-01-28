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
package org.gephi.io.processor.spi;

import org.gephi.io.importer.api.Container;

/**
 * The scaler is part of the processing job and is responsible of transforming the
 * position and size of imported nodes in order to fit in the system's scale.
 *
 * @author Mathieu Bastian
 * @see Processor
 */
public interface Scaler {

    /**
     * Scale <code>container</code> content to the system scale. Adapt and recenter
     * the scale of nodes positions and sizes.
     * @param container the container that is to be scaled to the right scale
     */
    public void doScale(Container container);
}
