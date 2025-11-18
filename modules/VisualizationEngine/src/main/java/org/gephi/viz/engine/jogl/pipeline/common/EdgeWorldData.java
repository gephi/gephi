package org.gephi.viz.engine.jogl.pipeline.common;

import org.gephi.viz.engine.spi.WorldData;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

public class EdgeWorldData implements WorldData {

    private final float[] backgroundColor;
    private final boolean someSelection;
    private final float minWeight;
    private final float maxWeight;
    private final float nodeScale;
    private final float edgeScale;
    private final float lightenNonSelectedFactor;
    private final OpenGLOptions openGLOptions;

    public EdgeWorldData(float[] backgroundColor,
                         boolean someSelection,
                         float minWeight,
                         float maxWeight,
                         float nodeScale,
                         float edgeScale,
                         float lightenNonSelectedFactor,
                         OpenGLOptions openGLOptions) {
        this.backgroundColor = backgroundColor;
        this.someSelection = someSelection;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.nodeScale = nodeScale;
        this.edgeScale = edgeScale;
        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
        this.openGLOptions = openGLOptions;
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public float getEdgeScale() {
        return edgeScale;
    }

    public float getNodeScale() {
        return nodeScale;
    }

    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public boolean hasSomeSelection() {
        return someSelection;
    }

    public OpenGLOptions getOpenGLOptions() {
        return openGLOptions;
    }
}
