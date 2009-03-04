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

import com.sun.opengl.util.BufferUtil;
import gephi.data.network.avl.param.ParamAVLTree;
import gephi.data.network.avl.simple.SimpleAVLTree;
import gephi.visualization.opengl.Object3d;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.media.opengl.GL;

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
    private int objectsIDs;
    private int maxDepth;
    private int classesCount;

    //Octant
	private Octant root;
    private ParamAVLTree<Octant> leaves;

    //States
    private List<Octant> visibleLeaves;

    public Octree()
    {
        leaves = new ParamAVLTree<Octant>();
        visibleLeaves = new ArrayList<Octant>();
    }

    public void addObject(int classID, Object3d obj)
    {
        root.addObject(classID, obj);
    }

    public void removeObject(int classID, Object3d obj)
    {
        root.removeObject(classID, obj);
    }

    public void updateVisibleOctant(GL gl)
	{


		//Switch to OpenGL select mode
		int capacity = 1*4*leaves.getCount();      //Each object take in maximium : 4 * name stack depth
		IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);
		gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
		gl.glRenderMode(GL.GL_SELECT);
		gl.glInitNames();
		gl.glPushName(0);

		//Draw the nodes cube in the select buffer
		for(Octant n : leaves)
		{
			gl.glLoadName(n.getNumber());
			n.displayOctreeNode(gl);
		}
		int nbRecords = gl.glRenderMode(GL.GL_RENDER);

		visibleLeaves.clear();

		//Get the hits and add the nodes' objects to the array
		for(int i=0; i< nbRecords; i++)
		{
			int hit = hitsBuffer.get(i*4+3) - 1; 		//-1 Because of the glPushName(0)

			Octant nodeHit = leaves.getItem(hit);
			visibleLeaves.add(nodeHit);
		}

	}

    public void displayOctree(GL gl)
	{
		gl.glColor3i(0, 0, 0);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		root.displayOctreeNode(gl);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

	}

    void addLeaf(Octant leaf)
    {
        leaves.add(leaf);
    }

    void removeLeaf(Octant leaf)
    {
        leaves.add(leaf);
    }

    int getClassesCount() {
        return classesCount;
    }
    
    int getMaxDepth()
    {
        return maxDepth;
    }

    int getNextObjectID()
    {
        return objectsIDs++;
    }
}
