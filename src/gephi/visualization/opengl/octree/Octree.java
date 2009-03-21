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
import gephi.data.network.avl.param.ParamAVLIterator;
import gephi.data.network.avl.param.ParamAVLTree;
import gephi.data.network.avl.simple.SimpleAVLTree;
import gephi.visualization.VizArchitecture;
import gephi.visualization.VizController;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.AbstractEngine.Limits;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.swing.GraphDrawable;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public class Octree implements VizArchitecture
{
    //Architecture
    private GraphDrawable drawable;
    private AbstractEngine engine;

    //Attributes
    private int objectsIDs;
    private int maxDepth;
    private int classesCount;
    private int size;

    //Octant
	private Octant root;
    private ParamAVLTree<Octant> leaves;

    //Iterator
    ConcurrentLinkedQueue<OctreeIterator> iteratorQueue;

    //States
    protected List<Octant> visibleLeaves;
    protected List<Octant> selectedLeaves;

    public Octree(int maxDepth, int size, int nbClasses)
    {
        this.maxDepth = maxDepth;
        this.classesCount = nbClasses;
        this.size = size;
    }

    public void initArchitecture()
    {
        this.engine = VizController.getInstance().getEngine();
        this.drawable = VizController.getInstance().getDrawable();

        leaves = new ParamAVLTree<Octant>(new AVLItemAccessor<Octant>()
        {
            public int getNumber(Octant item) {
                return item.getNumber();
            }
        });
        visibleLeaves = new ArrayList<Octant>();
        selectedLeaves = new ArrayList<Octant>();

        iteratorQueue = new ConcurrentLinkedQueue<OctreeIterator>();
        iteratorQueue.add(new OctreeIterator());
        iteratorQueue.add(new OctreeIterator());
        iteratorQueue.add(new OctreeIterator());
        iteratorQueue.add(new OctreeIterator());

        float dis = size/(float)Math.pow(2, this.maxDepth+1);
		root = new Octant(this, 0,dis, dis, dis, size);
    }

    public void addObject(int classID, Object3d obj)
    {
        if(obj.getOctant()!=null)
            obj.getOctant().addObject(classID, obj);
        else
            root.addObject(classID, obj);
    }

    public void removeObject(int classID, Object3d obj)
    {
        root.removeObject(classID, obj);
    }

    public void updateVisibleOctant(GL gl)
	{
        //Limits
        refreshLimits();

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
        //int depth=Integer.MAX_VALUE;
        //int minDepth=0;
		for(int i=0; i< nbRecords; i++)
		{
			int hit = hitsBuffer.get(i*4+3); 		//-1 Because of the glPushName(0)
            //int minZ = hitsBuffer.get(i*4+1);
            //if(minZ < depth)
            //{
            //    depth=minZ;
             //   minDepth=hit;
            //}

			Octant nodeHit = leaves.getItem(hit);
			visibleLeaves.add(nodeHit);
		}
        //System.out.println(minDepth);
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
        OctreeIterator itr = borrowIterator();
        itr.reset(visibleLeaves, classID);
        return itr;
	}

    public Iterator<Object3d> getSelectedObjectIterator(int classID) {
       OctreeIterator itr = borrowIterator();
        itr.reset(selectedLeaves, classID);
        return itr;
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

    private void refreshLimits()
    {
        float[] limits = engine.getGraphLimits();

        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for(Octant o : leaves)
        {
            float size = o.getSize()/2f;
            minX = Math.min(minX, o.getPosX()-size);
            maxX = Math.max(maxX, o.getPosX()+size);
            minY = Math.min(minY, o.getPosY()-size);
            maxY = Math.max(maxY, o.getPosY()+size);
            minZ = Math.min(minZ, o.getPosZ()-size);
            maxZ = Math.max(maxZ, o.getPosZ()+size);
        }

        float viewportMinX = Float.POSITIVE_INFINITY;
        float viewportMaxX = Float.NEGATIVE_INFINITY;
        float viewportMinY = Float.POSITIVE_INFINITY;
        float viewportMaxY = Float.NEGATIVE_INFINITY;
        double[] point;

        point = drawable.myGluProject(minX, minY, minZ);        //bottom far left
        viewportMinX = (float)Math.min(viewportMinX, point[0]);
        viewportMinY = (float)Math.min(viewportMinY, point[1]);

        point = drawable.myGluProject(minX, minY, maxZ);        //bottom near left
        viewportMinX = (float)Math.min(viewportMinX, point[0]);
        viewportMinY = (float)Math.min(viewportMinY, point[1]);

        point = drawable.myGluProject(minX, maxY, maxZ);        //up near left
        viewportMinX = (float)Math.min(viewportMinX, point[0]);
        viewportMaxY = (float)Math.max(viewportMaxY, point[1]);

        point = drawable.myGluProject(maxX, minY, maxZ);        //bottom near right
        viewportMaxX = (float)Math.max(viewportMaxX, point[0]);
        viewportMinY = (float)Math.min(viewportMinY, point[1]);

        point = drawable.myGluProject(maxX, minY, minZ);        //bottom far right
        viewportMaxX = (float)Math.max(viewportMaxX, point[0]);
        viewportMinY = (float)Math.min(viewportMinY, point[1]);

        point = drawable.myGluProject(maxX, maxY, minZ);        //up far right
        viewportMaxX = (float)Math.max(viewportMaxX, point[0]);
        viewportMaxY = (float)Math.max(viewportMaxY, point[1]);

        point = drawable.myGluProject(maxX, maxY, maxZ);        //up near right
        viewportMaxX = (float)Math.max(viewportMaxX, point[0]);
        viewportMaxY = (float)Math.max(viewportMaxY, point[1]);

        point = drawable.myGluProject(minX, maxY, minZ);        //up far left
        viewportMinX = (float)Math.min(viewportMinX, point[0]);
        viewportMinY = (float)Math.min(viewportMinY, point[1]);

        limits[Limits.MIN_X.ordinal()] = viewportMinX;
        limits[Limits.MAX_X.ordinal()] = viewportMaxX;
        limits[Limits.MIN_Y.ordinal()] = viewportMinY;
        limits[Limits.MAX_Y.ordinal()] = viewportMaxY;
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

    private OctreeIterator borrowIterator()
    {
        OctreeIterator itr =  iteratorQueue.poll();
        if(itr==null)
            System.err.println("Octree iterator starved");
        return itr;
    }

    private void returnIterator(OctreeIterator iterator)
    {
        iteratorQueue.add(iterator);

    }


    private class OctreeIterator implements Iterator<Object3d>, ResetableIterator
    {
        private int i=0;
        private int classID;
        private List<Octant> octants;
        private ParamAVLIterator<Object3d> octantIterator;

        public OctreeIterator()
        {
            octantIterator = new ParamAVLIterator<Object3d>();
        }

        public OctreeIterator(List<Octant> octants, int classID)
        {
            this();
            this.octants = octants;
            this.classID = classID;     
        }

        public void reset(List<Octant> octants, int classID)
        {
            this.octants = octants;
            this.classID = classID;
            i=0;
        }

        public boolean hasNext() {
           if(!octantIterator.hasNext())
           {
               while(i<octants.size())
               {
                    octantIterator.setNode(octants.get(i).getTree(classID));
                    i++;
                    if(octantIterator.hasNext())
                        return true;
               }
               returnIterator(this);
               return false;
           }
           return true;
        }

        public Object3d next() {
            Object3d obj = octantIterator.next();
            return obj;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
