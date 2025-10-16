package org.gephi.viz.engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.spi.PipelinedExecutor;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.spi.WorldData;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.spi.WorldUpdaterExecutionMode;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.TimeUtils;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;

/**
 * @param <R> Rendering target
 * @param <I> Events type
 * @author Eduardo Ramos
 */
public class VizEngine<R extends RenderingTarget, I> {

    public static final int DEFAULT_MAX_WORLD_UPDATES_PER_SECOND = 60;
    private static final RenderingLayer[] ALL_LAYERS = RenderingLayer.values();

    //Rendering target
    private final R renderingTarget;
    private boolean isSetUp = false;
    private boolean isPaused = false;
    private boolean isDestroyed = false;

    //State
    private int width = 0;
    private int height = 0;
    private Rect2D viewBoundaries = new Rect2D(0, 0, 0, 0);

    //Matrix
    private final Matrix4f modelMatrix = new Matrix4f().identity();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f modelViewProjectionMatrix = new Matrix4f();
    private final Matrix4f modelViewProjectionMatrixInverted = new Matrix4f();

    private final float[] modelViewProjectionMatrixFloats = new float[16];

    private final Vector2f translate = new Vector2f();

    // OpenGL options
    private final OpenGLOptions openGLOptions;

    //Renderers:
    private final Set<Renderer<R, ? extends WorldData>> allRenderers = new LinkedHashSet<>();
    private final List<Renderer<R, ? extends WorldData>> renderersPipeline = new ArrayList<>();

    //World updaters:
    private final Set<WorldUpdater<R>> allUpdaters = new LinkedHashSet<>();
    private final List<WorldUpdater<R>> updatersPipeline = new ArrayList<>();
    private final ExecutorService worldUpdaterManagerThread;
    private ExecutorService updatersThreadPool;
    private final WorldUpdaterExecutionMode worldUpdatersExecutionMode =
        WorldUpdaterExecutionMode.CONCURRENT_ASYNCHRONOUS;
    private Future<VizEngineModel> allUpdatersCompletableFuture = null;

    //Input listeners:
    private final Queue<I> eventsQueue = new ConcurrentLinkedQueue<>();
    private final Set<InputListener<R, I>> allInputListeners = new LinkedHashSet<>();
    private final List<InputListener<R, I>> inputListenersPipeline = new ArrayList<>();

    //Model, can't be null
    private volatile VizEngineModel engineModel;
    private List<? extends WorldData> currentWorldData = Collections.emptyList();

    //Settings:
    private int maxWorldUpdatesPerSecond = DEFAULT_MAX_WORLD_UPDATES_PER_SECOND;

    public VizEngine(R renderingTarget) {
        this.engineModel = VizEngineModel.createEmptyModel();
        this.openGLOptions = new OpenGLOptions();
        this.renderingTarget = Objects.requireNonNull(renderingTarget, "renderingTarget mandatory");
        this.worldUpdaterManagerThread = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "World Updater Manager"));
        loadModelViewProjection();
    }

    private void setup() {
        this.renderingTarget.setup(this);

        if (isSetUp) {
            return;
        }

        isSetUp = true;
        Logger.getLogger(VizEngine.class.getName())
            .log(Level.INFO, "World updaters execution mode: {0}", worldUpdatersExecutionMode);
    }

    public R getRenderingTarget() {
        return renderingTarget;
    }

    public OpenGLOptions getOpenGLOptions() {
        return openGLOptions;
    }

    private <T extends PipelinedExecutor> void setupPipelineOfElements(Set<T> allAvailable, List<T> dest,
                                                                       String elementType) {
        final List<T> elements = new ArrayList<>();

        final Set<String> categories = new HashSet<>();

        for (T t : allAvailable) {
            categories.add(t.getCategory());
        }

        categories.forEach((category) -> {
            //Find the best renderer:
            T bestElement = null;
            for (T r : allAvailable) {
                if (r.isAvailable(renderingTarget) && category.equals(r.getCategory())
                    && (bestElement == null || bestElement.getPreferenceInCategory() < r.getPreferenceInCategory())) {
                    bestElement = r;
                }
            }

            if (bestElement != null) {
                elements.add(bestElement);
                Logger.getLogger(VizEngine.class.getName()).log(Level.INFO,
                    "Using best available {0} ''{1}'' for category {2}",
                    new Object[] {elementType, bestElement.getName(), category});
            } else {
                Logger.getLogger(VizEngine.class.getName()).log(Level.WARNING,
                    "No available {0} for category {1}", new Object[] {elementType, category});
            }
        });

        dest.clear();
        dest.addAll(elements);
        dest.sort(new PipelinedExecutor.Comparator());
    }

    private void setupRenderersPipeline() {
        setupPipelineOfElements(allRenderers, renderersPipeline, "Renderer");
    }

    private void setupWorldUpdatersPipeline() {
        setupPipelineOfElements(allUpdaters, updatersPipeline, "WorldUpdater");
    }

    private void setupInputListenersPipeline() {
        setupPipelineOfElements(allInputListeners, inputListenersPipeline, "InputListener");
    }

    public void addInputListener(InputListener<R, I> listener) {
        allInputListeners.add(listener);
    }

    public void addRenderer(Renderer<R, ? extends WorldData> renderer) {
        if (renderer != null) {
            allRenderers.add(renderer);
        }
    }

    public void addWorldUpdater(WorldUpdater<R> updater) {
        if (updater != null) {
            allUpdaters.add(updater);
        }
    }

    public WorldUpdaterExecutionMode getWorldUpdatersExecutionMode() {
        return worldUpdatersExecutionMode;
    }

    public boolean isWorldUpdaterInPipeline(WorldUpdater<R> renderer) {
        return updatersPipeline.contains(renderer);
    }

    public Vector2fc getTranslate() {
        return translate;
    }

    public Vector2f getTranslate(Vector2f dest) {
        return dest.set(translate);
    }

    public void setTranslate(float x, float y) {
        translate.set(x, y);
        engineModel.getRenderingOptions().setPan(translate);
        loadModelViewProjection();
    }

    public void setTranslate(Vector2fc value) {
        translate.set(value);
        engineModel.getRenderingOptions().setPan(translate);
        loadModelViewProjection();
    }

    public void translate(float x, float y) {
        translate.add(x, y);
        engineModel.getRenderingOptions().setPan(translate);
        loadModelViewProjection();
    }

    public void translate(Vector2fc value) {
        translate.add(value);
        engineModel.getRenderingOptions().setPan(translate);
        loadModelViewProjection();
    }

    public float getZoom() {
        return engineModel.getRenderingOptions().getZoom();
    }

    public int getFps() {
        return renderingTarget.getFps();
    }

    public void setZoom(float zoom) {
        engineModel.getRenderingOptions().setZoom(zoom);
        loadModelViewProjection();
    }

    public float aspectRatio() {
        return (float) this.width / this.height;
    }

    public void centerOnGraph() {
        final Rect2D visibleGraphBoundaries = engineModel.getGraphIndex().getGraphBoundaries();

        final float[] center = visibleGraphBoundaries.center();
        centerOn(new Vector2f(center[0], center[1]), visibleGraphBoundaries.width(), visibleGraphBoundaries.height());
    }

    public void centerOn(Vector2fc center, float width, float height) {
        setTranslate(-center.x(), -center.y());

        if (width > 0 && height > 0) {
            final Rect2D visibleRange = getViewBoundaries();
            final float zoomFactor = Math.max(width / visibleRange.width(), height / visibleRange.height());

            engineModel.getRenderingOptions().setZoom(getZoom() / zoomFactor);
        }

        loadModelViewProjection();
    }

    private void loadModelViewProjection() {
        loadModel();
        loadView();
        loadProjection();

        projectionMatrix.mulAffine(viewMatrix, modelViewProjectionMatrix);
        modelViewProjectionMatrix.mulAffine(modelMatrix);

        modelViewProjectionMatrix.get(modelViewProjectionMatrixFloats);
        modelViewProjectionMatrix.invertAffine(modelViewProjectionMatrixInverted);

        calculateWorldBoundaries();
    }

    private void loadModel() {
        //Always identity at the moment
    }

    private void loadView() {
        float zoom = getZoom();
        viewMatrix.scaling(zoom, zoom, 1f);
        viewMatrix.translate(translate.x, translate.y, 0);
    }

    private void loadProjection() {
        projectionMatrix.setOrtho2D(-width / 2f, width / 2f, -height / 2f, height / 2f);
    }

    private void calculateWorldBoundaries() {
        final Vector3f minCoords = new Vector3f();
        final Vector3f maxCoords = new Vector3f();

        modelViewProjectionMatrixInverted.transformAab(-1, -1, 0, 1, 1, 0, minCoords, maxCoords);

        viewBoundaries = new Rect2D(minCoords.x, minCoords.y, maxCoords.x, maxCoords.y);
    }

    public void reshape(int width, int height) {
        this.width = width;
        this.height = height;

        loadModelViewProjection();
    }

    public synchronized void start() {
        isPaused = false;
        if (isDestroyed) {
            throw new IllegalStateException("VizEngine already destroyed, cannot start again. Use pause instead");
        }

        setup();
        renderingTarget.start();
    }

    public synchronized void setGraphModel(GraphModel graphModel, GraphRenderingOptions renderingOptions) {
        this.engineModel = new VizEngineModel(graphModel,
            renderingOptions != null ? renderingOptions : new GraphRenderingOptionsImpl());
        // Sync local translate from new model's pan
        this.translate.set(engineModel.getRenderingOptions().getPan());
        loadModelViewProjection();
    }

    public synchronized void initPipeline() {
        setupRenderersPipeline();
        setupWorldUpdatersPipeline();
        setupInputListenersPipeline();

        updatersPipeline.forEach((worldUpdater) -> {
            worldUpdater.init(renderingTarget);
        });

        renderersPipeline.forEach((renderer) -> {
            renderer.init(renderingTarget);
        });

        // Setup world updater threads
        if (worldUpdatersExecutionMode.isConcurrent()) {
            final int numThreads = Math.max(Math.min(updatersPipeline.size(), 4), 1);
            updatersThreadPool = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
                private int id = 1;

                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "World Updater " + id++);
                }
            });
        } else {
            updatersThreadPool = null;
        }

        loadModelViewProjection();
    }

    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized void destroy() {
        allInputListeners.clear();
        inputListenersPipeline.clear();

        this.renderingTarget.stop();
        if (worldUpdatersExecutionMode.isConcurrent()) {
            try {
                updatersThreadPool.shutdown();

                final boolean terminated = updatersThreadPool.awaitTermination(5, TimeUnit.SECONDS);
                if (!terminated) {
                    updatersThreadPool.shutdownNow();
                }

                worldUpdaterManagerThread.shutdown();

                final boolean managerTerminated = worldUpdaterManagerThread.awaitTermination(1, TimeUnit.SECONDS);
                if (!managerTerminated) {
                    worldUpdaterManagerThread.shutdownNow();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(VizEngine.class.getName())
                    .log(Level.WARNING, "Interrupted while destroying VizEngine", ex);
            }
        }

        Logger.getLogger(VizEngine.class.getName())
            .log(Level.INFO, "Disposing {0} world updaters", updatersPipeline.size());
        updatersPipeline.forEach((worldUpdater) -> {
            worldUpdater.dispose(renderingTarget);
        });

        Logger.getLogger(VizEngine.class.getName())
            .log(Level.INFO, "Disposing {0} renderers", renderersPipeline.size());
        renderersPipeline.forEach((renderer) -> {
            renderer.dispose(renderingTarget);
        });

        this.isDestroyed = true;
    }

    private CompletableFuture<Void> buildUpdaterFuture(final WorldUpdater<R> updater,
                                                       final VizEngineModel engineModel) {
        return CompletableFuture.runAsync(() -> {
            try {
                updater.updateWorld(engineModel);
            } catch (Throwable t) {
                Logger.getLogger(VizEngine.class.getName()).log(Level.SEVERE, null, t);
            }
        }, updatersThreadPool);
    }


    @SuppressWarnings("unchecked")
    public void display() {
        if (isPaused) {
            return;
        }

        renderingTarget.frameStart();

        // Get world data (might be empty if no world update was done this frame)
        List<? extends WorldData> worldData =
            worldUpdatersExecutionMode.isConcurrent() ? checkConcurrentWorldUpdateIsDone()
                : runWorldUpdatersSynchronous(this.engineModel);
        if (worldData.isEmpty()) {
            // No world update was done this frame, use last one
            worldData = currentWorldData;
        }

        // Render
        if (!worldData.isEmpty()) {
            for (RenderingLayer layer : ALL_LAYERS) {
                int rendererIndex = 0;
                for (Renderer<R, ? extends WorldData> renderer : renderersPipeline) {
                    if (renderer.getLayers().contains(layer)) {
                        // Get world data for this renderer:
                        WorldData localWorldData = worldData.get(rendererIndex);
                        ((Renderer<R, WorldData>) renderer).render(localWorldData, renderingTarget, layer);
                    }
                    rendererIndex++;
                }
            }
        }

        //Schedule next concurrent world update:
        if (worldUpdatersExecutionMode.isConcurrent()) {
            scheduleNextConcurrentWorldUpdateIfDone(this.engineModel);
        }

        // Commit model used for rendering this frame
        currentWorldData = worldData;

        renderingTarget.frameEnd();
    }

    private long lastWorldUpdateMillis = 0;

    private List<? extends WorldData> runWorldUpdatersSynchronous(VizEngineModel model) {
        //Control max world updates per second
        if (maxWorldUpdatesPerSecond >= 1) {
            if (TimeUtils.getTimeMillis() < lastWorldUpdateMillis + 1000 / maxWorldUpdatesPerSecond) {
                //Skip world update
                return Collections.emptyList();
            }
        }
        processInputEvents(model);

        for (WorldUpdater<R> worldUpdater : updatersPipeline) {
            worldUpdater.updateWorld(model);
        }
        lastWorldUpdateMillis = TimeUtils.getTimeMillis();

        return renderersPipeline.stream().map(
            r -> r.worldUpdated(model, renderingTarget)
        ).collect(Collectors.toList());
    }

    private List<? extends WorldData> checkConcurrentWorldUpdateIsDone() {
        if (allUpdatersCompletableFuture != null) {
            if (worldUpdatersExecutionMode.isSynchronous()) {
                return runWorldUpdated();
            } else {
                //Notify renderers if next concurrent asynchronous world data update is done:
                final boolean worldUpdateDone =
                    allUpdatersCompletableFuture.isDone();
                if (worldUpdateDone) {
                    return runWorldUpdated();
                }
            }
        }
        return Collections.emptyList();
    }

    private List<? extends WorldData> runWorldUpdated() {
        try {
            VizEngineModel modelUsedByUpdaters = allUpdatersCompletableFuture.get();
            allUpdatersCompletableFuture = null;

            return renderersPipeline.stream().map(
                r -> r.worldUpdated(modelUsedByUpdaters, renderingTarget)
            ).toList();
        } catch (Throwable t) {
            Logger.getLogger(VizEngine.class.getName()).log(Level.SEVERE, null, t);
            throw new RuntimeException(t);
        }
    }

    private void scheduleNextConcurrentWorldUpdateIfDone(VizEngineModel model) {
        if (!updatersThreadPool.isShutdown() && allUpdatersCompletableFuture == null) {
            //Control max world updates per second
            if (maxWorldUpdatesPerSecond >= 1) {
                if (TimeUtils.getTimeMillis() < lastWorldUpdateMillis + 1000 / maxWorldUpdatesPerSecond) {
                    //Skip world update
                    return;
                }
            }

            // Associate local model to the future
            allUpdatersCompletableFuture = CompletableFuture.supplyAsync(() -> {
                // Process input events
                processInputEvents(model);

                // Create and start a world update for each updater
                List<CompletableFuture<Void>> futures =
                    updatersPipeline.stream().map(u -> buildUpdaterFuture(u, model)).toList();

                // Finish executing all updaters and wait for them to finish
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                return model;
            }, worldUpdaterManagerThread);

            lastWorldUpdateMillis = TimeUtils.getTimeMillis();
        }
    }

    public GraphModel getGraphModel() {
        return engineModel.getGraphModel();
    }

    public GraphIndex getGraphIndex() {
        return engineModel.getGraphIndex();
    }

    public GraphSelection getGraphSelection() {
        return engineModel.getGraphSelection();
    }

    public GraphRenderingOptions getRenderingOptions() {
        return engineModel.getRenderingOptions();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Matrix4fc getModelMatrix() {
        return modelMatrix;
    }

    public Matrix4fc getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4fc getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4fc getModelViewProjectionMatrix() {
        return modelViewProjectionMatrix;
    }

    public Matrix4fc getModelViewProjectionMatrixInverted() {
        return modelViewProjectionMatrixInverted;
    }

    public Rect2D getViewBoundaries() {
        return viewBoundaries;
    }

    public void getBackgroundColor(float[] backgroundColorFloats) {
        System.arraycopy(engineModel.getRenderingOptions().getBackgroundColor(), 0, backgroundColorFloats, 0, 4);
    }

    public float[] getBackgroundColor() {
        float[] backgroundColor = engineModel.getRenderingOptions().getBackgroundColor();
        return Arrays.copyOf(backgroundColor, backgroundColor.length);
    }

    public void setBackgroundColor(Color color) {
        float[] backgroundColorComponents = new float[4];
        color.getRGBComponents(backgroundColorComponents);

        setBackgroundColor(backgroundColorComponents);
    }

    public void setBackgroundColor(float[] backgroundColor) {
        if (backgroundColor.length != 4) {
            throw new IllegalArgumentException("Expected 4 float RGBA color");
        }

        engineModel.getRenderingOptions().setBackgroundColor(Arrays.copyOf(backgroundColor, backgroundColor.length));
    }

    public int getMaxWorldUpdatesPerSecond() {
        return maxWorldUpdatesPerSecond;
    }

    public void setMaxWorldUpdatesPerSecond(int maxWorldUpdatesPerSecond) {
        this.maxWorldUpdatesPerSecond = maxWorldUpdatesPerSecond;
    }

    public void getModelViewProjectionMatrixFloats(float[] mvpFloats) {
        modelViewProjectionMatrix.get(mvpFloats);
    }

    public float[] getModelViewProjectionMatrixFloats() {
        return Arrays.copyOf(modelViewProjectionMatrixFloats, modelViewProjectionMatrixFloats.length);
    }

    public Vector2f screenCoordinatesToWorldCoordinates(int x, int y) {
        return screenCoordinatesToWorldCoordinates(x, y, new Vector2f());
    }

    public Vector2f screenCoordinatesToWorldCoordinates(int x, int y, Vector2f dest) {
        final float halfWidth = width / 2.0f;
        final float halfHeight = height / 2.0f;

        float xScreenNormalized = (-halfWidth + x) / halfWidth;
        float yScreenNormalized = (halfHeight - y) / halfHeight;

        final Vector3f worldCoordinates = new Vector3f();
        modelViewProjectionMatrixInverted.transformProject(xScreenNormalized, yScreenNormalized, 0, worldCoordinates);

        return dest.set(worldCoordinates.x, worldCoordinates.y);
    }

    public Vector2f worldCoordinatesToScreenCoordinates(float x, float y, Vector3f tempNDC, Vector2f dest) {
        // 1) world -> NDC
        final Vector3f ndc = new Vector3f();
        modelViewProjectionMatrix.transformProject(x, y, 0f, ndc); // ndc in [-1, 1]

        // 2) NDC -> pixels (origin at top-left)
        final float halfW = width / 2f;
        final float halfH = height / 2f;

        final float sx = halfW + ndc.x * halfW;   // map [-1,1] -> [0,width]
        final float sy = halfH + ndc.y * halfH;   // map [-1,1] -> [0,]

        return dest.set(sx, sy);
    }

    private void processInputEvents(VizEngineModel model) {
        for (InputListener<R, I> inputListener : inputListenersPipeline) {
            inputListener.frameStart(model);
        }

        // Collect all events from queue into a list
        List<I> events = new ArrayList<>();
        I event;
        while ((event = eventsQueue.poll()) != null) {
            events.add(event);
        }

        // Pass events through the pipeline
        // Each listener can compress/consume events and return remaining ones
        for (InputListener<R, I> inputListener : inputListenersPipeline) {
            events = inputListener.processEvents(events);
            if (events.isEmpty()) {
                break; // All events consumed, stop propagation
            }
        }

        for (InputListener<R, I> inputListener : inputListenersPipeline) {
            inputListener.frameEnd(model);
        }
    }

    public void queueEvent(I e) {
        eventsQueue.offer(e);
    }
}
