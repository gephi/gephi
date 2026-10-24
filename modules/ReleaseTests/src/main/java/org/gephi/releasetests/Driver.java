package org.gephi.releasetests;

import java.util.Map;
import java.util.function.Supplier;
import org.gephi.project.api.ProjectController;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;

/**
 * Runs the suite named by the {@link ReleaseTests#SUITE_PROPERTY} system property under the {@link Monitor}, then
 * exits Gephi with status 0 if it passed and 1 otherwise.
 */
final class Driver implements Runnable {

    static final long EXIT_TIMEOUT_MS = 60_000;

    private static final Map<String, Supplier<Suite>> SUITES = Map.of(
        "smoke", SmokeSuite::new);

    @Override
    public void run() {
        Results results = Results.get();
        results.started();
        Monitor monitor = new Monitor(results);
        try {
            String name = ReleaseTests.suite();
            Supplier<Suite> suite = SUITES.get(name);
            if (suite == null) {
                throw new IllegalArgumentException("Unknown suite '" + name + "', known suites: " + SUITES.keySet());
            }
            monitor.start();
            try {
                suite.get().run(new SuiteContext(results, monitor));
            } finally {
                monitor.stop();
            }
        } catch (SuiteContext.StepFailedException ex) {
            // Already recorded with its step
        } catch (Throwable t) {
            results.error(t);
        }
        exit(results);
    }

    /**
     * Closes the project and exits. Both need the Event Dispatch Thread, which a failed suite may have left stuck, so
     * Gephi is halted with status 1 if it hasn't exited within {@value #EXIT_TIMEOUT_MS} ms.
     */
    private static void exit(Results results) {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(EXIT_TIMEOUT_MS);
            } catch (InterruptedException ex) {
                return;
            }
            results.violation("exit", "Gephi didn't exit within " + EXIT_TIMEOUT_MS + " ms and was halted",
                ThreadDumps.write(results, "exit"));
            Runtime.getRuntime().halt(1);
        }, "Release Tests Exit Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();

        closeProject(results);
        results.finish();
        LifecycleManager.getDefault().exit(results.passed() ? 0 : 1);
    }

    /**
     * Closes the project a failed suite may have left open, as an open project makes Gephi ask to save it on exit.
     */
    private static void closeProject(Results results) {
        try {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            if (projectController.hasCurrentProject()) {
                projectController.closeCurrentProject();
            }
        } catch (Throwable t) {
            results.error(t);
        }
    }
}
