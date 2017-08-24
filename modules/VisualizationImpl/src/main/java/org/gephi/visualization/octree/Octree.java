package org.gephi.visualization.octree;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.Node;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.model.node.NodeModel;

public class Octree {

    //Const
    protected static final int NULL_ID = -1;
    //Architecture
    protected GraphLimits limits;
    private GraphDrawable drawable;
    protected VizController vizController;
    //Params
    protected final int maxDepth;
    protected final int size;
    //Root
    protected final Octant root;
    //Leaves
    protected final IntSortedSet garbageQueue;
    protected Octant[] leaves;
    protected int leavesCount;
    protected int length;
    protected int visibleLeaves;
    //Selected
    protected List<Octant> selectedLeaves;
    //Itr
    protected final OctantIterator nodeIterator;
    protected final SelectableIterator selectableIterator;
    protected final EdgeIterator edgeIterator;

    public Octree(int maxDepth, int size) {
        this.length = 0;
        this.leaves = new Octant[0];
        this.garbageQueue = new IntRBTreeSet();
        this.maxDepth = maxDepth;
        this.size = size;
        this.selectedLeaves = new ArrayList<>();
        this.nodeIterator = new OctantIterator();
        this.edgeIterator = new EdgeIterator(null);
        this.selectableIterator = new SelectableIterator();

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
        if (node.getOctant() != null) {
            throw new RuntimeException("Can't add a node to two octants");
        }
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
        node.setOctant(null);
    }

    public boolean repositionNodes() {
        List<NodeModel> movedNodes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Octant leaf = leaves[i];
            if (leaf != null) {
                int l = leaf.nodesLength;
                NodeModel[] nodes = leaf.nodes;
                for (int j = 0; j < l; j++) {
                    NodeModel node = nodes[j];
                    if (node != null) {
                        if (!node.isInOctreeLeaf(leaf)) {
                            removeNode(node);
                            movedNodes.add(node);
                        }
                    }
                }
            }
        }
        if (!movedNodes.isEmpty()) {
            for (NodeModel node : movedNodes) {
                addNode(node);
            }
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return leavesCount == 0;
    }

    public void clear() {
        for (int i = 0; i < length; i++) {
            Octant leaf = leaves[i];
            if (leaf != null) {
                leaf.clear();
            }
        }
        leaves = new Octant[0];
        leavesCount = 0;
        length = 0;
        garbageQueue.clear();
        selectedLeaves.clear();
        visibleLeaves = 0;
    }

    public Iterator<NodeModel> getNodeIterator() {
        nodeIterator.reset();
        return nodeIterator;
    }

    public Iterator<NodeModel> getSelectableNodeIterator() {
        selectableIterator.reset();
        return selectableIterator;
    }

    public Iterator<EdgeModel> getEdgeIterator() {
        nodeIterator.reset();
        edgeIterator.reset(nodeIterator);
        return edgeIterator;
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
        leavesCount++;
        octant.leafId = id;
        return id;
    }

    protected void removeLeaf(final Octant octant) {
        int id = octant.leafId;
        leaves[id] = null;
        leavesCount--;
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

        limits.setMinXoctree(minX);
        limits.setMaxXoctree(maxX);
        limits.setMinYoctree(minY);
        limits.setMaxYoctree(maxY);
        limits.setMinZoctree(minZ);
        limits.setMaxZoctree(maxZ);
    }

    public void updateVisibleOctant(GL2 gl) {
        if (leavesCount > 0) {
            //Limits
            refreshLimits();

            //Switch to OpenGL2 select mode
            int capacity = 1 * 4 * leavesCount;      //Each object take in maximium : 4 * name stack depth
            IntBuffer hitsBuffer = Buffers.newDirectIntBuffer(capacity);
            gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
            gl.glRenderMode(GL2.GL_SELECT);
            gl.glInitNames();
            gl.glPushName(0);
            gl.glDisable(GL2.GL_CULL_FACE);      //Disable flags
            //Draw the nodes cube in the select buffer
            for (Octant n : leaves) {
                if (n != null) {
                    gl.glLoadName(n.leafId);
                    n.displayOctant(gl);
                    n.visible = false;
                }
            }
            visibleLeaves = 0;
            int nbRecords = gl.glRenderMode(GL2.GL_RENDER);

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
    }

    public void updateSelectedOctant(GL2 gl, GLU glu, float[] mousePosition, float[] pickRectangle) {
        if (visibleLeaves > 0) {
            //Start Picking mode
            int capacity = 1 * 4 * visibleLeaves;      //Each object take in maximium : 4 * name stack depth
            IntBuffer hitsBuffer = Buffers.newDirectIntBuffer(capacity);

            gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
            gl.glRenderMode(GL2.GL_SELECT);
            gl.glDisable(GL2.GL_CULL_FACE);      //Disable flags

            gl.glInitNames();
            gl.glPushName(0);

            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            glu.gluPickMatrix(mousePosition[0], mousePosition[1], pickRectangle[0], pickRectangle[1], drawable.getViewport());
            gl.glMultMatrixf(drawable.getProjectionMatrix());

            gl.glMatrixMode(GL2.GL_MODELVIEW);

            //Draw the nodes' cube int the select buffer
            List<Octant> visibleLeavesList = new ArrayList<>();
            for (Octant n : leaves) {
                if (n != null && n.visible) {
                    int i = visibleLeavesList.size() + 1;
                    visibleLeavesList.add(n);
                    gl.glLoadName(i);
                    n.displayOctant(gl);
                }
            }

            //Restoring the original projection matrix
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glFlush();

            //Returning to normal rendering mode
            int nbRecords = gl.glRenderMode(GL2.GL_RENDER);

            //Clean previous selection
            selectedLeaves.clear();

            //Get the hits and put the node under selection in the selectionArray
            for (int i = 0; i < nbRecords; i++) {
                int hit = hitsBuffer.get(i * 4 + 3) - 1; 		//-1 Because of the glPushName(0)

                Octant nodeHit = visibleLeavesList.get(hit);
                selectedLeaves.add(nodeHit);
            }
        }
    }

    public void displayOctree(GL2 gl, GLU glu) {
        gl.glDisable(GL2.GL_CULL_FACE);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        for (Octant o : leaves) {
            if (o != null && o.visible) {
                gl.glColor3f(1, 0.5f, 0.5f);
                o.displayOctant(gl);
                o.displayOctantInfo(gl, glu);
            }
        }
        if (!vizController.getVizConfig().isWireFrame()) {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }
    }

    protected final class OctantIterator implements Iterator<NodeModel> {

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
            pointer = null;
            while (pointer == null) {
                while (nodesId < nodesLength && pointer == null) {
                    pointer = nodes[nodesId++];
                }
                if (pointer == null) {
                    octant = null;
                    while (leafId < leavesLength && (octant == null || !octant.visible)) {
                        octant = leaves[leafId++];
                    }
                    if (octant == null || !octant.visible) {
                        return false;
                    }
                    nodes = octant.nodes;
                    nodesId = 0;
                    nodesLength = octant.nodesLength;
                }
            }
            return pointer != null;
        }

        @Override
        public NodeModel next() {
            return pointer;
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

    protected final class SelectableIterator implements Iterator<NodeModel> {

        private int leavesLength;
        private int leafId;
        private Octant octant;
        private NodeModel[] nodes;
        private int nodesId;
        private int nodesLength;
        private NodeModel pointer;

        public SelectableIterator() {
            leavesLength = selectedLeaves.size();
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                while (nodesId < nodesLength && pointer == null) {
                    pointer = nodes[nodesId++];
                }
                if (pointer == null) {
                    octant = null;
                    while (leafId < leavesLength && octant == null) {
                        octant = selectedLeaves.get(leafId++);
                    }
                    if (octant == null) {
                        return false;
                    }
                    nodes = octant.nodes;
                    nodesId = 0;
                    nodesLength = octant.nodesLength;
                }
            }
            return pointer != null;
        }

        @Override
        public NodeModel next() {
            return pointer;
        }

        public void reset() {
            leafId = 0;
            octant = null;
            leavesLength = selectedLeaves.size();
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

    protected final class EdgeIterator implements Iterator<EdgeModel> {

        private Iterator<NodeModel> nodeItr;
        private EdgeModel[] edges;
        private int edgeId;
        private int edgeLength;
        private EdgeModel pointer;

        public EdgeIterator(Iterator<NodeModel> nodeIterator) {
            this.nodeItr = nodeIterator;
        }

        @Override
        public boolean hasNext() {
            pointer = null;
            while (pointer == null) {
                while (edgeId < edgeLength && pointer == null) {
                    pointer = edges[edgeId++];
                }
                if (pointer == null) {
                    if (nodeItr != null && nodeItr.hasNext()) {
                        NodeModel node = nodeItr.next();
                        edges = node.getEdges();
                        edgeLength = edges.length;
                        edgeId = 0;
                    } else {
                        return false;
                    }
                }
            }
            return pointer != null;
        }

        @Override
        public EdgeModel next() {
            return pointer;
        }

        public void reset(Iterator<NodeModel> nodeIterator) {
            nodeItr = nodeIterator;
            edges = null;
            edgeLength = 0;
            edgeId = 0;
            pointer = null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
