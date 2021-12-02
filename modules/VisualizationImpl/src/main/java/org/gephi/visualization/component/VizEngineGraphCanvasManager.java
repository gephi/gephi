package org.gephi.visualization.component;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTarget;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTargetAWT;
import org.gephi.viz.engine.lwjgl.VizEngineLWJGLConfigurator;
import org.gephi.viz.engine.lwjgl.pipeline.events.AWTEventsListener;
import org.gephi.viz.engine.lwjgl.pipeline.events.LWJGLInputEvent;
import org.gephi.viz.engine.spi.WorldUpdaterExecutionMode;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.joml.Vector2fc;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

public class VizEngineGraphCanvasManager {

    private static final boolean DISABLE_INDIRECT_RENDERING = false;
    private static final boolean DISABLE_INSTANCED_RENDERING = false;
    private static final boolean DISABLE_VAOS = false;

    private static final boolean DEBUG = false;
    private static final boolean USE_OPENGL_ES = false;

    private static final WorldUpdaterExecutionMode UPDATE_DATA_MODE = WorldUpdaterExecutionMode.CONCURRENT_ASYNCHRONOUS;

    private final Workspace workspace;
    private boolean initialized = false;
    private AWTGLCanvas glCanvas = null;

    // Engine:
    private VizEngine<LWJGLRenderingTarget, LWJGLInputEvent> engine = null;
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

        final LWJGLRenderingTargetAWT renderingTarget = new LWJGLRenderingTargetAWT(null);

        this.engine = VizEngineFactory.newEngine(
            renderingTarget,
            graphModel,
            Collections.singletonList(
                new VizEngineLWJGLConfigurator()
            )
        );

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

        engine.getLookup().lookup(GraphRenderingOptions.class)
            .setShowEdges(false); //FIXME in viz engine locking

        renderingTarget.setup(engine);

        final GLData glData = new GLData();

        if (USE_OPENGL_ES) {
            glData.api = GLData.API.GLES;
        }

        glData.samples = 4; //4 samples anti-aliasing
        glData.swapInterval = 0;

        final AWTGLCanvas glCanvas = new AWTGLCanvas(glData) {
            @Override
            public void initGL() {
                renderingTarget.initializeContext();
            }

            @Override
            public void paintGL() {
                renderingTarget.display();
                swapBuffers();
            }
        };

        this.glCanvas = glCanvas;

        glCanvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if (renderingTarget.getEngine() != null) {
                    renderingTarget.reshape(event.getComponent().getWidth(), event.getComponent().getHeight());
                }
            }
        });
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if (renderingTarget.getEngine() != null) {
                    renderingTarget.reshape(glCanvas.getWidth(), glCanvas.getHeight());
                }
            }
        });

        final AWTEventsListener eventsListener = new AWTEventsListener(glCanvas, engine);
        eventsListener.register();

        component.add(glCanvas, BorderLayout.CENTER);

        engine.start();

        initRenderingLoop(glCanvas);

        component.revalidate();
    }

    private void initRenderingLoop(AWTGLCanvas glCanvas) {
        final Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                if (!initialized) {
                    return; //Stop rendering loop
                }

                try {
                    if (glCanvas.isValid()) {
                        glCanvas.render();
                    }
                } catch (Throwable ex) {
                    //NOOP
                }

                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }

    public synchronized void destroy(JComponent component) {
        // Keep viz-engine state for when it's restarted:
        if (engine != null) {
            this.engineTranslate = engine.getTranslate();
            this.engineZoom = engine.getZoom();
            this.engineBackgroundColor = engine.getBackgroundColor();

            engine = null;
        }

        if (glCanvas != null) {
            component.remove(glCanvas);
            glCanvas.disposeCanvas();
            glCanvas = null;
        }
        this.initialized = false;
    }
}
