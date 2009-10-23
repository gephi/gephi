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
package org.gephi.filters;

import org.gephi.filters.api.Filter;
import org.gephi.graph.api.Predicate;

/**
 *
 * @author Mathieu Bastian
 */
public class DegreeRangeFilter implements Filter {

    private int minimum;
    private int maximum;
    private int lowerBound;
    private int upperBound;

    public Predicate getPredicate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getUpperBound() {
        return upperBound;
    }
}
