package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Element;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

public abstract class AbstractLabelUpdater<E extends Element> implements WorldUpdater<JOGLRenderingTarget, E> {

    private final VizEngine engine;
    protected final AbstractLabelData<E> labelData;
    protected boolean vaoSupported = false;
    protected boolean mipMapSupported = false;

    public AbstractLabelUpdater(VizEngine engine, AbstractLabelData<E> labelData) {
        this.engine = engine;
        this.labelData = labelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        final OpenGLOptions openGLOptions = engine.getOpenGLOptions();
        vaoSupported = openGLOptions.isVAOSupported();
        // Disable mipmap generation in intel GPUs. See https://github.com/gephi/gephi/issues/1494 (Some label characters fade away when zooming out)
        mipMapSupported = !openGLOptions.isVendorIntel();
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        labelData.dispose();
    }

    @Override
    public ElementsCallback<E> getElementsCallback() {
        return labelData.getElementsCallback();
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
