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

package gephi.visualization.opengl.octree;

import gephi.data.network.avl.simple.SimpleAVLTree;
import java.util.Set;

/**
 *
 * @author Mathieu
 */
public class Octree
{
    //Static
    public static byte CLASS_0 = 0;
	public static byte CLASS_1 = 1;
	public static byte CLASS_2 = 2;
	public static byte CLASS_3 = 3;

    //Attributes
    private int objectsID;
    private int maxDepth;
    private int classesCount;

    //Octant
	private Octant root;
    private SimpleAVLTree leaves;

    public Octree()
    {
        leaves = new SimpleAVLTree();
    }

    public void addLeaf(Octant leaf)
    {
        leaves.add(leaf);
    }

    public void removeLeaf(Octant leaf)
    {
        leaves.add(leaf);
    }

    public void addObject()
    {

    }

    public void removeObject()
    {

    }

    public int getClassesCount() {
        return classesCount;
    }
    
    public int getMaxDepth()
    {
        return maxDepth;
    }

    public int getNextObjectID()
    {
        return objectsID++;
    }
}
