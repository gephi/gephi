package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.opengl.GL2ES2;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.pipeline.common.VoidWorldData;
import org.gephi.viz.engine.jogl.util.SkijaTextRenderer;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.structure.NodesCallback;

import java.util.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Node label renderer adapted to use the generic {@link SkijaTextRenderer}.
 * <p>
 * Rendering strategy is unchanged: labels are shaped by Skia, packed into a texture atlas and drawn batched per page.
 * This class now just computes label positions/sizes from the graph/engine and feeds the generic renderer.
 */
public class NodeLabelRenderer implements Renderer<JOGLRenderingTarget, VoidWorldData> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(RenderingLayer.FRONT1);

    private final VizEngine engine;
    private final NodesCallback nodesCallback = new NodesCallback();

    // TODO: choose font?
    private final SkijaTextRenderer textRenderer = new SkijaTextRenderer();

    public NodeLabelRenderer(VizEngine engine) {
        this.engine = engine;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();
        if (gl == null) {
            return;
        }
        textRenderer.init(gl);
    }

    @Override
    public VoidWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        final GraphIndex gi = engine.getGraphIndex();
        final Rect2D viewBoundaries = engine.getViewBoundaries();
        gi.getVisibleNodes(nodesCallback, viewBoundaries);

        return VoidWorldData.INSTANCE;
    }

    // Temp vectors:
    private final Vector3f tempNDC = new Vector3f();
    private final Vector2f screenCoordinates = new Vector2f();

    @Override
    public void render(VoidWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        final GraphSelection selection = engine.getGraphSelection();
        final boolean someSelection = selection.someNodesOrEdgesSelection();
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();

        final int viewportWidth = engine.getWidth();
        final int viewportHeight = engine.getHeight();
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }

        final Node[] nodes = nodesCallback.getNodesArray();
        final int count = nodesCallback.getCount();
        if (nodes == null || count <= 0) {
            return;
        }

        final float pixelsPerWorldUnit = engine.pixelsPerWorldUnitAt(0, 0, tempNDC);

        if (pixelsPerWorldUnit <= 0) {
            return;
        }

        final float labelScale = 1f; // Scale of labels. TODO: to be passed as parameter
        final boolean scaleProportionalToNodeSize = true; // TODO: to be passed as parameter

        // start a new frame for the text renderer (screen-space API)
        textRenderer.beginFrame(viewportWidth, viewportHeight);

        for (int i = 0; i < count; i++) {
            final Node n = nodes[i];
            if (n == null) {
                continue;
            }

            if (someSelection && !selection.isNodeOrNeighbourSelected(n)) {
                continue;
            }

            final String text = n.getLabel();
            if (text == null || text.isEmpty()) {
                continue;
            }

            final float x = n.x();
            final float y = n.y();

            engine.worldCoordinatesToScreenCoordinates(x, y, tempNDC, screenCoordinates);

            // node diameter in px (approx from world units)
            final float nodeDiameterPx = Math.max(1f, 2f * n.size() * pixelsPerWorldUnit);

            // label height in px
            final int labelHeight = scaleProportionalToNodeSize
                ? Math.round(nodeDiameterPx * labelScale)
                : Math.round(labelScale);

            if (labelHeight <= 0) {
                continue;
            }

            // per-node label color:
            final int argb = 0xFF000000; // TODO: choose color based on node, or fixed color, etc

            textRenderer.addText(
                text,
                screenCoordinates.x,
                screenCoordinates.y,
                labelHeight, argb,
                gl
            );
        }

        // Single batched draw call per atlas page
        textRenderer.draw(gl);
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
        return "Node Labels (Skija cached, atlas-batched)";
    }

    public void dispose(JOGLRenderingTarget target) {
        final GL2ES2 gl = target.getDrawable().getGL().getGL2ES2();
        if (gl == null) {
            return;
        }
        textRenderer.dispose(gl);
    }
}
