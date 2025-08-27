package org.gephi.viz.engine.jogl;

import static com.jogamp.opengl.GLProfile.GL2;
import static com.jogamp.opengl.GLProfile.GL3;
import static com.jogamp.opengl.GLProfile.GL4;
import static com.jogamp.opengl.GLProfile.GLES2;
import static com.jogamp.opengl.GLProfile.GLES3;

import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.pipeline.DefaultJOGLEventListener;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawEdgeData;
import org.gephi.viz.engine.jogl.pipeline.arrays.ArrayDrawNodeData;
import org.gephi.viz.engine.jogl.pipeline.arrays.renderers.EdgeRendererArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.renderers.NodeRendererArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.renderers.RectangleSelectionArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.renderers.SimpleMouseSelectionArrayDraw;
import org.gephi.viz.engine.jogl.pipeline.arrays.updaters.EdgesUpdaterArrayDrawRendering;
import org.gephi.viz.engine.jogl.pipeline.arrays.updaters.NodesUpdaterArrayDrawRendering;
import org.gephi.viz.engine.jogl.pipeline.indirect.IndirectNodeData;
import org.gephi.viz.engine.jogl.pipeline.indirect.renderers.NodeRendererIndirect;
import org.gephi.viz.engine.jogl.pipeline.indirect.updaters.NodesUpdaterIndirectRendering;
import org.gephi.viz.engine.jogl.pipeline.instanced.InstancedEdgeData;
import org.gephi.viz.engine.jogl.pipeline.instanced.InstancedNodeData;
import org.gephi.viz.engine.jogl.pipeline.instanced.renderers.EdgeRendererInstanced;
import org.gephi.viz.engine.jogl.pipeline.instanced.renderers.NodeRendererInstanced;
import org.gephi.viz.engine.jogl.pipeline.instanced.updaters.EdgesUpdaterInstancedRendering;
import org.gephi.viz.engine.jogl.pipeline.instanced.updaters.NodesUpdaterInstancedRendering;
import org.gephi.viz.engine.jogl.pipeline.text.NodeLabelRenderer;
import org.gephi.viz.engine.spi.VizEngineConfigurator;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

/**
 *
 * @author Eduardo Ramos
 */
public class VizEngineJOGLConfigurator implements VizEngineConfigurator<JOGLRenderingTarget, NEWTEvent> {

    /**
     * Order of maximum programmable shader <i>core only</i> profiles
     *
     * <ul>
     * <li> GL4 </li>
     * <li> GL3 </li>
     * <li> GLES3 </li>
     * <li> GL2 </li>
     * <li> GLES2 </li>
     * </ul>
     *
     */
    public static final String[] GL_PROFILE_LIST_MAX_PROGSHADER_CORE_OR_GL2 =
        new String[] {GL4, GL3, GLES3, GL2, GLES2};

    public static GLCapabilities createCapabilities() {
        GLProfile.getDefaultDevice();

        GLProfile glProfile = GLProfile.get(GL_PROFILE_LIST_MAX_PROGSHADER_CORE_OR_GL2, true);
        GLCapabilities caps = new GLCapabilities(glProfile);

        Logger.getLogger(VizEngine.class.getName()).log(Level.CONFIG, GLProfile.glAvailabilityToString());
        Logger.getLogger(VizEngine.class.getName()).log(Level.INFO, "Chosen GL Profile: {0}", glProfile);

        caps.setAlphaBits(8);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        caps.setSampleBuffers(true);
        caps.setNumSamples(4);

        return caps;
    }

    @Override
    public void configure(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        final GraphRenderingOptionsImpl renderingOptions = new GraphRenderingOptionsImpl();
        final OpenGLOptions openGLOptions = new OpenGLOptions();

        engine.addToLookup(renderingOptions);
        engine.addToLookup(openGLOptions);

        setupIndirectRendering(engine);
        setupInstancedRendering(engine);
        setupVertexArrayRendering(engine);

        setupInputListeners(engine);
    }

    private void setupIndirectRendering(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        //Only nodes supported, edges don't have a LOD to benefit from
        final IndirectNodeData nodeData = new IndirectNodeData();

        engine.addRenderer(new NodeRendererIndirect(engine, nodeData));
        engine.addWorldUpdater(new NodesUpdaterIndirectRendering(engine, nodeData));
    }

    private void setupInstancedRendering(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        //Nodes:
        final InstancedNodeData nodeData = new InstancedNodeData();
        engine.addRenderer(new NodeRendererInstanced(engine, nodeData));
        engine.addWorldUpdater(new NodesUpdaterInstancedRendering(engine, nodeData));

        //Edges:
        final InstancedEdgeData indirectEdgeData = new InstancedEdgeData();

        engine.addRenderer(new EdgeRendererInstanced(engine, indirectEdgeData));
        engine.addWorldUpdater(new EdgesUpdaterInstancedRendering(engine, indirectEdgeData));
    }

    private void setupVertexArrayRendering(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        //Nodes:
        final ArrayDrawNodeData nodeData = new ArrayDrawNodeData();
        engine.addRenderer(new NodeRendererArrayDraw(engine, nodeData));
        engine.addWorldUpdater(new NodesUpdaterArrayDrawRendering(engine, nodeData));

        //Edges:
        final ArrayDrawEdgeData edgeData = new ArrayDrawEdgeData();
        engine.addRenderer(new EdgeRendererArrayDraw(engine, edgeData));
        engine.addWorldUpdater(new EdgesUpdaterArrayDrawRendering(engine, edgeData));

        //Selection:
        engine.addRenderer(new RectangleSelectionArrayDraw(engine));
        engine.addRenderer(new SimpleMouseSelectionArrayDraw(engine));

        // Node Label
        engine.addRenderer(new NodeLabelRenderer(engine));
    }

    private void setupInputListeners(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        engine.addInputListener(new DefaultJOGLEventListener(engine));
    }
}
