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
import org.gephi.graph.api.Sight;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.datastructure.avl.simple.SimpleAVLTree;

/**
 *
 * @author Mathieu Bastian
 */
public class SightImpl implements Sight, AVLItem {

    private final int number;
    private String name;
    private SimpleAVLTree children;
    private SightCache sightCache;

    public SightImpl(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void addChildren(SightImpl child) {
        if (children == null) {
            children = new SimpleAVLTree();
        }
        children.add(child);
    }

    public void removeChildren(SightImpl child) {
        if (children != null) {
            children.remove(child);
        }
    }

    public boolean hasChildren() {
        if(children!=null && children.getCount() > 0)
            return true;
        return false;
    }

    public Iterator<AVLItem> getChildrenIterator() {
        return children.iterator();
    }

    public SightCache getSightCache() {
        return sightCache;
    }

    public void setSightCache(SightCache sightCache) {
        this.sightCache = sightCache;
    }

    public Sight[] getChildren() {
        Sight[] sightArray = new Sight[children.getCount()];
        Iterator<AVLItem> itr = children.iterator();
        for(int i=0;itr.hasNext();i++)
        {
            Sight sight = (Sight)itr.next();
            sightArray[i] = sight;
        }
        return sightArray;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
