package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.PMVMatrix;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.gl.Constants;
import com.jogamp.graph.font.Font;
import org.gephi.viz.engine.util.structure.NodesCallback;

@SuppressWarnings("rawtypes")
public class NodeLabelRenderer implements Renderer<JOGLRenderingTarget> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(
        RenderingLayer.FRONT1
    );

    TextRegionUtil textRegionUtil;
    RegionRenderer regionRenderer;
    Font font;

    private final int[] sampleCount = new int[] { 4 };

    private final VizEngine engine;
    private final NodesCallback nodesCallback = new NodesCallback();

    public NodeLabelRenderer(VizEngine engine) {
        this.engine = engine;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        try {
            font = FontFactory.get(new File("../VisualizationEngine/fonts/Ubuntu-R.ttf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        regionRenderer = RegionRenderer.create(RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
        regionRenderer.setColorStatic(1f, 0f, 0f, 1.0f);
        textRegionUtil = new TextRegionUtil(Region.VBAA_RENDERING_BIT);

        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        regionRenderer.init(target.getDrawable().getGL().getGL2ES2());
        regionRenderer.setWeight(1.0f);
        
        // Disable global depth test to prevent depth buffer pollution
        // since the visualization engine uses render order for z-ordering
        regionRenderer.clearHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);
        
        regionRenderer.enable(gl, false);
    }

    @Override
    public void worldUpdated(JOGLRenderingTarget target) {
        GraphIndex graphIndex = engine.getGraphIndex();
        Rect2D viewBoundaries = engine.getViewBoundaries();

        graphIndex.getVisibleNodes(nodesCallback, viewBoundaries);
    }

    @Override
    public void render(JOGLRenderingTarget target, RenderingLayer layer) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        // Use engine's pre-computed matrices to minimize state manipulation
        regionRenderer.reshapeNotify(0, 0, engine.getWidth(), engine.getHeight());
        final PMVMatrix p = regionRenderer.getMatrix();
        
        // Set projection matrix directly from engine
        p.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        p.glLoadIdentity();
        float[] projMatrix = engine.getProjectionMatrix().get(new float[16]);
        p.glLoadMatrixf(projMatrix, 0);

        // Set modelview matrix directly from engine
        p.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        p.glLoadIdentity();
        float[] viewMatrix = engine.getViewMatrix().get(new float[16]);
        p.glLoadMatrixf(viewMatrix, 0);

        // Ensure vertex attribute 0 is enabled before RegionRenderer uses it
        // This is necessary when VAOs are not supported and other renderers
        // might have disabled it in the previous frame
        gl.glEnableVertexAttribArray(0);
        
        regionRenderer.enable(gl, true);

        // Draw labels
        for (Node node : nodesCallback.getNodes()) {
            float x = node.x();
            float y = node.y();
            p.glPushMatrix();
            p.glTranslatef(x, y, 0f);
            p.glScalef(10, 10, 1f);
            textRegionUtil.drawString3D(gl, regionRenderer, font, node.getLabel(), null, sampleCount);
            p.glPopMatrix();
        }

        regionRenderer.enable(gl, false);
        
        // Disable vertex attribute 0 after RegionRenderer is done
        // to maintain a clean state for other renderers
        gl.glDisableVertexAttribArray(0);
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

