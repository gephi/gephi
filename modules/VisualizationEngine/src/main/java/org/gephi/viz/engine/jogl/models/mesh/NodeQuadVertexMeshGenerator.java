package org.gephi.viz.engine.jogl.models.mesh;

import org.gephi.viz.engine.jogl.util.Mesh;

public class NodeQuadVertexMeshGenerator {

    // TODO: Check if removing in favor of vertex_id make sense

    public static Mesh generate() {
        final Mesh mesh = new Mesh();
        mesh.vertexCount = 6;
        mesh.vertexComponentSize = 2;
        mesh.vertexData = new float[mesh.vertexCount * mesh.vertexComponentSize];
        // top right
        mesh.vertexData[0] = 1.f;
        mesh.vertexData[1] = 1.f;

        // bottom right
        mesh.vertexData[2] = 1.f;
        mesh.vertexData[3] = -1.f;

        //top left
        mesh.vertexData[4] = -1.f;
        mesh.vertexData[5] = 1.f;

        // bottom right
        mesh.vertexData[6] = 1.f;
        mesh.vertexData[7] = -1.f;

        // bottom left
        mesh.vertexData[8] = -1.f;
        mesh.vertexData[9] = -1.f;

        // top left
        mesh.vertexData[10] = -1.f;
        mesh.vertexData[11] = 1.f;

        return mesh;
    }
}
