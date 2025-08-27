package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.GLRegion;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.plane.AffineTransform;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.math.Vec4f;
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
    private GLRegion labelsRegion;

    // Temporary variables:
    private final AffineTransform tmp1 = new AffineTransform();
    private final AffineTransform tmp2 = new AffineTransform();

    private final AffineTransform textTransform = new AffineTransform();
    private final AffineTransform textScaleTransform = new AffineTransform();

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

        // Create a reusable region for all labels. If you need per-label colors,
        // use (Region.VBAA_RENDERING_BIT | Region.COLORCHANNEL_RENDERING_BIT).

        labelsRegion = GLRegion.create(gl.getGLProfile(),
                Region.VBAA_RENDERING_BIT, /* colorTexSeq */ null);

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

        regionRenderer.reshapeNotify(0, 0, engine.getWidth(), engine.getHeight());
        final PMVMatrix p = regionRenderer.getMatrix();

        // Projection
        p.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        p.glLoadIdentity();
        p.glLoadMatrixf(engine.getProjectionMatrix().get(new float[16]), 0);

        // View
        p.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        p.glLoadIdentity();
        p.glLoadMatrixf(engine.getViewMatrix().get(new float[16]), 0);

        gl.glEnableVertexAttribArray(0);
        regionRenderer.enable(gl, true);

        // --------- BATCH BUILD THE REGION -----------
        labelsRegion.clear(gl);

        // 1) Pre-count total vertices/indices to avoid multiple grows
        int[] totalCounts = new int[2];
        for (Node node : nodesCallback.getNodes()) {
            final String text = node.getLabel();
            if (text == null || text.isEmpty()) continue;
            TextRegionUtil.countStringRegion(font, text, totalCounts);
        }
        labelsRegion.setBufferCapacity(totalCounts[0], totalCounts[1]); // one allocation

        // 2) Append each label with its own transform
        final float textScale = 10f;
        textScaleTransform.setToScale(textScale, textScale);

        for (Node node : nodesCallback.getNodes()) {
            final String text = node.getLabel();
            if (text == null || text.isEmpty()) continue;

            // transform: translate to (x,y) and scale once
            textTransform.setToTranslation(node.x(), node.y());
            textTransform.concatenate(textScaleTransform);

            // add shapes for this string into the single region (no extra grow)
            TextRegionUtil.addStringToRegion(
                    false,               // preGrowRegion (we already did a single setBufferCapacity)
                    labelsRegion,        // sink
                    font,
                    textTransform,
                    text,
                    new Vec4f(0, 1f, 1, 1),                // null -> uses RegionRenderer static color
                    tmp1, tmp2
            );
        }

        // 3) Render all labels with a single draw
        // If the region has no color channel, RegionRenderer's static color is used.
        labelsRegion.draw(gl, regionRenderer, sampleCount);

        regionRenderer.enable(gl, false);
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

