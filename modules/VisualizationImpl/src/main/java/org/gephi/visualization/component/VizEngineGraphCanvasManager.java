package org.gephi.visualization.component;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizConfig;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.events.StandardVizEventManager;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.VizEngineJOGLConfigurator;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

public class VizEngineGraphCanvasManager {
    private final VizController vizController;
    private GLWindow glWindow;
    private NewtCanvasAWT glCanvas;

    // Engine
    private transient VizEngine<JOGLRenderingTarget, NEWTEvent> engine = null;

    // States
    private boolean initialized = false;

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

        final GLCapabilities caps = VizEngineJOGLConfigurator.createCapabilities(VizConfig.getAntialiasing());

        final Display display = NewtFactory.createDisplay(null);
        final Screen screen = NewtFactory.createScreen(display, 0);

        this.glWindow = GLWindow.create(screen, caps);

        // Check for unsupported OpenGL before the engine registers its listener
        glWindow.addGLEventListener(new OpenGLVersionChecker());

        if (VizConfig.isEngineOpenGLDebug()) {
            glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);

            // Set logger to FINE to see debug messages
            Logger logger = Logger.getLogger(VizEngine.class.getSimpleName());
            logger.setLevel(Level.FINE);

            logger.log(Level.FINE, GLProfile.glAvailabilityToString());
        }

        final JOGLRenderingTarget renderingTarget = new JOGLRenderingTarget(glWindow);

        this.engine = VizEngineFactory.newEngine(
            renderingTarget,
            Collections.singletonList(
                new VizEngineJOGLConfigurator()
            )
        );
        this.engine.setDarkLaf(UIUtils.isDarkLookAndFeel());

        final OpenGLOptions glOptions = engine.getOpenGLOptions();
        glOptions.setDisableIndirectDrawing(VizConfig.isEngineDisableIndirectRendering());
        glOptions.setDisableInstancedDrawing(VizConfig.isEngineDisableInstancedRendering());
        glOptions.setDisableVAOS(VizConfig.isEngineDisableVAOs());
        glOptions.setDisableVertexArrayDrawing(VizConfig.isEngineDisableVertexArrayDrawing());
        glOptions.setDebug(VizConfig.isEngineOpenGLDebug());

        engine.addInputListener(new InputListener<>() {

            private VizEngineModel model;

            @Override
            public void frameStart(VizEngineModel model) {
                this.model = model;
            }

            @Override
            public void frameEnd(VizEngineModel model) {
                this.model = null;
            }

            @Override
            public List<NEWTEvent> processEvents(List<NEWTEvent> inputEvents) {
                if (engine != null && vizController.getVizEventManager() != null) {
                    StandardVizEventManager vizEventManager = vizController.getVizEventManager();
                    List<NEWTEvent> remainingEvents = new ArrayList<>();
                    for (NEWTEvent inputEvent : inputEvents) {
                        if (!(inputEvent instanceof MouseEvent &&
                            vizEventManager.processMouseEvent(glCanvas, VizEngineGraphCanvasManager.this, engine, model,
                                (MouseEvent) inputEvent))) {
                            remainingEvents.add(inputEvent);
                        }
                    }
                    return remainingEvents;
                }

                return inputEvents;
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

        runOnEdtAndWait(() -> {
            glCanvas = new NewtCanvasAWT(glWindow);

            component.add(glCanvas, BorderLayout.CENTER);

            component.revalidate();
            component.repaint();
        });

        engine.start();

        return engine;
    }

    public synchronized VizModel loadWorkspace(Workspace workspace) {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
        VizModel model = vizController.getModel(workspace);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        engine.setGraphModel(graphModel, model.toGraphRenderingOptions(), model.toGraphSelection());
        if (model.isDirectMouseSelection()) {
            vizController.enableMouseHandler();
        }
        return model;
    }

    public synchronized VizModel unloadWorkspace(Workspace workspace) {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
        VizModel model = vizController.getModel(workspace);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        if (engine.getGraphModel() == graphModel) {
            // We want to avoid calling that twice to not override zoom/pan with default values
            model.unsetup();

            // Only then, reset the engine's engine model
            engine.unsetGraphModel(graphModel);
        }
        vizController.disableMouseHandler();

        return model;
    }

    public synchronized void destroy(JComponent component) {
        if (glCanvas != null) {
            final NewtCanvasAWT canvasToRemove = glCanvas;
            runOnEdtAndWait(() -> {
                component.remove(canvasToRemove);
                component.revalidate();
                component.repaint();
            });
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

    private static void runOnEdtAndWait(final Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
            return;
        }

        try {
            javax.swing.SwingUtilities.invokeAndWait(runnable);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for EDT task", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("EDT task failed", ex.getCause());
        }
    }

    private static class OpenGLVersionChecker implements GLEventListener {

        private static final AtomicBoolean WARNED = new AtomicBoolean(false);

        @Override
        public void init(GLAutoDrawable drawable) {
            final GL gl = drawable.getGL();
            final String renderer = gl.glGetString(GL.GL_RENDERER);
            final String version = gl.glGetString(GL.GL_VERSION);

            // OpenGL version string starts with "major.minor"
            boolean tooOld = false;
            if (version != null && !version.isEmpty()) {
                try {
                    int major = Character.getNumericValue(version.charAt(0));
                    tooOld = major < 2;
                } catch (Exception ignored) {
                }
            }
            final boolean isGdiGeneric = renderer != null && renderer.contains("GDI Generic");

            if ((tooOld || isGdiGeneric) && WARNED.compareAndSet(false, true)) {
                final String rendererStr = renderer != null ? renderer : "unknown";
                final String versionStr = version != null ? version : "unknown";
                Logger.getLogger(VizEngineGraphCanvasManager.class.getName()).log(
                    Level.WARNING, "Unsupported OpenGL: renderer={0}, version={1}",
                    new Object[] {rendererStr, versionStr});
                SwingUtilities.invokeLater(() -> {
                    String msg = NbBundle.getMessage(VizEngineGraphCanvasManager.class,
                        "VizEngineGraphCanvasManager.opengl.unsupported.message", rendererStr, versionStr);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                });
            }
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }

        @Override
        public void display(GLAutoDrawable drawable) {
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }
    }
}
