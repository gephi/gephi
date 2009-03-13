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
import gephi.data.network.avl.ResetableIterator;
import gephi.data.network.avl.param.AVLItemAccessor;
import gephi.data.network.avl.param.ParamAVLTree;
import gephi.data.network.avl.simple.SimpleAVLTree;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.swing.GraphDrawable;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public class Octree
{

    //Architecture
    private GraphDrawable drawable;
    private AbstractEngine engine;

    //Attributes
    private int objectsIDs;
    private int maxDepth;
    private int classesCount;

    //Octant
	private Octant root;
    private ParamAVLTree<Octant> leaves;

    //Iterator

    //States
    protected List<Octant> visibleLeaves;
    protected List<Octant> selectedLeaves;

    public Octree(AbstractEngine engine, int maxDepth, int size, int nbClasses)
    {
        this.engine = engine;
        this.drawable = engine.getGraphDrawable();
        this.maxDepth = maxDepth;
        this.classesCount = nbClasses;

        leaves = new ParamAVLTree<Octant>(new AVLItemAccessor<Octant>()
        {
            public int getNumber(Octant item) {
                return item.getNumber();
            }
        });
        visibleLeaves = new ArrayList<Octant>();
        selectedLeaves = new ArrayList<Octant>();

        float dis = size/(float)Math.pow(2, maxDepth+1);
		root = new Octant(this, 0,dis, dis, dis, size);
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
			int hit = hitsBuffer.get(i*4+3); 		//-1 Because of the glPushName(0)

			Octant nodeHit = leaves.getItem(hit);
			visibleLeaves.add(nodeHit);
		}

	}

    public void updateSelectedOctant(GL gl, GLU glu, float[] mousePosition, float[] pickRectangle)
    {
        //Start Picking mode
		int capacity = 1*4*visibleLeaves.size();      //Each object take in maximium : 4 * name stack depth
		IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);

		gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
		gl.glRenderMode(GL.GL_SELECT);

		gl.glInitNames();
		gl.glPushName(0);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		glu.gluPickMatrix(mousePosition[0],mousePosition[1],pickRectangle[0],pickRectangle[1],drawable.viewport);
		gl.glMultMatrixd(drawable.projMatrix);

		gl.glMatrixMode(GL.GL_MODELVIEW);

		//Draw the nodes' cube int the select buffer
		int hitName=1;
		for(Octant node : visibleLeaves)
		{
			gl.glLoadName(hitName);
			node.displayOctreeNode(gl);
			hitName++;
		}

		//Restoring the original projection matrix
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glFlush();

		//Returning to normal rendering mode
		int nbRecords = gl.glRenderMode(GL.GL_RENDER);

		//Clean previous selection
		selectedLeaves.clear();

		//Get the hits and put the node under selection in the selectionArray
		for(int i=0; i< nbRecords; i++)
		{
			int hit = hitsBuffer.get(i*4+3) - 1; 		//-1 Because of the glPushName(0)

			Octant nodeHit = visibleLeaves.get(hit);
            selectedLeaves.add(nodeHit);
		}
    }

    public Iterator<Object3d> getObjectIterator(int classID)
	{
        return new OctreeIterator(visibleLeaves, classID);
	}

    public OctreeIterator getSelectedObjectIterator(int classID) {
        return new OctreeIterator(selectedLeaves, classID);
    }
    

    public void displayOctree(GL gl)
	{
		//gl.glColor3f(1, 0.5f, 0.5f);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		for(Octant o : visibleLeaves)
        {
            o.displayOctreeNode(gl);
        }
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

    private static class OctreeIterator implements Iterator<Object3d>, ResetableIterator
    {
        private int i;
        private int classID;
        private List<Octant> octants;
        private Iterator<Object3d> currentIterator;

        public OctreeIterator(List<Octant> octants, int classID)
        {
            this.octants = octants;
            this.classID = classID;
        }

        public void reset()
        {
            currentIterator=null;
            i=0;
        }

        public boolean hasNext() {
           if(currentIterator==null || !currentIterator.hasNext())
           {
               if(i<octants.size())
               {
                    currentIterator = octants.get(i).iterator(classID);
                    i++;
                    if(currentIterator.hasNext())
                        return true;
               }
               return false;
           }
           return true;
        }

        public Object3d next() {
            return currentIterator.next();
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
