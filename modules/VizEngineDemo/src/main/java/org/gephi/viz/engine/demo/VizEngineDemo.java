package org.gephi.viz.engine.demo;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.project.api.ProjectController;
import org.gephi.viz.engine.FrameTimings;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.VizEngineJOGLConfigurator;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Standalone demo that opens a Swing/NEWT window containing a single instance
 * of the Gephi {@link VizEngine}, configured with the JOGL rendering backend.
 *
 * <p>Bundled GEXF samples are cycled with {@code ARROW RIGHT}. The first sample
 * (Les Miserables) is loaded on startup. A custom graph file path may also be
 * passed as the first command-line argument, in which case it is added at the
 * front of the cycle list. {@code SPACE} toggles Force Atlas 2 on the current
 * graph; {@code ESC} (or closing the window) exits.</p>
 *
 * <p>Run with: {@code mvn -pl modules/VizEngineDemo -am compile exec:java}.</p>
 */
public final class VizEngineDemo {

    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    private static final String WINDOW_TITLE = "VizEngine Demo";

    /**
     * Bundled samples shipped on the classpath, cycled in this order.
     */
    private static final List<Sample> BUNDLED_SAMPLES = List.of(
            new Sample("Les Miserables",
                    "/org/gephi/viz/engine/demo/samples/Les Miserables.gexf"),
            new Sample("Comic Hero Network",
                    "/org/gephi/viz/engine/demo/samples/comic-hero-network.gexf")
    );

    /**
     * Worker thread that runs Force Atlas 2 iterations off the EDT/render thread.
     */
    private static final ExecutorService LAYOUT_EXECUTOR =
            Executors.newSingleThreadExecutor(daemon("VizEngineDemo-ForceAtlas2"));

    /**
     * Worker thread used for swapping graph samples.
     */
    private static final ExecutorService SAMPLE_LOADER =
            Executors.newSingleThreadExecutor(daemon("VizEngineDemo-SampleLoader"));

    /**
     * Toggled by the space bar; read by the layout worker each iteration.
     */
    private static volatile boolean layoutEnabled = false;

    /**
     * True while a sample swap is in flight, to debounce rapid arrow presses.
     */
    private static final AtomicBoolean SAMPLE_SWAPPING = new AtomicBoolean(false);

    private VizEngineDemo() {
    }

    public static void main(String[] args) {
        final String customPath = args.length > 0 ? args[0] : null;
        SwingUtilities.invokeLater(() -> start(customPath));
    }

    private static void start(final String customGraphPath) {
        final List<Sample> samples = buildSampleList(customGraphPath);
        final AtomicReference<Integer> currentIndex = new AtomicReference<>(0);

        final GraphModel initialModel = loadSample(samples.get(0));
        final AtomicReference<GraphModel> currentGraphModel = new AtomicReference<>(initialModel);

        final GLCapabilities caps = VizEngineJOGLConfigurator.createCapabilities(4);

        final Display display = NewtFactory.createDisplay(null);
        final Screen screen = NewtFactory.createScreen(display, 0);

        final GLWindow glWindow = GLWindow.create(screen, caps);
        glWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        final JOGLRenderingTarget renderingTarget = new JOGLRenderingTarget(glWindow);

        final VizEngine<JOGLRenderingTarget, NEWTEvent> engine =
                VizEngineFactory.newEngine(
                        renderingTarget,
                        initialModel,
                        Collections.singletonList(new VizEngineJOGLConfigurator())
                );

        applyDefaultLabelColumn(engine, initialModel);

        // Enable per-frame phase timings so the metrics HUD can break down
        // world-update / render / per-renderer p50 + p99 latencies.
        engine.setFrameTimingsEnabled(true);

        engine.start();

        final NewtCanvasAWT newtCanvas = new NewtCanvasAWT(glWindow);

        final JFrame frame = new JFrame(titleFor(samples.get(0)));
        frame.add(newtCanvas);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final RenderingMetricsHud metricsHud = new RenderingMetricsHud(frame);
        // Lock display order: frame total first (it drives the sparkline),
        // then the engine-internal phases.
        metricsHud.ensureSeries("frame", "world", "render");
        glWindow.addGLEventListener(new FrameTimeRecorder(metricsHud, engine));

        final CountDownLatch closedLatch = new CountDownLatch(1);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                metricsHud.dispose();
                closedLatch.countDown();
            }
        });

        glWindow.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        layoutEnabled = false;
                        engine.destroy();
                        glWindow.destroy();
                        frame.dispose();
                        break;
                    case KeyEvent.VK_SPACE:
                        toggleLayout(currentGraphModel);
                        break;
                    case KeyEvent.VK_RIGHT:
                        cycleToNextSample(samples, currentIndex, currentGraphModel, engine, frame);
                        break;
                    case KeyEvent.VK_L:
                        toggleNodeLabels(engine, currentGraphModel);
                        break;
                    default:
                        // ignored
                }
            }
        });

        renderingTarget.setFrame(frame);
        renderingTarget.setWindowTitleFormat(titleFor(samples.get(0)) + " - FPS: $FPS");

        frame.setVisible(true);
        metricsHud.start();

        System.out.println(WINDOW_TITLE + " started - SPACE: toggle Force Atlas 2 | "
                + "RIGHT: next sample (" + samples.size() + " available) | "
                + "L: toggle node labels | ESC: exit.");
    }

    /**
     * Toggles node-label visibility on the engine's rendering options.
     *
     * <p>Defensively re-applies the default label column from the live graph
     * model before flipping the flag: when the engine swaps graph models on
     * sample cycling, a fresh {@link GraphRenderingOptions} instance is
     * created internally and the previously configured columns are lost
     * unless we restore them on the new options.</p>
     */
    private static void toggleNodeLabels(VizEngine<JOGLRenderingTarget, NEWTEvent> engine,
                                         AtomicReference<GraphModel> currentGraphModel) {
        final GraphModel graphModel = currentGraphModel.get();
        final GraphRenderingOptions options = engine.getRenderingOptions();

        final Column nodeLabelColumn = graphModel.defaultColumns().nodeLabel();
        options.setNodeLabelColumns(new Column[] {nodeLabelColumn});

        final boolean newValue = !options.isShowNodeLabels();
        options.setShowNodeLabels(newValue);

        System.out.println("Node labels: " + (newValue ? "ON" : "OFF")
                + " (column=" + (nodeLabelColumn != null ? nodeLabelColumn.getId() : "<null>")
                + ", nodes=" + graphModel.getGraph().getNodeCount() + ")");
    }

    /**
     * {@link GLEventListener} that records frame-level metrics into the HUD
     * after each rendered frame. Registered on the {@link GLWindow} after
     * {@code engine.start()}, so it runs after the engine's own listener
     * and therefore sees the time between two completed frames as well as
     * the per-phase timings the engine just published.
     */
    private static final class FrameTimeRecorder implements GLEventListener {

        private final RenderingMetricsHud hud;
        private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
        private long lastFrameNanos = 0L;

        FrameTimeRecorder(RenderingMetricsHud hud,
                          VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
            this.hud = hud;
            this.engine = engine;
        }

        @Override
        public void init(GLAutoDrawable drawable) {
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
        }

        @Override
        public void display(GLAutoDrawable drawable) {
            final long now = System.nanoTime();
            if (lastFrameNanos != 0L) {
                hud.recordSample("frame", now - lastFrameNanos);
            }
            lastFrameNanos = now;

            final FrameTimings t = engine.getLastFrameTimings();
            if (t == null || t == FrameTimings.EMPTY) {
                return;
            }
            hud.recordSample("world", t.worldUpdateNs());
            hud.recordSample("render", t.renderNs());
            for (var entry : t.perRendererCategoryNs().entrySet()) {
                // Prefix per-renderer rows so the HUD can detect and indent them.
                hud.recordSample("render:" + entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }
    }

    /**
     * Starts or stops a Force Atlas 2 layout iteration loop on the dedicated
     * worker thread. The loop reads the volatile {@link #layoutEnabled} flag
     * each iteration, plus the latest graph model from {@code currentGraphModel}
     * so cycling samples while running is safe (the loop will simply observe
     * the new model on its next iteration via the same reference, but for
     * cleanliness we stop and restart on each toggle).
     */
    private static void toggleLayout(final AtomicReference<GraphModel> currentGraphModel) {
        if (layoutEnabled) {
            System.out.println("Stopping Force Atlas 2");
            layoutEnabled = false;
            return;
        }

        final GraphModel graphModel = currentGraphModel.get();
        System.out.println("Starting Force Atlas 2");
        LAYOUT_EXECUTOR.submit(() -> {
            layoutEnabled = true;

            final ForceAtlas2 forceAtlas2 = new ForceAtlas2Builder().buildLayout();
            forceAtlas2.setGraphModel(graphModel);
            forceAtlas2.setBarnesHutOptimize(true);
            forceAtlas2.setScalingRatio(1000.0);
            forceAtlas2.setAdjustSizes(true);
            forceAtlas2.initAlgo();
            try {
                while (layoutEnabled && forceAtlas2.canAlgo()) {
                    forceAtlas2.goAlgo();
                }
            } finally {
                forceAtlas2.endAlgo();
            }
        });
    }

    /**
     * Loads the next sample in the cycle on a worker thread (sample files can
     * be tens of MB and importing them blocks for a noticeable time), then
     * swaps the engine's {@link GraphModel}. Concurrent presses are debounced
     * via {@link #SAMPLE_SWAPPING}.
     */
    private static void cycleToNextSample(final List<Sample> samples,
                                          final AtomicReference<Integer> currentIndex,
                                          final AtomicReference<GraphModel> currentGraphModel,
                                          final VizEngine<JOGLRenderingTarget, NEWTEvent> engine,
                                          final JFrame frame) {
        if (samples.size() <= 1) {
            System.out.println("Only one sample available, nothing to cycle to.");
            return;
        }
        if (!SAMPLE_SWAPPING.compareAndSet(false, true)) {
            System.out.println("Sample swap already in progress, ignoring.");
            return;
        }

        final int nextIdx = (currentIndex.get() + 1) % samples.size();
        final Sample nextSample = samples.get(nextIdx);

        SAMPLE_LOADER.submit(() -> {
            try {
                // Stop any running FA2 so it doesn't keep iterating on the soon-to-be-replaced model.
                layoutEnabled = false;

                System.out.println("Switching to sample: " + nextSample.name());
                SwingUtilities.invokeLater(() -> frame.setTitle(
                        titleFor(nextSample) + " (loading...)"));

                final GraphModel newModel = loadSample(nextSample);

                engine.setGraphModel(newModel, null, null);
                applyDefaultLabelColumn(engine, newModel);

                currentGraphModel.set(newModel);
                currentIndex.set(nextIdx);

                SwingUtilities.invokeLater(() -> frame.setTitle(titleFor(nextSample)));
            } catch (RuntimeException e) {
                System.err.println("Failed to load sample " + nextSample.name() + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                SAMPLE_SWAPPING.set(false);
            }
        });
    }

    /**
     * Builds the ordered cycle list. If a custom path is provided it goes
     * first; the bundled samples follow.
     */
    private static List<Sample> buildSampleList(String customGraphPath) {
        final List<Sample> samples = new ArrayList<>();
        if (customGraphPath != null) {
            final File f = new File(customGraphPath).getAbsoluteFile();
            samples.add(new Sample(f.getName(), f));
        }
        samples.addAll(BUNDLED_SAMPLES);
        return Collections.unmodifiableList(samples);
    }

    /**
     * Loads the given sample: ensures it's available as an on-disk file
     * (extracting from the classpath if needed), creates a fresh Gephi
     * project, runs the importer and returns the resulting {@link GraphModel}.
     */
    private static GraphModel loadSample(Sample sample) {
        final File file = sample.toFile();
        if (!file.exists()) {
            throw new IllegalStateException("Graph file not found: " + file.getAbsolutePath());
        }

        System.out.println("Loading graph: " + file.getAbsolutePath());

        final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController == null) {
            throw new IllegalStateException(
                    "ProjectController service not available - check that project-api is on the classpath.");
        }
        projectController.newProject();

        final ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        if (importController == null) {
            throw new IllegalStateException(
                    "ImportController service not available - check that io-importer-plugin is on the classpath.");
        }

        final Container container;
        try {
            container = importController.importFile(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to import graph file: " + file.getAbsolutePath(), e);
        }
        if (container == null) {
            throw new IllegalStateException(
                    "No importer available for: " + file.getAbsolutePath()
                            + " - is io-importer-plugin on the classpath?");
        }
        importController.process(container);

        return Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    private static void applyDefaultLabelColumn(VizEngine<JOGLRenderingTarget, NEWTEvent> engine,
                                                GraphModel graphModel) {
        final GraphRenderingOptions options = engine.getRenderingOptions();
        options.setNodeLabelColumns(new Column[]{graphModel.defaultColumns().nodeLabel()});
    }

    private static String titleFor(Sample sample) {
        return WINDOW_TITLE + " - " + sample.name();
    }

    private static java.util.concurrent.ThreadFactory daemon(String name) {
        return runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * A graph sample that is either bundled on the classpath or provided as
     * an external file by the user. Classpath samples are extracted lazily
     * on first use and the temp file is reused for subsequent loads.
     */
    private static final class Sample {

        private final String name;
        private final String classpathResource;
        private final File externalFile;
        private File extractedFile;

        Sample(String name, String classpathResource) {
            this.name = name;
            this.classpathResource = classpathResource;
            this.externalFile = null;
        }

        Sample(String name, File externalFile) {
            this.name = name;
            this.classpathResource = null;
            this.externalFile = externalFile;
        }

        String name() {
            return name;
        }

        synchronized File toFile() {
            if (externalFile != null) {
                return externalFile;
            }
            if (extractedFile != null && extractedFile.exists()) {
                return extractedFile;
            }
            try (InputStream in = VizEngineDemo.class.getResourceAsStream(classpathResource)) {
                if (in == null) {
                    throw new IllegalStateException(
                            "Sample resource not found on classpath: " + classpathResource);
                }
                final String suffix = classpathResource.substring(classpathResource.lastIndexOf('.'));
                final Path tempFile = Files.createTempFile("viz-engine-demo-", suffix);
                tempFile.toFile().deleteOnExit();
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                extractedFile = tempFile.toFile();
                return extractedFile;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to extract sample: " + classpathResource, e);
            }
        }
    }
}
