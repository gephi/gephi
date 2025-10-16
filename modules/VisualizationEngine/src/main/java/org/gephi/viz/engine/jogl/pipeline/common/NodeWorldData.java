package org.gephi.viz.engine.jogl.pipeline.common;

import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.spi.WorldData;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

public class NodeWorldData implements WorldData {

    private final boolean someSelection;
    private final float[] backgroundColor;
    private final float maxNodeSize;
    private final float zoom;
    private final float lightenNonSelectedFactor;
    private final OpenGLOptions openGLOptions;
    private final GLCapabilitiesSummary glCapabilitiesSummary;

    public NodeWorldData(boolean someSelection,
                         float[] backgroundColor,
                         float maxNodeSize,
                         float zoom,
                         float lightenNonSelectedFactor,
                         OpenGLOptions openGLOptions,
                         GLCapabilitiesSummary glCapabilitiesSummary) {
        this.someSelection = someSelection;
        this.backgroundColor = backgroundColor;
        this.maxNodeSize = maxNodeSize;
        this.zoom = zoom;
        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
        this.openGLOptions = openGLOptions;
        this.glCapabilitiesSummary = glCapabilitiesSummary;
    }

    public boolean hasSomeSelection() {
        return someSelection;
    }

    public float getMaxNodeSize() {
        return maxNodeSize;
    }

    public float getZoom() {
        return zoom;
    }

    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public OpenGLOptions getOpenGLOptions() {
        return openGLOptions;
    }

    public GLCapabilitiesSummary getGLCapabilitiesSummary() {
        return glCapabilitiesSummary;
    }
}
