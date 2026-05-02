package org.gephi.viz.engine.jogl.pipeline.common;

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_TARGET_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SELFLOOP_NODE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SOURCE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_TARGET_SIZE_LOCATION;
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
import org.gephi.viz.engine.jogl.models.edgecircle.CommonEdgeCircleSelfLoop;
import org.gephi.viz.engine.jogl.models.edgecircle.EdgeCircleSelfLoopNoSelection;
import org.gephi.viz.engine.jogl.models.edgecircle.EdgeCircleSelfLoopSelectionSelected;
import org.gephi.viz.engine.jogl.models.edgecircle.EdgeCircleSelfLoopSelectionUnselected;
import org.gephi.viz.engine.jogl.models.edgeline.CommonEdgeLineModel;
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
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.Mesh;
import org.gephi.viz.engine.jogl.util.gl.GLBuffer;
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
    // NOTE: Why secondary buffers and VAOs?
    // Sadly, we cannot use glDrawArraysInstancedBaseInstance in MacOS and it will be never available

    protected GLBuffer vertexGLBufferUndirected;
    protected GLBuffer vertexGLBufferDirected;
    protected GLBuffer attributesGLBufferDirected;
    protected GLBuffer attributesGLBufferDirectedSecondary;
    protected GLBuffer attributesGLBufferUndirected;
    protected GLBuffer attributesGLBufferUndirectedSecondary;


    final public static int ATTRIBS_STRIDE_SELFLOOP = CommonEdgeCircleSelfLoop.TOTAL_ATTRIBUTES_FLOATS;
    protected GLBuffer vertexGLBufferSelfLoop;
    protected GLBuffer attributesGLBufferSelfLoop;
    protected GLBuffer attributesGLBufferSelfLoopSecondary;

    protected final EdgesCallback edgesCallback;
    protected final NodesCallback nodesCallback;

    protected static final int ATTRIBS_STRIDE = CommonEdgeLineModel.TOTAL_ATTRIBUTES_FLOATS;


    protected static final int VERTEX_COUNT_MAX =
        Math.max(CommonEdgeLineDirected.VERTEX_COUNT, CommonEdgeLineUndirected.VERTEX_COUNT);

    protected final boolean instanced;
    protected final boolean usesSecondaryBuffer;

    protected ManagedDirectBuffer attributesBuffer;
    protected ManagedDirectBuffer selfLoopAttributesBuffer;

    protected float[] attributesBufferBatch;
    protected static final int BATCH_EDGES_SIZE = 32768;
    protected static final int BATCH_SELFLOOP_EDGES_SIZE = 8192;

    protected float[] selfLoopAttributesBufferBatch;

    // States
    protected boolean hideNonSelected;
    protected boolean edgeSelectionColor;
    protected boolean edgeWeightEnabled;
    protected float edgeBothSelectionColor;
    protected float edgeOutSelectionColor;
    protected float edgeInSelectionColor;
    protected GraphRenderingOptions.EdgeColorMode edgeColorMode;

    public AbstractEdgeData(final EdgesCallback edgesCallback, final NodesCallback nodesCallback, boolean instanced,
                            boolean usesSecondaryBuffer) {
        this.startedTime = System.currentTimeMillis();
        this.edgesCallback = edgesCallback;
        this.nodesCallback = nodesCallback;
        this.instanced = instanced;
        this.usesSecondaryBuffer = usesSecondaryBuffer;
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

        initBuffers(gl);
    }

    protected void initBuffers(GL gl) {
        attributesBufferBatch = new float[ATTRIBS_STRIDE * BATCH_EDGES_SIZE];
        attributesBuffer = new ManagedDirectBuffer(GL_FLOAT, ATTRIBS_STRIDE * BATCH_EDGES_SIZE);

        selfLoopAttributesBufferBatch = new float[ATTRIBS_STRIDE_SELFLOOP * BATCH_SELFLOOP_EDGES_SIZE];
        selfLoopAttributesBuffer =
            new ManagedDirectBuffer(GL_FLOAT, ATTRIBS_STRIDE_SELFLOOP * BATCH_SELFLOOP_EDGES_SIZE);
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

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = selfLoopCounter.unselectedCountToDraw;

            edgeCircleSelfLoopSelectionUnselected.useProgram(
                gl,
                mvpFloats,
                backgroundColorFloats,
                lightenNonSelectedFactor,
                globalTime,
                selectedTime,
                edgeScale,
                minWeight,
                maxWeight,
                edgeRescaleMin,
                edgeRescaleMax,
                nodeScale
            );

            if (usesSecondaryBuffer) {
                setupSelfLoopVertexArrayAttributesSecondary(gl, data);
            } else {
                setupSelfLoopVertexArrayAttributes(gl, data);
            }
        } else {
            instanceCount = selfLoopCounter.selectedCountToDraw;

            if (someSelection) {
                if (data.isEdgeSelectionColor()) {
                    edgeCircleSelfLoopNoSelection.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight,
                        edgeRescaleMin,
                        edgeRescaleMax,
                        nodeScale
                    );
                } else {
                    edgeCircleSelfLoopSelectionSelected.useProgram(
                        gl,
                        mvpFloats,
                        backgroundColorFloats,
                        lightenNonSelectedFactor,
                        globalTime,
                        selectedTime,
                        edgeScale,
                        minWeight,
                        maxWeight,
                        edgeRescaleMin,
                        edgeRescaleMax,
                        nodeScale
                    );
                }
            } else {
                edgeCircleSelfLoopNoSelection.useProgram(
                    gl,
                    mvpFloats,
                    edgeScale,
                    minWeight,
                    maxWeight,
                    edgeRescaleMin,
                    edgeRescaleMax,
                    nodeScale
                );
            }
            setupSelfLoopVertexArrayAttributes(gl, data);
        }
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
        float lightenNonSelectedFactor = data.getLightenNonSelectedFactor();
        final float minWeight = data.getMinWeight();
        final float maxWeight = data.getMaxWeight();
        final float edgeRescaleMin = data.getEdgeRescaleMin();
        final float edgeRescaleMax = data.getEdgeRescaleMax();

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = undirectedInstanceCounter.unselectedCountToDraw;

            lineUndirectedModelSelectionUnselected.useProgram(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight,
                edgeRescaleMin,
                edgeRescaleMax,
                backgroundColorFloats,
                lightenNonSelectedFactor,
                nodeScale,
                globalTime,
                selectedTime
            );

            if (usesSecondaryBuffer) {
                setupUndirectedVertexArrayAttributesSecondary(gl, data);
            } else {
                setupUndirectedVertexArrayAttributes(gl, data);
            }
        } else {
            instanceCount = undirectedInstanceCounter.selectedCountToDraw;
            lineUndirectedModelNoSelection.useProgram(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight,
                edgeRescaleMin,
                edgeRescaleMax,
                nodeScale
            );

            if (someSelection) {
                if (data.isEdgeSelectionColor()) {
                    lineUndirectedModelNoSelection.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight,
                        edgeRescaleMin,
                        edgeRescaleMax,
                        nodeScale
                    );
                } else {
                    lineUndirectedModelSelectionSelected.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight,
                        edgeRescaleMin,
                        edgeRescaleMax,
                        nodeScale,
                        globalTime,
                        selectedTime
                    );
                }
            } else {
                lineUndirectedModelNoSelection.useProgram(
                    gl,
                    mvpFloats,
                    edgeScale,
                    minWeight,
                    maxWeight,
                    edgeRescaleMin,
                    edgeRescaleMax,
                    nodeScale
                );
            }

            setupUndirectedVertexArrayAttributes(gl, data);
        }

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
        float lightenNonSelectedFactor = data.getLightenNonSelectedFactor();
        final float minWeight = data.getMinWeight();
        final float maxWeight = data.getMaxWeight();
        final float edgeRescaleMin = data.getEdgeRescaleMin();
        final float edgeRescaleMax = data.getEdgeRescaleMax();

        final float edgeInset = Constants.getEdgeInset();

        final int instanceCount;
        if (renderingUnselectedEdges) {
            instanceCount = directedInstanceCounter.unselectedCountToDraw;
            lineDirectedModelSelectionUnselected.useProgram(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight,
                edgeRescaleMin,
                edgeRescaleMax,
                backgroundColorFloats,
                lightenNonSelectedFactor,
                nodeScale,
                edgeInset,
                globalTime,
                selectedTime
            );

            if (usesSecondaryBuffer) {
                setupDirectedVertexArrayAttributesSecondary(gl, data);
            } else {
                setupDirectedVertexArrayAttributes(gl, data);
            }
        } else {
            instanceCount = directedInstanceCounter.selectedCountToDraw;
            lineDirectedModelNoSelection.useProgram(
                gl,
                mvpFloats,
                edgeScale,
                minWeight,
                maxWeight,
                edgeRescaleMin,
                edgeRescaleMax,
                nodeScale,
                edgeInset
            );

            if (someSelection) {
                if (data.isEdgeSelectionColor()) {
                    lineDirectedModelNoSelection.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight,
                        edgeRescaleMin,
                        edgeRescaleMax,
                        nodeScale,
                        edgeInset

                    );
                } else {
                    lineDirectedModelSelectionSelected.useProgram(
                        gl,
                        mvpFloats,
                        edgeScale,
                        minWeight,
                        maxWeight,
                        edgeRescaleMin,
                        edgeRescaleMax,
                        nodeScale,
                        edgeInset,
                        globalTime,
                        selectedTime

                    );
                }
            } else {
                lineDirectedModelNoSelection.useProgram(
                    gl,
                    mvpFloats,
                    edgeScale,
                    minWeight,
                    maxWeight,
                    edgeRescaleMin,
                    edgeRescaleMax,
                    nodeScale,
                    edgeInset
                );
            }

            setupDirectedVertexArrayAttributes(gl, data);
        }

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
            model.getRenderingOptions().isLightenNonSelected() ? model.getRenderingOptions().getLightenNonSelectedFactor() : 0f,
            engine.getOpenGLOptions()
        );
    }

    protected abstract void updateData(GraphSelection selection);

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
        final float lightenNonSelectedFactor = renderingOptions.isLightenNonSelected() ? renderingOptions.getLightenNonSelectedFactor() : 0f;
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

    protected int updateDirectedData(
        final boolean isUndirected,
        final int maxIndex,
        final Edge[] visibleEdgesArray,
        final float[] edgeWeightsArray,
        final float[] attribs, int index
    ) {
        return updateDirectedData(isUndirected, maxIndex, visibleEdgesArray, edgeWeightsArray,
            attribs, index, null);
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
        final float[] attribs, int index
    ) {
        return updateUndirectedData(isDirected, maxIndex, visibleEdgesArray, edgeWeightsArray, attribs,
            index, null);
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


    protected void fillUndirectedEdgeAttributesDataBase(final float[] buffer, final Edge edge, final int index,
                                                        final float weight) {
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

        //Size (weight or constant):
        buffer[index + 4] = weight;
    }

    protected void fillUndirectedEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge,
                                                                    final int index, final float weight) {
        fillUndirectedEdgeAttributesDataBase(buffer, edge, index, weight);

        buffer[index + 5] = computeElementColor(edge);//Color

        //Source and target size:
        buffer[index + 6] = edge.getSource().size();
        buffer[index + 7] = edge.getTarget().size();
    }

    protected void fillUndirectedEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge, final int index,
                                                                 final boolean selected, final float weight) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        fillUndirectedEdgeAttributesDataBase(buffer, edge, index, weight);

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                boolean sourceSelected = nodesCallback.isSelected(source.getStoreId());
                boolean targetSelected = nodesCallback.isSelected(target.getStoreId());

                if (sourceSelected || targetSelected) {
                    buffer[index + 5] = edgeBothSelectionColor;//Color — undirected has no in/out
                } else {
                    buffer[index + 5] = computeElementColor(edge);//Color
                }
            } else {
                // When a node is selected, color the edge with the opposite node color
                if (someSelection) {
                    if (nodesCallback.isSelected(source.getStoreId())) {
                        buffer[index + 5] = Float.intBitsToFloat(target.getRGBA());
                    } else if (nodesCallback.isSelected(target.getStoreId())) {
                        buffer[index + 5] = Float.intBitsToFloat(source.getRGBA());
                    } else {
                        buffer[index + 5] = computeElementColor(edge);//Color
                    }
                } else {
                    buffer[index + 5] = computeElementColor(edge);//Color
                }
            }
        } else {
            buffer[index + 5] = computeElementColor(edge);//Color
        }

        //Source and target size:
        buffer[index + 6] = edge.getSource().size();
        buffer[index + 7] = edge.getTarget().size();
    }

    protected void fillDirectedEdgeAttributesDataBase(final float[] buffer, final Edge edge, final int index,
                                                      final float weight) {
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

        //Size (weight or constant):
        buffer[index + 4] = weight;
    }

    protected void fillSelfLoopEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge,
                                                               final int index, final boolean selected,
                                                               final float weight) {
        final Node source = edge.getSource();

        // Self loop for the moment are just circle like nodes so let's try to have same buffer
        //

        final float sourceX = source.x();
        final float sourceY = source.y();

        //Position:
        buffer[index] = sourceX;
        buffer[index + 1] = sourceY;
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

        //Size (weight or constant):
        buffer[index + 3] = weight;

        //Source and target size, here it's used for an offset to be applied to the circle:
        // so that it's not right under the node.
        buffer[index + 4] = source.size();
    }

    protected void fillSelfLoopEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge,
                                                                  final int index, final float weight) {
        final Node source = edge.getSource();

        // Self loop for the moment are just circle like nodes so let's try to have same buffer
        //

        final float sourceX = source.x();
        final float sourceY = source.y();

        //Position:
        buffer[index] = sourceX;
        buffer[index + 1] = sourceY;
        //Color:
        buffer[index + 2] = computeElementColor(edge);

        //Size (weight or constant):
        buffer[index + 3] = weight;

        //Source and target size, here it's used for an offset to be applied to the circle:
        // so that it's not right under the node.
        buffer[index + 4] = source.size();
    }

    protected void fillDirectedEdgeAttributesDataWithoutSelection(final float[] buffer, final Edge edge,
                                                                  final int index, final float weight) {
        fillDirectedEdgeAttributesDataBase(buffer, edge, index, weight);

        //Color:
        buffer[index + 5] = computeElementColor(edge);//Color

        //Source and target size:
        buffer[index + 6] = edge.getSource().size();
        buffer[index + 7] = edge.getTarget().size();
    }

    protected void fillDirectedEdgeAttributesDataWithSelection(final float[] buffer, final Edge edge, final int index,
                                                               final boolean selected, final float weight) {
        final Node source = edge.getSource();
        final Node target = edge.getTarget();

        fillDirectedEdgeAttributesDataBase(buffer, edge, index, weight);

        //Color:
        if (selected) {
            if (someSelection && edgeSelectionColor) {
                boolean sourceSelected = nodesCallback.isSelected(source.getStoreId());
                boolean targetSelected = nodesCallback.isSelected(target.getStoreId());

                if (sourceSelected && targetSelected) {
                    buffer[index + 5] = edgeBothSelectionColor;//Color
                } else if (sourceSelected) {
                    buffer[index + 5] = edgeOutSelectionColor;//Color
                } else if (targetSelected) {
                    buffer[index + 5] = edgeInSelectionColor;//Color
                } else {
                    buffer[index + 5] = computeElementColor(edge);//Color
                }
            } else {
                // When a node is selected, color the edge with the opposite node color
                if (someSelection) {
                    if (nodesCallback.isSelected(source.getStoreId())) {
                        buffer[index + 5] = Float.intBitsToFloat(target.getRGBA());
                    } else if (nodesCallback.isSelected(target.getStoreId())) {
                        buffer[index + 5] = Float.intBitsToFloat(source.getRGBA());
                    } else {
                        buffer[index + 5] = computeElementColor(edge);//Color
                    }
                } else {
                    buffer[index + 5] = computeElementColor(edge);//Color
                }
            }
        } else {
            buffer[index + 5] = computeElementColor(edge);//Color
        }

        //Source and target size:
        buffer[index + 6] = source.size();
        buffer[index + 7] = target.size();
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
    private UndirectedEdgesVAO undirectedEdgesVAOSecondary;
    private DirectedEdgesVAO directedEdgesVAO;
    private DirectedEdgesVAO directedEdgesVAOSecondary;
    private SelfLoopEdgesVAO selfLoopEdgesVAO;
    private SelfLoopEdgesVAO selfLoopEdgesVAOSecondary;

    public void setupSelfLoopVertexArrayAttributes(GL2ES2 gl, EdgeWorldData data) {
        if (selfLoopEdgesVAO == null) {
            selfLoopEdgesVAO = new SelfLoopEdgesVAO(
                data.getOpenGLOptions(),
                attributesGLBufferSelfLoop
            );
        }

        selfLoopEdgesVAO.use(gl);
    }

    public void setupSelfLoopVertexArrayAttributesSecondary(GL2ES2 gl, EdgeWorldData data) {
        if (selfLoopEdgesVAOSecondary == null) {
            selfLoopEdgesVAOSecondary = new SelfLoopEdgesVAO(
                data.getOpenGLOptions(),
                attributesGLBufferSelfLoopSecondary
            );
        }

        selfLoopEdgesVAOSecondary.use(gl);
    }

    public void setupUndirectedVertexArrayAttributes(GL2ES2 gl, EdgeWorldData data) {
        if (undirectedEdgesVAO == null) {
            undirectedEdgesVAO = new UndirectedEdgesVAO(
                data.getOpenGLOptions(),
                attributesGLBufferUndirected
            );
        }

        undirectedEdgesVAO.use(gl);
    }

    public void setupUndirectedVertexArrayAttributesSecondary(GL2ES2 gl,
                                                              EdgeWorldData data) {
        if (undirectedEdgesVAOSecondary == null) {
            undirectedEdgesVAOSecondary = new UndirectedEdgesVAO(
                data.getOpenGLOptions(),
                attributesGLBufferUndirectedSecondary
            );
        }

        undirectedEdgesVAOSecondary.use(gl);
    }

    public void unsetupSelfLoopVertexArrayAttributes(GL2ES2 gl) {
        if (selfLoopEdgesVAO != null) {
            selfLoopEdgesVAO.stopUsing(gl);
        }

        if (selfLoopEdgesVAOSecondary != null) {
            selfLoopEdgesVAOSecondary.stopUsing(gl);
        }
    }

    public void unsetupUndirectedVertexArrayAttributes(GL2ES2 gl) {
        if (undirectedEdgesVAO != null) {
            undirectedEdgesVAO.stopUsing(gl);
        }

        if (undirectedEdgesVAOSecondary != null) {
            undirectedEdgesVAOSecondary.stopUsing(gl);
        }
    }

    public void setupDirectedVertexArrayAttributes(GL2ES2 gl, EdgeWorldData data) {
        if (directedEdgesVAO == null) {
            directedEdgesVAO = new DirectedEdgesVAO(
                data.getOpenGLOptions(),
                attributesGLBufferDirected
            );
        }

        directedEdgesVAO.use(gl);
    }

    public void setupDirectedVertexArrayAttributesSecondary(GL2ES2 gl,
                                                            EdgeWorldData data) {
        if (directedEdgesVAOSecondary == null) {
            directedEdgesVAOSecondary = new DirectedEdgesVAO(
                data.getOpenGLOptions(),
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

        if (attributesGLBufferDirected != null) {
            attributesGLBufferDirected.destroy(gl);
            attributesGLBufferDirected = null;
        }

        if (attributesGLBufferDirectedSecondary != null) {
            attributesGLBufferDirectedSecondary.destroy(gl);
            attributesGLBufferDirectedSecondary = null;
        }

        if (attributesGLBufferUndirected != null) {
            attributesGLBufferUndirected.destroy(gl);
            attributesGLBufferUndirected = null;
        }

        if (attributesGLBufferUndirectedSecondary != null) {
            attributesGLBufferUndirectedSecondary.destroy(gl);
            attributesGLBufferUndirectedSecondary = null;
        }
        if (attributesGLBufferSelfLoop != null) {
            attributesGLBufferSelfLoop.destroy(gl);
            attributesGLBufferSelfLoop = null;

        }
        if (attributesGLBufferSelfLoopSecondary != null) {
            attributesGLBufferSelfLoopSecondary.destroy(gl);
            attributesGLBufferSelfLoopSecondary = null;

        }

        if (attributesBuffer != null) {
            attributesBuffer.destroy();
            attributesBuffer = null;
        }

        if (selfLoopAttributesBuffer != null) {
            selfLoopAttributesBuffer.destroy();
            selfLoopAttributesBuffer = null;
        }

        // Destroy and reset VAOs to prevent reuse after re-init
        if (undirectedEdgesVAO != null) {
            undirectedEdgesVAO.destroy(gl.getGL2ES2());
            undirectedEdgesVAO = null;
        }

        if (undirectedEdgesVAOSecondary != null) {
            undirectedEdgesVAOSecondary.destroy(gl.getGL2ES2());
            undirectedEdgesVAOSecondary = null;
        }

        if (directedEdgesVAO != null) {
            directedEdgesVAO.destroy(gl.getGL2ES2());
            directedEdgesVAO = null;
        }

        if (directedEdgesVAOSecondary != null) {
            directedEdgesVAOSecondary.destroy(gl.getGL2ES2());
            directedEdgesVAOSecondary = null;
        }

        if (selfLoopEdgesVAO != null) {
            selfLoopEdgesVAO.destroy(gl.getGL2ES2());
            selfLoopEdgesVAO = null;
        }

        if (selfLoopEdgesVAOSecondary != null) {
            selfLoopEdgesVAOSecondary.destroy(gl.getGL2ES2());
            selfLoopEdgesVAOSecondary = null;
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

        private final GLBuffer attributesBuffer;

        public SelfLoopEdgesVAO(OpenGLOptions openGLOptions,
                                GLBuffer attributesBuffer) {
            super(openGLOptions);
            this.attributesBuffer = attributesBuffer;
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferSelfLoop.bind(gl);
            {
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonEdgeCircleSelfLoop.VERTEX_FLOATS, GL_FLOAT,
                    false,
                    0, 0);
            }
            vertexGLBufferSelfLoop.unbind(gl);

            this.attributesBuffer.bind(gl);
            {
                int stride = ATTRIBS_STRIDE_SELFLOOP * Float.BYTES;
                int offset = 0;
                gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, CommonEdgeCircleSelfLoop.POSITION_FLOATS,
                    GL_FLOAT, false,
                    stride, offset);
                offset += CommonEdgeCircleSelfLoop.POSITION_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_COLOR_LOCATION,
                    CommonEdgeCircleSelfLoop.COLOR_FLOATS * Float.BYTES,
                    GL_UNSIGNED_BYTE,
                    false, stride, offset);
                offset += CommonEdgeCircleSelfLoop.COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, CommonEdgeCircleSelfLoop.SIZE_FLOATS, GL_FLOAT,
                    false, stride,
                    offset);
                offset += CommonEdgeCircleSelfLoop.SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SELFLOOP_NODE_SIZE_LOCATION,
                    CommonEdgeCircleSelfLoop.NODE_SIZE_FLOATS, GL_FLOAT, false, stride, offset);


            }
            this.attributesBuffer.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION,
                SHADER_POSITION_LOCATION,
                SHADER_COLOR_LOCATION,
                SHADER_SIZE_LOCATION,
                SHADER_SELFLOOP_NODE_SIZE_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instanced) {
                return new int[] {
                    SHADER_POSITION_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_SIZE_LOCATION,
                    SHADER_SELFLOOP_NODE_SIZE_LOCATION

                };
            } else {
                return null;
            }
        }

    }

    private class UndirectedEdgesVAO extends GLVertexArrayObject {

        private final GLBuffer attributesBuffer;

        public UndirectedEdgesVAO(OpenGLOptions openGLOptions,
                                  GLBuffer attributesBuffer) {
            super(openGLOptions);
            this.attributesBuffer = attributesBuffer;
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferUndirected.bind(gl);
            {
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonEdgeLineUndirected.VERTEX_FLOATS, GL_FLOAT, false,
                    0, 0);
            }
            vertexGLBufferUndirected.unbind(gl);

            attributesBuffer.bind(gl);
            {
                final int stride = ATTRIBS_STRIDE * Float.BYTES;
                int offset = 0;
                gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, CommonEdgeLineUndirected.POSITION_SOURCE_FLOATS,
                    GL_FLOAT, false, stride, offset);
                offset += CommonEdgeLineUndirected.POSITION_SOURCE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_POSITION_TARGET_LOCATION,
                    CommonEdgeLineUndirected.POSITION_TARGET_FLOATS, GL_FLOAT, false, stride, offset);
                offset += CommonEdgeLineUndirected.POSITION_TARGET_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, CommonEdgeLineUndirected.SIZE_FLOATS, GL_FLOAT, false,
                    stride, offset);
                offset += CommonEdgeLineUndirected.SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_COLOR_LOCATION, CommonEdgeLineUndirected.COLOR_FLOATS * Float.BYTES,
                    GL_UNSIGNED_BYTE, false, stride, offset);
                offset += CommonEdgeLineUndirected.COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SOURCE_SIZE_LOCATION, CommonEdgeLineUndirected.SOURCE_SIZE_FLOATS,
                    GL_FLOAT, false, stride, offset);
                offset += CommonEdgeLineUndirected.SOURCE_SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_TARGET_SIZE_LOCATION, CommonEdgeLineUndirected.TARGET_SIZE_FLOATS,
                    GL_FLOAT, false, stride, offset);
            }
            attributesBuffer.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION,
                SHADER_POSITION_LOCATION,
                SHADER_POSITION_TARGET_LOCATION,
                SHADER_SIZE_LOCATION,
                SHADER_COLOR_LOCATION,
                SHADER_SOURCE_SIZE_LOCATION,
                SHADER_TARGET_SIZE_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instanced) {
                return new int[] {
                    SHADER_POSITION_LOCATION,
                    SHADER_POSITION_TARGET_LOCATION,
                    SHADER_SIZE_LOCATION,
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

        public DirectedEdgesVAO(OpenGLOptions openGLOptions,
                                GLBuffer attributesBuffer) {
            super(openGLOptions);
            this.attributesBuffer = attributesBuffer;
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexGLBufferDirected.bind(gl);
            {

                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonEdgeLineDirected.VERTEX_FLOATS, GL_FLOAT, false, 0,
                    0);
            }
            vertexGLBufferDirected.unbind(gl);

            attributesBuffer.bind(gl);
            {
                int stride = ATTRIBS_STRIDE * Float.BYTES;
                int offset = 0;
                gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, CommonEdgeLineDirected.POSITION_SOURCE_FLOATS,
                    GL_FLOAT, false, stride, offset);
                offset += CommonEdgeLineDirected.POSITION_SOURCE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_POSITION_TARGET_LOCATION, CommonEdgeLineDirected.POSITION_TARGET_FLOATS,
                    GL_FLOAT, false, stride, offset);
                offset += CommonEdgeLineDirected.POSITION_TARGET_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, CommonEdgeLineDirected.SIZE_FLOATS, GL_FLOAT, false,
                    stride, offset);
                offset += CommonEdgeLineDirected.SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_COLOR_LOCATION, CommonEdgeLineDirected.COLOR_FLOATS * Float.BYTES,
                    GL_UNSIGNED_BYTE, false, stride, offset);
                offset += CommonEdgeLineDirected.COLOR_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_SOURCE_SIZE_LOCATION, CommonEdgeLineDirected.SOURCE_SIZE_FLOATS,
                    GL_FLOAT, false, stride, offset);
                offset += CommonEdgeLineDirected.SOURCE_SIZE_FLOATS * Float.BYTES;

                gl.glVertexAttribPointer(SHADER_TARGET_SIZE_LOCATION, CommonEdgeLineDirected.TARGET_SIZE_FLOATS,
                    GL_FLOAT, false, stride, offset);
            }
            attributesBuffer.unbind(gl);
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            return new int[] {
                SHADER_VERT_LOCATION,
                SHADER_POSITION_LOCATION,
                SHADER_POSITION_TARGET_LOCATION,
                SHADER_SIZE_LOCATION,
                SHADER_COLOR_LOCATION,
                SHADER_SOURCE_SIZE_LOCATION,
                SHADER_TARGET_SIZE_LOCATION
            };
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instanced) {
                return new int[] {
                    SHADER_POSITION_LOCATION,
                    SHADER_POSITION_TARGET_LOCATION,
                    SHADER_SIZE_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_SOURCE_SIZE_LOCATION,
                    SHADER_TARGET_SIZE_LOCATION
                };
            } else {
                return null;
            }
        }
    }

    public EdgesCallback getEdgesCallback() {
        return edgesCallback;
    }
}
