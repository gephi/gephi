package org.gephi.viz.engine.jogl.pipeline.common;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.models.EdgeLineModelDirected;
import org.gephi.viz.engine.jogl.models.EdgeLineModelUndirected;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.pipeline.common.InstanceCounter;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import static org.gephi.viz.engine.util.gl.Constants.*;
import org.gephi.viz.engine.jogl.util.gl.GLBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.EdgesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class AbstractEdgeData {

    protected final EdgeLineModelUndirected lineModelUndirected = new EdgeLineModelUndirected();
    protected final EdgeLineModelDirected lineModelDirected = new EdgeLineModelDirected();

    protected final InstanceCounter undirectedInstanceCounter = new InstanceCounter();
    protected final InstanceCounter directedInstanceCounter = new InstanceCounter();

    // NOTE: Why secondary buffers and VAOs?
    // Sadly, we cannot use glDrawArraysInstancedBaseInstance in MacOS and it will be never available

    protected GLBuffer vertexGLBufferUndirected;
    protected GLBuffer vertexGLBufferDirected;
    protected GLBuffer attributesGLBufferDirected;
    protected GLBuffer attributesGLBufferDirectedSecondary;
    protected GLBuffer attributesGLBufferUndirected;
    protected GLBuffer attributesGLBufferUndirectedSecondary;

    protected final EdgesCallback edgesCallback = new EdgesCallback();

    protected static final int ATTRIBS_STRIDE = Math.max(
        EdgeLineModelUndirected.TOTAL_ATTRIBUTES_FLOATS,
        EdgeLineModelDirected.TOTAL_ATTRIBUTES_FLOATS
    );

    protected static final int VERTEX_COUNT_UNDIRECTED = EdgeLineModelUndirected.VERTEX_COUNT;
    protected static final int VERTEX_COUNT_DIRECTED = EdgeLineModelDirected.VERTEX_COUNT;
    protected static final int VERTEX_COUNT_MAX = Math.max(VERTEX_COUNT_DIRECTED, VERTEX_COUNT_UNDIRECTED);

    protected final boolean instanced;
    protected final boolean usesSecondaryBuffer;

    protected ManagedDirectBuffer attributesBuffer;

    protected float[] attributesBufferBatch;
    protected static final int BATCH_EDGES_SIZE = 32768;

    public AbstractEdgeData(boolean instanced, boolean usesSecondaryBuffer) {
        this.instanced = instanced;
        this.usesSecondaryBuffer = usesSecondaryBuffer;
    }

    public void init(GL2ES2 gl) {
        lineModelDirected.initGLPrograms(gl);
        lineModelUndirected.initGLPrograms(gl);
        initBuffers(gl);
    }

    protected void initBuffers(GL gl) {
        attributesBufferBatch = new float[ATTRIBS_STRIDE * BATCH_EDGES_SIZE];
        attributesBuffer = new ManagedDirectBuffer(GL_FLOAT, ATTRIBS_STRIDE * BATCH_EDGES_SIZE);
    }

    protected int setupShaderProgramForRenderingLayerUndirected(final GL2ES2 gl,
                                                                final RenderingLayer layer,
                                                                final VizEngine engine,
                                                                final float[] mvpFloats) {
        final boolean someSelection = engine.getLookup().lookup(GraphSelection.class).someNodesOrEdgesSelection();
        final boolean renderingUnselectedEdges = layer.isBack();
        if (!someSelection && renderingUnselectedEdges) {
            return 0;
        }

        final float[] backgroundColorFloats = engine.getBackgroundColor();

        final GraphRenderingOptions renderingOptions = engine.getLookup().lookup(GraphRenderingOptions.class);

        final float edgeScale = renderingOptions.getEdgeScale();
        float lightenNonSelectedFactor = renderingOptions.getLightenNonSelectedFactor();

        final GraphIndex graphIndex = engine.getLookup().lookup(GraphIndex.class);

        final float minWeight = graphIndex.getEdgesMinWeight();
        final float maxWeight = graphIndex.getEdgesMaxWeight();

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = undirectedInstanceCounter.unselectedCountToDraw;

            lineModelUndirected.useProgramWithSelectionUnselected(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight,
                backgroundColorFloats,
                lightenNonSelectedFactor
            );

            if (usesSecondaryBuffer) {
                setupUndirectedVertexArrayAttributesSecondary(gl, engine);
            } else {
                setupUndirectedVertexArrayAttributes(gl, engine);
            }
        } else {
            instanceCount = undirectedInstanceCounter.selectedCountToDraw;
            lineModelUndirected.useProgram(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight
            );

            if (someSelection) {
                if (edgeSelectionColor) {
                    lineModelUndirected.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight
                    );
                } else {
                    lineModelUndirected.useProgramWithSelectionSelected(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight
                    );
                }
            } else {
                lineModelUndirected.useProgram(
                    gl,
                    mvpFloats,
                    edgeScale,
                    minWeight,
                    maxWeight
                );
            }

            setupUndirectedVertexArrayAttributes(gl, engine);
        }

        return instanceCount;
    }

    protected int setupShaderProgramForRenderingLayerDirected(final GL2ES2 gl,
                                                              final RenderingLayer layer,
                                                              final VizEngine engine,
                                                              final float[] mvpFloats) {
        final boolean someSelection = engine.getLookup().lookup(GraphSelection.class).someNodesOrEdgesSelection();
        final boolean renderingUnselectedEdges = layer.isBack();
        if (!someSelection && renderingUnselectedEdges) {
            return 0;
        }

        final float[] backgroundColorFloats = engine.getBackgroundColor();

        final GraphRenderingOptions renderingOptions = engine.getLookup().lookup(GraphRenderingOptions.class);

        final float edgeScale = renderingOptions.getEdgeScale();
        float lightenNonSelectedFactor = renderingOptions.getLightenNonSelectedFactor();

        final GraphIndex graphIndex = engine.getLookup().lookup(GraphIndex.class);

        final float minWeight = graphIndex.getEdgesMinWeight();
        final float maxWeight = graphIndex.getEdgesMaxWeight();

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = directedInstanceCounter.unselectedCountToDraw;
            lineModelDirected.useProgramWithSelectionUnselected(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight,
                backgroundColorFloats,
                lightenNonSelectedFactor
            );

            if (usesSecondaryBuffer) {
                setupDirectedVertexArrayAttributesSecondary(gl, engine);
            } else {
                setupDirectedVertexArrayAttributes(gl, engine);
            }
        } else {
            instanceCount = directedInstanceCounter.selectedCountToDraw;
            lineModelDirected.useProgram(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight
            );

            if (someSelection) {
                if (someSelection && edgeSelectionColor) {
                    lineModelDirected.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight
                    );
                } else {
                    lineModelDirected.useProgramWithSelectionSelected(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight
                    );
                }
            } else {
                lineModelDirected.useProgram(
                    gl,
                    mvpFloats,
                    edgeScale,
                    minWeight,
                    maxWeight
                );
            }

            setupDirectedVertexArrayAttributes(gl, engine);
        }

        return instanceCount;
    }

    protected int updateDirectedData(
        final Graph graph,
        final boolean someSelection, final boolean hideNonSelected, final int visibleEdgesCount, final Edge[] visibleEdgesArray, final GraphSelection graphSelection, final boolean edgeSelectionColor, final float edgeBothSelectionColor, final float edgeOutSelectionColor, final float edgeInSelectionColor,
        final float[] attribs, int index
    ) {
        return updateDirectedData(graph, someSelection, hideNonSelected, visibleEdgesCount, visibleEdgesArray, graphSelection, edgeSelectionColor, edgeBothSelectionColor, edgeOutSelectionColor, edgeInSelectionColor, attribs, index, null);
    }

    protected int updateDirectedData(
        final Graph graph,
        final boolean someSelection, final boolean hideNonSelected, final int visibleEdgesCount, final Edge[] visibleEdgesArray, final GraphSelection graphSelection, final boolean edgeSelectionColor, final float edgeBothSelectionColor, final float edgeOutSelectionColor, final float edgeInSelectionColor,
        final float[] attribs, int index, final FloatBuffer directBuffer
    ) {
        checkBufferIndexing(directBuffer, attribs, index);

        if (graph.isUndirected()) {
            directedInstanceCounter.unselectedCount = 0;
            directedInstanceCounter.selectedCount = 0;
            return index;
        }

        saveSelectionState(this.someSelection, edgeSelectionColor, graphSelection, edgeBothSelectionColor, edgeOutSelectionColor, edgeInSelectionColor);

        int newEdgesCountUnselected = 0;
        int newEdgesCountSelected = 0;
        if (someSelection) {
            if (hideNonSelected) {
                for (int j = 0; j < visibleEdgesCount; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (!edge.isDirected()) {
                        continue;
                    }

                    final boolean selected = graphSelection.isEdgeSelected(edge);
                    if (!selected) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    fillDirectedEdgeAttributesDataWithSelection(attribs, edge, index, selected);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            } else {
                //First non-selected (bottom):
                for (int j = 0; j < visibleEdgesCount; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (!edge.isDirected()) {
                        continue;
                    }

                    if (graphSelection.isEdgeSelected(edge)) {
                        continue;
                    }

                    newEdgesCountUnselected++;

                    fillDirectedEdgeAttributesDataWithSelection(attribs, edge, index, false);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }

                //Then selected ones (up):
                for (int j = 0; j < visibleEdgesCount; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (!edge.isDirected()) {
                        continue;
                    }

                    if (!graphSelection.isEdgeSelected(edge)) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    fillDirectedEdgeAttributesDataWithSelection(attribs, edge, index, true);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            }
        } else {
            //Just all edges, no selection active:
            for (int j = 0; j < visibleEdgesCount; j++) {
                final Edge edge = visibleEdgesArray[j];
                if (!edge.isDirected()) {
                    continue;
                }

                newEdgesCountSelected++;

                fillDirectedEdgeAttributesDataWithoutSelection(attribs, edge, index);
                index += ATTRIBS_STRIDE;

                if (directBuffer != null && index == attribs.length) {
                    directBuffer.put(attribs, 0, attribs.length);
                    index = 0;
                }
            }
        }

        //Remaining:
        if (directBuffer != null && index > 0) {
            directBuffer.put(attribs, 0, index);
            index = 0;
        }

        directedInstanceCounter.unselectedCount = newEdgesCountUnselected;
        directedInstanceCounter.selectedCount = newEdgesCountSelected;

        return index;
    }

    protected int updateUndirectedData(
        final Graph graph,
        final boolean someSelection, final boolean hideNonSelected, final int visibleEdgesCount, final Edge[] visibleEdgesArray, final GraphSelection graphSelection, final boolean edgeSelectionColor, final float edgeBothSelectionColor, final float edgeOutSelectionColor, final float edgeInSelectionColor,
        final float[] attribs, int index
    ) {
        return updateUndirectedData(graph, someSelection, hideNonSelected, visibleEdgesCount, visibleEdgesArray, graphSelection, edgeSelectionColor, edgeBothSelectionColor, edgeOutSelectionColor, edgeInSelectionColor, attribs, index, null);
    }

    protected int updateUndirectedData(
        final Graph graph,
        final boolean someSelection, final boolean hideNonSelected, final int visibleEdgesCount, final Edge[] visibleEdgesArray, final GraphSelection graphSelection, final boolean edgeSelectionColor, final float edgeBothSelectionColor, final float edgeOutSelectionColor, final float edgeInSelectionColor,
        final float[] attribs, int index, final FloatBuffer directBuffer
    ) {
        checkBufferIndexing(directBuffer, attribs, index);

        if (graph.isDirected()) {
            undirectedInstanceCounter.unselectedCount = 0;
            undirectedInstanceCounter.selectedCount = 0;
            return index;
        }

        saveSelectionState(someSelection, edgeSelectionColor, graphSelection, edgeBothSelectionColor, edgeOutSelectionColor, edgeInSelectionColor);

        int newEdgesCountUnselected = 0;
        int newEdgesCountSelected = 0;
        //Undirected edges:
        if (someSelection) {
            if (hideNonSelected) {
                for (int j = 0; j < visibleEdgesCount; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge.isDirected()) {
                        continue;
                    }

                    if (!graphSelection.isEdgeSelected(edge)) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    fillUndirectedEdgeAttributesDataWithSelection(attribs, edge, index, true);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            } else {
                //First non-selected (bottom):
                for (int j = 0; j < visibleEdgesCount; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge.isDirected()) {
                        continue;
                    }

                    if (graphSelection.isEdgeSelected(edge)) {
                        continue;
                    }

                    newEdgesCountUnselected++;

                    fillUndirectedEdgeAttributesDataWithSelection(attribs, edge, index, false);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }

                //Then selected ones (up):
                for (int j = 0; j < visibleEdgesCount; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge.isDirected()) {
                        continue;
                    }

                    if (!graphSelection.isEdgeSelected(edge)) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    fillUndirectedEdgeAttributesDataWithSelection(attribs, edge, index, true);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            }
        } else {
            //Just all edges, no selection active:
            for (int j = 0; j < visibleEdgesCount; j++) {
                final Edge edge = visibleEdgesArray[j];
                if (edge.isDirected()) {
                    continue;
                }

                newEdgesCountSelected++;

                fillUndirectedEdgeAttributesDataWithoutSelection(attribs, edge, index);
                index += ATTRIBS_STRIDE;

                if (directBuffer != null && index == attribs.length) {
                    directBuffer.put(attribs, 0, attribs.length);
                    index = 0;
                }
            }
        }

        //Remaining:
        if (directBuffer != null && index > 0) {
            directBuffer.put(attribs, 0, index);
            index = 0;
        }

        undirectedInstanceCounter.unselectedCount = newEdgesCountUnselected;
        undirectedInstanceCounter.selectedCount = newEdgesCountSelected;

        return index;
    }

    private void checkBufferIndexing(final FloatBuffer directBuffer, final float[] attribs, final int index) {
        if (directBuffer != null) {
            if (attribs.length % ATTRIBS_STRIDE != 0) {
                throw new IllegalArgumentException("When filling a directBuffer, attribs buffer length should be a multiple of ATTRIBS_STRIDE = " + ATTRIBS_STRIDE);
            }

            if (index % ATTRIBS_STRIDE != 0) {
                throw new IllegalArgumentException("When filling a directBuffer, index should be a multiple of ATTRIBS_STRIDE = " + ATTRIBS_STRIDE);
            }
        }
    }

    private boolean someSelection;
    private boolean edgeSelectionColor;
    private GraphSelection graphSelection;
    private float edgeBothSelectionColor;
    private float edgeOutSelectionColor;
    private float edgeInSelectionColor;

    private void saveSelectionState(final boolean someSelection, final boolean edgeSelectionColor, final GraphSelection graphSelection, final float edgeBothSelectionColor, final float edgeOutSelectionColor, final float edgeInSelectionColor) {
        this.someSelection = someSelection;
        this.edgeSelectionColor = edgeSelectionColor;
        this.graphSelection = graphSelection;
        this.edgeBothSelectionColor = edgeBothSelectionColor;
        this.edgeOutSelectionColor = edgeOutSelectionColor;
        this.edgeInSelectionColor = edgeInSelectionColor;
    }

    protected void fillUndirectedEdgeAttributesDataBase(final float[] buffer, final Edge edge, final int index) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        final float sourceX = source.x();
        final float sourceY = source.y();
        final float targetX = target.x();
        final float targetY = target.y();

        //Position:
        buffer[index] = sourceX;
        buffer[index + 1] = sourceY;

        //Target position:
        buffer[index + 2] = targetX;
        buffer[index + 3] = targetY;

        //Size:
        buffer[index + 4] = (float) edge.getWeight();

        //Source color:
        buffer[index + 5] = Float.intBitsToFloat(source.getRGBA());

        //Target color:
        buffer[index + 6] = Float.intBitsToFloat(target.getRGBA());
    }

    protected void fillUndirectedEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge, final int index) {
        fillUndirectedEdgeAttributesDataBase(buffer, edge, index);

        buffer[index + 7] = Float.intBitsToFloat(edge.getRGBA());//Color

        //Source and target size:
        buffer[index + 8] = edge.getSource().size();
        buffer[index + 9] = edge.getTarget().size();
    }

    protected void fillUndirectedEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge, final int index, final boolean selected) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        fillUndirectedEdgeAttributesDataBase(buffer, edge, index);

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                boolean sourceSelected = graphSelection.isNodeSelected(source);
                boolean targetSelected = graphSelection.isNodeSelected(target);

                if (sourceSelected && targetSelected) {
                    buffer[index + 7] = edgeBothSelectionColor;//Color
                } else if (sourceSelected) {
                    buffer[index + 7] = edgeOutSelectionColor;//Color
                } else if (targetSelected) {
                    buffer[index + 7] = edgeInSelectionColor;//Color
                } else {
                    buffer[index + 7] = Float.intBitsToFloat(edge.getRGBA());//Color
                }
            } else {
                if (someSelection && edge.alpha() <= 0) {
                    if (graphSelection.isNodeSelected(source)) {
                        buffer[index + 7] = Float.intBitsToFloat(target.getRGBA());//Color
                    } else {
                        buffer[index + 7] = Float.intBitsToFloat(source.getRGBA());//Color
                    }
                } else {
                    buffer[index + 7] = Float.intBitsToFloat(edge.getRGBA());//Color
                }
            }
        } else {
            buffer[index + 7] = Float.intBitsToFloat(edge.getRGBA());//Color
        }

        //Source and target size:
        buffer[index + 8] = edge.getSource().size();
        buffer[index + 9] = edge.getTarget().size();
    }

    protected void fillDirectedEdgeAttributesDataBase(final float[] buffer, final Edge edge, final int index) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        final float sourceX = source.x();
        final float sourceY = source.y();
        final float targetX = target.x();
        final float targetY = target.y();

        //Position:
        buffer[index] = sourceX;
        buffer[index + 1] = sourceY;

        //Target position:
        buffer[index + 2] = targetX;
        buffer[index + 3] = targetY;

        //Size:
        buffer[index + 4] = (float) edge.getWeight();

        //Source color:
        buffer[index + 5] = Float.intBitsToFloat(source.getRGBA());
    }

    protected void fillDirectedEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge, final int index) {
        fillDirectedEdgeAttributesDataBase(buffer, edge, index);

        //Color:
        buffer[index + 6] = Float.intBitsToFloat(edge.getRGBA());//Color

        //Source and target size:
        buffer[index + 7] = edge.getSource().size();
        buffer[index + 8] = edge.getTarget().size();
    }

    protected void fillDirectedEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge, final int index, final boolean selected) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        fillDirectedEdgeAttributesDataBase(buffer, edge, index);

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                boolean sourceSelected = graphSelection.isNodeSelected(source);
                boolean targetSelected = graphSelection.isNodeSelected(target);

                if (sourceSelected && targetSelected) {
                    buffer[index + 6] = edgeBothSelectionColor;//Color
                } else if (sourceSelected) {
                    buffer[index + 6] = edgeOutSelectionColor;//Color
                } else if (targetSelected) {
                    buffer[index + 6] = edgeInSelectionColor;//Color
                } else {
                    buffer[index + 6] = Float.intBitsToFloat(edge.getRGBA());//Color
                }
            } else {
                if (someSelection && edge.alpha() <= 0) {
                    if (graphSelection.isNodeSelected(source)) {
                        buffer[index + 6] = Float.intBitsToFloat(target.getRGBA());//Color
                    } else {
                        buffer[index + 6] = Float.intBitsToFloat(source.getRGBA());//Color
                    }
                } else {
                    buffer[index + 6] = Float.intBitsToFloat(edge.getRGBA());//Color
                }
            }
        } else {
            buffer[index + 6] = Float.intBitsToFloat(edge.getRGBA());//Color
        }

        //Source and target size:
        buffer[index + 7] = source.size();
        buffer[index + 8] = target.size();
    }

    private UndirectedEdgesVAO undirectedEdgesVAO;
    private UndirectedEdgesVAO undirectedEdgesVAOSecondary;
    private DirectedEdgesVAO directedEdgesVAO;
    private DirectedEdgesVAO directedEdgesVAOSecondary;

    public void setupUndirectedVertexArrayAttributes(GL2ES2 gl, VizEngine engine) {
        if (undirectedEdgesVAO == null) {
            undirectedEdgesVAO = new UndirectedEdgesVAO(
                engine.getLookup().lookup(GLCapabilitiesSummary.class),
                engine.getLookup().lookup(OpenGLOptions.class),
                attributesGLBufferUndirected
            );
        }

        undirectedEdgesVAO.use(gl);
    }

    public void setupUndirectedVertexArrayAttributesSecondary(GL2ES2 gl, VizEngine engine) {
        if (undirectedEdgesVAOSecondary == null) {
            undirectedEdgesVAOSecondary = new UndirectedEdgesVAO(
                engine.getLookup().lookup(GLCapabilitiesSummary.class),
                engine.getLookup().lookup(OpenGLOptions.class),
                attributesGLBufferUndirectedSecondary
            );
        }

        undirectedEdgesVAOSecondary.use(gl);
    }

    public void unsetupUndirectedVertexArrayAttributes(GL2ES2 gl) {
        if (undirectedEdgesVAO != null) {
            undirectedEdgesVAO.stopUsing(gl);
        }

        if (undirectedEdgesVAOSecondary != null) {
            undirectedEdgesVAOSecondary.stopUsing(gl);
        }
    }

    public void setupDirectedVertexArrayAttributes(GL2ES2 gl, VizEngine engine) {
        if (directedEdgesVAO == null) {
            directedEdgesVAO = new DirectedEdgesVAO(
                engine.getLookup().lookup(GLCapabilitiesSummary.class),
                engine.getLookup().lookup(OpenGLOptions.class),
                attributesGLBufferDirected
            );
        }

        directedEdgesVAO.use(gl);
    }

    public void setupDirectedVertexArrayAttributesSecondary(GL2ES2 gl, VizEngine engine) {
        if (directedEdgesVAOSecondary == null) {
            directedEdgesVAOSecondary = new DirectedEdgesVAO(
                engine.getLookup().lookup(GLCapabilitiesSummary.class),
                engine.getLookup().lookup(OpenGLOptions.class),
                attributesGLBufferDirectedSecondary
            );
        }

        directedEdgesVAOSecondary.use(gl);
    }

    public void unsetupDirectedVertexArrayAttributes(GL2ES2 gl) {
        if (directedEdgesVAO != null) {
            directedEdgesVAO.stopUsing(gl);
        }

        if (directedEdgesVAOSecondary != null) {
            directedEdgesVAOSecondary.stopUsing(gl);
        }
    }

    public void dispose(GL gl) {
        if (vertexGLBufferUndirected != null) {
            vertexGLBufferUndirected.destroy(gl);
        }

        if (vertexGLBufferDirected != null) {
            vertexGLBufferDirected.destroy(gl);
        }

        if (attributesGLBufferDirected != null) {
            attributesGLBufferDirected.destroy(gl);
        }

        if (attributesGLBufferDirectedSecondary != null) {
            attributesGLBufferDirectedSecondary.destroy(gl);
        }

        if (attributesGLBufferUndirected != null) {
            attributesGLBufferUndirected.destroy(gl);
        }

        if (attributesGLBufferUndirectedSecondary != null) {
            attributesGLBufferUndirectedSecondary.destroy(gl);
        }

        edgesCallback.reset();
    }

    private class UndirectedEdgesVAO extends GLVertexArrayObject {

        private final GLBuffer attributesBuffer;

        public UndirectedEdgesVAO(GLCapabilitiesSummary capabilities, OpenGLOptions openGLOptions, GLBuffer attributesBuffer) {
            super(capabilities, openGLOptions);
            this.attributesBuffer = attributesBuffer;
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferUndirected.bind(gl);
            {
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, EdgeLineModelUndirected.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            }
            vertexGLBufferUndirected.unbind(gl);

            attributesBuffer.bind(gl);
            {
                int stride = ATTRIBS_STRIDE * Float.BYTES;
                int offset = 0;
                gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, EdgeLineModelUndirected.POSITION_SOURCE_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelUndirected.POSITION_SOURCE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_POSITION_TARGET_LOCATION, EdgeLineModelUndirected.POSITION_TARGET_LOCATION, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelUndirected.POSITION_TARGET_LOCATION * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, EdgeLineModelUndirected.SIZE_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelUndirected.SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SOURCE_COLOR_LOCATION, EdgeLineModelUndirected.SOURCE_COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                offset += EdgeLineModelUndirected.SOURCE_COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_TARGET_COLOR_LOCATION, EdgeLineModelUndirected.TARGET_COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                offset += EdgeLineModelUndirected.TARGET_COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_COLOR_LOCATION, EdgeLineModelUndirected.COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                offset += EdgeLineModelUndirected.COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SOURCE_SIZE_LOCATION, EdgeLineModelDirected.SOURCE_SIZE_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelDirected.SOURCE_SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_TARGET_SIZE_LOCATION, EdgeLineModelDirected.TARGET_SIZE_FLOATS, GL_FLOAT, false, stride, offset);
            }
            attributesBuffer.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[]{
                SHADER_VERT_LOCATION,
                SHADER_POSITION_LOCATION,
                SHADER_POSITION_TARGET_LOCATION,
                SHADER_SIZE_LOCATION,
                SHADER_SOURCE_COLOR_LOCATION,
                SHADER_TARGET_COLOR_LOCATION,
                SHADER_COLOR_LOCATION,
                SHADER_SOURCE_SIZE_LOCATION,
                SHADER_TARGET_SIZE_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instanced) {
                return new int[]{
                    SHADER_POSITION_LOCATION,
                    SHADER_POSITION_TARGET_LOCATION,
                    SHADER_SIZE_LOCATION,
                    SHADER_SOURCE_COLOR_LOCATION,
                    SHADER_TARGET_COLOR_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_SOURCE_SIZE_LOCATION,
                    SHADER_TARGET_SIZE_LOCATION
                };
            } else {
                return null;
            }
        }

    }

    private class DirectedEdgesVAO extends GLVertexArrayObject {

        private final GLBuffer attributesBuffer;

        public DirectedEdgesVAO(GLCapabilitiesSummary capabilities, OpenGLOptions openGLOptions, GLBuffer attributesBuffer) {
            super(capabilities, openGLOptions);
            this.attributesBuffer = attributesBuffer;
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferDirected.bind(gl);
            {
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, EdgeLineModelDirected.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            }
            vertexGLBufferDirected.unbind(gl);

            attributesBuffer.bind(gl);
            {
                int stride = ATTRIBS_STRIDE * Float.BYTES;
                int offset = 0;
                gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, EdgeLineModelDirected.POSITION_SOURCE_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelDirected.POSITION_SOURCE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_POSITION_TARGET_LOCATION, EdgeLineModelDirected.POSITION_TARGET_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelDirected.POSITION_TARGET_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, EdgeLineModelDirected.SIZE_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelDirected.SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SOURCE_COLOR_LOCATION, EdgeLineModelDirected.SOURCE_COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                offset += EdgeLineModelDirected.SOURCE_COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_COLOR_LOCATION, EdgeLineModelDirected.COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                offset += EdgeLineModelDirected.COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SOURCE_SIZE_LOCATION, EdgeLineModelDirected.SOURCE_SIZE_FLOATS, GL_FLOAT, false, stride, offset);
                offset += EdgeLineModelDirected.SOURCE_SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_TARGET_SIZE_LOCATION, EdgeLineModelDirected.TARGET_SIZE_FLOATS, GL_FLOAT, false, stride, offset);
            }
            attributesBuffer.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[]{
                SHADER_VERT_LOCATION,
                SHADER_POSITION_LOCATION,
                SHADER_POSITION_TARGET_LOCATION,
                SHADER_SIZE_LOCATION,
                SHADER_SOURCE_COLOR_LOCATION,
                SHADER_COLOR_LOCATION,
                SHADER_SOURCE_SIZE_LOCATION,
                SHADER_TARGET_SIZE_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instanced) {
                return new int[]{
                    SHADER_POSITION_LOCATION,
                    SHADER_POSITION_TARGET_LOCATION,
                    SHADER_SIZE_LOCATION,
                    SHADER_SOURCE_COLOR_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_SOURCE_SIZE_LOCATION,
                    SHADER_TARGET_SIZE_LOCATION
                };
            } else {
                return null;
            }
        }

    }
}
