/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gmail.com>
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
package org.gephi.layout;

import org.gephi.layout.force.ForceVector;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class ForceLayoutData extends ForceVector {

    public float energy0;
    public float step;
    public int progress;

    public ForceLayoutData() {
        progress = 0;
        step = 0;
        energy0 = Float.POSITIVE_INFINITY;
    }
}
