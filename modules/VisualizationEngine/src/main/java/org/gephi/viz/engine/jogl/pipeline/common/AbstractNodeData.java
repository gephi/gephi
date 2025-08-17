package org.gephi.viz.engine.jogl.pipeline.common;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.GLBuffers;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.models.NodeDiskModel;
import org.gephi.viz.engine.jogl.models.NodeDiskVertexDataGenerator;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.pipeline.common.InstanceCounter;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndexImpl;

import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static org.gephi.viz.engine.jogl.util.gl.GLBufferMutable.GL_BUFFER_TYPE_ARRAY;
import static org.gephi.viz.engine.jogl.util.gl.GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW;
import static org.gephi.viz.engine.util.gl.Constants.*;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_INTS_COUNT;

import org.gephi.viz.engine.jogl.util.gl.GLBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractNodeData {

    protected static final float BORDER_SIZE = 0.16f;
    protected static final float INSIDE_CIRCLE_SIZE = 1 - BORDER_SIZE;

    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_64 = 128;
    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_32 = 16;
    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_16 = 2;

    // NOTE: Why secondary buffers and VAOs?
    // Sadly, we cannot use glDrawArraysInstancedBaseInstance in MacOS and it will be never available

    protected GLBuffer vertexGLBuffer;
    protected GLBuffer attributesGLBuffer;
    protected GLBuffer attributesGLBufferSecondary;
    protected GLBuffer commandsGLBuffer;
    protected final NodesCallback nodesCallback = new NodesCallback();

    protected static final int ATTRIBS_STRIDE = NodeDiskModel.TOTAL_ATTRIBUTES_FLOATS;

    protected final NodeDiskModel diskModel;

    private final NodeDiskVertexDataGenerator generator64;
    private final NodeDiskVertexDataGenerator generator32;
    private final NodeDiskVertexDataGenerator generator16;
    private final NodeDiskVertexDataGenerator generator8;

    protected final int circleVertexCount64;
    protected final int circleVertexCount32;
    protected final int circleVertexCount16;
    protected final int circleVertexCount8;
    protected final int firstVertex64;
    protected final int firstVertex32;
    protected final int firstVertex16;
    protected final int firstVertex8;
    protected final boolean instancedRendering;
    protected final boolean indirectCommands;

    // State:
    protected final InstanceCounter instanceCounter = new InstanceCounter();
    protected float maxNodeSize = 0;
    protected float maxNodeSizeToDraw = 0;

    // Buffers for vertex attributes:
    protected static final int BATCH_NODES_SIZE = 32768;
    protected ManagedDirectBuffer attributesBuffer;
    protected float[] attributesBufferBatch;
    protected ManagedDirectBuffer commandsBuffer;
    private int[] commandsBufferBatch;

    public AbstractNodeData(final boolean instancedRendering, final boolean indirectCommands) {
        this.instancedRendering = instancedRendering;
        this.indirectCommands = indirectCommands;

        diskModel = new NodeDiskModel();

        generator64 = new NodeDiskVertexDataGenerator(64);
        generator32 = new NodeDiskVertexDataGenerator(32);
        generator16 = new NodeDiskVertexDataGenerator(16);
        generator8 = new NodeDiskVertexDataGenerator(8);

        circleVertexCount64 = generator64.getVertexCount();
        circleVertexCount32 = generator32.getVertexCount();
        circleVertexCount16 = generator16.getVertexCount();
        circleVertexCount8 = generator8.getVertexCount();

        firstVertex64 = 0;
        firstVertex32 = generator64.getVertexCount();
        firstVertex16 = firstVertex32 + generator32.getVertexCount();
        firstVertex8 = firstVertex16 + generator16.getVertexCount();
    }

    public void init(GL2ES2 gl) {
        diskModel.initGLPrograms(gl);
        initBuffers(gl);
    }

    protected void initBuffers(GL gl) {
        attributesBufferBatch = new float[ATTRIBS_STRIDE * BATCH_NODES_SIZE];
        attributesBuffer = new ManagedDirectBuffer(GL_FLOAT, ATTRIBS_STRIDE * BATCH_NODES_SIZE);

        if (indirectCommands) {
            commandsBufferBatch = new int[INDIRECT_DRAW_COMMAND_INTS_COUNT * BATCH_NODES_SIZE];
            commandsBuffer = new ManagedDirectBuffer(GL_UNSIGNED_INT, INDIRECT_DRAW_COMMAND_INTS_COUNT * BATCH_NODES_SIZE);
        }
    }

    protected void initCirclesGLVertexBuffer(GL gl, final int bufferName) {
        final NodeDiskVertexDataGenerator generator64 = new NodeDiskVertexDataGenerator(64);
        final NodeDiskVertexDataGenerator generator32 = new NodeDiskVertexDataGenerator(32);
        final NodeDiskVertexDataGenerator generator16 = new NodeDiskVertexDataGenerator(16);
        final NodeDiskVertexDataGenerator generator8 = new NodeDiskVertexDataGenerator(8);

        final float[] circleVertexData = new float[
            generator64.getVertexData().length
                + generator32.getVertexData().length
                + generator16.getVertexData().length
                + generator8.getVertexData().length
            ];

        int offset = 0;
        System.arraycopy(generator64.getVertexData(), 0, circleVertexData, offset, generator64.getVertexData().length);
        offset += generator64.getVertexData().length;
        System.arraycopy(generator32.getVertexData(), 0, circleVertexData, offset, generator32.getVertexData().length);
        offset += generator32.getVertexData().length;
        System.arraycopy(generator16.getVertexData(), 0, circleVertexData, offset, generator16.getVertexData().length);
        offset += generator16.getVertexData().length;
        System.arraycopy(generator8.getVertexData(), 0, circleVertexData, offset, generator8.getVertexData().length);


        final FloatBuffer circleVertexBuffer = GLBuffers.newDirectFloatBuffer(circleVertexData);
        vertexGLBuffer = new GLBufferMutable(bufferName, GL_BUFFER_TYPE_ARRAY);
        vertexGLBuffer.bind(gl);
        vertexGLBuffer.init(gl, circleVertexBuffer, GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBuffer.unbind(gl);
    }

    protected int setupShaderProgramForRenderingLayer(final GL2ES2 gl,
                                                      final RenderingLayer layer,
                                                      final VizEngine engine,
                                                      final float[] mvpFloats,
                                                      final boolean isRenderingOutsideCircle) {
        final boolean someSelection = engine.getLookup().lookup(GraphSelection.class).someNodesOrEdgesSelection();
        final boolean renderingUnselectedNodes = layer.isBack();
        if (!someSelection && renderingUnselectedNodes) {
            return 0;
        }

        final float[] backgroundColorFloats = engine.getBackgroundColor();

        final int instanceCount;
        final float sizeMultiplier = isRenderingOutsideCircle ? 1f : INSIDE_CIRCLE_SIZE;

        if (renderingUnselectedNodes) {
            instanceCount = instanceCounter.unselectedCountToDraw;
            final float colorLightenFactor = engine.getLookup().lookup(GraphRenderingOptions.class).getLightenNonSelectedFactor();
            final float colorMultiplier = isRenderingOutsideCircle ? NODER_BORDER_DARKEN_FACTOR : 1f;
            diskModel.useProgramWithSelectionUnselected(
                gl,
                mvpFloats,
                sizeMultiplier,
                backgroundColorFloats,
                colorLightenFactor,
                colorMultiplier
            );

            setupSecondaryVertexArrayAttributes(gl, engine);
        } else {
            instanceCount = instanceCounter.selectedCountToDraw;

            if (someSelection) {
                final float colorMultiplier = isRenderingOutsideCircle ? NODER_BORDER_DARKEN_FACTOR : 1f;
                diskModel.useProgramWithSelectionSelected(
                    gl,
                    mvpFloats,
                    sizeMultiplier,
                    colorMultiplier
                );
            } else {
                final float colorMultiplier = isRenderingOutsideCircle ? NODER_BORDER_DARKEN_FACTOR : 1f;
                diskModel.useProgram(gl, mvpFloats, sizeMultiplier, colorMultiplier);
            }

            setupVertexArrayAttributes(gl, engine);
        }

        return instanceCount;
    }

    protected void updateData(final float zoom,
                              final GraphIndexImpl spatialIndex,
                              final GraphRenderingOptions renderingOptions,
                              final GraphSelection selection) {
        if (!renderingOptions.isShowNodes()) {
            instanceCounter.clearCount();
            return;
        }

        spatialIndex.indexNodes();

        //Selection:
        final boolean someSelection = selection.someNodesOrEdgesSelection();
        final float lightenNonSelectedFactor = renderingOptions.getLightenNonSelectedFactor();
        final boolean hideNonSelected = someSelection && (renderingOptions.isHideNonSelected() || lightenNonSelectedFactor >= 1);

        final int totalNodes = spatialIndex.getNodeCount();

        attributesBuffer.ensureCapacity(totalNodes * ATTRIBS_STRIDE);
        if (indirectCommands) {
            commandsBuffer.ensureCapacity(totalNodes * INDIRECT_DRAW_COMMAND_INTS_COUNT);
        }

        final FloatBuffer attribs = attributesBuffer.floatBuffer();
        final IntBuffer commands = indirectCommands ? commandsBuffer.intBuffer() : null;

        spatialIndex.getVisibleNodes(nodesCallback);

        final Node[] visibleNodesArray = nodesCallback.getNodesArray();
        final int visibleNodesCount = nodesCallback.getCount();

        int newNodesCountUnselected = 0;
        int newNodesCountSelected = 0;

        float newMaxNodeSize = 0;
        for (int j = 0; j < visibleNodesCount; j++) {
            final float size = visibleNodesArray[j].size();
            newMaxNodeSize = Math.max(size, newMaxNodeSize);
        }

        int attributesIndex = 0;
        int commandIndex = 0;
        int instanceId = 0;
        if (someSelection) {
            if (hideNonSelected) {
                for (int j = 0; j < visibleNodesCount; j++) {
                    final Node node = visibleNodesArray[j];

                    final boolean selected = selection.isNodeSelected(node);
                    if (!selected) {
                        continue;
                    }

                    newNodesCountSelected++;
                    fillNodeAttributesData(node, attributesIndex);
                    attributesIndex += ATTRIBS_STRIDE;

                    if (attributesIndex == attributesBufferBatch.length) {
                        attribs.put(attributesBufferBatch);
                        attributesIndex = 0;
                    }

                    if (indirectCommands) {
                        fillNodeCommandData(node, zoom, commandIndex, instanceId);
                        instanceId++;
                        commandIndex += INDIRECT_DRAW_COMMAND_INTS_COUNT;

                        if (commandIndex == commandsBufferBatch.length) {
                            commands.put(commandsBufferBatch);
                            commandIndex = 0;
                        }
                    }
                }
            } else {
                //First non-selected (bottom):
                for (int j = 0; j < visibleNodesCount; j++) {
                    final Node node = visibleNodesArray[j];

                    final boolean selected = selection.isNodeSelected(node);
                    if (selected) {
                        continue;
                    }

                    newNodesCountUnselected++;

                    fillNodeAttributesData(node, attributesIndex);
                    attributesIndex += ATTRIBS_STRIDE;

                    if (attributesIndex == attributesBufferBatch.length) {
                        attribs.put(attributesBufferBatch);
                        attributesIndex = 0;
                    }

                    if (indirectCommands) {
                        fillNodeCommandData(node, zoom, commandIndex, instanceId);
                        instanceId++;
                        commandIndex += INDIRECT_DRAW_COMMAND_INTS_COUNT;

                        if (commandIndex == commandsBufferBatch.length) {
                            commands.put(commandsBufferBatch);
                            commandIndex = 0;
                        }
                    }
                }

                instanceId = 0;//Reset instance id, since we draw elements in 2 separate attribute buffers (main/selected and secondary/unselected)
                //Then selected ones (up):
                for (int j = 0; j < visibleNodesCount; j++) {
                    final Node node = visibleNodesArray[j];

                    final boolean selected = selection.isNodeSelected(node);
                    if (!selected) {
                        continue;
                    }

                    newNodesCountSelected++;

                    fillNodeAttributesData(node, attributesIndex);
                    attributesIndex += ATTRIBS_STRIDE;

                    if (attributesIndex == attributesBufferBatch.length) {
                        attribs.put(attributesBufferBatch);
                        attributesIndex = 0;
                    }

                    if (indirectCommands) {
                        fillNodeCommandData(node, zoom, commandIndex, instanceId);
                        instanceId++;
                        commandIndex += INDIRECT_DRAW_COMMAND_INTS_COUNT;

                        if (commandIndex == commandsBufferBatch.length) {
                            commands.put(commandsBufferBatch);
                            commandIndex = 0;
                        }
                    }
                }
            }
        } else {
            //Just all nodes, no selection active:
            for (int j = 0; j < visibleNodesCount; j++) {
                final Node node = visibleNodesArray[j];

                newNodesCountSelected++;

                fillNodeAttributesData(node, attributesIndex);
                attributesIndex += ATTRIBS_STRIDE;

                if (attributesIndex == attributesBufferBatch.length) {
                    attribs.put(attributesBufferBatch);
                    attributesIndex = 0;
                }

                if (indirectCommands) {
                    fillNodeCommandData(node, zoom, commandIndex, instanceId);
                    instanceId++;
                    commandIndex += INDIRECT_DRAW_COMMAND_INTS_COUNT;

                    if (commandIndex == commandsBufferBatch.length) {
                        commands.put(commandsBufferBatch);
                        commandIndex = 0;
                    }
                }
            }
        }

        //Remaining:
        if (attributesIndex > 0) {
            attribs.put(attributesBufferBatch, 0, attributesIndex);
        }

        if (indirectCommands && commandIndex > 0) {
            commands.put(commandsBufferBatch, 0, commandIndex);
        }

        instanceCounter.unselectedCount = newNodesCountUnselected;
        instanceCounter.selectedCount = newNodesCountSelected;
        maxNodeSize = newMaxNodeSize;
    }

    protected void fillNodeAttributesData(final Node node, final int index) {
        final float x = node.x();
        final float y = node.y();
        final float size = node.size();
        final int rgba = node.getRGBA();

        //Position:
        attributesBufferBatch[index] = x;
        attributesBufferBatch[index + 1] = y;

        //Color:
        attributesBufferBatch[index + 2] = Float.intBitsToFloat(rgba);

        //Size:
        attributesBufferBatch[index + 3] = size;
    }

    protected void fillNodeCommandData(final Node node, final float zoom, final int index, final int instanceId) {
        //Indirect Draw:
        //Choose LOD:
        final float observedSize = node.size() * zoom;

        final int circleVertexCount;
        final int firstVertex;
        if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_64) {
            circleVertexCount = circleVertexCount64;
            firstVertex = firstVertex64;
        } else if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_32) {
            circleVertexCount = circleVertexCount32;
            firstVertex = firstVertex32;
        } else if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_16) {
            circleVertexCount = circleVertexCount16;
            firstVertex = firstVertex16;
        } else {
            circleVertexCount = circleVertexCount8;
            firstVertex = firstVertex8;
        }

        commandsBufferBatch[index] = circleVertexCount;//vertex count
        commandsBufferBatch[index + 1] = 1;//instance count
        commandsBufferBatch[index + 2] = firstVertex;//first vertex
        commandsBufferBatch[index + 3] = instanceId;//base instance
    }

    private NodesVAO nodesVAO;
    private NodesVAO nodesVAOSecondary;

    public void setupVertexArrayAttributes(GL2ES2 gl, VizEngine engine) {
        if (nodesVAO == null) {
            nodesVAO = new NodesVAO(
                engine.getLookup().lookup(GLCapabilitiesSummary.class),
                engine.getLookup().lookup(OpenGLOptions.class),
                vertexGLBuffer, attributesGLBuffer
            );
        }

        nodesVAO.use(gl);
    }

    public void setupSecondaryVertexArrayAttributes(GL2ES2 gl, VizEngine engine) {
        if (nodesVAOSecondary == null) {
            nodesVAOSecondary = new NodesVAO(
                engine.getLookup().lookup(GLCapabilitiesSummary.class),
                engine.getLookup().lookup(OpenGLOptions.class),
                vertexGLBuffer, attributesGLBufferSecondary
            );
        }

        nodesVAOSecondary.use(gl);
    }

    public void unsetupVertexArrayAttributes(GL2ES2 gl) {
        if (nodesVAO != null) {
            nodesVAO.stopUsing(gl);
        }

        if (nodesVAOSecondary != null) {
            nodesVAOSecondary.stopUsing(gl);
        }
    }

    public void dispose(GL gl) {
        attributesBufferBatch = null;
        commandsBufferBatch = null;
        if (attributesBuffer != null) {
            attributesBuffer.destroy();
            attributesBuffer = null;
        }

        if (vertexGLBuffer != null) {
            vertexGLBuffer.destroy(gl);
            vertexGLBuffer = null;
        }

        if (attributesGLBuffer != null) {
            attributesGLBuffer.destroy(gl);
            attributesGLBuffer = null;
        }

        if (attributesGLBufferSecondary != null) {
            attributesGLBufferSecondary.destroy(gl);
            attributesGLBufferSecondary = null;
        }
        if (commandsBuffer != null) {
            commandsBuffer.destroy();
            commandsBuffer = null;
        }

        nodesCallback.reset();
    }

    private class NodesVAO extends GLVertexArrayObject {

        private final GLBuffer vertexBuffer;
        private final GLBuffer attributesBuffer;

        public NodesVAO(GLCapabilitiesSummary capabilities, OpenGLOptions openGLOptions, final GLBuffer vertexBuffer, final GLBuffer attributesBuffer) {
            super(capabilities, openGLOptions);
            this.vertexBuffer = vertexBuffer;
            this.attributesBuffer = attributesBuffer;
        }

        @Override
        protected void configure(GL2ES2 gl) {
            vertexBuffer.bind(gl);
            {
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, NodeDiskModel.VERTEX_FLOATS, GL_FLOAT, false, 0, 0);
            }
            vertexBuffer.unbind(gl);

            if (instancedRendering) {
                attributesBuffer.bind(gl);
                {
                    final int stride = ATTRIBS_STRIDE * Float.BYTES;
                    int offset = 0;

                    gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, NodeDiskModel.POSITION_FLOATS, GL_FLOAT, false, stride, offset);
                    offset += NodeDiskModel.POSITION_FLOATS * Float.BYTES;

                    gl.glVertexAttribPointer(SHADER_COLOR_LOCATION, NodeDiskModel.COLOR_FLOATS * Float.BYTES, GL_UNSIGNED_BYTE, false, stride, offset);
                    offset += NodeDiskModel.COLOR_FLOATS * Float.BYTES;

                    gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, NodeDiskModel.SIZE_FLOATS, GL_FLOAT, false, stride, offset);
                }
                attributesBuffer.unbind(gl);
            }
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            if (instancedRendering) {
                return new int[]{
                    SHADER_VERT_LOCATION,
                    SHADER_POSITION_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_SIZE_LOCATION
                };
            } else {
                return new int[]{
                    SHADER_VERT_LOCATION
                };
            }
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instancedRendering) {
                return new int[]{
                    SHADER_POSITION_LOCATION,
                    SHADER_COLOR_LOCATION,
                    SHADER_SIZE_LOCATION
                };
            } else {
                return null;
            }
        }

    }
}
