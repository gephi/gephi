package org.gephi.visualization.component;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JComponent;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.VizController;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTarget;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTargetAWT;
import org.gephi.viz.engine.lwjgl.VizEngineLWJGLConfigurator;
import org.gephi.viz.engine.lwjgl.pipeline.events.AWTEventsListener;
import org.gephi.viz.engine.lwjgl.pipeline.events.LWJGLInputEvent;
import org.gephi.viz.engine.lwjgl.pipeline.events.MouseEvent;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.spi.WorldUpdaterExecutionMode;
import org.gephi.viz.engine.util.gl.BasicFPSAnimator;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.joml.Vector2fc;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.openide.util.Lookup;
import org.lwjgl.system.Platform;

public class VizEngineGraphCanvasManager {

    private static final String ANIMATOR_THREAD_NAME = "VizEngineGraphCanvas";
    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor(runnable -> {
        return new Thread(runnable, ANIMATOR_THREAD_NAME);
    });

    private static final int MAX_FPS = 30;

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
    private BasicFPSAnimator graphicsAnimator;

    public VizEngineGraphCanvasManager(Workspace workspace) {
        this.workspace = Objects.requireNonNull(workspace);
    }

    public synchronized void init(JComponent component) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        this.initialized = true;

        final GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        final LWJGLRenderingTargetAWT renderingTarget = new LWJGLRenderingTargetAWT();

        this.engine = VizEngineFactory.newEngine(
            renderingTarget,
            graphModel,
            Collections.singletonList(
                new VizEngineLWJGLConfigurator()
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

//        engine.getLookup().lookup(GraphRenderingOptions.class)
//                .setShowEdges(false); //FIXME in viz engine locking

        renderingTarget.setup(engine);

        final GLData glData = new GLData();

        if (USE_OPENGL_ES) {
            glData.api = GLData.API.GLES;
        }

        if (Platform.get() == Platform.MACOSX) {
            //In mac we have to set the version or it won't give the latest automatically

            glData.majorVersion = 3;
            glData.minorVersion = 2;
            glData.forwardCompatible = true;
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
                    renderingTarget.reshape(glCanvas.getFramebufferWidth(), glCanvas.getFramebufferHeight());
                }
            }
        });
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if (renderingTarget.getEngine() != null) {
                    renderingTarget.reshape(glCanvas.getFramebufferWidth(), glCanvas.getFramebufferHeight());
                }
            }
        });

        final AWTEventsListener eventsListener = new AWTEventsListener(glCanvas, engine);
        eventsListener.register();

        final VizController vizController = Lookup.getDefault().lookup(VizController.class);

        engine.addInputListener(new InputListener<LWJGLRenderingTarget, LWJGLInputEvent>() {
            @Override
            public boolean processEvent(LWJGLInputEvent inputEvent) {
                if (inputEvent instanceof MouseEvent) {
                    if (vizController.getVizEventManager().processMouseEvent(engine, (MouseEvent) inputEvent)) {
                        return true;
                    }
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
            public void init(LWJGLRenderingTarget lwjglRenderingTarget) {
                //NOP
            }

            @Override
            public int getOrder() {
                return -100; // Execute before default listener of viz engine (has order = 0)
            }
        });

        component.add(glCanvas, BorderLayout.CENTER);

        engine.start();

        initRenderingLoop(glCanvas);

        component.revalidate();
    }

    private void initRenderingLoop(AWTGLCanvas glCanvas) {
        final Runnable renderLoop = () -> {
            if (glCanvas.isValid()) {
                glCanvas.render();
            }
        };

        graphicsAnimator = new BasicFPSAnimator(renderLoop, MAX_FPS);
        THREAD_POOL.submit(graphicsAnimator);
    }

    public synchronized void destroy(JComponent component) {
        // Keep viz-engine state for when it's restarted:
        if (engine != null) {
            this.engineTranslate = engine.getTranslate();
            this.engineZoom = engine.getZoom();
            this.engineBackgroundColor = engine.getBackgroundColor();

            //TODO: Keep more state of GraphRenderingOptions
            workspace.remove(engine);
            engine = null;
        }

        if (glCanvas != null) {
            component.remove(glCanvas);
            glCanvas.disposeCanvas();
            glCanvas = null;
        }

        this.initialized = false;

        if (graphicsAnimator != null) {
            this.graphicsAnimator.shutdown();
            this.graphicsAnimator = null;
        }
    }
}
