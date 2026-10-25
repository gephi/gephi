package org.gephi.releasetests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphLock;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.api.StatisticsController;
import org.gephi.statistics.spi.Statistics;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;

/**
 * What suites drive Gephi with: the controllers, and helpers that call them from the thread the user interface uses
 * and wait for their background work. Each wait gives up after {@link #TIMEOUT}.
 */
final class SuiteContext {

    static final Duration TIMEOUT = Duration.ofSeconds(60);

    final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
    final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
    final ImportController importController = Lookup.getDefault().lookup(ImportController.class);
    final ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
    final LayoutController layoutController = Lookup.getDefault().lookup(LayoutController.class);
    final StatisticsController statisticsController = Lookup.getDefault().lookup(StatisticsController.class);
    final FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

    private final Results results;
    private final Monitor monitor;

    SuiteContext(Results results, Monitor monitor) {
        this.results = results;
        this.monitor = monitor;
    }

    /**
     * Runs <code>body</code> as a step of the suite and records its outcome. A failed step ends the suite.
     */
    <T> T step(String name, Callable<T> body) throws StepFailedException {
        results.stepStarted(name);
        long startMs = Results.uptimeMs();
        try {
            T result = body.call();
            results.stepFinished(name, startMs, Results.uptimeMs() - startMs, null);
            return result;
        } catch (Throwable t) {
            results.stepFinished(name, startMs, Results.uptimeMs() - startMs, t);
            throw new StepFailedException(t);
        }
    }

    void step(String name, Body body) throws StepFailedException {
        step(name, () -> {
            body.run();
            return null;
        });
    }

    /**
     * Sets whether the Overview must be drawing, see {@link Monitor#setExpectRendering(boolean)}.
     */
    void expectRendering(boolean expectRendering) {
        monitor.setExpectRendering(expectRendering);
    }

    /**
     * Returns a file in the output directory.
     */
    File file(String name) {
        return new File(results.getOutDir(), name);
    }

    /**
     * Imports a file from this package's resources into a new project, as opening a file does.
     */
    Workspace importResource(String name) throws Exception {
        File file = file(name);
        try (InputStream stream = SuiteContext.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new FileNotFoundException(name);
            }
            Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Container container = importController.importFile(file);
        check(container != null, "No importer for " + name);
        Project project = projectController.newProject();
        Workspace workspace = project.getCurrentWorkspace();
        importController.process(container, new DefaultProcessor(), workspace);
        return workspace;
    }

    /**
     * Runs the registered layout of type <code>builderType</code> for <code>duration</code>, started and stopped as
     * from the Layout panel.
     */
    void runLayout(Class<? extends LayoutBuilder> builderType, Duration duration) throws Exception {
        startLayout(builderType);
        Thread.sleep(duration.toMillis());
        stopLayout();
    }

    /**
     * Starts the registered layout of type <code>builderType</code> as from the Layout panel, and returns while it
     * runs.
     */
    void startLayout(Class<? extends LayoutBuilder> builderType) throws Exception {
        LayoutBuilder builder = Lookup.getDefault().lookupAll(LayoutBuilder.class).stream()
            .filter(builderType::isInstance)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No registered " + builderType.getSimpleName()));
        onEdt(() -> {
            Layout layout = layoutController.getModel().getLayout(builder);
            layoutController.setLayout(layout);
            layoutController.executeLayout();
        });
    }

    /**
     * Stops the layout as from the Layout panel and waits for it to end.
     */
    void stopLayout() throws Exception {
        onEdt(layoutController::stopLayout);
        await("Layout", () -> !layoutController.getModel().isRunning());
    }

    /**
     * Writes a thread dump and returns the name of its file, to give with a failure.
     */
    String threadDump(String reason) {
        return ThreadDumps.write(results, reason);
    }

    /**
     * Records a number in the results.
     */
    void metric(String name, Number value) {
        results.metric(name, value);
    }

    /**
     * Runs <code>statistics</code> as from the Statistics panel and waits for it to finish.
     */
    void runStatistics(Statistics statistics) throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        onEdt(() -> statisticsController.execute(statistics, task -> finished.countDown()));
        if (!finished.await(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            String dump = ThreadDumps.write(results, "statistics");
            throw new AssertionError(statistics.getClass().getSimpleName() + " didn't finish within "
                + TIMEOUT.toSeconds() + " s, see " + dump);
        }
    }

    /**
     * Filters the visible graph with <code>query</code>, or removes the filter when it's <code>null</code>, as from
     * the Filters panel, and waits for the result to be applied.
     */
    void filterVisible(Query query) throws Exception {
        onEdt(() -> filterController.filterVisible(query));
        await("Filter", () -> !filterController.getModel().isRunning());
    }

    /**
     * Takes a screenshot of the Overview, as from its screenshot button, once it has drawn the graph, which a new graph or
     * workspace only does a moment after it's shown. It waits for two world updates from now on.
     */
    BufferedImage overviewScreenshot(String name) throws Exception {
        VisualizationController controller = Lookup.getDefault().lookup(VisualizationController.class);
        check(controller instanceof VizController, "No Overview to take a screenshot of");
        // Frames and world updates are counted for good, so it's the ones from now on that show the current graph
        long updatesBefore = ((VizController) controller).getEngine().map(engine -> engine.getWorldUpdateCount())
            .orElse(0L);
        await("Overview frame with the graph", () -> ((VizController) controller).getEngine()
            .map(engine -> engine.getFrameCount() > 0 && engine.getWorldUpdateCount() >= updatesBefore + 2)
            .orElse(false));
        File written = ((VizController) controller).getScreenshotController().takeScreenshot(1, false, file(name))
            .get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        return ImageIO.read(written);
    }

    /**
     * Fails unless at least one pixel in a thousand differs from the top left one.
     */
    static void checkNotBlank(BufferedImage image, String what) {
        check(image != null, what + " can't be read");
        int background = image.getRGB(0, 0);
        long different = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != background) {
                    different++;
                }
            }
        }
        long total = (long) image.getWidth() * image.getHeight();
        check(different * 1000 >= total, what + " is blank, " + different + " of " + total + " pixels differ");
    }

    /**
     * Fails unless the graph lock is free at some point within {@link #TIMEOUT}: no thread holding it, none waiting.
     * The Overview and the monitor hold it for moments, so it is looked at until it's seen free. A hold that is never
     * released, like one left by an iteration that was neither finished nor broken, is what this finds.
     */
    void checkLocksReleased(Graph graph) throws InterruptedException {
        GraphLock lock = graph.getLock();
        await(() -> lock.getReadLockCount() == 0 && !lock.isWriteLocked() && lock.getQueueLength() == 0,
            () -> "The graph lock is still held after " + TIMEOUT.toSeconds() + " s, with " + lock.getReadLockCount()
                + " read holds, write locked: " + lock.isWriteLocked() + ", " + lock.getQueueLength()
                + " threads waiting, see " + threadDump("lock"), 5);
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Runs <code>body</code> on the Event Dispatch Thread and waits for it.
     */
    static void onEdt(Body body) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            body.run();
            return null;
        });
        SwingUtilities.invokeLater(task);
        try {
            task.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof Exception exception) {
                throw exception;
            }
            throw new IllegalStateException(ex.getCause());
        } catch (TimeoutException ex) {
            task.cancel(false);
            throw new IllegalStateException(
                "The Event Dispatch Thread didn't run the call within " + TIMEOUT.toSeconds() + " s", ex);
        }
    }

    static void await(String what, BooleanSupplier done) throws InterruptedException {
        await(done, () -> what + " didn't finish within " + TIMEOUT.toSeconds() + " s", 50);
    }

    private static void await(BooleanSupplier done, Supplier<String> failure, long pollMs)
        throws InterruptedException {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (!done.getAsBoolean()) {
            if (System.nanoTime() >= deadline) {
                throw new AssertionError(failure.get());
            }
            Thread.sleep(pollMs);
        }
    }

    interface Body {

        void run() throws Exception;
    }

    /**
     * Thrown once a failed step has been recorded, to end the suite.
     */
    static final class StepFailedException extends Exception {

        private static final long serialVersionUID = 1L;

        StepFailedException(Throwable cause) {
            super(cause);
        }
    }
}
