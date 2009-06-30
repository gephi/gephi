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

package org.gephi.dynamic;

import org.gephi.dynamic.api.State;
import org.gephi.dynamic.api.History;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Scalable concurrent history
 *
 * @author Julian Bilcke
 */
public class TreeHistory implements History {
    
    private NavigableSet<State> set;
    private int size;

    public TreeHistory() {
        set = new ConcurrentSkipListSet<State>(new StateComparator());
        size = 0;
    }

    public State getLastStateExistingInInterval(State from, State to) {
         return set.subSet(from, to).last();
    }

    public State getLowerState(State to) {
        return set.lower(to);
    }
    public void addState(State state) {
        set.add(state);
        size++;
    }

    public int getSize() {
        // implemented as a variable since getting the size is slow with
        // ConcurrentSkipListSets
        return size;
    }


    private class StateComparator implements Comparator<State> {
        public int compare(State s1, State s2) {
            return s1.getComparable().compareTo(s2);
        }
    }

}
