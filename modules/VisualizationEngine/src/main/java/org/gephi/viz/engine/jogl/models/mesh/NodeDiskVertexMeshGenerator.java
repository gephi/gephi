package org.gephi.viz.engine.jogl.models.mesh;

import org.gephi.viz.engine.jogl.util.Mesh;

public class NodeDiskVertexMeshGenerator {

    public static Mesh generateFilledCircle(int triangleAmount) {
        final double twicePi = 2.0 * Math.PI;

        final Mesh mesh = new Mesh();
        mesh.vertexCount = triangleAmount * 3;
        mesh.vertexComponentSize = 2;

        final int circleFloatsCount = mesh.vertexCount * mesh.vertexComponentSize;
        mesh.vertexData = new float[circleFloatsCount];

        final int triangleComponentSize = 3 * mesh.vertexComponentSize;

        //Circle:
        for (int i = 0; i < triangleAmount; i++) {
            double current_radian = i * twicePi / triangleAmount;
            double next_radian = (i + 1) * twicePi / triangleAmount;
            int index_offset = triangleComponentSize * i;
            //Center
            mesh.vertexData[index_offset] = 0;//X
            mesh.vertexData[index_offset + 1] = 0;//Y

            //Triangle start:
            mesh.vertexData[index_offset + 2] = (float) Math.cos(current_radian);//X
            mesh.vertexData[index_offset + 3] = (float) Math.sin(current_radian);//Y

            //Triangle end:
            if (i == triangleAmount - 1) {
                //Last point
                mesh.vertexData[index_offset + 4] = 1;//X
                mesh.vertexData[index_offset + 5] = 0;//Y
            } else {
                mesh.vertexData[index_offset + 4] = (float) Math.cos(next_radian);//X
                mesh.vertexData[index_offset + 5] = (float) Math.sin(next_radian);//Y
            }
        }

        return mesh;
    }
}
