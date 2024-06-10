package org.gephi.visualization.component;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;
import javax.swing.JComponent;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.Window;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.VizController;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.VizEngineJOGLConfigurator;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.spi.WorldUpdaterExecutionMode;
import org.gephi.viz.engine.util.gl.BasicFPSAnimator;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.joml.Vector2fc;
import org.openide.util.Lookup;

public class VizEngineGraphCanvasManager {

    private static final boolean DISABLE_INDIRECT_RENDERING = false;
    private static final boolean DISABLE_INSTANCED_RENDERING = false;
    private static final boolean DISABLE_VAOS = false;

    private static final boolean DEBUG = false;

    private static final WorldUpdaterExecutionMode UPDATE_DATA_MODE = WorldUpdaterExecutionMode.CONCURRENT_ASYNCHRONOUS;

    private final Workspace workspace;
    private boolean initialized = false;

    private GLWindow glWindow;
    private NewtCanvasAWT glCanvas;

    // Engine:
    private VizEngine<JOGLRenderingTarget, NEWTEvent> engine = null;
    private JOGLRenderingTarget renderingTarget = null;
    // Engine state saved for when it's restarted:
    private Vector2fc engineTranslate = null;
    private float engineZoom = 0;
    private float[] engineBackgroundColor = null;

    public VizEngineGraphCanvasManager(Workspace workspace) {
        this.workspace = Objects.requireNonNull(workspace);
    }

    public synchronized void init(JComponent component) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        this.initialized = true;

        final GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        final GLCapabilities caps = VizEngineJOGLConfigurator.createCapabilities();

        final Display display = NewtFactory.createDisplay(null);
        final Screen screen = NewtFactory.createScreen(display, 0);

        glWindow = GLWindow.create(screen, caps);

        if (DEBUG) {
            glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        }

        this.renderingTarget = new JOGLRenderingTarget(glWindow);

        this.engine = VizEngineFactory.newEngine(
            renderingTarget,
            graphModel,
            Collections.singletonList(
                new VizEngineJOGLConfigurator()
            )
        );

        workspace.add(engine);

        if (engineTranslate != null) {
            engine.setTranslate(engineTranslate);
            engine.setZoom(engineZoom);
            engine.setBackgroundColor(engineBackgroundColor);
        }

        final OpenGLOptions glOptions = engine.getLookup().lookup(OpenGLOptions.class);
        glOptions.setDisableIndirectDrawing(DISABLE_INDIRECT_RENDERING);
        glOptions.setDisableInstancedDrawing(DISABLE_INSTANCED_RENDERING);
        glOptions.setDisableVAOS(DISABLE_VAOS);
        glOptions.setDebug(DEBUG);

        engine.setWorldUpdatersExecutionMode(UPDATE_DATA_MODE);

        renderingTarget.setup(engine);

        final VizController vizController = Lookup.getDefault().lookup(VizController.class);

        engine.addInputListener(new InputListener<>() {
            @Override
            public boolean processEvent(NEWTEvent inputEvent) {
                if (inputEvent instanceof MouseEvent && vizController.getVizEventManager() != null) {
                    return vizController.getVizEventManager().processMouseEvent(engine, (MouseEvent) inputEvent);
                }

                return false;
            }

            @Override
            public String getCategory() {
                return "GephiDesktop";
            }

            @Override
            public int getPreferenceInCategory() {
                return 0;
            }

            @Override
            public String getName() {
                return "Gephi Viz Event Manager";
            }

            @Override
            public void init(JOGLRenderingTarget renderingTarget) {
                //NOP
            }

            @Override
            public int getOrder() {
                return -100; // Execute before default listener of viz engine (has order = 0)
            }
        });

        /*
        if (!Utilities.isMac()) {
            newtCanvas = new HighDPIFixCanvas(glWindow);
        } else {
            newtCanvas = new NewtCanvasAWT(glWindow);
        }
        */

        glCanvas = new NewtCanvasAWT(glWindow);

        component.add(glCanvas, BorderLayout.CENTER);

        engine.start();

        component.revalidate();
    }

    public synchronized void destroy(JComponent component) {
        // Keep viz-engine state for when it's restarted:
        if (engine != null) {
            engineTranslate = engine.getTranslate();
            engineZoom = engine.getZoom();
            engineBackgroundColor = engine.getBackgroundColor();

            //TODO: Keep more state of GraphRenderingOptions
            workspace.remove(engine);
            engine = null;
            renderingTarget = null;
        }

        if (glCanvas != null) {
            component.remove(glCanvas);
            glCanvas.destroy();
            final Window newtChild = glCanvas.getNEWTChild();
            if (newtChild != null) {
                newtChild.destroy();
            }
            glCanvas = null;
        }

        initialized = false;
    }
}
