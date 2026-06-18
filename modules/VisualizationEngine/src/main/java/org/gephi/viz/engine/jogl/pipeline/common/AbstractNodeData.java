package org.gephi.viz.engine.jogl.pipeline.common;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_SAMPLE_ALPHA_TO_COVERAGE;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static org.gephi.viz.engine.jogl.util.gl.GLBufferMutable.GL_BUFFER_TYPE_ARRAY;
import static org.gephi.viz.engine.jogl.util.gl.GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_ELEMENT_INDEX_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.GLConstants.INDIRECT_DRAW_COMMAND_INTS_COUNT;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.models.mesh.NodeDiskVertexMeshGenerator;
import org.gephi.viz.engine.jogl.models.nodedisk.CommonNodeDiskModel;
import org.gephi.viz.engine.jogl.models.nodedisk.NodeDiskModelNoSelection;
import org.gephi.viz.engine.jogl.models.nodedisk.NodeDiskModelPicking;
import org.gephi.viz.engine.jogl.models.nodedisk.NodeDiskModelSelectionSelected;
import org.gephi.viz.engine.jogl.models.nodedisk.NodeDiskModelSelectionUnselected;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.Mesh;
import org.gephi.viz.engine.jogl.util.gl.GLBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLFunctions;
import org.gephi.viz.engine.jogl.util.gl.GLVertexArrayObject;
import org.gephi.viz.engine.jogl.util.gl.PickingFramebuffer;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.pipeline.common.InstanceCounter;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.ColorUtils;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractNodeData extends AbstractSelectionData {

    // NOTE: Why secondary buffers and VAOs?
    // Sadly, we cannot use glDrawArraysInstancedBaseInstance in MacOS and it will be never available

    protected GLBuffer vertexGLBuffer;
    protected GLBuffer attributesGLBuffer;
    protected GLBuffer attributesGLBufferSecondary;
    protected GLBuffer commandsGLBuffer;
    protected final NodesCallback nodesCallback;

    // Shared node data texture (x, y, rawSize, colorBits) indexed by node store id. Filled from ALL
    // nodes of the visible graph so that edges can resolve endpoints even if off-screen.
    protected final NodeDataTextureStore nodeDataTextureStore;

    // A single per-instance attribute: the node store id used to texelFetch the node data texture.
    protected static final int ATTRIBS_STRIDE = 1;

    protected final NodeDiskModelNoSelection diskModelNoSelection;
    protected final NodeDiskModelSelectionSelected diskModelSelectionSelected;
    protected final NodeDiskModelSelectionUnselected diskModelSelectionUnselected;

    // GPU picking: a program that renders nodes with a flat store-id color into an offscreen buffer,
    // plus the offscreen target itself. Used on demand to resolve the node under the cursor.
    protected final NodeDiskModelPicking diskModelPicking = new NodeDiskModelPicking();
    protected final PickingFramebuffer pickingFramebuffer = new PickingFramebuffer();
    // Captured each frame from the engine so the picking pass can (re)build its VAOs off the render
    // thread's world data.
    protected OpenGLOptions openGLOptions;

    // Single resolution-independent quad; the disk is produced in the fragment shader via an SDF, so
    // no per-size triangulated circle LOD is needed anymore.
    protected final Mesh nodeMesh = NodeDiskVertexMeshGenerator.generateQuad();

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

    // Visible-set signature for the attribute (and indirect-command) buffers: while the on-screen set
    // of nodes and their draw order are unchanged (idle, in-place layout, pan/zoom that keeps the same
    // nodes visible), the per-instance store-id buffers are identical and need not be re-uploaded.
    private long runningAttribHash;
    protected long pendingAttribHash;
    private long uploadedAttribHash;
    private int uploadedUnselectedCount = -1;
    private int uploadedSelectedCount = -1;
    private boolean attribUploadedOnce = false;

    public AbstractNodeData(final NodesCallback nodesCallback,
                            final NodeDataTextureStore nodeDataTextureStore,
                            final boolean instancedRendering,
                            final boolean indirectCommands) {
        this.startedTime = System.currentTimeMillis();
        this.instancedRendering = instancedRendering;
        this.indirectCommands = indirectCommands;
        this.nodesCallback = nodesCallback;
        this.nodeDataTextureStore = nodeDataTextureStore;

        diskModelNoSelection = new NodeDiskModelNoSelection();
        diskModelSelectionSelected = new NodeDiskModelSelectionSelected();
        diskModelSelectionUnselected = new NodeDiskModelSelectionUnselected();
    }

    public void init(GL2ES2 gl) {
        diskModelNoSelection.initGLPrograms(gl);
        diskModelSelectionSelected.initGLPrograms(gl);
        diskModelSelectionUnselected.initGLPrograms(gl);
        diskModelPicking.initGLPrograms(gl);
        nodeDataTextureStore.init(gl);
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

    protected void initNodeVertexGLBuffer(GL gl, final int bufferName) {
        final FloatBuffer nodeVertexBuffer = GLBuffers.newDirectFloatBuffer(nodeMesh.vertexData);
        vertexGLBuffer = new GLBufferMutable(bufferName, GL_BUFFER_TYPE_ARRAY);
        vertexGLBuffer.bind(gl);
        vertexGLBuffer.init(gl, nodeVertexBuffer, GL_BUFFER_USAGE_STATIC_DRAW);
        vertexGLBuffer.unbind(gl);
    }

    protected int setupShaderProgramForRenderingLayer(final GL2ES2 gl,
                                                      final RenderingLayer layer,
                                                      final NodeWorldData data,
                                                      final float[] mvpFloats) {
        final boolean someSelection = data.hasSomeSelection();
        final boolean renderingUnselectedNodes = layer.getLevel() == 1;

        if (!someSelection && renderingUnselectedNodes) {
            return 0;
        }

        final float[] backgroundColorFloats = data.getBackgroundColor();
        final float nodeScale = data.getNodeScale();

        // Bind the shared node data textures (position + style) sampled (via texelFetch) by all node
        // shaders.
        nodeDataTextureStore.bind(gl);
        gl.glActiveTexture(GL_TEXTURE0);

        // Antialiase the SDF disk rim. Blending is globally disabled for nodes, so we rely on
        // sample-alpha-to-coverage (MSAA) using the coverage written to the fragment alpha. No-op on
        // a non-multisampled framebuffer (rim falls back to a hard edge, like the old geometry).
        gl.glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE);

        final int instanceCount;
        // if the background is dark (luma <.5) the node border with lighten (color * (factor > 1)) otherwise it's darken (color * (factor < 1))
        float nodeBorderColorFactor =
            ColorUtils.isColorDark(backgroundColorFloats) ? 1f + Constants.getNodeBorderDarkenFactor() :
                Constants.getNodeBorderDarkenFactor();

        if (renderingUnselectedNodes) {
            instanceCount = instanceCounter.unselectedCountToDraw;
            final float colorLightenFactor = data.getLightenNonSelectedFactor();

            diskModelSelectionUnselected.useProgram(
                gl,
                mvpFloats,
                backgroundColorFloats,
                colorLightenFactor,
                globalTime,
                this.selectedTime,
                nodeBorderColorFactor,
                nodeScale
            );

            setupSecondaryVertexArrayAttributes(gl, data);
        } else {
            instanceCount = instanceCounter.selectedCountToDraw;

            if (someSelection) {

                diskModelSelectionSelected.useProgram(
                    gl,
                    mvpFloats,
                    globalTime,
                    this.selectedTime,
                    nodeBorderColorFactor,
                    nodeScale
                );
            } else {
                diskModelNoSelection.useProgram(gl, mvpFloats, nodeBorderColorFactor, nodeScale);
            }

            setupVertexArrayAttributes(gl, data);
        }

        return instanceCount;
    }

    public NodeWorldData createWorldData(VizEngineModel model, VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        this.openGLOptions = engine.getOpenGLOptions();
        return new NodeWorldData(
            someSelection,
            model.getRenderingOptions().getBackgroundColor(),
            maxNodeSize,
            currentZoom,
            model.getRenderingOptions().getNodeScale(),
            model.getRenderingOptions().isLightenNonSelected() ?
                model.getRenderingOptions().getLightenNonSelectedFactor() : 0f,
            engine.getOpenGLOptions()
        );
    }

    public void update(GraphRenderingOptions renderingOptions, Graph graph) {
        // Always (re)build the shared node data texture from ALL nodes of the visible graph, even when
        // nodes are not drawn, so that edges can still resolve their (possibly off-screen) endpoints.
        nodeDataTextureStore.fillFromGraph(graph);

        if (!renderingOptions.isShowNodes()) {
            instanceCounter.clearCount();
            pendingAttribHash = 0L;
            return;
        }

        //Selection and other states updates
        currentZoom = renderingOptions.getZoom();
        currentNodeScale = renderingOptions.getNodeScale();

        // Get visible nodes (dense list, no nulls; iterate O(visible))
        final Node[] visibleNodesArray = nodesCallback.getCompactNodesArray();
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
        runningAttribHash = 1469598103934665603L;
        if (someSelection) {
            //First non-selected (bottom):
            for (int j = 0; j < totalNodes; j++) {
                final Node node = visibleNodesArray[j];

                final boolean selected = nodesCallback.isSelected(node.getStoreId(), true);
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
            for (int j = 0; j < totalNodes; j++) {
                final Node node = visibleNodesArray[j];

                final boolean selected = nodesCallback.isSelected(node.getStoreId(), true);
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
            for (int j = 0; j < totalNodes; j++) {
                final Node node = visibleNodesArray[j];

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
        pendingAttribHash = runningAttribHash;
    }

    protected void fillNodeAttributesData(final Node node, final int index) {
        // Per-instance attribute is just the node store id; the shader reads x/y/size/color from the
        // node data texture via texelFetch.
        final int storeId = node.getStoreId();
        attributesBufferBatch[index] = storeId;
        runningAttribHash = (runningAttribHash ^ storeId) * 1099511628211L;
    }

    /**
     * Returns whether the per-instance attribute (and indirect-command) buffers need re-uploading,
     * i.e. the visible node set or its draw order changed since the last upload. Updates the
     * tracked signature when it returns {@code true}, so callers must upload when it does.
     */
    protected boolean attributesUploadNeeded() {
        final int u = instanceCounter.unselectedCount;
        final int s = instanceCounter.selectedCount;
        if (attribUploadedOnce && uploadedAttribHash == pendingAttribHash
            && uploadedUnselectedCount == u && uploadedSelectedCount == s) {
            return false;
        }
        uploadedAttribHash = pendingAttribHash;
        uploadedUnselectedCount = u;
        uploadedSelectedCount = s;
        attribUploadedOnce = true;
        return true;
    }

    protected void fillNodeCommandData(final Node node, final int index, final int instanceId) {
        //Indirect Draw: every node is a single SDF quad (no LOD), so the geometry is identical.
        commandsBufferBatch[index] = nodeMesh.vertexCount;//vertex count
        commandsBufferBatch[index + 1] = 1;//instance count
        commandsBufferBatch[index + 2] = 0;//first vertex
        commandsBufferBatch[index + 3] = instanceId;//base instance
    }

    private NodesVAO nodesVAO;
    private NodesVAO nodesVAOSecondary;

    public void setupVertexArrayAttributes(GL2ES2 gl, NodeWorldData data) {
        useNodesVAO(gl, data.getOpenGLOptions());
    }

    public void setupSecondaryVertexArrayAttributes(GL2ES2 gl, NodeWorldData data) {
        useNodesVAOSecondary(gl, data.getOpenGLOptions());
    }

    // Single VAO-creation path shared by the render passes (which have the per-frame world data) and
    // the on-demand picking pass (which only has the captured openGLOptions).
    private void useNodesVAO(GL2ES2 gl, OpenGLOptions options) {
        if (nodesVAO == null) {
            nodesVAO = new NodesVAO(options, vertexGLBuffer, attributesGLBuffer);
        }
        nodesVAO.use(gl);
    }

    private void useNodesVAOSecondary(GL2ES2 gl, OpenGLOptions options) {
        if (nodesVAOSecondary == null) {
            nodesVAOSecondary = new NodesVAO(options, vertexGLBuffer, attributesGLBufferSecondary);
        }
        nodesVAOSecondary.use(gl);
    }

    public void unsetupVertexArrayAttributes(GL2ES2 gl) {
        gl.glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE);

        if (nodesVAO != null) {
            nodesVAO.stopUsing(gl);
        }

        if (nodesVAOSecondary != null) {
            nodesVAOSecondary.stopUsing(gl);
        }
    }

    /**
     * Renders all currently visible nodes into an offscreen buffer with a flat per-node store-id color
     * and reads back the pixel under the given screen position to identify the node there (GPU
     * picking). Must be called on the GL thread, after the regular frame has been rendered (so the node
     * data texture and the per-node index buffers are up to date).
     *
     * @param screenX  cursor x in screen pixels (origin top-left)
     * @param screenY  cursor y in screen pixels (origin top-left)
     * @param width    current viewport width in pixels
     * @param height   current viewport height in pixels
     * @param mvpFloats the same model-view-projection used to render the frame
     * @param nodeScale the same node scale used to render the frame
     * @return the node under the cursor, or {@code null} if there is none (or picking is unavailable)
     */
    public Node pickNode(final GL gl, final int screenX, final int screenY, final int width, final int height,
                         final float[] mvpFloats, final float nodeScale) {
        if (openGLOptions == null || width <= 0 || height <= 0) {
            return null;
        }
        if (screenX < 0 || screenX >= width || screenY < 0 || screenY >= height) {
            return null;
        }

        final int unselected = instanceCounter.unselectedCountToDraw;
        final int selected = instanceCounter.selectedCountToDraw;
        final int total = unselected + selected;
        if (total <= 0) {
            return null;
        }

        if (!pickingFramebuffer.ensureSize(gl, width, height)) {
            return null;
        }

        final GL2ES2 gl2 = gl.getGL2ES2();

        // Save the currently bound framebuffer and viewport: the on-screen target may itself be an FBO
        // (e.g. GLJPanel), so we cannot assume binding 0 restores it.
        final int[] savedFbo = new int[1];
        gl2.glGetIntegerv(GL.GL_FRAMEBUFFER_BINDING, savedFbo, 0);
        final int[] savedViewport = new int[4];
        gl2.glGetIntegerv(GL.GL_VIEWPORT, savedViewport, 0);

        pickingFramebuffer.bind(gl2);
        gl2.glViewport(0, 0, width, height);
        // Exact ids: no antialiasing/coverage (blending is already globally disabled).
        gl2.glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE);
        gl2.glClearColor(0f, 0f, 0f, 0f);
        gl2.glClear(GL_COLOR_BUFFER_BIT);

        nodeDataTextureStore.bind(gl2);
        gl2.glActiveTexture(GL_TEXTURE0);

        diskModelPicking.useProgram(gl2, mvpFloats, nodeScale);

        if (instancedRendering) {
            final GL2ES3 gl3 = gl.getGL2ES3();
            if (unselected > 0) {
                useNodesVAOSecondary(gl2, openGLOptions);
                GLFunctions.drawInstanced(gl3, 0, nodeMesh.vertexCount, unselected);
            }
            if (selected > 0) {
                useNodesVAO(gl2, openGLOptions);
                GLFunctions.drawInstanced(gl3, 0, nodeMesh.vertexCount, selected);
            }
        } else {
            // Array-draw fallback: one draw per node, with the store id as a constant generic attribute.
            useNodesVAO(gl2, openGLOptions);
            final FloatBuffer attribs = attributesBuffer.floatBuffer();
            attribs.position(0);
            final float[] one = new float[ATTRIBS_STRIDE];
            for (int i = 0; i < total; i++) {
                attribs.get(one);
                gl2.glVertexAttrib1f(SHADER_ELEMENT_INDEX_LOCATION, one[0]);
                GLFunctions.drawArraysSingleInstance(gl2, 0, nodeMesh.vertexCount);
            }
        }

        GLFunctions.stopUsingProgram(gl2);
        if (nodesVAO != null) {
            nodesVAO.stopUsing(gl2);
        }
        if (nodesVAOSecondary != null) {
            nodesVAOSecondary.stopUsing(gl2);
        }

        // Flip Y: glReadPixels origin is bottom-left, screen origin is top-left.
        final int storeId = pickingFramebuffer.readPixelId(gl2, screenX, height - 1 - screenY);

        gl2.glBindFramebuffer(GL.GL_FRAMEBUFFER, savedFbo[0]);
        gl2.glViewport(savedViewport[0], savedViewport[1], savedViewport[2], savedViewport[3]);

        if (storeId < 0) {
            return null;
        }
        final Node[] nodes = nodesCallback.getNodesArray();
        if (storeId >= nodes.length) {
            return null;
        }
        return nodes[storeId];
    }

    /**
     * Whether GPU node picking is usable (the offscreen framebuffer has not failed to initialize).
     */
    public boolean isPickingAvailable() {
        return !pickingFramebuffer.isUnavailable();
    }

    public void dispose(GL gl) {
        attributesBufferBatch = null;
        commandsBufferBatch = null;
        // Force a re-upload after a re-init (GL buffers are recreated).
        attribUploadedOnce = false;
        uploadedUnselectedCount = -1;
        uploadedSelectedCount = -1;
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
        diskModelNoSelection.destroy(gl.getGL2ES2());
        diskModelSelectionSelected.destroy(gl.getGL2ES2());
        diskModelSelectionUnselected.destroy(gl.getGL2ES2());
        diskModelPicking.destroy(gl.getGL2ES2());

        // Offscreen picking target
        pickingFramebuffer.dispose(gl);

        // The node data texture is shared with the edge pipeline; the node pipeline owns its lifecycle.
        nodeDataTextureStore.dispose(gl);

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
                gl.glVertexAttribPointer(SHADER_VERT_LOCATION, CommonNodeDiskModel.VERTEX_FLOATS, GL_FLOAT, false,
                    0, 0);
            }
            vertexBuffer.unbind(gl);

            if (instancedRendering) {
                attributesBuffer.bind(gl);
                {
                    final int stride = ATTRIBS_STRIDE * Float.BYTES;
                    gl.glVertexAttribPointer(SHADER_ELEMENT_INDEX_LOCATION, ATTRIBS_STRIDE, GL_FLOAT, false,
                        stride, 0);
                }
                attributesBuffer.unbind(gl);
            }
            // Non-instanced (array-draw) path sets the element index as a constant generic vertex
            // attribute per draw call (glVertexAttrib1f), so no array pointer is configured here.
        }

        @Override
        protected int[] getUsedAttributeLocations() {
            if (instancedRendering) {
                return new int[] {
                    SHADER_VERT_LOCATION,
                    SHADER_ELEMENT_INDEX_LOCATION
                };
            } else {
                return new int[] {
                    SHADER_VERT_LOCATION
                };
            }
        }

        @Override
        protected int[] getInstancedAttributeLocations() {
            if (instancedRendering) {
                return new int[] {
                    SHADER_ELEMENT_INDEX_LOCATION
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
