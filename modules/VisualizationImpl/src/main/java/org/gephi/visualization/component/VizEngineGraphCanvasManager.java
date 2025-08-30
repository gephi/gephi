package org.gephi.visualization.component;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JComponent;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.VizEngineJOGLConfigurator;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

public class VizEngineGraphCanvasManager {

    // Normally all of these options should be false
    // because the engine will auto-detect what's compatible with OpenGL Driver.
    private static final boolean DISABLE_INDIRECT_RENDERING = false;
    private static final boolean DISABLE_INSTANCED_RENDERING = false;
    private static final boolean DISABLE_VAOS = false;

    private static final boolean DEBUG = false;

    private final VizController vizController;

    private boolean initialized = false;

    private GLWindow glWindow;
    private NewtCanvasAWT glCanvas;

    // Engine:
    private transient VizEngine<JOGLRenderingTarget, NEWTEvent> engine = null;

    // Engine state saved for when it's restarted:


    public VizEngineGraphCanvasManager(VizController vizController) {
        this.vizController = Objects.requireNonNull(vizController);
    }

    public Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine() {
        return Optional.ofNullable(engine);
    }

    public Optional<Float> getSurfaceScale() {
        if (glWindow != null) {
            return Optional.of(glWindow.getCurrentSurfaceScale(new float[2])[0]);
        }
        return Optional.empty();
    }

    public synchronized VizEngine<JOGLRenderingTarget, NEWTEvent> init(final JComponent component) {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }

        this.initialized = true;

        final GLCapabilities caps = VizEngineJOGLConfigurator.createCapabilities();

        final Display display = NewtFactory.createDisplay(null);
        final Screen screen = NewtFactory.createScreen(display, 0);

        this.glWindow = GLWindow.create(screen, caps);

        if (DEBUG) {
            glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        }

        final JOGLRenderingTarget renderingTarget = new JOGLRenderingTarget(glWindow);

        this.engine = VizEngineFactory.newEngine(
            renderingTarget,
            Collections.singletonList(
                new VizEngineJOGLConfigurator()
            )
        );

//        workspace.add(engine);

        // Previous state for this workspace? keep it:
//        if (engineTranslate != null) {
//            engine.setTranslate(engineTranslate);
//            engine.setZoom(engineZoom);
//            engine.setBackgroundColor(engineBackgroundColor);
//        }

        final OpenGLOptions glOptions = engine.getLookup().lookup(OpenGLOptions.class);
        glOptions.setDisableIndirectDrawing(DISABLE_INDIRECT_RENDERING);
        glOptions.setDisableInstancedDrawing(DISABLE_INSTANCED_RENDERING);
        glOptions.setDisableVAOS(DISABLE_VAOS);
        glOptions.setDebug(DEBUG);

        engine.addInputListener(new InputListener<>() {
            @Override
            public boolean processEvent(NEWTEvent inputEvent) {
                if (engine != null && inputEvent instanceof MouseEvent && vizController.getVizEventManager() != null) {
                    return vizController.getVizEventManager()
                        .processMouseEvent(glCanvas, VizEngineGraphCanvasManager.this, engine, (MouseEvent) inputEvent);
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
                int i = -100;
                return i; // Execute before default listener of viz engine (has order = 0)
            }
        });

        glCanvas = new NewtCanvasAWT(glWindow);

        component.add(glCanvas, BorderLayout.CENTER);

        engine.start();

        component.revalidate();

        return engine;
    }

    public synchronized VizModel loadWorkspace(Workspace workspace) {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
        VizModel model = vizController.getModel(workspace);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        engine.setGraphModel(graphModel, model.toGraphRenderingOptions());
        return model;
    }

    public synchronized void destroy(JComponent component) {
        if (glCanvas != null) {
            component.remove(glCanvas);
            component.revalidate();
        }

        // Keep viz-engine state for when it's restarted:
        if (engine != null) {
            engine.pause();

//            engineTranslate = engine.getTranslate();
//            engineZoom = engine.getZoom();
//            engineBackgroundColor = engine.getBackgroundColor();

            //TODO: Keep more state of GraphRenderingOptions
//            workspace.remove(engine);
            //Logger.getLogger("").info("Destroying viz-engine...");
            //engine.destroy(); // This crashes in windows!!
        }

        if (glWindow != null) {
            //Logger.getLogger("").info("Destroying glWindow...");
            glWindow.destroy();
            glWindow = null;
            glCanvas = null;
        }

        initialized = false;
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized void reinit(JComponent component) {
        destroy(component);
        init(component);
    }
}
