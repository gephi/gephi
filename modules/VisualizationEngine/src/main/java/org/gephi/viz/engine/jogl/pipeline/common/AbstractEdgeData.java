package org.gephi.viz.engine.jogl.pipeline.common;

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static org.gephi.viz.engine.jogl.pipeline.common.NodeDataTextureStore.NODE_TEXTURE_UNIT;
import static org.gephi.viz.engine.util.gl.Constants.ELEMENT_TEXTURE_UNIT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.models.DataTextureModelSupport;
import org.gephi.viz.engine.jogl.models.edgecircle.CommonEdgeCircleSelfLoop;
import org.gephi.viz.engine.jogl.models.edgecircle.EdgeCircleSelfLoopNoSelection;
import org.gephi.viz.engine.jogl.models.edgecircle.EdgeCircleSelfLoopSelectionSelected;
import org.gephi.viz.engine.jogl.models.edgecircle.EdgeCircleSelfLoopSelectionUnselected;
import org.gephi.viz.engine.jogl.models.edgeline.directed.CommonEdgeLineDirected;
import org.gephi.viz.engine.jogl.models.edgeline.directed.EdgeLineDirectedModelNoSelection;
import org.gephi.viz.engine.jogl.models.edgeline.directed.EdgeLineDirectedModelSelectionSelected;
import org.gephi.viz.engine.jogl.models.edgeline.directed.EdgeLineDirectedModelSelectionUnselected;
import org.gephi.viz.engine.jogl.models.edgeline.undirected.CommonEdgeLineUndirected;
import org.gephi.viz.engine.jogl.models.edgeline.undirected.EdgeLineUndirectedModelNoSelection;
import org.gephi.viz.engine.jogl.models.edgeline.undirected.EdgeLineUndirectedModelSelectionSelected;
import org.gephi.viz.engine.jogl.models.edgeline.undirected.EdgeLineUndirectedModelSelectionUnselected;
import org.gephi.viz.engine.jogl.models.mesh.EdgeLineMeshGenerator;
import org.gephi.viz.engine.jogl.models.mesh.NodeDiskVertexMeshGenerator;
import org.gephi.viz.engine.jogl.util.Mesh;
import org.gephi.viz.engine.jogl.util.gl.GLBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLDataTexture;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.pipeline.common.InstanceCounter;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.EdgesCallback;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 * Base class for the texture-backed edge pipelines.
 * <p>
 * Per-edge data is stored in {@code RGBA32F} "element" textures (one per category: undirected,
 * directed, self-loop) and read by the vertex shaders via {@code texelFetch}. Each texture holds the
 * category's edges ordered as {@code [unselected | selected]}. The texel layout is:
 * <ul>
 *   <li>line edges: {@code (sourceStoreId, targetStoreId, weight, colorBits)}</li>
 *   <li>self-loops: {@code (nodeStoreId, weight, colorBits, _)}</li>
 * </ul>
 * Node positions/sizes are not duplicated in the edge texels; the shaders resolve them from the
 * shared {@link NodeDataTextureStore node data texture} using the stored store ids.
 * <p>
 * The per-draw element index comes from {@code gl_InstanceID} (instanced) or
 * {@code gl_VertexID / vertsPerElement} (array-draw), offset by {@code u_elementOffset} so a single
 * texture can be drawn in ranges ({@code [unselected]} / {@code [selected]}) and, for array-draw, in
 * vertex-buffer-sized batches.
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractEdgeData extends AbstractSelectionData {

    protected final EdgeLineUndirectedModelNoSelection lineUndirectedModelNoSelection =
        new EdgeLineUndirectedModelNoSelection();
    protected final EdgeLineUndirectedModelSelectionSelected lineUndirectedModelSelectionSelected =
        new EdgeLineUndirectedModelSelectionSelected();
    protected final EdgeLineUndirectedModelSelectionUnselected lineUndirectedModelSelectionUnselected =
        new EdgeLineUndirectedModelSelectionUnselected();

    protected final EdgeLineDirectedModelNoSelection lineDirectedModelNoSelection =
        new EdgeLineDirectedModelNoSelection();
    protected final EdgeLineDirectedModelSelectionSelected lineDirectedModelSelectionSelected =
        new EdgeLineDirectedModelSelectionSelected();
    protected final EdgeLineDirectedModelSelectionUnselected lineDirectedModelSelectionUnselected =
        new EdgeLineDirectedModelSelectionUnselected();

    protected final EdgeCircleSelfLoopNoSelection edgeCircleSelfLoopNoSelection = new EdgeCircleSelfLoopNoSelection();
    protected final EdgeCircleSelfLoopSelectionSelected edgeCircleSelfLoopSelectionSelected =
        new EdgeCircleSelfLoopSelectionSelected();
    protected final EdgeCircleSelfLoopSelectionUnselected edgeCircleSelfLoopSelectionUnselected =
        new EdgeCircleSelfLoopSelectionUnselected();

    protected final InstanceCounter undirectedInstanceCounter = new InstanceCounter();
    protected final InstanceCounter directedInstanceCounter = new InstanceCounter();
    protected final InstanceCounter selfLoopCounter = new InstanceCounter();

    protected final Mesh undirectedEdgeMesh = EdgeLineMeshGenerator.undirectedMeshGenerator();
    protected final Mesh directedEdgeMesh = EdgeLineMeshGenerator.directedMeshGenerator();
    protected final Mesh selfLoopMesh = NodeDiskVertexMeshGenerator.generateFilledCircle(48);

    // Geometry buffers (static, just the repeated mesh). Created by the subclasses.
    protected GLBuffer vertexGLBufferUndirected;
    protected GLBuffer vertexGLBufferDirected;
    protected GLBuffer vertexGLBufferSelfLoop;

    protected final EdgesCallback edgesCallback;
    protected final NodesCallback nodesCallback;

    // Shared node data texture (x, y, rawSize, colorBits) indexed by node store id; owned (filled,
    // uploaded and disposed) by the active node pipeline. Edge shaders sample it via texelFetch to
    // resolve their endpoints, so here we only bind it.
    protected final NodeDataTextureStore nodeDataTextureStore;

    // One RGBA texel per edge.
    public static final int ELEMENT_TEXEL_FLOATS = 4;
    protected static final int ATTRIBS_STRIDE = ELEMENT_TEXEL_FLOATS;
    public static final int ATTRIBS_STRIDE_SELFLOOP = ELEMENT_TEXEL_FLOATS;

    // Per-category element textures, each holding [unselected | selected].
    protected final GLDataTexture undirectedElementTexture = new GLDataTexture(ELEMENT_TEXEL_FLOATS);
    protected final GLDataTexture directedElementTexture = new GLDataTexture(ELEMENT_TEXEL_FLOATS);
    protected final GLDataTexture selfLoopElementTexture = new GLDataTexture(ELEMENT_TEXEL_FLOATS);

    protected static final int VERTEX_COUNT_MAX =
        Math.max(CommonEdgeLineDirected.VERTEX_COUNT, CommonEdgeLineUndirected.VERTEX_COUNT);

    protected final boolean instanced;

    // Small staging arrays used while filling the element textures' CPU buffers.
    protected float[] attributesBufferBatch;
    protected float[] selfLoopAttributesBufferBatch;
    protected static final int BATCH_EDGES_SIZE = 32768;
    protected static final int BATCH_SELFLOOP_EDGES_SIZE = 8192;

    // Program currently set up for the category/pass being drawn, so subclasses can adjust the
    // element offset uniform per batch.
    protected GLShaderProgram activeProgram;

    // States
    protected boolean hideNonSelected;
    protected boolean edgeSelectionColor;
    protected boolean edgeWeightEnabled;
    protected float edgeBothSelectionColor;
    protected float edgeOutSelectionColor;
    protected float edgeInSelectionColor;
    protected GraphRenderingOptions.EdgeColorMode edgeColorMode;

    public AbstractEdgeData(final EdgesCallback edgesCallback, final NodesCallback nodesCallback,
                            final NodeDataTextureStore nodeDataTextureStore, boolean instanced) {
        this.startedTime = System.currentTimeMillis();
        this.edgesCallback = edgesCallback;
        this.nodesCallback = nodesCallback;
        this.nodeDataTextureStore = nodeDataTextureStore;
        this.instanced = instanced;
    }

    public void init(GL2ES2 gl) {
        edgeCircleSelfLoopNoSelection.initGLProgram(gl);
        edgeCircleSelfLoopSelectionUnselected.initGLProgram(gl);
        edgeCircleSelfLoopSelectionSelected.initGLProgram(gl);

        lineDirectedModelNoSelection.initProgram(gl);
        lineDirectedModelSelectionSelected.initProgram(gl);
        lineDirectedModelSelectionUnselected.initProgram(gl);

        lineUndirectedModelNoSelection.initProgram(gl);
        lineUndirectedModelSelectionSelected.initProgram(gl);
        lineUndirectedModelSelectionUnselected.initProgram(gl);

        undirectedElementTexture.init(gl);
        directedElementTexture.init(gl);
        selfLoopElementTexture.init(gl);

        initBuffers(gl);
    }

    protected void initBuffers(GL gl) {
        attributesBufferBatch = new float[ATTRIBS_STRIDE * BATCH_EDGES_SIZE];
        selfLoopAttributesBufferBatch = new float[ATTRIBS_STRIDE_SELFLOOP * BATCH_SELFLOOP_EDGES_SIZE];
    }

    /**
     * Binds the shared node data texture and the given per-element edge texture to their texture
     * units (the sampler uniforms were set to those units after each program link).
     */
    private void bindDataTextures(final GL2ES2 gl, final GLDataTexture elementTexture) {
        nodeDataTextureStore.bind(gl, NODE_TEXTURE_UNIT);
        elementTexture.bind(gl, ELEMENT_TEXTURE_UNIT);
        gl.glActiveTexture(GL_TEXTURE0);
    }

    /**
     * Updates the element offset uniform for the active program (used by array-draw to draw the
     * element texture in vertex-buffer-sized batches).
     */
    protected void setElementOffset(final GL2ES2 gl, final int elementOffset) {
        if (activeProgram != null) {
            DataTextureModelSupport.setElementOffset(gl, activeProgram, elementOffset);
        }
    }

    protected int setupShaderProgramForRenderingLayerSelfLoop(
        final GL2ES2 gl,
        final RenderingLayer layer,
        final EdgeWorldData data,
        final float[] mvpFloats
    ) {
        final boolean someSelection = data.hasSomeSelection();
        final boolean renderingUnselectedEdges = layer.getLevel() == 1;
        if (!someSelection && renderingUnselectedEdges) {
            return 0;
        }

        final float[] backgroundColorFloats = data.getBackgroundColor();
        final float edgeScale = data.getEdgeScale();
        final float nodeScale = data.getNodeScale();
        final float lightenNonSelectedFactor = data.getLightenNonSelectedFactor();
        final float minWeight = data.getMinWeight();
        final float maxWeight = data.getMaxWeight();
        final float edgeRescaleMin = data.getEdgeRescaleMin();
        final float edgeRescaleMax = data.getEdgeRescaleMax();
        final int vertsPerElement = instanced ? 0 : selfLoopMesh.vertexCount;

        bindDataTextures(gl, selfLoopElementTexture);

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = selfLoopCounter.unselectedCountToDraw;

            edgeCircleSelfLoopSelectionUnselected.useProgram(
                gl, mvpFloats, backgroundColorFloats, lightenNonSelectedFactor, globalTime, selectedTime,
                edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale, vertsPerElement
            );
            activeProgram = edgeCircleSelfLoopSelectionUnselected.getProgram();
        } else {
            instanceCount = selfLoopCounter.selectedCountToDraw;

            if (someSelection && !data.isEdgeSelectionColor()) {
                edgeCircleSelfLoopSelectionSelected.useProgram(
                    gl, mvpFloats, backgroundColorFloats, lightenNonSelectedFactor, globalTime, selectedTime,
                    edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale, vertsPerElement
                );
                activeProgram = edgeCircleSelfLoopSelectionSelected.getProgram();
            } else {
                edgeCircleSelfLoopNoSelection.useProgram(
                    gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale,
                    vertsPerElement
                );
                activeProgram = edgeCircleSelfLoopNoSelection.getProgram();
            }
        }

        setupSelfLoopVertexArrayAttributes(gl, data);
        final int passBase = renderingUnselectedEdges ? 0 : selfLoopCounter.unselectedCountToDraw;
        DataTextureModelSupport.setElementOffset(gl, activeProgram, passBase);
        return instanceCount;
    }

    protected int setupShaderProgramForRenderingLayerUndirected(final GL2ES2 gl,
                                                                final RenderingLayer layer,
                                                                final EdgeWorldData data,
                                                                final float[] mvpFloats) {
        final boolean someSelection = data.hasSomeSelection();
        final boolean renderingUnselectedEdges = layer.getLevel() == 1;
        if (!someSelection && renderingUnselectedEdges) {
            return 0;
        }

        final float[] backgroundColorFloats = data.getBackgroundColor();
        final float edgeScale = data.getEdgeScale();
        final float nodeScale = data.getNodeScale() * (1f - Constants.getEdgeInset());
        final float lightenNonSelectedFactor = data.getLightenNonSelectedFactor();
        final float minWeight = data.getMinWeight();
        final float maxWeight = data.getMaxWeight();
        final float edgeRescaleMin = data.getEdgeRescaleMin();
        final float edgeRescaleMax = data.getEdgeRescaleMax();
        final int vertsPerElement = instanced ? 0 : CommonEdgeLineUndirected.VERTEX_COUNT;

        bindDataTextures(gl, undirectedElementTexture);

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = undirectedInstanceCounter.unselectedCountToDraw;

            lineUndirectedModelSelectionUnselected.useProgram(
                gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax,
                backgroundColorFloats, lightenNonSelectedFactor, nodeScale, globalTime, selectedTime, vertsPerElement
            );
            activeProgram = lineUndirectedModelSelectionUnselected.getProgram();
        } else {
            instanceCount = undirectedInstanceCounter.selectedCountToDraw;

            if (someSelection && !data.isEdgeSelectionColor()) {
                lineUndirectedModelSelectionSelected.useProgram(
                    gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale,
                    globalTime, selectedTime, vertsPerElement
                );
                activeProgram = lineUndirectedModelSelectionSelected.getProgram();
            } else {
                lineUndirectedModelNoSelection.useProgram(
                    gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale,
                    vertsPerElement
                );
                activeProgram = lineUndirectedModelNoSelection.getProgram();
            }
        }

        setupUndirectedVertexArrayAttributes(gl, data);
        final int passBase = renderingUnselectedEdges ? 0 : undirectedInstanceCounter.unselectedCountToDraw;
        DataTextureModelSupport.setElementOffset(gl, activeProgram, passBase);
        return instanceCount;
    }

    protected int setupShaderProgramForRenderingLayerDirected(final GL2ES2 gl,
                                                              final RenderingLayer layer,
                                                              final EdgeWorldData data,
                                                              final float[] mvpFloats) {
        final boolean someSelection = data.hasSomeSelection();
        final boolean renderingUnselectedEdges = layer.getLevel() == 1;
        if (!someSelection && renderingUnselectedEdges) {
            return 0;
        }

        final float[] backgroundColorFloats = data.getBackgroundColor();
        final float edgeScale = data.getEdgeScale();
        final float nodeScale = data.getNodeScale();
        final float lightenNonSelectedFactor = data.getLightenNonSelectedFactor();
        final float minWeight = data.getMinWeight();
        final float maxWeight = data.getMaxWeight();
        final float edgeRescaleMin = data.getEdgeRescaleMin();
        final float edgeRescaleMax = data.getEdgeRescaleMax();
        final float edgeInset = Constants.getEdgeInset();
        final int vertsPerElement = instanced ? 0 : CommonEdgeLineDirected.VERTEX_COUNT;

        bindDataTextures(gl, directedElementTexture);

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = directedInstanceCounter.unselectedCountToDraw;

            lineDirectedModelSelectionUnselected.useProgram(
                gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax,
                backgroundColorFloats, lightenNonSelectedFactor, nodeScale, edgeInset, globalTime, selectedTime,
                vertsPerElement
            );
            activeProgram = lineDirectedModelSelectionUnselected.getProgram();
        } else {
            instanceCount = directedInstanceCounter.selectedCountToDraw;

            if (someSelection && !data.isEdgeSelectionColor()) {
                lineDirectedModelSelectionSelected.useProgram(
                    gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale,
                    edgeInset, globalTime, selectedTime, vertsPerElement
                );
                activeProgram = lineDirectedModelSelectionSelected.getProgram();
            } else {
                lineDirectedModelNoSelection.useProgram(
                    gl, mvpFloats, edgeScale, minWeight, maxWeight, edgeRescaleMin, edgeRescaleMax, nodeScale,
                    edgeInset, vertsPerElement
                );
                activeProgram = lineDirectedModelNoSelection.getProgram();
            }
        }

        setupDirectedVertexArrayAttributes(gl, data);
        final int passBase = renderingUnselectedEdges ? 0 : directedInstanceCounter.unselectedCountToDraw;
        DataTextureModelSupport.setElementOffset(gl, activeProgram, passBase);
        return instanceCount;
    }

    public EdgeWorldData createWorldData(VizEngineModel model, VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        return new EdgeWorldData(
            model.getRenderingOptions().getBackgroundColor(),
            someSelection,
            edgeSelectionColor,
            edgeWeightEnabled ? edgesCallback.getMinWeight() : 0f,
            edgeWeightEnabled ? edgesCallback.getMaxWeight() : 1f,
            model.getRenderingOptions().isEdgeRescaleWeightEnabled() ? model.getRenderingOptions().getEdgeRescaleMin() :
                1f,
            model.getRenderingOptions().isEdgeRescaleWeightEnabled() ? model.getRenderingOptions().getEdgeRescaleMax() :
                1f,
            model.getRenderingOptions().getNodeScale(),
            model.getRenderingOptions().getEdgeScale(),
            model.getRenderingOptions().isLightenNonSelected() ?
                model.getRenderingOptions().getLightenNonSelectedFactor() : 0f,
            engine.getOpenGLOptions()
        );
    }

    public void update(GraphIndex graphIndex, GraphSelection selection, GraphRenderingOptions renderingOptions,
                       Rect2D viewBoundaries) {
        if (!renderingOptions.isShowEdges()) {
            undirectedInstanceCounter.clearCount();
            directedInstanceCounter.clearCount();
            selfLoopCounter.clearCount();
            return;
        }

        //Selection:
        this.someSelection = selection.someNodesOrEdgesSelection();
        final float lightenNonSelectedFactor =
            renderingOptions.isLightenNonSelected() ? renderingOptions.getLightenNonSelectedFactor() : 0f;
        final boolean hideNonSelectedFlag = renderingOptions.isHideNonSelectedEdges();
        // If hide-non-selected is enabled but there is no active selection, hide all edges
        if (!someSelection && hideNonSelectedFlag) {
            undirectedInstanceCounter.clearCount();
            directedInstanceCounter.clearCount();
            selfLoopCounter.clearCount();
            return;
        }
        // When there is a selection, hide unselected edges if the flag is on
        this.hideNonSelected = someSelection && (hideNonSelectedFlag || lightenNonSelectedFactor >= 1);
        this.edgeSelectionColor = renderingOptions.isEdgeSelectionColor();
        this.edgeColorMode = renderingOptions.getEdgeColorMode();
        this.edgeWeightEnabled = renderingOptions.isEdgeWeightEnabled();
        this.edgeBothSelectionColor =
            Float.intBitsToFloat(renderingOptions.getEdgeBothSelectionColor().getRGB());
        this.edgeInSelectionColor = Float.intBitsToFloat(renderingOptions.getEdgeInSelectionColor().getRGB());
        this.edgeOutSelectionColor = Float.intBitsToFloat(renderingOptions.getEdgeOutSelectionColor().getRGB());

        updateData(selection);
    }

    /**
     * Fills the element textures' CPU buffers (one per category, ordered {@code [unselected | selected]}),
     * which {@link #updateBuffers(GL)} later uploads to the GPU. Shared by all edge pipelines.
     */
    protected void updateData(final GraphSelection selection) {
        final int totalEdges = edgesCallback.getCount();
        final Edge[] visibleEdgesArray = edgesCallback.getEdgesArray();
        final float[] edgeWeightsArray = edgesCallback.getEdgeWeightsArray();
        final int maxIndex = edgesCallback.getMaxIndex();
        final boolean isDirected = edgesCallback.isDirected();
        final boolean isUndirected = edgesCallback.isUndirected();
        final boolean hasSelfLoop = edgesCallback.hasSelfLoop();

        final int reserve = Math.max(totalEdges, 1);

        if (hasSelfLoop) {
            final FloatBuffer selfLoopBuffer = selfLoopElementTexture.beginFill(reserve);
            updateSelfLoop(maxIndex, visibleEdgesArray, edgeWeightsArray, selfLoopAttributesBufferBatch, 0,
                selfLoopBuffer);
        } else {
            selfLoopCounter.clearCount();
        }

        final FloatBuffer undirectedBuffer = undirectedElementTexture.beginFill(reserve);
        updateUndirectedData(isDirected, maxIndex, visibleEdgesArray, edgeWeightsArray, attributesBufferBatch, 0,
            undirectedBuffer);

        final FloatBuffer directedBuffer = directedElementTexture.beginFill(reserve);
        updateDirectedData(isUndirected, maxIndex, visibleEdgesArray, edgeWeightsArray, attributesBufferBatch, 0,
            directedBuffer);
    }

    /**
     * Uploads the filled element textures to the GPU and promotes the per-category counts. Shared by
     * all edge pipelines. The shared node data texture is uploaded by the node pipeline.
     */
    public void updateBuffers(GL gl) {
        undirectedElementTexture.upload(gl,
            undirectedInstanceCounter.unselectedCount + undirectedInstanceCounter.selectedCount);
        directedElementTexture.upload(gl,
            directedInstanceCounter.unselectedCount + directedInstanceCounter.selectedCount);
        if (edgesCallback.hasSelfLoop()) {
            selfLoopElementTexture.upload(gl, selfLoopCounter.unselectedCount + selfLoopCounter.selectedCount);
        }

        undirectedInstanceCounter.promoteCountToDraw();
        directedInstanceCounter.promoteCountToDraw();
        selfLoopCounter.promoteCountToDraw();
    }

    protected int updateSelfLoop(final int maxIndex,
                                 final Edge[] visibleEdgesArray,
                                 final float[] edgeWeightsArray,
                                 final float[] attribs,
                                 int index,
                                 final FloatBuffer directBuffer) {

        int selfLoopEdgeIndex = 0;
        int unselectedSelfLoopEdgeIndex = 0;
        //Undirected edges:
        if (someSelection) {

            if (hideNonSelected) {
                for (int i = 0; i <= maxIndex; i++) {
                    Edge e = visibleEdgesArray[i];

                    // Discard if source and target node are not the same
                    if (e == null  // If edge is null
                        || e.getSource() != e.getTarget() // or is not self loop
                        || !edgesCallback.isSelected(i) // or is not selected
                    ) {
                        continue; // Filter out
                    }

                    selfLoopEdgeIndex++;
                    final float weight = edgeWeightEnabled ? edgeWeightsArray[i] : 1f;


                    fillSelfLoopEdgeAttributesDataWithSelection(attribs, e, index, true, weight);
                    index += ATTRIBS_STRIDE_SELFLOOP;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }


            } else {
                for (int i = 0; i <= maxIndex; i++) {
                    Edge e = visibleEdgesArray[i];

                    // Discard if source and target node are not the same
                    if (e == null  // If edge is null
                        || e.getSource() != e.getTarget() // or is not self loop
                        || edgesCallback.isSelected(i) // or is selected
                    ) {
                        continue; // Filter out
                    }

                    unselectedSelfLoopEdgeIndex++;
                    final float weight = edgeWeightEnabled ? edgeWeightsArray[i] : 1f;

                    fillSelfLoopEdgeAttributesDataWithSelection(attribs, e, index, false, weight);
                    index += ATTRIBS_STRIDE_SELFLOOP;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }

                }

                for (int i = 0; i <= maxIndex; i++) {
                    Edge e = visibleEdgesArray[i];

                    // Discard if source and target node are not the same
                    if (e == null  // If edge is null
                        || e.getSource() != e.getTarget() // or is not self loop
                        || !edgesCallback.isSelected(i) // or is not selected
                    ) {
                        continue; // Filter out
                    }

                    selfLoopEdgeIndex++;
                    final float weight = edgeWeightEnabled ? edgeWeightsArray[i] : 1f;


                    fillSelfLoopEdgeAttributesDataWithSelection(attribs, e, index, true, weight);
                    index += ATTRIBS_STRIDE_SELFLOOP;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            }
        } else {
            //Just all edges, no selection active:
            // Get Index of self loop edges

            for (int i = 0; i <= maxIndex; i++) {
                Edge e = visibleEdgesArray[i];

                // Discard if source and target node are not the same
                if (e == null || e.getSource() != e.getTarget()) {
                    continue;
                }

                selfLoopEdgeIndex++;
                final float weight = edgeWeightEnabled ? edgeWeightsArray[i] : 1f;


                fillSelfLoopEdgeAttributesDataWithoutSelection(attribs, e, index, weight);
                index += ATTRIBS_STRIDE_SELFLOOP;

                if (directBuffer != null && index == attribs.length) {
                    directBuffer.put(attribs, 0, attribs.length);
                    index = 0;
                }

            }
        }

        // Flush remaining data in batch buffer to directBuffer
        if (directBuffer != null && index > 0) {
            directBuffer.put(attribs, 0, index);
            index = 0;
        }

        selfLoopCounter.selectedCount = selfLoopEdgeIndex;
        selfLoopCounter.unselectedCount = unselectedSelfLoopEdgeIndex;

        return index;
    }

    protected int updateDirectedData(
        final boolean isUndirected,
        final int maxIndex,
        final Edge[] visibleEdgesArray,
        final float[] edgeWeightsArray,
        final float[] attribs, int index, final FloatBuffer directBuffer
    ) {
        checkBufferIndexing(directBuffer, attribs, index);

        if (isUndirected) {
            directedInstanceCounter.unselectedCount = 0;
            directedInstanceCounter.selectedCount = 0;
            return index;
        }

        int newEdgesCountUnselected = 0;
        int newEdgesCountSelected = 0;
        if (someSelection) {
            if (hideNonSelected) {
                for (int j = 0; j <= maxIndex; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge == null) {
                        continue;
                    }
                    if (edge.getSource() == edge.getTarget()) {
                        continue;
                    }
                    if (!edge.isDirected()) {
                        continue;
                    }

                    final boolean selected = edgesCallback.isSelected(j);
                    if (!selected) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                    fillDirectedEdgeAttributesDataWithSelection(attribs, edge, index, selected, weight);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            } else {
                //First non-selected (bottom):
                for (int j = 0; j <= maxIndex; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge == null) {
                        continue;
                    }
                    if (edge.getSource() == edge.getTarget()) {
                        continue;
                    }
                    if (!edge.isDirected()) {
                        continue;
                    }

                    if (edgesCallback.isSelected(j)) {
                        continue;
                    }

                    newEdgesCountUnselected++;

                    float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                    fillDirectedEdgeAttributesDataWithSelection(attribs, edge, index, false, weight);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }

                //Then selected ones (up):
                for (int j = 0; j <= maxIndex; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge == null) {
                        continue;
                    }
                    if (edge.getSource() == edge.getTarget()) {
                        continue;
                    }
                    if (!edge.isDirected()) {
                        continue;
                    }

                    if (!edgesCallback.isSelected(j)) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                    fillDirectedEdgeAttributesDataWithSelection(attribs, edge, index, true, weight);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            }
        } else {
            //Just all edges, no selection active:
            for (int j = 0; j <= maxIndex; j++) {
                final Edge edge = visibleEdgesArray[j];
                if (edge == null) {
                    continue;
                }
                if (edge.getSource() == edge.getTarget()) {
                    continue;
                }
                if (!edge.isDirected()) {
                    continue;
                }

                newEdgesCountSelected++;

                float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                fillDirectedEdgeAttributesDataWithoutSelection(attribs, edge, index, weight);
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
        final boolean isDirected,
        final int maxIndex,
        final Edge[] visibleEdgesArray,
        final float[] edgeWeightsArray,
        final float[] attribs, int index, final FloatBuffer directBuffer
    ) {
        checkBufferIndexing(directBuffer, attribs, index);

        if (isDirected) {
            undirectedInstanceCounter.unselectedCount = 0;
            undirectedInstanceCounter.selectedCount = 0;
            return index;
        }

        int newEdgesCountUnselected = 0;
        int newEdgesCountSelected = 0;
        //Undirected edges:
        if (someSelection) {
            if (hideNonSelected) {
                for (int j = 0; j <= maxIndex; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge == null) {
                        continue;
                    }
                    if (edge.getSource() == edge.getTarget()) {
                        continue;
                    }
                    if (edge.isDirected()) {
                        continue;
                    }

                    if (!edgesCallback.isSelected(j)) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                    fillUndirectedEdgeAttributesDataWithSelection(attribs, edge, index, true, weight);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            } else {
                //First non-selected (bottom):
                for (int j = 0; j <= maxIndex; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge == null) {
                        continue;
                    }
                    if (edge.getSource() == edge.getTarget()) {
                        continue;
                    }
                    if (edge.isDirected()) {
                        continue;
                    }

                    if (edgesCallback.isSelected(j)) {
                        continue;
                    }

                    newEdgesCountUnselected++;

                    float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                    fillUndirectedEdgeAttributesDataWithSelection(attribs, edge, index, false, weight);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }

                //Then selected ones (up):
                for (int j = 0; j <= maxIndex; j++) {
                    final Edge edge = visibleEdgesArray[j];
                    if (edge == null) {
                        continue;
                    }
                    if (edge.getSource() == edge.getTarget()) {
                        continue;
                    }
                    if (edge.isDirected()) {
                        continue;
                    }

                    if (!edgesCallback.isSelected(j)) {
                        continue;
                    }

                    newEdgesCountSelected++;

                    float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                    fillUndirectedEdgeAttributesDataWithSelection(attribs, edge, index, true, weight);
                    index += ATTRIBS_STRIDE;

                    if (directBuffer != null && index == attribs.length) {
                        directBuffer.put(attribs, 0, attribs.length);
                        index = 0;
                    }
                }
            }
        } else {
            //Just all edges, no selection active:
            for (int j = 0; j <= maxIndex; j++) {
                final Edge edge = visibleEdgesArray[j];
                if (edge == null) {
                    continue;
                }
                if (edge.getSource() == edge.getTarget()) {
                    continue;
                }
                if (edge.isDirected()) {
                    continue;
                }

                newEdgesCountSelected++;

                float weight = edgeWeightEnabled ? edgeWeightsArray[j] : 1f;
                fillUndirectedEdgeAttributesDataWithoutSelection(attribs, edge, index, weight);
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
                throw new IllegalArgumentException(
                    "When filling a directBuffer, attribs buffer length should be a multiple of ATTRIBS_STRIDE = " +
                        ATTRIBS_STRIDE);
            }

            if (index % ATTRIBS_STRIDE != 0) {
                throw new IllegalArgumentException(
                    "When filling a directBuffer, index should be a multiple of ATTRIBS_STRIDE = " + ATTRIBS_STRIDE);
            }
        }
    }

    //Line edge texel: (sourceStoreId, targetStoreId, weight, colorBits)
    protected void fillUndirectedEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge,
                                                                    final int index, final float weight) {
        buffer[index] = edge.getSource().getStoreId();
        buffer[index + 1] = edge.getTarget().getStoreId();
        buffer[index + 2] = weight;
        buffer[index + 3] = computeElementColor(edge);
    }

    protected void fillUndirectedEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge, final int index,
                                                                 final boolean selected, final float weight) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        buffer[index] = source.getStoreId();
        buffer[index + 1] = target.getStoreId();
        buffer[index + 2] = weight;

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                boolean sourceSelected = nodesCallback.isSelected(source.getStoreId());
                boolean targetSelected = nodesCallback.isSelected(target.getStoreId());

                if (sourceSelected || targetSelected) {
                    buffer[index + 3] = edgeBothSelectionColor;//Color — undirected has no in/out
                } else {
                    buffer[index + 3] = computeElementColor(edge);//Color
                }
            } else {
                // When a node is selected, color the edge with the opposite node color
                if (someSelection) {
                    if (nodesCallback.isSelected(source.getStoreId())) {
                        buffer[index + 3] = Float.intBitsToFloat(target.getRGBA());
                    } else if (nodesCallback.isSelected(target.getStoreId())) {
                        buffer[index + 3] = Float.intBitsToFloat(source.getRGBA());
                    } else {
                        buffer[index + 3] = computeElementColor(edge);//Color
                    }
                } else {
                    buffer[index + 3] = computeElementColor(edge);//Color
                }
            }
        } else {
            buffer[index + 3] = computeElementColor(edge);//Color
        }
    }

    //Self-loop texel: (nodeStoreId, weight, colorBits, _)
    protected void fillSelfLoopEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge,
                                                               final int index, final boolean selected,
                                                               final float weight) {
        final Node source = edge.getSource();

        buffer[index] = source.getStoreId();
        buffer[index + 1] = weight;

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                // Self-loop: source == target, so always "both" selection color
                if (nodesCallback.isSelected(source.getStoreId())) {
                    buffer[index + 2] = edgeBothSelectionColor;
                } else {
                    buffer[index + 2] = computeElementColor(edge);
                }
            } else if (someSelection && nodesCallback.isSelected(source.getStoreId())) {
                // Color by node (source == target for self-loops)
                buffer[index + 2] = Float.intBitsToFloat(source.getRGBA());
            } else {
                buffer[index + 2] = computeElementColor(edge);
            }
        } else {
            buffer[index + 2] = computeElementColor(edge);
        }

        buffer[index + 3] = 0f;//Padding
    }

    protected void fillSelfLoopEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge,
                                                                  final int index, final float weight) {
        final Node source = edge.getSource();

        buffer[index] = source.getStoreId();
        buffer[index + 1] = weight;
        buffer[index + 2] = computeElementColor(edge);
        buffer[index + 3] = 0f;//Padding
    }

    protected void fillDirectedEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge,
                                                                  final int index, final float weight) {
        buffer[index] = edge.getSource().getStoreId();
        buffer[index + 1] = edge.getTarget().getStoreId();
        buffer[index + 2] = weight;
        buffer[index + 3] = computeElementColor(edge);
    }

    protected void fillDirectedEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge, final int index,
                                                               final boolean selected, final float weight) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        buffer[index] = source.getStoreId();
        buffer[index + 1] = target.getStoreId();
        buffer[index + 2] = weight;

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                boolean sourceSelected = nodesCallback.isSelected(source.getStoreId());
                boolean targetSelected = nodesCallback.isSelected(target.getStoreId());

                if (sourceSelected && targetSelected) {
                    buffer[index + 3] = edgeBothSelectionColor;//Color
                } else if (sourceSelected) {
                    buffer[index + 3] = edgeOutSelectionColor;//Color
                } else if (targetSelected) {
                    buffer[index + 3] = edgeInSelectionColor;//Color
                } else {
                    buffer[index + 3] = computeElementColor(edge);//Color
                }
            } else {
                // When a node is selected, color the edge with the opposite node color
                if (someSelection) {
                    if (nodesCallback.isSelected(source.getStoreId())) {
                        buffer[index + 3] = Float.intBitsToFloat(target.getRGBA());
                    } else if (nodesCallback.isSelected(target.getStoreId())) {
                        buffer[index + 3] = Float.intBitsToFloat(source.getRGBA());
                    } else {
                        buffer[index + 3] = computeElementColor(edge);//Color
                    }
                } else {
                    buffer[index + 3] = computeElementColor(edge);//Color
                }
            }
        } else {
            buffer[index + 3] = computeElementColor(edge);//Color
        }
    }

    private float computeElementColor(final Edge edge) {
        final int colorInt;
        switch (edgeColorMode) {
            case SOURCE: {
                colorInt = edge.getSource().getRGBA();
                break;
            }
            case TARGET: {
                colorInt = edge.getTarget().getRGBA();
                break;
            }
            case MIXED: {
                final int s = edge.getSource().getRGBA();
                final int t = edge.getTarget().getRGBA();
                if (s == t) {
                    colorInt = s;
                    break;
                }
                final int b0 = ((s) & 0xFF) + ((t) & 0xFF);
                final int b1 = ((s >>> 8) & 0xFF) + ((t >>> 8) & 0xFF);
                final int b2 = ((s >>> 16) & 0xFF) + ((t >>> 16) & 0xFF);
                final int b3 = ((s >>> 24) & 0xFF) + ((t >>> 24) & 0xFF);
                colorInt = ((b3 >>> 1) << 24) | ((b2 >>> 1) << 16) | ((b1 >>> 1) << 8) | (b0 >>> 1);
                break;
            }
            case SELF:
            default: {
                colorInt = edge.getRGBA();
                break;
            }
        }
        return Float.intBitsToFloat(colorInt);
    }

    private UndirectedEdgesVAO undirectedEdgesVAO;
    private DirectedEdgesVAO directedEdgesVAO;
    private SelfLoopEdgesVAO selfLoopEdgesVAO;

    public void setupSelfLoopVertexArrayAttributes(GL2ES2 gl, EdgeWorldData data) {
        if (selfLoopEdgesVAO == null) {
            selfLoopEdgesVAO = new SelfLoopEdgesVAO(data.getOpenGLOptions());
        }

        selfLoopEdgesVAO.use(gl);
    }

    public void setupUndirectedVertexArrayAttributes(GL2ES2 gl, EdgeWorldData data) {
        if (undirectedEdgesVAO == null) {
            undirectedEdgesVAO = new UndirectedEdgesVAO(data.getOpenGLOptions());
        }

        undirectedEdgesVAO.use(gl);
    }

    public void setupDirectedVertexArrayAttributes(GL2ES2 gl, EdgeWorldData data) {
        if (directedEdgesVAO == null) {
            directedEdgesVAO = new DirectedEdgesVAO(data.getOpenGLOptions());
        }

        directedEdgesVAO.use(gl);
    }

    public void unsetupSelfLoopVertexArrayAttributes(GL2ES2 gl) {
        if (selfLoopEdgesVAO != null) {
            selfLoopEdgesVAO.stopUsing(gl);
        }
    }

    public void unsetupUndirectedVertexArrayAttributes(GL2ES2 gl) {
        if (undirectedEdgesVAO != null) {
            undirectedEdgesVAO.stopUsing(gl);
        }
    }

    public void unsetupDirectedVertexArrayAttributes(GL2ES2 gl) {
        if (directedEdgesVAO != null) {
            directedEdgesVAO.stopUsing(gl);
        }
    }

    public void dispose(GL gl) {
        attributesBufferBatch = null;
        selfLoopAttributesBufferBatch = null;

        if (vertexGLBufferUndirected != null) {
            vertexGLBufferUndirected.destroy(gl);
            vertexGLBufferUndirected = null;
        }

        if (vertexGLBufferDirected != null) {
            vertexGLBufferDirected.destroy(gl);
            vertexGLBufferDirected = null;
        }

        if (vertexGLBufferSelfLoop != null) {
            vertexGLBufferSelfLoop.destroy(gl);
            vertexGLBufferSelfLoop = null;
        }

        // Per-element edge textures are owned by this pipeline (the node texture is not).
        undirectedElementTexture.dispose(gl);
        directedElementTexture.dispose(gl);
        selfLoopElementTexture.dispose(gl);

        // Destroy and reset VAOs to prevent reuse after re-init
        if (undirectedEdgesVAO != null) {
            undirectedEdgesVAO.destroy(gl.getGL2ES2());
            undirectedEdgesVAO = null;
        }

        if (directedEdgesVAO != null) {
            directedEdgesVAO.destroy(gl.getGL2ES2());
            directedEdgesVAO = null;
        }

        if (selfLoopEdgesVAO != null) {
            selfLoopEdgesVAO.destroy(gl.getGL2ES2());
            selfLoopEdgesVAO = null;
        }

        // Destroy shader programs
        lineUndirectedModelSelectionSelected.destroy(gl.getGL2ES2());
        lineUndirectedModelSelectionUnselected.destroy(gl.getGL2ES2());
        lineUndirectedModelNoSelection.destroy(gl.getGL2ES2());

        lineDirectedModelNoSelection.destroy(gl.getGL2ES2());
        lineDirectedModelSelectionSelected.destroy(gl.getGL2ES2());
        lineDirectedModelSelectionUnselected.destroy(gl.getGL2ES2());

        edgeCircleSelfLoopNoSelection.destroy(gl.getGL2ES2());
        edgeCircleSelfLoopSelectionSelected.destroy(gl.getGL2ES2());
        edgeCircleSelfLoopSelectionUnselected.destroy(gl.getGL2ES2());

        edgesCallback.reset();
    }

    private class SelfLoopEdgesVAO extends GLVertexArrayObject {

        public SelfLoopEdgesVAO(OpenGLOptions openGLOptions) {
            super(openGLOptions);
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferSelfLoop.bind(gl);
            gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonEdgeCircleSelfLoop.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            vertexGLBufferSelfLoop.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            return null;
        }
    }

    private class UndirectedEdgesVAO extends GLVertexArrayObject {

        public UndirectedEdgesVAO(OpenGLOptions openGLOptions) {
            super(openGLOptions);
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferUndirected.bind(gl);
            gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonEdgeLineUndirected.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            vertexGLBufferUndirected.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            return null;
        }
    }

    private class DirectedEdgesVAO extends GLVertexArrayObject {

        public DirectedEdgesVAO(OpenGLOptions openGLOptions) {
            super(openGLOptions);
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferDirected.bind(gl);
            gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonEdgeLineDirected.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            vertexGLBufferDirected.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            return null;
        }
    }

    public EdgesCallback getEdgesCallback() {
        return edgesCallback;
    }
}
