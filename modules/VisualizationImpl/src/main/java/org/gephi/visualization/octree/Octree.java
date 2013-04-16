/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.octree;

import com.sun.opengl.util.BufferUtil;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Node;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizController;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author mbastian
 */
public class Octree {

    //Const
    protected static final int NULL_ID = -1;
    //Architecture
    protected GraphLimits limits;
    private GraphDrawableImpl drawable;
    protected VizController vizController;
    //Params
    protected final int maxDepth;
    protected final int size;
    //Root
    protected final Octant root;
    //Leaves
    protected final IntSortedSet garbageQueue;
    protected Octant[] leaves;
    protected int length;
    protected int visibleLeaves;
    //Selected
    protected List<Octant> selectedLeaves;
    //Itr
    protected OctantIterator nodeIterator;

    public Octree(int maxDepth, int size) {
        this.length = 0;
        this.leaves = new Octant[0];
        this.garbageQueue = new IntRBTreeSet();
        this.maxDepth = maxDepth;
        this.size = size;
        this.selectedLeaves = new ArrayList<Octant>();
        this.nodeIterator = new OctantIterator();

        //Init root
        float dis = size / (float) Math.pow(2, this.maxDepth + 1);
        root = new Octant(0, dis, dis, dis, size);
    }

    public void initArchitecture() {
        this.drawable = VizController.getInstance().getDrawable();
        this.limits = VizController.getInstance().getLimits();
        this.vizController = VizController.getInstance();
    }

    public void addNode(NodeModel node) {
        Octant octant = root;
        int depth = octant.depth;

        clampPosition(node);

        while (depth < maxDepth) {
            if (octant.children == null) {
                subdivide(octant);
            }

            int index = node.octreePosition(octant.posX, octant.posY, octant.posZ, octant.size);
            octant = octant.children[index];
            depth = octant.depth;
        }

        if (octant.isEmpty()) {
            addLeaf(octant);
        }

        octant.addNode(node);
        node.setOctant(octant);
    }

    public void removeNode(NodeModel node) {
        Octant octant = node.getOctant();
        octant.removeNode(node);
        if (octant.isEmpty()) {
            removeLeaf(octant);
        }
    }

    public Iterator<NodeModel> getNodeIterator() {
        nodeIterator.reset();
        return nodeIterator;
    }

    protected int addLeaf(final Octant octant) {
        int id;
        if (!garbageQueue.isEmpty()) {
            id = garbageQueue.firstInt();
            garbageQueue.remove(id);
        } else {
            id = length++;
            ensureArraySize(id);
        }
        leaves[id] = octant;
        octant.leafId = id;
        return id;
    }

    protected void removeLeaf(final Octant octant) {
        int id = octant.leafId;
        leaves[id] = null;
        garbageQueue.add(id);
        octant.leafId = NULL_ID;
    }

    private void ensureArraySize(int index) {
        if (index >= leaves.length) {
            Octant[] newArray = new Octant[index + 1];
            System.arraycopy(leaves, 0, newArray, 0, leaves.length);
            leaves = newArray;
        }
    }

    private void subdivide(Octant octant) {
        float quantum = octant.size / 4;
        float newSize = octant.size / 2;
        float posX = octant.posX;
        float posY = octant.posY;
        float posZ = octant.posZ;
        int depth = octant.depth;

        Octant o1 = new Octant(depth + 1, posX + quantum, posY + quantum, posZ - quantum, newSize);
        Octant o2 = new Octant(depth + 1, posX - quantum, posY + quantum, posZ - quantum, newSize);
        Octant o3 = new Octant(depth + 1, posX + quantum, posY + quantum, posZ + quantum, newSize);
        Octant o4 = new Octant(depth + 1, posX - quantum, posY + quantum, posZ + quantum, newSize);

        Octant o5 = new Octant(depth + 1, posX + quantum, posY - quantum, posZ - quantum, newSize);
        Octant o6 = new Octant(depth + 1, posX - quantum, posY - quantum, posZ - quantum, newSize);
        Octant o7 = new Octant(depth + 1, posX + quantum, posY - quantum, posZ + quantum, newSize);
        Octant o8 = new Octant(depth + 1, posX - quantum, posY - quantum, posZ + quantum, newSize);

        octant.children = new Octant[]{o1, o2, o3, o4, o5, o6, o7, o8};
    }

    private void clampPosition(NodeModel nodeModel) {
        //Clamp Hack to avoid nodes to be outside octree
        float quantum = size / 2;
        Node node = nodeModel.getNode();
        float x = node.x();
        float y = node.y();
        float z = node.z();
        if (x > root.posX + quantum) {
            node.setX(root.posX + quantum);
        } else if (x < root.posX - quantum) {
            node.setX(root.posX - quantum);
        }
        if (y > root.posY + quantum) {
            node.setY(root.posY + quantum);
        } else if (y < root.posY - quantum) {
            node.setY(root.posY - quantum);
        }
        if (z > root.posZ + quantum) {
            node.setZ(root.posZ + quantum);
        } else if (z < root.posZ - quantum) {
            node.setZ(root.posZ - quantum);
        }
    }

    private void refreshLimits() {
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Octant o : leaves) {
            if (o != null) {
                float octanSize = o.getSize() / 2f;
                minX = Math.min(minX, o.getPosX() - octanSize);
                maxX = Math.max(maxX, o.getPosX() + octanSize);
                minY = Math.min(minY, o.getPosY() - octanSize);
                maxY = Math.max(maxY, o.getPosY() + octanSize);
                minZ = Math.min(minZ, o.getPosZ() - octanSize);
                maxZ = Math.max(maxZ, o.getPosZ() + octanSize);
            }
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

    public void updateVisibleOctant(GL gl) {
        //Limits
        refreshLimits();

        //Switch to OpenGL select mode
        int capacity = 1 * 4 * leaves.length;      //Each object take in maximium : 4 * name stack depth
        IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);
        gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
        gl.glRenderMode(GL.GL_SELECT);
        gl.glInitNames();
        gl.glPushName(0);
        gl.glDisable(GL.GL_CULL_FACE);      //Disable flags
        //Draw the nodes cube in the select buffer
        for (Octant n : leaves) {
            gl.glLoadName(n.leafId);
            n.displayOctant(gl);
            n.visible = false;
        }
        visibleLeaves = 0;
        int nbRecords = gl.glRenderMode(GL.GL_RENDER);
        if (vizController.getVizModel().isCulling()) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
        }

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

            Octant nodeHit = leaves[hit];
            nodeHit.visible = true;
            visibleLeaves++;
        }
        if (minDepth != -1) {
            Octant closestOctant = leaves[minDepth];
            Vec3f pos = new Vec3f(closestOctant.getPosX(), closestOctant.getPosY(), closestOctant.getPosZ());
            limits.setClosestPoint(pos);
        }
    }

    public void updateSelectedOctant(GL gl, GLU glu, float[] mousePosition, float[] pickRectangle) {
        //Start Picking mode
        int capacity = 1 * 4 * visibleLeaves;      //Each object take in maximium : 4 * name stack depth
        IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);

        gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
        gl.glRenderMode(GL.GL_SELECT);
        gl.glDisable(GL.GL_CULL_FACE);      //Disable flags

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
        for (int i = 0; i < leaves.length; i++) {
            Octant node = leaves[i];
            if (node.visible) {
                gl.glLoadName(hitName);
                node.displayOctant(gl);
                hitName++;
            }
        }

        //Restoring the original projection matrix
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glFlush();

        //Returning to normal rendering mode
        int nbRecords = gl.glRenderMode(GL.GL_RENDER);
        if (vizController.getVizModel().isCulling()) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
        }

        //Clean previous selection
        selectedLeaves.clear();

        //Get the hits and put the node under selection in the selectionArray
        for (int i = 0; i < nbRecords; i++) {
            int hit = hitsBuffer.get(i * 4 + 3) - 1; 		//-1 Because of the glPushName(0)

            Octant nodeHit = leaves[hit];
            selectedLeaves.add(nodeHit);
        }
    }

    private class OctantIterator implements Iterator<NodeModel> {

        private int leafId;
        private Octant octant;
        private int leavesLength;
        private NodeModel[] nodes;
        private int nodesId;
        private int nodesLength;
        private NodeModel pointer;

        public OctantIterator() {
            leavesLength = leaves.length;
        }

        @Override
        public boolean hasNext() {
            while (pointer == null) {
                while (nodesId < nodesLength && pointer == null) {
                    pointer = nodes[nodesId++];
                }
                if (pointer == null) {
                    while (leafId < leavesLength && !octant.visible) {
                        octant = leaves[leafId++];
                    }
                    if (octant == null || !octant.visible) {
                        return false;
                    }
                    nodes = octant.nodes;
                    nodesId = 0;
                    nodesLength = nodes.length;
                }
            }
            return true;
        }

        @Override
        public NodeModel next() {
            NodeModel r = pointer;
            pointer = null;
            return r;
        }

        public void reset() {
            leafId = 0;
            octant = null;
            leavesLength = leaves.length;
            nodes = null;
            nodesId = 0;
            nodesLength = 0;
            pointer = null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
