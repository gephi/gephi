package org.gephi.viz.engine.jogl.pipeline.common;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.GLBuffers;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.models.NodeDiskModel;
import org.gephi.viz.engine.jogl.models.mesh.NodeDiskVertexMeshGenerator;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.Mesh;
import org.gephi.viz.engine.jogl.util.gl.GLBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.pipeline.common.InstanceCounter;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static org.gephi.viz.engine.jogl.util.gl.GLBufferMutable.GL_BUFFER_TYPE_ARRAY;
import static org.gephi.viz.engine.jogl.util.gl.GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW;
import static org.gephi.viz.engine.util.gl.Constants.*;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_INTS_COUNT;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractNodeData extends AbstractSelectionData {

    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_64 = 128;
    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_32 = 16;
    protected static final int OBSERVED_SIZE_LOD_THRESHOLD_16 = 2;

    // NOTE: Why secondary buffers and VAOs?
    // Sadly, we cannot use glDrawArraysInstancedBaseInstance in MacOS and it will be never available

    protected GLBuffer vertexGLBuffer;
    protected GLBuffer attributesGLBuffer;
    protected GLBuffer attributesGLBufferSecondary;
    protected GLBuffer commandsGLBuffer;
    protected final NodesCallback nodesCallback;

    protected static final int ATTRIBS_STRIDE = NodeDiskModel.TOTAL_ATTRIBUTES_FLOATS;

    protected final NodeDiskModel diskModel;

    protected final Mesh circleMesh64 = NodeDiskVertexMeshGenerator.generateFilledCircle(64);
    protected final Mesh circleMesh32 = NodeDiskVertexMeshGenerator.generateFilledCircle(32);
    protected final Mesh circleMesh16 = NodeDiskVertexMeshGenerator.generateFilledCircle(16);
    protected final Mesh circleMesh8 = NodeDiskVertexMeshGenerator.generateFilledCircle(8);


    protected final int firstVertex64;
    protected final int firstVertex32;
    protected final int firstVertex16;
    protected final int firstVertex8;
    protected final boolean instancedRendering;
    protected final boolean indirectCommands;

    // States
    protected final InstanceCounter instanceCounter = new InstanceCounter();
    protected float maxNodeSize = 0;
    protected float currentNodeScale;
    protected float currentZoom;


    // Buffers for vertex attributes:
    protected static final int BATCH_NODES_SIZE = 32768;
    protected ManagedDirectBuffer attributesBuffer;
    protected float[] attributesBufferBatch;
    protected ManagedDirectBuffer commandsBuffer;
    private int[] commandsBufferBatch;

    public AbstractNodeData(final NodesCallback nodesCallback, final boolean instancedRendering,
                            final boolean indirectCommands) {
        this.startedTime = System.currentTimeMillis();
        this.instancedRendering = instancedRendering;
        this.indirectCommands = indirectCommands;
        this.nodesCallback = nodesCallback;

        diskModel = new NodeDiskModel();


        firstVertex64 = 0;
        firstVertex32 = circleMesh64.vertexCount;
        firstVertex16 = circleMesh64.vertexCount + circleMesh32.vertexCount;
        firstVertex8 = circleMesh64.vertexCount + circleMesh32.vertexCount + circleMesh16.vertexCount;
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
            commandsBuffer =
                    new ManagedDirectBuffer(GL_UNSIGNED_INT, INDIRECT_DRAW_COMMAND_INTS_COUNT * BATCH_NODES_SIZE);
        }
    }

    protected void initCirclesGLVertexBuffer(GL gl, final int bufferName) {

        final float[] circleVertexData = new float[
                circleMesh64.vertexData.length
                        + circleMesh32.vertexData.length
                        + circleMesh16.vertexData.length
                        + circleMesh8.vertexData.length
                ];

        int offset = 0;
        System.arraycopy(circleMesh64.vertexData, 0, circleVertexData, offset, circleMesh64.vertexData.length);
        offset += circleMesh64.vertexData.length;
        System.arraycopy(circleMesh32.vertexData, 0, circleVertexData, offset, circleMesh32.vertexData.length);
        offset += circleMesh32.vertexData.length;
        System.arraycopy(circleMesh16.vertexData, 0, circleVertexData, offset, circleMesh16.vertexData.length);
        offset += circleMesh16.vertexData.length;
        System.arraycopy(circleMesh8.vertexData, 0, circleVertexData, offset, circleMesh8.vertexData.length);


        final FloatBuffer circleVertexBuffer = GLBuffers.newDirectFloatBuffer(circleVertexData);
        vertexGLBuffer = new GLBufferMutable(bufferName, GL_BUFFER_TYPE_ARRAY);
        vertexGLBuffer.bind(gl);
        vertexGLBuffer.init(gl, circleVertexBuffer, GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBuffer.unbind(gl);
    }

    protected int setupShaderProgramForRenderingLayer(final GL2ES2 gl,
                                                      final RenderingLayer layer,
                                                      final NodeWorldData data,
                                                      final float[] mvpFloats) {
        final boolean someSelection = data.hasSomeSelection();
        final boolean renderingUnselectedNodes = layer.isBack();

        if (!someSelection && renderingUnselectedNodes) {
            return 0;
        }

        final float[] backgroundColorFloats = data.getBackgroundColor();

        final int instanceCount;
        // Compute background Luma to detect if the background is light or dark.
        double backgroundLuma = backgroundColorFloats[0] * .2126 + backgroundColorFloats[1] * .7152 + backgroundColorFloats[2] * .0722;
        // if the background is dark (luma <.5) the node border with lighten (color * (factor > 1)) otherwise it's darken (color * (factor < 1))
        float nodeBorderColorFactor = backgroundLuma < .5f ? 1f + Constants.getNodeBorderDarkenFactor() : Constants.getNodeBorderDarkenFactor();

        if (renderingUnselectedNodes) {
            instanceCount = instanceCounter.unselectedCountToDraw;
            final float colorLightenFactor = data.getLightenNonSelectedFactor();

            diskModel.useProgramWithSelectionUnselected(
                    gl,
                    mvpFloats,
                    backgroundColorFloats,
                    colorLightenFactor,
                    globalTime,
                    this.selectedTime,
                    nodeBorderColorFactor
            );

            setupSecondaryVertexArrayAttributes(gl, data);
        } else {
            instanceCount = instanceCounter.selectedCountToDraw;

            if (someSelection) {

                diskModel.useProgramWithSelectionSelected(
                        gl,
                        mvpFloats,
                        globalTime,
                        this.selectedTime,
                        nodeBorderColorFactor
                );
            } else {
                diskModel.useProgram(gl, mvpFloats, nodeBorderColorFactor);
            }

            setupVertexArrayAttributes(gl, data);
        }

        return instanceCount;
    }

    public NodeWorldData createWorldData(VizEngineModel model, VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        return new NodeWorldData(
                someSelection,
                model.getRenderingOptions().getBackgroundColor(),
                maxNodeSize,
                currentZoom,
                model.getRenderingOptions().getLightenNonSelectedFactor(),
                engine.getOpenGLOptions()
        );
    }

    public void update(GraphRenderingOptions renderingOptions) {
        if (!renderingOptions.isShowNodes()) {
            instanceCounter.clearCount();
            return;
        }

        //Selection and other states updates
        currentZoom = renderingOptions.getZoom();
        currentNodeScale = renderingOptions.getNodeScale();

        // Get visible nodes
        final Node[] visibleNodesArray = nodesCallback.getNodesArray();
        final int maxIndex = nodesCallback.getMaxIndex();
        final int totalNodes = nodesCallback.getCount();
        someSelection = nodesCallback.hasSelection();

        attributesBuffer.ensureCapacity(totalNodes * ATTRIBS_STRIDE);
        if (indirectCommands) {
            commandsBuffer.ensureCapacity(totalNodes * INDIRECT_DRAW_COMMAND_INTS_COUNT);
        }

        final FloatBuffer attribs = attributesBuffer.floatBuffer();
        final IntBuffer commands = indirectCommands ? commandsBuffer.intBuffer() : null;


        int newNodesCountUnselected = 0;
        int newNodesCountSelected = 0;

        float newMaxNodeSize = nodesCallback.getMaxNodeSize() * currentNodeScale;

        int attributesIndex = 0;
        int commandIndex = 0;
        int instanceId = 0;
        if (someSelection) {
            //First non-selected (bottom):
            for (int j = 0; j <= maxIndex; j++) {
                final Node node = visibleNodesArray[j];
                if (node == null) {
                    continue;
                }

                final boolean selected = nodesCallback.isSelected(j, true);
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
                    fillNodeCommandData(node, commandIndex, instanceId);
                    instanceId++;
                    commandIndex += INDIRECT_DRAW_COMMAND_INTS_COUNT;

                    if (commandIndex == commandsBufferBatch.length) {
                        commands.put(commandsBufferBatch);
                        commandIndex = 0;
                    }
                }
            }

            instanceId =
                    0;//Reset instance id, since we draw elements in 2 separate attribute buffers (main/selected and secondary/unselected)
            //Then selected ones (up):
            for (int j = 0; j <= maxIndex; j++) {
                final Node node = visibleNodesArray[j];
                if (node == null) {
                    continue;
                }

                final boolean selected = nodesCallback.isSelected(j, true);
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
                    fillNodeCommandData(node, commandIndex, instanceId);
                    instanceId++;
                    commandIndex += INDIRECT_DRAW_COMMAND_INTS_COUNT;

                    if (commandIndex == commandsBufferBatch.length) {
                        commands.put(commandsBufferBatch);
                        commandIndex = 0;
                    }
                }
            }
        } else {
            //Just all nodes, no selection active:
            for (int j = 0; j <= maxIndex; j++) {
                final Node node = visibleNodesArray[j];
                if (node == null) {
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
                    fillNodeCommandData(node, commandIndex, instanceId);
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
        final float size = node.size() * currentNodeScale;
        final int rgba = node.getRGBA();

        //Position:
        attributesBufferBatch[index] = x;
        attributesBufferBatch[index + 1] = y;

        //Color:
        attributesBufferBatch[index + 2] = Float.intBitsToFloat(rgba);

        //Size:
        attributesBufferBatch[index + 3] = size;
    }

    protected void fillNodeCommandData(final Node node, final int index, final int instanceId) {
        //Indirect Draw:
        //Choose LOD:
        final float observedSize = node.size() * currentNodeScale * currentZoom;

        final int circleVertexCount;
        final int firstVertex;
        if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_64) {
            circleVertexCount = circleMesh64.vertexCount;
            firstVertex = firstVertex64;
        } else if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_32) {
            circleVertexCount = circleMesh32.vertexCount;
            firstVertex = firstVertex32;
        } else if (observedSize > OBSERVED_SIZE_LOD_THRESHOLD_16) {
            circleVertexCount = circleMesh16.vertexCount;
            firstVertex = firstVertex16;
        } else {
            circleVertexCount = circleMesh8.vertexCount;
            firstVertex = firstVertex8;
        }

        commandsBufferBatch[index] = circleVertexCount;//vertex count
        commandsBufferBatch[index + 1] = 1;//instance count
        commandsBufferBatch[index + 2] = firstVertex;//first vertex
        commandsBufferBatch[index + 3] = instanceId;//base instance
    }

    private NodesVAO nodesVAO;
    private NodesVAO nodesVAOSecondary;

    public void setupVertexArrayAttributes(GL2ES2 gl, NodeWorldData data) {
        if (nodesVAO == null) {
            nodesVAO = new NodesVAO(data.getOpenGLOptions(),
                    vertexGLBuffer, attributesGLBuffer
            );
        }

        nodesVAO.use(gl);
    }

    public void setupSecondaryVertexArrayAttributes(GL2ES2 gl, NodeWorldData data) {
        if (nodesVAOSecondary == null) {
            nodesVAOSecondary = new NodesVAO(data.getOpenGLOptions(),
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

        if (commandsGLBuffer != null) {
            commandsGLBuffer.destroy(gl);
            commandsGLBuffer = null;
        }

        // Destroy and reset VAOs to prevent reuse after re-init
        if (nodesVAO != null) {
            nodesVAO.destroy(gl.getGL2ES2());
            nodesVAO = null;
        }

        if (nodesVAOSecondary != null) {
            nodesVAOSecondary.destroy(gl.getGL2ES2());
            nodesVAOSecondary = null;
        }

        // Destroy shader programs
        diskModel.destroy(gl.getGL2ES2());

        nodesCallback.reset();
    }

    private class NodesVAO extends GLVertexArrayObject {

        private final GLBuffer vertexBuffer;
        private final GLBuffer attributesBuffer;

        public NodesVAO(OpenGLOptions openGLOptions, final GLBuffer vertexBuffer,
                        final GLBuffer attributesBuffer) {
            super(openGLOptions);
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

                    gl.glVertexAttribPointer(SHADER_POSITION_LOCATION, NodeDiskModel.POSITION_FLOATS, GL_FLOAT, false,
                            stride, offset);
                    offset += NodeDiskModel.POSITION_FLOATS * Float.BYTES;

                    gl.glVertexAttribPointer(SHADER_COLOR_LOCATION, NodeDiskModel.COLOR_FLOATS * Float.BYTES,
                            GL_UNSIGNED_BYTE, false, stride, offset);
                    offset += NodeDiskModel.COLOR_FLOATS * Float.BYTES;

                    gl.glVertexAttribPointer(SHADER_SIZE_LOCATION, NodeDiskModel.SIZE_FLOATS, GL_FLOAT, false, stride,
                            offset);
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

    public NodesCallback getNodesCallback() {
        return nodesCallback;
    }
}
