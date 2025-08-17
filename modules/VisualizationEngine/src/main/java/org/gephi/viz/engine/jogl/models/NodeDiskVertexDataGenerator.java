package org.gephi.viz.engine.jogl.models;

public class NodeDiskVertexDataGenerator {
    public static final int VERTEX_FLOATS = 2;
    private final int triangleAmount;
    private final float[] vertexData;
    private final int vertexCount;

    public NodeDiskVertexDataGenerator(int triangleAmount) {
        this.triangleAmount = triangleAmount;
        this.vertexData = generateFilledCircle(triangleAmount);

        this.vertexCount = triangleAmount * 3;
    }

    public int getTriangleAmount() {
        return triangleAmount;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public float[] getVertexData() {
        return vertexData;
    }

    private static float[] generateFilledCircle(int triangleAmount) {
        final double twicePi = 2.0 * Math.PI;

        final int circleFloatsCount = (triangleAmount * 3) * VERTEX_FLOATS;
        final float[] data = new float[circleFloatsCount];
        final int triangleFloats = 3 * VERTEX_FLOATS;

        //Circle:
        for (int i = 1, j = 0; i <= triangleAmount; i++, j += triangleFloats) {
            //Center
            data[j + 0] = 0;//X
            data[j + 1] = 0;//Y

            //Triangle start:
            data[j + 2] = (float) Math.cos((i - 1) * twicePi / triangleAmount);//X
            data[j + 3] = (float) Math.sin((i - 1) * twicePi / triangleAmount);//Y

            //Triangle end:
            if (i == triangleAmount) {
                //Last point
                data[j + 4] = 1;//X
                data[j + 5] = 0;//Y
            } else {
                data[j + 4] = (float) Math.cos(i * twicePi / triangleAmount);//X
                data[j + 5] = (float) Math.sin(i * twicePi / triangleAmount);//Y
            }
        }

        return data;
    }
}
