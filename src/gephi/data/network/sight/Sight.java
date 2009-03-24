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

package gephi.data.network.sight;

import gephi.data.network.avl.simple.AVLItem;
import gephi.data.network.avl.simple.SimpleAVLTree;
import java.util.Iterator;

/**
 *
 * @author Mathieu
 */
public class Sight implements AVLItem
{
    private final int number;
    private SimpleAVLTree children;
    private SightCache sightCache;

    public Sight(int number)
    {
        this.number=number;
        this.sightCache = new SightCache(this);
    }

    public int getNumber() {
        return number;
    }

    public void addChildren(Sight child)
    {
        if(children==null)
            children = new SimpleAVLTree();
        children.add(child);
    }

    public void removeChildren(Sight child)
    {
        if(children!=null)
            children.remove(child);
    }

    public boolean hasChildren()
    {
        return children.getCount()>0;
    }

    public Iterator<AVLItem> getChildrenIterator()
    {
        return children.iterator();
    }

    public SightCache getSightCache() {
        return sightCache;
    }

    public void setSightCache(SightCache sightCache) {
        this.sightCache = sightCache;
    }
}
