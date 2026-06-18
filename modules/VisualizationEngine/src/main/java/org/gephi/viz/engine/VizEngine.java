package org.gephi.viz.engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.ElementsCallback;
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
import org.gephi.viz.engine.status.GraphSelectionImpl;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.util.TimeUtils;
import org.gephi.viz.engine.util.gl.DataUploadStats;
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
    public static final int DEFAULT_FPS = 60;
    public static final boolean DEFAULT_DARK_LAF = false;
    private static final RenderingLayer[] ALL_LAYERS = RenderingLayer.values();

    //Rendering target
    private final R renderingTarget;
    private boolean isSetUp = false;
    private boolean isDestroyed = false;
    private volatile boolean updating = true;

    //State
    private int width = 0;
    private int height = 0;
    private Rect2D viewBoundaries = new Rect2D(0, 0, 0, 0);

    //Matrix
    private final Matrix4f modelMatrix = new Matrix4f().identity();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f modelViewProjectionMatrix = new Matrix4f();
    float[] mvpFloats = new float[16]; // Float[] version of modelViewProjectionMatrix

    private final Matrix4f modelViewProjectionMatrixInverted = new Matrix4f();

    private final float[] modelViewProjectionMatrixFloats = new float[16];

    private final Vector2f translate = new Vector2f();

    // OpenGL options
    private final OpenGLOptions openGLOptions;

    //Renderers:
    private final Set<Renderer<R, ? extends WorldData>> allRenderers = new LinkedHashSet<>();
    private final List<Renderer<R, ? extends WorldData>> renderersPipeline = new ArrayList<>();

    //World updaters:
    private final Set<WorldUpdater<R, ?>> allUpdaters = new LinkedHashSet<>();
    private final List<WorldUpdater<R, ?>> updatersPipeline = new ArrayList<>();
    private final List<ElementsCallback<?>> updatersElementsCallbacks = new ArrayList<>();
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
    private boolean darkLaf = DEFAULT_DARK_LAF;
    private final int maxWorldUpdatesPerSecond = DEFAULT_MAX_WORLD_UPDATES_PER_SECOND;

    // --- Redundant world-update skipping (opt-in via markDataDirty) ---
    // While a graph is interacted with, the engine reschedules a full world update every throttle
    // tick even when nothing changed, redoing O(N+E) CPU work and GPU uploads. Once a host opts in
    // by calling markDataDirty(), the engine instead skips the update on frames where nothing
    // relevant changed: graph structure (GraphObserver), selection, rendering options or view
    // boundaries. Node geometry (x/y/size/color) has no cheap change signal in graphstore, so the
    // host must call markDataDirty() whenever it mutates it (layout, manual move, appearance).
    // Until the first markDataDirty() call the engine keeps its original always-update behaviour,
    // so hosts that mutate the graph without notifying stay correct (uploads are still de-duplicated
    // downstream by content hashing).
    private volatile boolean hostManagesDirtiness = false;
    private volatile boolean dataDirty = true;
    private final Object graphObserverLock = new Object();
    private GraphObserver graphObserver;
    private GraphModel graphObserverModel;
    private long lastWorldSignature = Long.MIN_VALUE;

    //CPU-side timings of the most recent display() call. Read by external
    //performance monitors; written only from the render thread. Disabled by
    //default so production callers don't pay the cost of the nanoTime calls
    //and the per-frame Map allocation; enable with
    //{@link #setFrameTimingsEnabled(boolean)}.
    private volatile boolean frameTimingsEnabled = false;
    private volatile FrameTimings lastFrameTimings = FrameTimings.EMPTY;

    public VizEngine(R renderingTarget) {
        this.engineModel = createEmptyModel();
        this.openGLOptions = new OpenGLOptions();
        this.renderingTarget = Objects.requireNonNull(renderingTarget, "renderingTarget mandatory");
        this.worldUpdaterManagerThread = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "World Updater Manager"));
        loadModelViewProjection();
    }

    private void setup() {
        if (isSetUp) {
            return;
        }

        this.renderingTarget.setup(this);

        isSetUp = true;
        Logger.getLogger(VizEngine.class.getSimpleName())
            .log(Level.FINE, "World updaters execution mode: {0}", worldUpdatersExecutionMode);
    }

    public R getRenderingTarget() {
        return renderingTarget;
    }

    /**
     * The renderers actually used to draw each frame (one per category, already filtered by
     * availability and preference). Read-only; intended for rendering targets that need to reach a
     * specific active renderer (e.g. for GPU picking).
     */
    public List<Renderer<R, ? extends WorldData>> getRenderersPipeline() {
        return Collections.unmodifiableList(renderersPipeline);
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
                Logger.getLogger(VizEngine.class.getSimpleName()).log(Level.FINE,
                    "Using best available {0} ''{1}'' for category {2}",
                    new Object[] {elementType, bestElement.getName(), category});
            } else {
                Logger.getLogger(VizEngine.class.getSimpleName()).log(Level.WARNING,
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

    private void setupElementsCallbackPipeline() {
        updatersElementsCallbacks.clear();
        for (WorldUpdater<R, ?> updater : updatersPipeline) {
            ElementsCallback<?> callback = updater.getElementsCallback();
            if (callback != null && !updatersElementsCallbacks.contains(callback)) {
                updatersElementsCallbacks.add(callback);
            }
        }
    }

    public void addInputListener(InputListener<R, I> listener) {
        allInputListeners.add(listener);
    }

    public void addRenderer(Renderer<R, ? extends WorldData> renderer) {
        if (renderer != null) {
            allRenderers.add(renderer);
        }
    }

    public void addWorldUpdater(WorldUpdater<R, ?> updater) {
        if (updater != null) {
            allUpdaters.add(updater);
        }
    }

    public WorldUpdaterExecutionMode getWorldUpdatersExecutionMode() {
        return worldUpdatersExecutionMode;
    }

    public boolean isWorldUpdaterInPipeline(WorldUpdater<R, ?> renderer) {
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

        final float width = visibleGraphBoundaries.width();
        final float height = visibleGraphBoundaries.height();
        if (Float.isInfinite(width) || Float.isInfinite(height)) {
            return;
        }

        final float[] center = visibleGraphBoundaries.center();
        centerOn(new Vector2f(center[0], center[1]), width, height);
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

    /**
     * Centers the view on a specific tile of a larger image by adjusting the zoom and translation.
     *
     * @param tileX       the X coordinate of the tile in the larger image
     * @param tileY       the Y coordinate of the tile in the larger image
     * @param imageWidth  the width of the full image
     * @param imageHeight the height of the full image
     */
    public void centerOnTile(float tileX, float tileY, float imageWidth, float imageHeight) {
        // Calculate scale factor from the full image dimensions
        float scaleFactor = imageWidth / width;

        // Calculate the offset of this tile from the top-left corner of the full image
        float tileOffsetX = (tileX + width / 2f - imageWidth / 2f) / width;
        float tileOffsetY = (tileY + height / 2f - imageHeight / 2f) / height;

        // Apply zoom scaling
        float newZoom = getZoom() * scaleFactor;
        engineModel.getRenderingOptions().setZoom(newZoom);

        // Adjust translate based on tile offset and original translate
        // The tile offset needs to be in world coordinates, so we divide by the new zoom
        float translateOffsetX = -tileOffsetX * width / newZoom;
        float translateOffsetY = -tileOffsetY * height / newZoom;

        Vector2fc pan = engineModel.getRenderingOptions().getPan();
        translate.set(
            pan.x() + translateOffsetX,
            pan.y() + translateOffsetY
        );

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
        if (isDestroyed) {
            throw new IllegalStateException("VizEngine already destroyed, cannot start again. Use pause instead");
        }

        setup();
    }

    public synchronized void setGraphModel(GraphModel graphModel, GraphRenderingOptions renderingOptions,
                                           GraphSelection graphSelection) {
        if (this.engineModel.getGraphModel() != graphModel) {
            this.engineModel = new VizEngineModel(graphModel,
                renderingOptions != null ? renderingOptions : new GraphRenderingOptionsImpl(darkLaf),
                graphSelection);
            // New model: drop the stale observer/signature and force a refresh on the next frame.
            destroyGraphObserver();
            lastWorldSignature = Long.MIN_VALUE;
            dataDirty = true;
        }

        // Sync local translate from new model's pan
        this.translate.set(engineModel.getRenderingOptions().getPan());
        loadModelViewProjection();
    }

    public synchronized void unsetGraphModel(GraphModel graphModel) {
        if (engineModel.getGraphModel() == graphModel) {
            this.engineModel = createEmptyModel();
            this.translate.set(0, 0);
            destroyGraphObserver();
            lastWorldSignature = Long.MIN_VALUE;
            dataDirty = true;
            loadModelViewProjection();
        }
    }

    private VizEngineModel createEmptyModel() {
        Configuration config = Configuration.builder().enableSpatialIndex(true).build();
        GraphModel emptyModel = GraphModel.Factory.newInstance(config);
        return new VizEngineModel(emptyModel, new GraphRenderingOptionsImpl(darkLaf), null);
    }

    public synchronized void initPipeline() {
        setupRenderersPipeline();
        setupWorldUpdatersPipeline();
        setupInputListenersPipeline();
        setupElementsCallbackPipeline();

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

    public synchronized void disposePipeline() {
        // Cancel any pending world updates
        if (allUpdatersCompletableFuture != null) {
            allUpdatersCompletableFuture.cancel(false);
            allUpdatersCompletableFuture = null;
        }

        // Shutdown thread pool if it exists
        if (updatersThreadPool != null) {
            updatersThreadPool.shutdown();
            try {
                updatersThreadPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(VizEngine.class.getSimpleName())
                    .log(Level.WARNING, "Interrupted while disposing VizEngine", ex);
            }
        }

        updatersPipeline.forEach((worldUpdater) -> {
            worldUpdater.dispose(renderingTarget);
        });
        updatersElementsCallbacks.forEach(ElementsCallback::reset);

        renderersPipeline.forEach((renderer) -> {
            renderer.dispose(renderingTarget);
        });

        // Clear all pipelines and world data
        renderersPipeline.clear();
        updatersPipeline.clear();
        updatersElementsCallbacks.clear();
        inputListenersPipeline.clear();

        // Clear current world data to prevent using disposed resources
        currentWorldData = Collections.emptyList();

        // Clear any pending input events
        eventsQueue.clear();

        // Reset world update timing to allow immediate update on next init
        lastWorldUpdateMillis = 0;

        // Drop the change observer; force a refresh if the pipeline is re-initialized.
        destroyGraphObserver();
        lastWorldSignature = Long.MIN_VALUE;
        dataDirty = true;
    }

    public synchronized void destroy() {
        allInputListeners.clear();
        inputListenersPipeline.clear();

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
                Logger.getLogger(VizEngine.class.getSimpleName())
                    .log(Level.WARNING, "Interrupted while destroying VizEngine", ex);
            }
        }

        Logger.getLogger(VizEngine.class.getSimpleName())
            .log(Level.FINE, "Disposing {0} world updaters", updatersPipeline.size());
        updatersPipeline.forEach((worldUpdater) -> {
            worldUpdater.dispose(renderingTarget);
        });
        updatersElementsCallbacks.forEach(ElementsCallback::reset);

        Logger.getLogger(VizEngine.class.getSimpleName())
            .log(Level.FINE, "Disposing {0} renderers", renderersPipeline.size());
        renderersPipeline.forEach((renderer) -> {
            renderer.dispose(renderingTarget);
        });

        destroyGraphObserver();

        this.isDestroyed = true;
    }

    private CompletableFuture<Void> buildUpdaterFuture(final WorldUpdater<R, ?> updater,
                                                       final VizEngineModel engineModel) {
        return CompletableFuture.runAsync(() -> {
            try {
                updater.updateWorld(engineModel);
            } catch (Throwable t) {
                Logger.getLogger(VizEngine.class.getSimpleName()).log(Level.SEVERE, null, t);
            }
        }, updatersThreadPool);
    }

    private CompletableFuture<Void> buildCallbackFuture(final ElementsCallback<?> callback,
                                                        final GraphIndex graphIndex,
                                                        final GraphRenderingOptions renderingOptions,
                                                        final Rect2D boundaries) {
        return CompletableFuture.runAsync(() -> {
            try {
                callback.run(graphIndex, renderingOptions, boundaries);
            } catch (Throwable t) {
                Logger.getLogger(VizEngine.class.getSimpleName()).log(Level.SEVERE, null, t);
            }
        }, updatersThreadPool);
    }


    @SuppressWarnings("unchecked")
    public void display() {
        // Snapshot the flag once so the rest of the method observes a single
        // value (and so HotSpot can hoist the check).
        final boolean timed = frameTimingsEnabled;

        final long frameStartNs = timed ? System.nanoTime() : 0L;
        renderingTarget.frameStart();

        // Get world data (might be empty if no world update was done this frame)
        final long worldUpdateStartNs = timed ? System.nanoTime() : 0L;
        List<? extends WorldData> worldData =
            worldUpdatersExecutionMode.isConcurrent() ? checkConcurrentWorldUpdateIsDone()
                : runWorldUpdatersSynchronous(this.engineModel);
        if (worldData.isEmpty() || !updating) {
            // No world update was done this frame, use last one
            worldData = currentWorldData;
        }
        final long worldUpdateNs = timed ? System.nanoTime() - worldUpdateStartNs : 0L;

        // Render

        // Fetch mvpMatrix
        modelViewProjectionMatrix.get(mvpFloats);

        final LinkedHashMap<String, Long> perCategoryNs = timed ? new LinkedHashMap<>() : null;
        final long renderStartNs = timed ? System.nanoTime() : 0L;
        if (!worldData.isEmpty()) {
            for (RenderingLayer layer : ALL_LAYERS) {
                int rendererIndex = 0;
                for (Renderer<R, ? extends WorldData> renderer : renderersPipeline) {
                    if (renderer.getLayers().contains(layer)) {
                        // Get world data for this renderer:
                        WorldData localWorldData = worldData.get(rendererIndex);
                        if (timed) {
                            final long rs = System.nanoTime();
                            ((Renderer<R, WorldData>) renderer).render(localWorldData, renderingTarget, layer,
                                mvpFloats);
                            perCategoryNs.merge(renderer.getCategory(), System.nanoTime() - rs, Long::sum);
                        } else {
                            ((Renderer<R, WorldData>) renderer).render(localWorldData, renderingTarget, layer,
                                mvpFloats);
                        }
                    }
                    rendererIndex++;
                }
            }
        }
        final long renderNs = timed ? System.nanoTime() - renderStartNs : 0L;

        //Schedule next concurrent world update:
        if (worldUpdatersExecutionMode.isConcurrent() && updating) {
            scheduleNextConcurrentWorldUpdateIfDone(this.engineModel);
        }

        // Commit model used for rendering this frame
        currentWorldData = worldData;

        renderingTarget.frameEnd();

        if (timed) {
            final long totalNs = System.nanoTime() - frameStartNs;
            lastFrameTimings = new FrameTimings(
                totalNs, worldUpdateNs, renderNs, Map.copyOf(perCategoryNs));
        }
    }

    /**
     * Returns CPU-side timings collected during the most recent
     * {@link #display()} call, or {@link FrameTimings#EMPTY} when timing
     * collection is disabled. Safe to call from any thread; values are
     * updated atomically once per frame.
     */
    public FrameTimings getLastFrameTimings() {
        return lastFrameTimings;
    }

    /**
     * Whether per-frame CPU timings (see {@link #getLastFrameTimings()}) are
     * collected during {@link #display()}.
     */
    public boolean isFrameTimingsEnabled() {
        return frameTimingsEnabled;
    }

    /**
     * Toggles per-frame CPU timing collection. Off by default so callers
     * don't pay the cost of the {@code nanoTime} calls and the per-frame
     * map allocation. When turned off, {@link #getLastFrameTimings()} starts
     * returning {@link FrameTimings#EMPTY} again from the next frame on.
     */
    public void setFrameTimingsEnabled(boolean enabled) {
        this.frameTimingsEnabled = enabled;
        if (!enabled) {
            this.lastFrameTimings = FrameTimings.EMPTY;
        }
    }

    private long lastWorldUpdateMillis = 0;

    private List<? extends WorldData> runWorldUpdatersSynchronous(VizEngineModel model) {
        if (!updating) {
            return Collections.emptyList();
        }

        //Control max world updates per second
        if (maxWorldUpdatesPerSecond >= 1) {
            if (TimeUtils.getTimeMillis() < lastWorldUpdateMillis + 1000 / maxWorldUpdatesPerSecond) {
                //Skip world update
                return Collections.emptyList();
            }
        }

        // Skip the whole update (CPU rebuild + GPU upload) when nothing relevant changed.
        if (!shouldRunWorldUpdate()) {
            DataUploadStats.recordWorldUpdateSkipped();
            return Collections.emptyList();
        }
        DataUploadStats.recordWorldUpdate();

        processInputEvents(model);

        Rect2D viewBoundaries = getViewBoundaries();
        for (ElementsCallback<?> callback : updatersElementsCallbacks) {
            callback.run(model.getGraphIndex(), model.getRenderingOptions(), viewBoundaries);
        }
        for (WorldUpdater<R, ?> worldUpdater : updatersPipeline) {
            worldUpdater.updateWorld(model);
        }
        lastWorldUpdateMillis = TimeUtils.getTimeMillis();

        return renderersPipeline.stream().map(
            r -> r.worldUpdated(model, renderingTarget, mvpFloats)
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
                r -> r.worldUpdated(modelUsedByUpdaters, renderingTarget, mvpFloats)
            ).toList();
        } catch (Throwable t) {
            Logger.getLogger(VizEngine.class.getSimpleName()).log(Level.SEVERE, null, t);
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

            // Skip the whole update (CPU rebuild + GPU upload) when nothing relevant changed.
            if (!shouldRunWorldUpdate()) {
                DataUploadStats.recordWorldUpdateSkipped();
                return;
            }
            DataUploadStats.recordWorldUpdate();

            // Associate local model to the future
            allUpdatersCompletableFuture = CompletableFuture.supplyAsync(() -> {
                // Process input events
                processInputEvents(model);

                // Run all elements callbacks first
                CompletableFuture.allOf(updatersElementsCallbacks.stream()
                    .map(c -> buildCallbackFuture(c, model.getGraphIndex(),
                        model.getRenderingOptions(), getViewBoundaries()))
                    .toArray(CompletableFuture[]::new)).join();

                // Run all world updaters
                CompletableFuture.allOf(updatersPipeline.stream()
                    .map(u -> buildUpdaterFuture(u, model))
                    .toArray(CompletableFuture[]::new)).join();

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

    public void setDarkLaf(boolean darkLaf) {
        this.darkLaf = darkLaf;
        if (darkLaf && Arrays.equals(engineModel.getRenderingOptions().getBackgroundColor(),
            GraphRenderingOptions.DEFAULT_BACKGROUND_COLOR)) {
            engineModel.getRenderingOptions().setBackgroundColor(GraphRenderingOptions.DEFAULT_DARK_BACKGROUND_COLOR);
        } else if (!darkLaf && Arrays.equals(engineModel.getRenderingOptions().getBackgroundColor(),
            GraphRenderingOptions.DEFAULT_DARK_BACKGROUND_COLOR)) {
            engineModel.getRenderingOptions().setBackgroundColor(GraphRenderingOptions.DEFAULT_BACKGROUND_COLOR);
        }
    }

    public void pauseUpdating() {
        updating = false;
    }

    public void resumeUpdating() {
        updating = true;
    }

    /**
     * Signals that the graph's node visual data (positions, sizes or colors) may have changed and
     * the world data must be rebuilt and re-uploaded on the next frame.
     * <p>
     * Calling this method also opts this engine into <i>redundant world-update skipping</i>: from
     * the first call on, the engine stops rescheduling a full world update on every frame and only
     * does so when something relevant changed (graph structure, selection, rendering options, view
     * boundaries) or when this method is called again. Hosts that run layouts, move nodes or apply
     * appearance changes should call this whenever they mutate node geometry/color so the view does
     * not freeze; structure/selection/option/view changes are detected automatically and do not
     * require a call.
     */
    public void markDataDirty() {
        this.hostManagesDirtiness = true;
        this.dataDirty = true;
    }

    /**
     * Decides whether a world update should run this tick. Always {@code true} until a host opts in
     * via {@link #markDataDirty()}; afterwards, only when a relevant input changed since the last
     * update. Has the side effect of consuming the change flags (graph observer, signature) when it
     * returns {@code true}.
     */
    private boolean shouldRunWorldUpdate() {
        if (!hostManagesDirtiness) {
            return true;
        }

        boolean changed = dataDirty;

        // hasGraphChanged() resets the observer, so it must be called every tick to not miss a change.
        if (pollGraphChanged()) {
            changed = true;
        }

        final long signature = computeWorldSignature();
        if (signature != lastWorldSignature) {
            changed = true;
        }

        if (changed) {
            lastWorldSignature = signature;
            dataDirty = false;
        }
        return changed;
    }

    /**
     * Lazily (re)creates the graph observer for the current model and returns whether the graph
     * structure changed since the last poll. Synchronized so the render-thread poll cannot race the
     * lifecycle methods that drop the observer.
     */
    private boolean pollGraphChanged() {
        synchronized (graphObserverLock) {
            final GraphModel model = engineModel.getGraphModel();
            if (model == null) {
                return false;
            }
            if (graphObserver == null || graphObserverModel != model || graphObserver.isDestroyed()) {
                destroyGraphObserverLocked();
                graphObserver = model.createGraphObserver(model.getGraphVisible(), false);
                graphObserverModel = model;
            }
            return graphObserver.hasGraphChanged();
        }
    }

    private void destroyGraphObserver() {
        synchronized (graphObserverLock) {
            destroyGraphObserverLocked();
        }
    }

    private void destroyGraphObserverLocked() {
        if (graphObserver != null) {
            if (!graphObserver.isDestroyed()) {
                graphObserver.destroy();
            }
            graphObserver = null;
            graphObserverModel = null;
        }
    }

    /**
     * Cheap 64-bit signature of everything (other than node geometry, see {@link #markDataDirty()})
     * that affects the world data: view boundaries, selection and rendering options. A change in any
     * of these flips the signature so the world update runs and re-uploads as needed.
     */
    private long computeWorldSignature() {
        long h = 1125899906842597L;

        final Rect2D vb = viewBoundaries;
        if (vb != null) {
            h = mix(h, Float.floatToIntBits(vb.minX));
            h = mix(h, Float.floatToIntBits(vb.minY));
            h = mix(h, Float.floatToIntBits(vb.maxX));
            h = mix(h, Float.floatToIntBits(vb.maxY));
        }

        final GraphSelection selection = engineModel.getGraphSelection();
        if (selection instanceof GraphSelectionImpl s) {
            h = mix(h, s.getNodes().hashCode());
            h = mix(h, s.getNodesWithNeighbours().hashCode());
            h = mix(h, s.getEdges().hashCode());
            h = mix(h, s.getMode() != null ? s.getMode().ordinal() : -1);
        }

        final GraphRenderingOptions o = engineModel.getRenderingOptions();
        h = mix(h, o.isShowNodes() ? 1 : 0);
        h = mix(h, o.isShowEdges() ? 1 : 0);
        h = mix(h, o.isShowNodeLabels() ? 1 : 0);
        h = mix(h, o.isShowEdgeLabels() ? 1 : 0);
        h = mix(h, o.isHideNonSelectedNodeLabels() ? 1 : 0);
        h = mix(h, o.isHideNonSelectedEdgeLabels() ? 1 : 0);
        h = mix(h, o.isHideNonSelectedEdges() ? 1 : 0);
        h = mix(h, o.isLightenNonSelected() ? 1 : 0);
        h = mix(h, Float.floatToIntBits(o.getLightenNonSelectedFactor()));
        h = mix(h, Float.floatToIntBits(o.getZoom()));
        h = mix(h, Float.floatToIntBits(o.getNodeScale()));
        h = mix(h, Float.floatToIntBits(o.getEdgeScale()));
        h = mix(h, o.isEdgeWeightEnabled() ? 1 : 0);
        h = mix(h, o.isEdgeRescaleWeightEnabled() ? 1 : 0);
        h = mix(h, Float.floatToIntBits(o.getEdgeRescaleMin()));
        h = mix(h, Float.floatToIntBits(o.getEdgeRescaleMax()));
        h = mix(h, o.isEdgeSelectionColor() ? 1 : 0);
        h = mix(h, o.getEdgeColorMode() != null ? o.getEdgeColorMode().ordinal() : -1);
        h = mix(h, o.getEdgeBothSelectionColor().getRGB());
        h = mix(h, o.getEdgeInSelectionColor().getRGB());
        h = mix(h, o.getEdgeOutSelectionColor().getRGB());
        final float[] bg = o.getBackgroundColor();
        if (bg != null) {
            for (float c : bg) {
                h = mix(h, Float.floatToIntBits(c));
            }
        }
        return h;
    }

    private static long mix(long h, int value) {
        h ^= value & 0xFFFFFFFFL;
        h *= 0x100000001b3L;
        return h;
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

    /**
     * Converts a world position (x, y) into the corresponding screen viewport position in pixels.
     *
     * @param x       World position X
     * @param y       World position Y
     * @param tempNDC Temporary vector to hold the NDC coordinates
     * @param dest    Result vector where screen position will be stored
     * @return The same dest vector
     */
    public Vector2f worldCoordinatesToScreenCoordinates(float x, float y, Vector3f tempNDC, Vector2f dest) {
        // 1) world -> NDC
        modelViewProjectionMatrix.transformProject(x, y, 0f, tempNDC); // ndc in [-1, 1]

        // 2) NDC -> pixels (origin at top-left)
        final float halfW = width / 2f;
        final float halfH = height / 2f;

        final float sx = halfW + tempNDC.x * halfW;   // map [-1,1] -> [0,width]
        final float sy = halfH + tempNDC.y * halfH;   // map [-1,1] -> [0,]

        return dest.set(sx, sy);
    }

    /**
     * Returns the local number of screen pixels that correspond to one world unit near (x, y),
     * measured along the +X direction in world space.
     */
    public float pixelsPerWorldUnitAt(float x, float y, Vector3f tempNDC) {
        final float halfW = width / 2f;
        final float halfH = height / 2f;

        // (x, y) -> NDC -> pixels
        modelViewProjectionMatrix.transformProject(x, y, 0f, tempNDC);
        final float sx0 = halfW + tempNDC.x * halfW;
        final float sy0 = halfH + tempNDC.y * halfH;

        // (x + 1, y) -> NDC -> pixels
        modelViewProjectionMatrix.transformProject(x + 1f, y, 0f, tempNDC);
        final float sx1 = halfW + tempNDC.x * halfW;
        final float sy1 = halfH + tempNDC.y * halfH;

        return (float) Math.hypot(sx1 - sx0, sy1 - sy0);
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
