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
package org.gephi.data.network.sight;

import java.util.Iterator;
import org.gephi.data.network.Dhns;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.datastructure.avl.simple.SimpleAVLTree;

/**
 *
 * @author Mathieu Bastian
 */
public class SightManager {

    private int sightCounter = 1;
    private SimpleAVLTree sightTree = new SimpleAVLTree();
    private Sight mainSight;
    private Dhns dhns;

    public SightManager(Dhns dhns)
    {
        this.dhns = dhns;
    }

    public Sight getMainSight()
    {
        if(mainSight==null)
            mainSight = createSight();
        return mainSight;
    }

    public Sight createSight() {
        Sight sight = new Sight(getSightCounter());
        sight.setSightCache(new SightCache(dhns, sight));
        sightCounter++;
        sightTree.add(sight);
        return sight;
    }

    public void removeSight(Sight sight) {
        sightTree.remove(sight);
    }

    public void removeSight(int number) {
        sightTree.remove(number);
    }

    public Sight getSight(int number) {
        return (Sight) sightTree.get(number);
    }

    public Iterator<AVLItem> getSightIterator() {
        return sightTree.iterator();
    }

    public int getSightCounter() {
        return sightCounter;
    }

    public void setSightCounter(int sightCounter) {
        this.sightCounter = sightCounter;
    }
}