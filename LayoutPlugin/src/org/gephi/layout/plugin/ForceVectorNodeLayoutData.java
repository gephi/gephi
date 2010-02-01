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
package org.gephi.layout.plugin;

import org.gephi.graph.spi.LayoutData;


/**
 *
 * @author Mathieu Bastian
 */
public class ForceVectorNodeLayoutData implements LayoutData {

    //Data
    public float dx = 0;
    public float dy = 0;
    public float old_dx = 0;
    public float old_dy = 0;
    public float freeze = 0f;
}
