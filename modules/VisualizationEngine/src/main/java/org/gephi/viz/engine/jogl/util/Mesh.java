package org.gephi.viz.engine.jogl.util;

public class Mesh {
    /**
     * Data class for Mesh
     * [a, b, c] > 3 vertex = vertexData.length = 3
     * ^
     * 1 component per Vertex
     * <p>
     * [ax,ay, bx,by, cx,cy] > 3 vertex = vertexData.length = 6
     * ^--^
     * 2 components per Vertex
     * <p>
     * [ax,ay,az, bx,by,bz, cx,cy,cz] > 3 vertex = vertexData.length = 9
     * ^--^--^
     * 3 components per Vertex
     */
    public float[] vertexData;
    public int vertexCount;
    public int vertexComponentSize;


}

