package org.gephi.viz.engine.jogl.models.mesh;


import org.gephi.viz.engine.jogl.util.Mesh;

public class EdgeLineMeshGenerator {
    public static Mesh undirectedMeshGenerator() {
        final Mesh mesh = new Mesh();
        //lineEnd, sideVector
        mesh.vertexData = new float[] {
            //Triangle 1
            0, -1,// bottom left corner
            1, -1,// top left corner
            0, 1,// bottom right corner
            //Triangle 2
            0, 1,// bottom right corner
            1, -1,// top left corner
            1, 1// top right corner
        };
        mesh.vertexComponentSize = 2;
        mesh.vertexCount = 6;
        return mesh;
    }

    public static Mesh directedMeshGenerator() {
        final Mesh mesh = new Mesh();
        //lineEnd, sideVector
        mesh.vertexData = new float[] {
            //First 6 are the edge line as a rectangle:
            //Triangle 1
            0, 1, 0,// bottom right corner
            0, -1, 0,// bottom left corner
            1, -1, -1,// top left corner
            //Triangle 2
            1, -1, -1,// top left corner
            1, 1, -1,// top right corner
            0, 1, 0,// bottom right corner
            //Last 3 are the arrow tip triangle:
            1, 0, 0,//arrow tip
            1, -2, -1,// arrow bottom left vertex
            1, 2, -1// arrow bottom right vertex
        };
        mesh.vertexComponentSize = 3;
        mesh.vertexCount = 9;
        return mesh;
    }
}
