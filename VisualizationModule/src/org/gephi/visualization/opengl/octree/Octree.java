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
package org.gephi.visualization.opengl.octree;

import com.sun.opengl.util.BufferUtil;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.gleem.linalg.Vec3f;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class Octree implements VizArchitecture {
    //Architecture

    private GraphDrawableImpl drawable;
    private AbstractEngine engine;
    private GraphLimits limits;

    //Attributes
    private int objectsIDs;
    private int maxDepth;
    private int classesCount;
    private int size;
    private int cacheMarker;

    //Octant
    private Octant root;
    private ParamAVLTree<Octant> leaves;

    //Iterator
    private ConcurrentLinkedQueue<OctreeIterator> iteratorQueue;

    //States
    protected List<Octant> visibleLeaves;
    protected List<Octant> selectedLeaves;

    //Utils
    protected ParamAVLIterator<Object3dImpl> updatePositionIterator = new ParamAVLIterator<Object3dImpl>();
    protected ParamAVLIterator<Object3dImpl> cleanObjectsIterator = new ParamAVLIterator<Object3dImpl>();

    public Octree(int maxDepth, int size, int nbClasses) {
        this.maxDepth = maxDepth;
        this.classesCount = nbClasses;
        this.size = size;
    }

    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.drawable = VizController.getInstance().getDrawable();
        this.limits = VizController.getInstance().getLimits();

        leaves = new ParamAVLTree<Octant>(new AVLItemAccessor<Octant>() {

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

        float dis = size / (float) Math.pow(2, this.maxDepth + 1);
        root = new Octant(this, 0, dis, dis, dis, size);
    }

    public void addObject(int classID, Object3dImpl obj) {
        Octant[] octants = obj.getOctants();
        boolean manualAdd = true;
        for (int i = 0; i < octants.length; i++) {
            Octant o = obj.getOctants()[i];
            if (o != null) {
                o.addObject(classID, obj);
                manualAdd = false;
            }
        }

        if (manualAdd) {
            root.addObject(classID, obj);
        }
    }

    public void removeObject(int classID, Object3dImpl obj) {
        Octant[] octants = obj.getOctants();
        for (int i = 0; i < octants.length; i++) {
            Octant o = obj.getOctants()[i];
            if (o != null) {
                octants[i].removeObject(classID, obj);
            }
        }
    }

    public void updateVisibleOctant(GL gl) {
        //Limits
        refreshLimits();

        //Switch to OpenGL select mode
        int capacity = 1 * 4 * leaves.getCount();      //Each object take in maximium : 4 * name stack depth
        IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);
        gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
        gl.glRenderMode(GL.GL_SELECT);
        gl.glInitNames();
        gl.glPushName(0);

        //Draw the nodes cube in the select buffer
        for (Octant n : leaves) {
            n.resetUpdatePositionFlag();        //Profit from the loop to do this, because this method is always after updating position
            gl.glLoadName(n.getNumber());
            n.displayOctreeNode(gl);
        }
        int nbRecords = gl.glRenderMode(GL.GL_RENDER);

        visibleLeaves.clear();

        //Get the hits and add the nodes' objects to the array
        int depth = Integer.MAX_VALUE;
        int minDepth = -1;
        for (int i = 0; i < nbRecords; i++) {
            int hit = hitsBuffer.get(i * 4 + 3); 		//-1 Because of the glPushName(0)
            int minZ = hitsBuffer.get(i * 4 + 1);
            if (minZ < depth) {
                depth = minZ;
                minDepth = hit;
            }

            Octant nodeHit = leaves.getItem(hit);
            visibleLeaves.add(nodeHit);
        }
        if (minDepth != -1) {
            Octant closestOctant = leaves.getItem(minDepth);
            Vec3f pos = new Vec3f(closestOctant.getPosX(), closestOctant.getPosY(), closestOctant.getPosZ());
            limits.setClosestPoint(pos);
        }
    //System.out.println(minDepth);
    }

    public void updateSelectedOctant(GL gl, GLU glu, float[] mousePosition, float[] pickRectangle) {
        //Start Picking mode
        int capacity = 1 * 4 * visibleLeaves.size();      //Each object take in maximium : 4 * name stack depth
        IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);

        gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
        gl.glRenderMode(GL.GL_SELECT);

        gl.glInitNames();
        gl.glPushName(0);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        glu.gluPickMatrix(mousePosition[0], mousePosition[1], pickRectangle[0], pickRectangle[1], drawable.getViewport());
        gl.glMultMatrixd(drawable.getProjectionMatrix());

        gl.glMatrixMode(GL.GL_MODELVIEW);

        //Draw the nodes' cube int the select buffer
        int hitName = 1;
        for (int i = 0; i < visibleLeaves.size(); i++) {
            Octant node = visibleLeaves.get(i);
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
        for (int i = 0; i < nbRecords; i++) {
            int hit = hitsBuffer.get(i * 4 + 3) - 1; 		//-1 Because of the glPushName(0)

            Octant nodeHit = visibleLeaves.get(hit);
            selectedLeaves.add(nodeHit);
        }
    }

    public void cleanDeletedObjects(int classID) {
        for (Octant o : leaves) {
            for (cleanObjectsIterator.setNode(o.getTree(classID)); cleanObjectsIterator.hasNext();) {
                Object3dImpl obj = cleanObjectsIterator.next();
                if (!obj.isCacheMatching(cacheMarker)) {
                    removeObject(classID, obj);
                    obj.resetOctant();
                }
            }
        }
    }

    public void resetObjectClass(int classID)
    {
        for (Octant o : leaves) {
            ParamAVLTree<Object3dImpl> tree = o.getTree(classID);

            //Reset octants in objects
            for (cleanObjectsIterator.setNode(tree); cleanObjectsIterator.hasNext();) {
                Object3dImpl obj = cleanObjectsIterator.next();
                obj.resetOctant();
            }

            //Empty the tree
            o.clear(classID);
        }
    }

    public void updateObjectsPosition(int classID) {
        for (Octant o : leaves) {
            if (o.isRequiringUpdatePosition()) {
                for (updatePositionIterator.setNode(o.getTree(classID)); updatePositionIterator.hasNext();) {
                    Object3dImpl obj = updatePositionIterator.next();
                    if (!obj.isInOctreeLeaf(o)) {
                        o.removeObject(classID, obj);
                        obj.resetOctant();
                        addObject(classID, obj);
                        //TODO break the loop somehow
                    }
                }

            }
        }
    }

    public Iterator<Object3dImpl> getObjectIterator(int classID) {
        OctreeIterator itr = borrowIterator();
        itr.reset(visibleLeaves, classID);
        return itr;
    }

    public Iterator<Object3dImpl> getSelectedObjectIterator(int classID) {
        OctreeIterator itr = borrowIterator();
        itr.reset(selectedLeaves, classID);
        return itr;
    }

    public void displayOctree(GL gl) {
        //gl.glColor3f(1, 0.5f, 0.5f);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
        for (Octant o : visibleLeaves) {
            o.displayOctreeNode(gl);
        }
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
    }

    void addLeaf(Octant leaf) {
        leaves.add(leaf);
    }

    void removeLeaf(Octant leaf) {
        leaves.remove(leaf);
    }

    private void refreshLimits() {

        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Octant o : leaves) {
            float octanSize = o.getSize() / 2f;
            minX = Math.min(minX, o.getPosX() - octanSize);
            maxX = Math.max(maxX, o.getPosX() + octanSize);
            minY = Math.min(minY, o.getPosY() - octanSize);
            maxY = Math.max(maxY, o.getPosY() + octanSize);
            minZ = Math.min(minZ, o.getPosZ() - octanSize);
            maxZ = Math.max(maxZ, o.getPosZ() + octanSize);
        }

        int viewportMinX = Integer.MAX_VALUE;
        int viewportMaxX = Integer.MIN_VALUE;
        int viewportMinY = Integer.MAX_VALUE;
        int viewportMaxY = Integer.MIN_VALUE;
        double[] point;

        point = drawable.myGluProject(minX, minY, minZ);        //bottom far left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(minX, minY, maxZ);        //bottom near left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(minX, maxY, maxZ);        //up near left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, minY, maxZ);        //bottom near right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, minY, minZ);        //bottom far right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, maxY, minZ);        //up far right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, maxY, maxZ);        //up near right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(minX, maxY, minZ);        //up far left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        limits.setMinXoctree(minX);
        limits.setMaxXoctree(maxX);
        limits.setMinYoctree(minY);
        limits.setMaxYoctree(maxY);
        limits.setMinZoctree(minZ);
        limits.setMaxZoctree(maxZ);

        limits.setMinXviewport(viewportMinX);
        limits.setMaxXviewport(viewportMaxX);
        limits.setMinYviewport(viewportMinY);
        limits.setMaxYviewport(viewportMaxY);
    }

    int getClassesCount() {
        return classesCount;
    }

    int getMaxDepth() {
        return maxDepth;
    }

    int getNextObjectID() {
        return objectsIDs++;
    }

    private OctreeIterator borrowIterator() {
        OctreeIterator itr = iteratorQueue.poll();
        if (itr == null) {
            System.err.println("Octree iterator starved");
        }
        return itr;
    }

    private void returnIterator(OctreeIterator iterator) {
        iteratorQueue.add(iterator);

    }

    public void setCacheMarker(int cacheMarker) {
        this.cacheMarker = cacheMarker;
    }

    private class OctreeIterator implements Iterator<Object3dImpl>, ResetableIterator {

        private int i = 0;
        private int classID;
        private List<Octant> octants;
        private ParamAVLIterator<Object3dImpl> octantIterator;

        public OctreeIterator() {
            octantIterator = new ParamAVLIterator<Object3dImpl>();
        }

        public OctreeIterator(List<Octant> octants, int classID) {
            this();
            this.octants = octants;
            this.classID = classID;
        }

        public void reset(List<Octant> octants, int classID) {
            this.octants = octants;
            this.classID = classID;
            i = 0;
        }

        public boolean hasNext() {
            if (!octantIterator.hasNext()) {
                while (i < octants.size()) {
                    octantIterator.setNode(octants.get(i).getTree(classID));
                    i++;
                    if (octantIterator.hasNext()) {
                        return true;
                    }
                }
                returnIterator(this);
                return false;
            }
            return true;
        }

        public Object3dImpl next() {
            Object3dImpl obj = octantIterator.next();
            return obj;
        }

        public void remove() {
            octantIterator.remove();
        }
    }
}
