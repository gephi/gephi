package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.PMVMatrix;
import java.io.File;
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
    RegionRenderer regionRenderer;

    final float[] mvpFloats = new float[16];

    Font font;
    final int fontSet = FontFactory.UBUNTU;
    final int fontFamily = 0; // default
    final int fontStyleBits = 0; // default
    final float fontSize = 10.0f;

    /* 2nd pass texture size antialias SampleCount
           4 is usually enough */
    private final int[] sampleCount = new int[] { 4 };

    // NOOP: legacy fields removed


    @Override
    public void init(JOGLRenderingTarget target) {
        try {
            font = FontFactory.get(new File("/Users/mathieu.bastian/githome/gephi/gephi/modules/VisualizationEngine/fonts/Ubuntu-R.ttf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create region renderer with blending enabled (JOGL 2.5.0 API)
        regionRenderer = RegionRenderer.create(null, RegionRenderer.defaultBlendDisable);
        regionRenderer.setColorStatic(0f, 0f, 0f, 1.0f);
        textRegionUtil = new TextRegionUtil(Region.VBAA_RENDERING_BIT);

        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        regionRenderer.init(target.getDrawable().getGL().getGL2ES2());
        regionRenderer.setWeight(1.0f);
        regionRenderer.enable(gl, false);
    }

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {

    }

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        String text = "Hello World";

        final int width = target.getDrawable().getSurfaceWidth();
        final int height = target.getDrawable().getSurfaceHeight();

        // Ensure viewport matches drawable size (some drivers don't auto-update)
//        gl.glViewport(0, 0, width, height);

        // Setup orthographic projection matching the window size
        regionRenderer.reshapeOrtho(width, height, -1000f, 1000f);

        // the RegionRenderer PMVMatrix define where we want to render our shape
        final PMVMatrix pmv = regionRenderer.getMatrix();
        pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        pmv.glLoadIdentity();
        // Position text in screen space (pixels): bottom-left corner
        pmv.glTranslatef(20f, 20f, 0f);
        // Scale to a readable size
        pmv.glScalef(24.0f, 24.0f, 1f);
        // Matrices are consumed from RegionRenderer.getMatrix() on enable()

        // Temporarily disable depth test so text is always visible on top
//        final boolean depthWasEnabled = gl.glIsEnabled(GL.GL_DEPTH_TEST);
//        if (depthWasEnabled) {
//            gl.glDisable(GL.GL_DEPTH_TEST);
//        }

        // Ensure blending is enabled for text alpha
//        gl.glEnable(GL.GL_BLEND);
        regionRenderer.enable(gl, true);
        textRegionUtil.drawString3D(gl, regionRenderer, font, text, null, sampleCount);
        regionRenderer.enable(gl, false);
//        gl.glDisable(GL.GL_BLEND);

        // Restore depth testing state
//        if (depthWasEnabled) {
//            gl.glEnable(GL.GL_DEPTH_TEST);
//        }
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

