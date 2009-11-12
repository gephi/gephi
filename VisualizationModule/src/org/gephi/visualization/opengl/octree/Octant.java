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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.media.opengl.GL;
import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class Octant implements AVLItem {

    //Static
    private static int OctantIDs = 0;
    //Octree
    private Octree octree;
    //Coordinates
    private float size;
    private float posX;
    private float posY;
    private float posZ;
    private int depth;
    //Attributes
    private final int octantID;
    private int objectsCount = 0;
    private Octant[] children;
    private AtomicBoolean updateFlag = new AtomicBoolean();
    //Models
    private List<ParamAVLTree<ModelImpl>> modelClasses;

    public Octant(Octree octree, int depth, float posX, float posY, float posZ, float size) {
        this.size = size;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.depth = depth;
        this.octree = octree;
        this.octantID = OctantIDs++;
    }

    public void addObject(int classID, ModelImpl obj) {
        if (children == null && depth < octree.getMaxDepth()) {
            //Create children
            subdivide();
        }

        if (depth == octree.getMaxDepth()) {
            //First item add - Initialize
            if (objectsCount == 0) {
                octree.addLeaf(this);

                modelClasses = new ArrayList<ParamAVLTree<ModelImpl>>(octree.getClassesCount());
                for (int i = 0; i < octree.getClassesCount(); i++) {
                    modelClasses.add(new ParamAVLTree<ModelImpl>(new AVLItemAccessor<ModelImpl>() {

                        public int getNumber(ModelImpl item) {
                            return item.getNumber();
                        }
                    }));
                }
            }

            //Get the list
            ParamAVLTree<ModelImpl> objectClass = this.modelClasses.get(classID);

            //Set Octant
            obj.setOctant(this);

            //Add at this node
            obj.setID(octree.getNextObjectID());
            if (objectClass.add(obj)) {
                objectsCount++;
            }
        } else {
            if (classID == 0 && this == octree.root) {
                //Clamp Hack to avoid nodes to be outside octree
                float quantum = size / 2;
                float x = obj.getObj().x();
                float y = obj.getObj().y();
                float z = obj.getObj().z();
                if (x > posX + quantum) {
                    obj.getObj().setX(posX + quantum);
                } else if (x < posX - quantum) {
                    obj.getObj().setX(posX - quantum);
                }
                if (y > posY + quantum) {
                    obj.getObj().setY(posY + quantum);
                } else if (y < posY - quantum) {
                    obj.getObj().setY(posY - quantum);
                }
                if (z > posZ + quantum) {
                    obj.getObj().setZ(posZ + quantum);
                } else if (z < posZ - quantum) {
                    obj.getObj().setZ(posZ - quantum);
                }
            }

            for (int index : obj.octreePosition(posX, posY, posZ, size)) {
                children[index].addObject(classID, obj);
            }
        }
    }

    public void removeObject(int classID, ModelImpl obj) {
        //Get the list
        ParamAVLTree<ModelImpl> objectClass = this.modelClasses.get(classID);

        if (objectClass.remove(obj)) {
            objectsCount--;
        }

        if (objectsCount == 0) {
            //Remove leaf
            octree.removeLeaf(this);
        }

    }

    public void clear(int classID) {
        ParamAVLTree<ModelImpl> tree = getTree(classID);
        int count = tree.getCount();
        tree.clear();
        objectsCount -= count;
        if (objectsCount == 0) {
            //Remove leaf
            octree.removeLeaf(this);
        }
    }

    public void subdivide() {
        float quantum = size / 4;
        float newSize = size / 2;
        Octant o1 = new Octant(octree, depth + 1, posX + quantum, posY + quantum, posZ - quantum, newSize);
        Octant o2 = new Octant(octree, depth + 1, posX - quantum, posY + quantum, posZ - quantum, newSize);
        Octant o3 = new Octant(octree, depth + 1, posX + quantum, posY + quantum, posZ + quantum, newSize);
        Octant o4 = new Octant(octree, depth + 1, posX - quantum, posY + quantum, posZ + quantum, newSize);

        Octant o5 = new Octant(octree, depth + 1, posX + quantum, posY - quantum, posZ - quantum, newSize);
        Octant o6 = new Octant(octree, depth + 1, posX - quantum, posY - quantum, posZ - quantum, newSize);
        Octant o7 = new Octant(octree, depth + 1, posX + quantum, posY - quantum, posZ + quantum, newSize);
        Octant o8 = new Octant(octree, depth + 1, posX - quantum, posY - quantum, posZ + quantum, newSize);

        children = new Octant[]{o1, o2, o3, o4, o5, o6, o7, o8};
    }

    public Iterator<ModelImpl> iterator(int classID) {
        return this.modelClasses.get(classID).iterator();
    }

    public ParamAVLTree<ModelImpl> getTree(int classID) {
        return modelClasses.get(classID);
    }

    public void displayOctreeNode(GL gl) {
        /*if(children==null && depth==octree.getMaxDepth() && objectsCount>0)
        {*/

        float quantum = size / 2;
        gl.glBegin(GL.GL_QUAD_STRIP);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ - quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX - quantum, posY - quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ + quantum);
        gl.glEnd();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ - quantum);

        gl.glVertex3f(posX - quantum, posY - quantum, posZ + quantum);
        gl.glVertex3f(posX - quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ + quantum);
        gl.glEnd();
        /*}
        else if(children!=null)
        {
        for(Octant o : children)
        {
        o.displayOctreeNode(gl);
        }
        }*/
    }

    public int getNumber() {
        return octantID;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public float getSize() {
        return size;
    }

    public boolean isRequiringUpdatePosition() {
        return updateFlag.get();
    }

    public void requireUpdatePosition() {
        updateFlag.set(true);
        if (!updateFlag.getAndSet(true)) {
            octree.vizController.getScheduler().requireUpdatePosition();
        }
    }

    public void resetUpdatePositionFlag() {
        updateFlag.set(false);
    }
}
