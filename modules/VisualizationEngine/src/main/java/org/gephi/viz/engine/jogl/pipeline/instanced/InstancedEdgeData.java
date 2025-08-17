package org.gephi.viz.engine.jogl.pipeline.instanced;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.FloatBuffer;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.models.EdgeLineModelDirected;
import org.gephi.viz.engine.jogl.models.EdgeLineModelUndirected;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractEdgeData;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndexImpl;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;

/**
 *
 * @author Eduardo Ramos
 */
public class InstancedEdgeData extends AbstractEdgeData {

    private final int[] bufferName = new int[6];

    private static final int VERT_BUFFER_UNDIRECTED = 0;
    private static final int VERT_BUFFER_DIRECTED = 1;
    private static final int ATTRIBS_BUFFER_UNDIRECTED = 2;
    private static final int ATTRIBS_BUFFER_UNDIRECTED_SECONDARY = 3;
    private static final int ATTRIBS_BUFFER_DIRECTED = 4;
    private static final int ATTRIBS_BUFFER_DIRECTED_SECONDARY = 5;

    public InstancedEdgeData() {
        super(true, true);
    }

    public void update(VizEngine engine, GraphIndexImpl graphIndex) {
        updateData(
            graphIndex,
            engine.getLookup().lookup(GraphRenderingOptions.class),
            engine.getLookup().lookup(GraphSelection.class)
        );
    }

    public void drawInstanced(GL3ES3 gl, RenderingLayer layer, VizEngine engine, float[] mvpFloats) {
        drawUndirected(gl, engine, layer, mvpFloats);
        drawDirected(gl, engine, layer, mvpFloats);
    }

    private void drawUndirected(GL3ES3 gl, VizEngine engine, RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerUndirected(gl, layer, engine, mvpFloats);

        lineModelUndirected.drawInstanced(gl, instanceCount);
        lineModelUndirected.stopUsingProgram(gl);
        unsetupUndirectedVertexArrayAttributes(gl);
    }

    private void drawDirected(GL3ES3 gl, VizEngine engine, RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerDirected(gl, layer, engine, mvpFloats);

        lineModelDirected.drawInstanced(gl, instanceCount);
        lineModelDirected.stopUsingProgram(gl);
        unsetupDirectedVertexArrayAttributes(gl);
    }

    @Override
    protected void initBuffers(GL gl) {
        super.initBuffers(gl);
        gl.glGenBuffers(bufferName.length, bufferName, 0);

        final FloatBuffer undirectedVertexData = GLBuffers.newDirectFloatBuffer(EdgeLineModelUndirected.getVertexData());

        vertexGLBufferUndirected = new GLBufferMutable(bufferName[VERT_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBufferUndirected.bind(gl);
        vertexGLBufferUndirected.init(gl, undirectedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBufferUndirected.unbind(gl);

        final FloatBuffer directedVertexData = GLBuffers.newDirectFloatBuffer(EdgeLineModelDirected.getVertexData());
        vertexGLBufferDirected = new GLBufferMutable(bufferName[VERT_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        vertexGLBufferDirected.bind(gl);
        vertexGLBufferDirected.init(gl, directedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBufferDirected.unbind(gl);

        //Initialize for batch edges size:
        attributesGLBufferDirected = new GLBufferMutable(bufferName[ATTRIBS_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferDirected.bind(gl);
        attributesGLBufferDirected.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferDirected.unbind(gl);

        attributesGLBufferDirectedSecondary = new GLBufferMutable(bufferName[ATTRIBS_BUFFER_DIRECTED_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferDirectedSecondary.bind(gl);
        attributesGLBufferDirectedSecondary.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferDirectedSecondary.unbind(gl);

        attributesGLBufferUndirected = new GLBufferMutable(bufferName[ATTRIBS_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferUndirected.bind(gl);
        attributesGLBufferUndirected.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferUndirected.unbind(gl);

        attributesGLBufferUndirectedSecondary = new GLBufferMutable(bufferName[ATTRIBS_BUFFER_UNDIRECTED_SECONDARY], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferUndirectedSecondary.bind(gl);
        attributesGLBufferUndirectedSecondary.init(gl, ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE, GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferUndirectedSecondary.unbind(gl);
    }

    public void updateBuffers(GL gl) {
        final FloatBuffer buf = attributesBuffer.floatBuffer();

        buf.limit(undirectedInstanceCounter.unselectedCount * ATTRIBS_STRIDE);
        buf.position(0);

        attributesGLBufferUndirectedSecondary.bind(gl);
        attributesGLBufferUndirectedSecondary.updateWithOrphaning(gl, buf);
        attributesGLBufferUndirectedSecondary.unbind(gl);

        int offset = buf.limit();
        buf.limit(offset + undirectedInstanceCounter.selectedCount * ATTRIBS_STRIDE);
        buf.position(offset);

        attributesGLBufferUndirected.bind(gl);
        attributesGLBufferUndirected.updateWithOrphaning(gl, buf);
        attributesGLBufferUndirected.unbind(gl);

        offset = buf.limit();
        buf.limit(offset + directedInstanceCounter.unselectedCount * ATTRIBS_STRIDE);
        buf.position(offset);

        attributesGLBufferDirectedSecondary.bind(gl);
        attributesGLBufferDirectedSecondary.updateWithOrphaning(gl, buf);
        attributesGLBufferDirectedSecondary.unbind(gl);

        offset = buf.limit();
        buf.limit(offset + directedInstanceCounter.selectedCount * ATTRIBS_STRIDE);
        buf.position(offset);

        attributesGLBufferDirected.bind(gl);
        attributesGLBufferDirected.updateWithOrphaning(gl, buf);
        attributesGLBufferDirected.unbind(gl);

        undirectedInstanceCounter.promoteCountToDraw();
        directedInstanceCounter.promoteCountToDraw();
    }

    private void updateData(final GraphIndexImpl graphIndex, final GraphRenderingOptions renderingOptions, final GraphSelection graphSelection) {
        if (!renderingOptions.isShowEdges()) {
            undirectedInstanceCounter.clearCount();
            directedInstanceCounter.clearCount();
            return;
        }

        graphIndex.indexEdges();

        //Selection:
        final boolean someSelection = graphSelection.someNodesOrEdgesSelection();
        final float lightenNonSelectedFactor = renderingOptions.getLightenNonSelectedFactor();
        final boolean hideNonSelected = someSelection && (renderingOptions.isHideNonSelected() || lightenNonSelectedFactor >= 1);
        final boolean edgeSelectionColor = renderingOptions.isEdgeSelectionColor();
        final float edgeBothSelectionColor = Float.intBitsToFloat(renderingOptions.getEdgeBothSelectionColor().getRGB());
        final float edgeInSelectionColor = Float.intBitsToFloat(renderingOptions.getEdgeInSelectionColor().getRGB());
        final float edgeOutSelectionColor = Float.intBitsToFloat(renderingOptions.getEdgeOutSelectionColor().getRGB());

        final int totalEdges = graphIndex.getEdgeCount();

        attributesBuffer.ensureCapacity(totalEdges * ATTRIBS_STRIDE);

        final FloatBuffer attribsDirectBuffer = attributesBuffer.floatBuffer();

        graphIndex.getVisibleEdges(edgesCallback);

        final Edge[] visibleEdgesArray = edgesCallback.getEdgesArray();
        final int visibleEdgesCount = edgesCallback.getCount();

        final Graph graph = graphIndex.getVisibleGraph();

        updateUndirectedData(
            graph,
            someSelection, hideNonSelected, visibleEdgesCount, visibleEdgesArray, graphSelection, edgeSelectionColor, edgeBothSelectionColor, edgeOutSelectionColor, edgeInSelectionColor,
            attributesBufferBatch, 0, attribsDirectBuffer
        );
        updateDirectedData(
            graph,
            someSelection, hideNonSelected, visibleEdgesCount, visibleEdgesArray, graphSelection, edgeSelectionColor, edgeBothSelectionColor, edgeOutSelectionColor, edgeInSelectionColor,
            attributesBufferBatch, 0, attribsDirectBuffer
        );
    }

    @Override
    public void dispose(GL gl) {
        super.dispose(gl);
        attributesBufferBatch = null;

        if (attributesBuffer != null) {
            attributesBuffer.destroy();
            attributesBuffer = null;
        }
    }
}
