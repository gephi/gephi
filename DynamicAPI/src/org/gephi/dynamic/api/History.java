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

package org.gephi.dynamic.api;

import org.gephi.dynamic.*;
import java.util.NavigableSet;

/**
 * Warning: this class does not permit the use of null elements,
 * because null arguments and return values cannot be reliably distinguished
 * from the absence of elements.
 * 
 * @author Julian Bilcke
 */
public interface History {

    public State getLastStateExistingInInterval(State from, State to);
    public void addState(State state);
    public State getLowerState(State state);
    public int getSize();

}
