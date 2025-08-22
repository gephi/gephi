package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.opengl.GL2ES2;
import java.io.IOException;
import java.util.EnumSet;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.util.gl.Constants;
import com.jogamp.graph.font.Font;

public class NodeLabelRenderer implements Renderer<JOGLRenderingTarget> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(
        RenderingLayer.FRONT1
    );

    TextRegionUtil textRegionUtil;
    RenderState renderState;
    RegionRenderer regionRenderer;

    Font font;
    static int fontSet = FontFactory.UBUNTU;
    static int fontFamily = 0; // default
    static int fontStyleBits = 0; // default
    final float fontSize = 10.0f;

    /* 2nd pass texture size antialias SampleCount
           4 is usually enough */
    private final int[] sampleCount = new int[] { 4 };

    /* variables used to update the PMVMatrix before rendering */
    private float xTranslate = -40f;
    private float yTranslate =  0f;
    private float zTranslate = -100f;
    private float angleRotate = 0f;


    @Override
    public void init(JOGLRenderingTarget target) {
        try {
            font = FontFactory.get(fontSet).get(fontFamily, fontStyleBits);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        regionRenderer = RegionRenderer.create(RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
//        regionRenderer.setHintBits(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);

        textRegionUtil = new TextRegionUtil(Region.VBAA_RENDERING_BIT);

        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        regionRenderer.init(target.getDrawable().getGL().getGL2ES2());
        regionRenderer.enable(gl, true);
    }

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {

    }

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {

    }

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return Constants.RENDERING_ORDER_LABELS;
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE_LABEL;
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Node Labels";
    }
}

