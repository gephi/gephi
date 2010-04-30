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
package org.gephi.ranking.api;

/**
 * Interface that defines the single {@link #interpolate(float)} method. This interface is implemented by built-in interpolators.
 * @author Mathieu Bastian
 */
public interface Interpolator {

    /**
     * This function takes an input value between 0 and 1 and returns another value, also between 0 and 1.
     * @param x a value between 0 and 1
     * @return a value between 0 and 1. Values outside of this boundary may be clamped to the interval [0,1] and cause undefined results.
     */
    public float interpolate(float x);
}
