package org.gephi.viz.engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.spi.PipelinedExecutor;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.spi.WorldUpdaterExecutionMode;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.status.GraphSelectionImpl;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.structure.GraphIndexImpl;
import org.gephi.viz.engine.util.TimeUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @param <R> Rendering target
 * @param <I> Events type
 * @author Eduardo Ramos
 */
public class VizEngine<R extends RenderingTarget, I> {

    public static final int DEFAULT_MAX_WORLD_UPDATES_PER_SECOND = 60;

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

    private float zoom = 0.3f;
    private final Vector2f translate = new Vector2f();

    //Renderers:
    private final Set<Renderer<R>> allRenderers = new LinkedHashSet<>();
    private final List<Renderer<R>> renderersPipeline = new ArrayList<>();

    //World updaters:
    private final Set<WorldUpdater<R>> allUpdaters = new LinkedHashSet<>();
    private final List<WorldUpdater<R>> updatersPipeline = new ArrayList<>();
    private ExecutorService updaterManagerThread;
    private ExecutorService updatersThreadPool;
    private final WorldUpdaterExecutionMode worldUpdatersExecutionMode =
        WorldUpdaterExecutionMode.CONCURRENT_SYNCHRONOUS;

    //Input listeners:
    private final List<I> eventsQueue = Collections.synchronizedList(new ArrayList<>());
    private final Set<InputListener<R, I>> allInputListeners = new LinkedHashSet<>();
    private final List<InputListener<R, I>> inputListenersPipeline = new ArrayList<>();

    //Graph:
    private final GraphModel graphModel;

    //Graph Index
    private final GraphIndexImpl graphIndex;

    //Selection
    private final GraphSelectionImpl graphSelection;

    //Rendering Options
    private final GraphRenderingOptionsImpl renderingOptions;

    //Settings:
    private final float[] backgroundColor = new float[] {1, 1, 1, 1};
    private int maxWorldUpdatesPerSecond = DEFAULT_MAX_WORLD_UPDATES_PER_SECOND;

    //Lookup for communication between components:
    private final InstanceContent instanceContent;
    private final AbstractLookup lookup;

    public VizEngine(GraphModel graphModel, R renderingTarget) {
        this.graphModel = Objects.requireNonNull(graphModel, "graphModel mandatory");
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(instanceContent);
        this.renderingTarget = Objects.requireNonNull(renderingTarget, "renderingTarget mandatory");
        this.graphIndex = new GraphIndexImpl(graphModel);
        this.graphSelection = new GraphSelectionImpl();
        this.renderingOptions = new GraphRenderingOptionsImpl();
        loadModelViewProjection();
    }

    public void setup() {
        this.renderingTarget.setup(this);

        if (isSetUp) {
            return;
        }

        final int numThreads = Math.max(Math.min(updatersPipeline.size(), 4), 1);
        if (worldUpdatersExecutionMode.isConcurrent()) {
            updaterManagerThread = Executors.newSingleThreadExecutor(
                runnable -> new Thread(runnable, "World Updater Manager Thread"));
            updatersThreadPool = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
                private int id = 1;

                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "World Updater " + id++);
                }
            });
        }

        isSetUp = true;
        Logger.getLogger(VizEngine.class.getName())
            .log(Level.INFO, "World updaters execution mode: {0}", worldUpdatersExecutionMode);
    }

    public R getRenderingTarget() {
        return renderingTarget;
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

    public boolean hasInputListener(InputListener<R, I> listener) {
        return allInputListeners.contains(listener);
    }

    public boolean removeInputListener(InputListener<R, I> listener) {
        return allInputListeners.remove(listener);
    }

    public Set<InputListener<R, I>> getAllInputListeners() {
        return Collections.unmodifiableSet(allInputListeners);
    }

    public List<InputListener<R, I>> getInputListenersPipeline() {
        return Collections.unmodifiableList(inputListenersPipeline);
    }

    public void addRenderer(Renderer<R> renderer) {
        if (renderer != null) {
            allRenderers.add(renderer);
        }
    }

    public boolean hasRenderer(Renderer<R> renderer) {
        return allRenderers.contains(renderer);
    }

    public boolean removeRenderer(Renderer<R> renderer) {
        return allRenderers.remove(renderer);
    }

    public Set<Renderer<R>> getAllRenderers() {
        return Collections.unmodifiableSet(allRenderers);
    }

    public List<Renderer<R>> getRenderersPipeline() {
        //TODO: check initialized
        return Collections.unmodifiableList(renderersPipeline);
    }

    public boolean isRendererInPipeline(Renderer<R> renderer) {
        return renderersPipeline.contains(renderer);
    }

    public void addWorldUpdater(WorldUpdater<R> updater) {
        if (updater != null) {
            allUpdaters.add(updater);
        }
    }

    public boolean hasWorldUpdater(WorldUpdater<R> updater) {
        return allUpdaters.contains(updater);
    }

    public boolean removeWorldUpdater(WorldUpdater<R> updater) {
        return allUpdaters.remove(updater);
    }

    public Set<WorldUpdater<R>> getAllWorldUpdaters() {
        return Collections.unmodifiableSet(allUpdaters);
    }

    public List<WorldUpdater<R>> getWorldUpdatersPipeline() {
        //TODO: check initialized
        return Collections.unmodifiableList(updatersPipeline);
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
        loadModelViewProjection();
    }

    public void setTranslate(Vector2fc value) {
        translate.set(value);
        loadModelViewProjection();
    }

    public void translate(float x, float y) {
        translate.add(x, y);
        loadModelViewProjection();
    }

    public void translate(Vector2fc value) {
        translate.add(value);
        loadModelViewProjection();
    }

    public float getZoom() {
        return zoom;
    }

    public int getFps() {
        return renderingTarget.getFps();
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        loadModelViewProjection();
    }

    public float aspectRatio() {
        return (float) this.width / this.height;
    }

    public void centerOnGraph() {
        final Rect2D visibleGraphBoundaries = graphIndex.getGraphBoundaries();

        final float[] center = visibleGraphBoundaries.center();
        centerOn(new Vector2f(center[0], center[1]), visibleGraphBoundaries.width(), visibleGraphBoundaries.height());
    }

    public void centerOn(Vector2fc center, float width, float height) {
        setTranslate(-center.x(), -center.y());

        if (width > 0 && height > 0) {
            final Rect2D visibleRange = getViewBoundaries();
            final float zoomFactor = Math.max(width / visibleRange.width(), height / visibleRange.height());

            zoom /= zoomFactor;
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

        loadModelViewProjection();
    }

    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized void destroy() {
        allInputListeners.clear();
        inputListenersPipeline.clear();

        this.renderingTarget.stop();
        if (updatersThreadPool != null) {
            try {
                updatersThreadPool.shutdown();
                final boolean terminated = updatersThreadPool.awaitTermination(10, TimeUnit.SECONDS);
                if (!terminated) {
                    updatersThreadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                //NOOP
            } finally {
                updatersThreadPool = null;
            }
        }

        System.out.println("Dispose updaters");
        updatersPipeline.forEach((worldUpdater) -> {
            worldUpdater.dispose(renderingTarget);
        });

        System.out.println("Dispose renderers");
        renderersPipeline.forEach((renderer) -> {
            renderer.dispose(renderingTarget);
        });

        this.isDestroyed = true;
    }

    private Future<?> allUpdatersCompletableFuture = null;

    private CompletableFuture<WorldUpdater<R>> completableFutureOfUpdater(final WorldUpdater<R> updater) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                updater.updateWorld();
            } catch (Throwable t) {
                Logger.getLogger(VizEngine.class.getName()).log(Level.SEVERE, null, t);
            }
            return updater;
        }, updatersThreadPool);
    }

    private static final RenderingLayer[] ALL_LAYERS = RenderingLayer.values();

    public void display() {
        if (isPaused) {
            return;
        }

        renderingTarget.frameStart();

        processInputEvents();

        if (updatersThreadPool == null) {
            runWorldUpdaters();
        } else {
            checkConcurrentWorldUpdateIsDone();
        }

        //Call renderers for the current frame:
        for (RenderingLayer layer : ALL_LAYERS) {
            for (Renderer<R> renderer : renderersPipeline) {
                if (renderer.getLayers().contains(layer)) {
                    renderer.render(renderingTarget, layer);
                }
            }
        }

        //Schedule next concurrent world update:
        if (updatersThreadPool != null) {
            scheduleNextConcurrentWorldUpdateIfDone();
        }

        renderingTarget.frameEnd();
    }

    private long lastWorldUpdateMillis = 0;

    private void runWorldUpdaters() {
        //Control max world updates per second
        if (maxWorldUpdatesPerSecond >= 1) {
            if (TimeUtils.getTimeMillis() < lastWorldUpdateMillis + 1000 / maxWorldUpdatesPerSecond) {
                //Skip world update
                return;
            }
        }

        for (WorldUpdater<R> worldUpdater : updatersPipeline) {
            worldUpdater.updateWorld();
        }
        lastWorldUpdateMillis = TimeUtils.getTimeMillis();

        for (Renderer<R> renderer : renderersPipeline) {
            renderer.worldUpdated(renderingTarget);
        }
    }

    private void checkConcurrentWorldUpdateIsDone() {
        if (allUpdatersCompletableFuture != null) {
            if (worldUpdatersExecutionMode.isSynchronous()) {
                try {
                    allUpdatersCompletableFuture.get();

                    allUpdatersCompletableFuture = null;

                    //Notify renderers when next concurrent synchronous world data update is done:
                    for (Renderer<R> renderer : renderersPipeline) {
                        renderer.worldUpdated(renderingTarget);
                    }
                } catch (Throwable t) {
                    Logger.getLogger(VizEngine.class.getName()).log(Level.SEVERE, null, t);
                }
            } else {
                //Notify renderers if next concurrent asynchronous world data update is done:
                final boolean worldUpdateDone =
                    allUpdatersCompletableFuture.isDone();
                if (worldUpdateDone) {
                    allUpdatersCompletableFuture = null;

                    for (Renderer<R> renderer : renderersPipeline) {
                        renderer.worldUpdated(renderingTarget);
                    }
                }
            }
        }

    }

    private void scheduleNextConcurrentWorldUpdateIfDone() {
        if (!updatersThreadPool.isShutdown() && allUpdatersCompletableFuture == null) {
            //Control max world updates per second
            if (maxWorldUpdatesPerSecond >= 1) {
                if (TimeUtils.getTimeMillis() < lastWorldUpdateMillis + 1000 / maxWorldUpdatesPerSecond) {
                    //Skip world update
                    return;
                }
            }

            allUpdatersCompletableFuture = updaterManagerThread.submit(new Runnable() {
                @Override
                public void run() {
                    graphModel.getGraph().readLock();

                    // Create a world update future for each updated
                    final List<CompletableFuture<WorldUpdater<R>>> futures = new ArrayList<>();
                    updatersPipeline.forEach(updater -> futures.add(completableFutureOfUpdater(updater)));

                    // Wait until all world updates are done
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    graphModel.getGraph().readUnlock();
                }
            });

            lastWorldUpdateMillis = TimeUtils.getTimeMillis();
        }
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void addToLookup(Object instance) {
        instanceContent.add(instance);
    }

    public void removeFromLookup(Object instance) {
        instanceContent.remove(instance);
    }

    public GraphModel getGraphModel() {
        return graphModel;
    }

    public GraphIndex getGraphIndex() {
        return graphIndex;
    }

    public GraphSelection getGraphSelection() {
        return graphSelection;
    }

    public GraphRenderingOptions getRenderingOptions() {
        return renderingOptions;
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
        System.arraycopy(this.backgroundColor, 0, backgroundColorFloats, 0, 4);
    }

    public float[] getBackgroundColor() {
        return Arrays.copyOf(backgroundColor, backgroundColor.length);
    }

    public void setBackgroundColor(Color color) {
        float[] backgroundColorComponents = new float[4];
        color.getRGBComponents(backgroundColorComponents);

        setBackgroundColor(backgroundColorComponents);
    }

    public void setBackgroundColor(float[] color) {
        if (color.length != 4) {
            throw new IllegalArgumentException("Expected 4 float RGBA color");
        }

        System.arraycopy(color, 0, backgroundColor, 0, 4);
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

    public Vector2f worldCoordinatesToScreenCoordinates(float x, float y) {
        return worldCoordinatesToScreenCoordinates(x, y, new Vector2f());
    }

    public Vector2f worldCoordinatesToScreenCoordinates(float x, float y, Vector2f dest) {
        final Vector3f screenCoordinates = new Vector3f();
        modelViewProjectionMatrix.transformProject(x, y, 0, screenCoordinates);

        return dest.set(screenCoordinates.x, screenCoordinates.y);
    }

    public Vector2f worldCoordinatesToScreenCoordinates(Vector2fc worldCoordinates) {
        return worldCoordinatesToScreenCoordinates(worldCoordinates, new Vector2f());
    }

    public Vector2f worldCoordinatesToScreenCoordinates(Vector2fc worldCoordinates, Vector2f dest) {
        final Vector3f screenCoordinates = new Vector3f();
        modelViewProjectionMatrix.transformProject(worldCoordinates.x(), worldCoordinates.y(), 0, screenCoordinates);

        return dest.set(screenCoordinates.x, screenCoordinates.y);
    }

    private void processInputEvents() {
        for (InputListener<R, I> inputListener : inputListenersPipeline) {
            inputListener.frameStart();
        }

        final Object[] events = eventsQueue.toArray();
        eventsQueue.clear();

        for (Object event : events) {
            for (InputListener<R, I> inputListener : inputListenersPipeline) {
                final boolean consumed = inputListener.processEvent((I) event);
                if (consumed) {
                    break;
                }
            }
        }

        for (InputListener<R, I> inputListener : inputListenersPipeline) {
            inputListener.frameEnd();
        }
    }

    public void queueEvent(I e) {
        eventsQueue.add(e);
    }
}
