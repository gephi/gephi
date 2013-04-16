/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.octree;

import com.sun.opengl.util.GLUT;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.model.node.NodeModel;

/**
 *
 * @author mbastian
 */
public class Octant {

    //Static
    protected static final long ONEOVERPHI = 106039;
    protected static final float TRIM_THRESHOLD = 1000;
    protected static final float TRIM_RATIO = 0.3f;
    //LeafId
    protected int leafId = Octree.NULL_ID;
    //Coordinates
    protected float size;
    protected float posX;
    protected float posY;
    protected float posZ;
    protected int depth;
    //Children
    protected Octant[] children;
    //Stats
    protected int nodeCount = 0;
    //Data
    protected NodeModel[] nodes;
    protected int[] garbage;
    protected int garbageLength;
    protected int length;
    //Flags
    protected boolean visible;

    public Octant(int depth, float posX, float posY, float posZ, float size) {
        this.size = size;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.depth = depth;
    }

    protected void addNode(NodeModel nodeModel) {
        int id;
        if (garbageLength > 0) {
            id = removeGarbage();
        } else {
            id = length++;
            growNodes(id);
        }
        nodes[id] = nodeModel;
        nodeCount++;
        nodeModel.setOctantId(id);
    }

    protected void removeNode(NodeModel nodeModel) {
        int id = nodeModel.getOctantId();
        nodes[id] = null;
        nodeCount--;
        addGarbage(id);
        trimNodes();
    }

    protected void clear() {
        nodes = null;
        garbage = null;
        length = 0;
        garbage = null;
        garbageLength = 0;
        nodeCount = 0;
    }

    protected boolean isEmpty() {
        return nodeCount == 0;
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

    protected void displayOctant(GL gl) {

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
    }

    protected void displayOctantInfo(GL gl, GLU glu) {
        GLUT glut = new GLUT();

        float quantum = size / 2;
        float height = 15;

        gl.glPushMatrix();
        gl.glTranslatef(posX - quantum, posY + quantum - height, posZ + quantum);
        gl.glScalef(0.1f, 0.1f, 0.1f);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
        glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, "ID: " + leafId);
        gl.glPopMatrix();

        height += 15;
        gl.glPushMatrix();
        gl.glTranslatef(posX - quantum, posY + quantum - height, posZ + quantum);
        gl.glScalef(0.1f, 0.1f, 0.1f);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
        glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, "objectsCount: " + nodeCount);
        gl.glPopMatrix();
    }

    private void addGarbage(int index) {
        if (garbage == null) {
            garbage = new int[10];
        } else if (garbageLength == garbage.length) {
            final int newLength = (int) Math.min(Math.max((ONEOVERPHI * garbage.length) >>> 16, garbageLength + 1), Integer.MAX_VALUE);
            final int t[] = new int[newLength];
            System.arraycopy(garbage, 0, t, 0, garbage.length);
            garbage = t;
        }
        garbage[garbageLength++] = index;
    }

    private int removeGarbage() {
        return garbage[garbageLength--];
    }

    private void growNodes(final int index) {
        if (nodes == null) {
            nodes = new NodeModel[10];
        } else if (index > nodes.length) {
            final int newLength = (int) Math.min(Math.max((ONEOVERPHI * nodes.length) >>> 16, index), Integer.MAX_VALUE);
            final NodeModel t[] = new NodeModel[newLength];
            System.arraycopy(nodes, 0, t, 0, nodes.length);
            nodes = t;
        }
    }

    private void trimNodes() {
        if (length >= TRIM_THRESHOLD && ((float) nodeCount) / length < TRIM_RATIO) {
            NodeModel t[] = new NodeModel[nodeCount];
            if (nodeCount > 0) {
                int c = 0;
                for (int i = 0; i < nodes.length; i++) {
                    NodeModel n = nodes[i];
                    if (n != null) {
                        n.setOctantId(c);
                        t[c++] = n;
                    }
                }
            }
            length = t.length;
            nodes = t;
            garbage = null;
            garbageLength = 0;
        }
    }
}
