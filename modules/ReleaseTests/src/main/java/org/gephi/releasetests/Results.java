package org.gephi.releasetests;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a suite run found. It's written to <code>results.json</code> in the output directory after every change, so
 * that it survives a hang or a crash, and read by the supervisor once Gephi has exited.
 * <p>
 * Times are milliseconds since the JVM started. A violation fails the run; a slowdown is only reported, with the
 * thread dump taken at that time.
 */
final class Results {

    private static Results instance;

    private final String suite;
    private final File outDir;
    private final List<Step> steps = new ArrayList<>();
    private final List<Event> violations = new ArrayList<>();
    private final List<Event> slowdowns = new ArrayList<>();
    private final Map<String, Number> metrics = new LinkedHashMap<>();
    private String runningStep;
    private String error;
    private boolean started;
    private boolean finished;
    private int fileCount;

    private Results(String suite, File outDir) {
        this.suite = suite;
        this.outDir = outDir;
    }

    static synchronized Results get() {
        if (instance == null) {
            instance = new Results(ReleaseTests.suite(), ReleaseTests.outDir());
        }
        return instance;
    }

    static long uptimeMs() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    static String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    File getOutDir() {
        return outDir;
    }

    /**
     * Returns a new file in <code>dir</code> under the output directory, numbered so that names don't collide and
     * sort in the order they were created.
     */
    synchronized File newFile(String dir, String name) {
        File parent = new File(outDir, dir);
        parent.mkdirs();
        return new File(parent, String.format("%03d-%s", ++fileCount, name));
    }

    String relativePath(File file) {
        return outDir.toPath().relativize(file.toPath()).toString();
    }

    /**
     * Tells the supervisor the driver started. The results may have been written before, for errors logged while
     * Gephi starts.
     */
    synchronized void started() {
        started = true;
        write();
    }

    synchronized void stepStarted(String name) {
        runningStep = name;
        write();
    }

    synchronized void stepFinished(String name, long startMs, long durationMs, Throwable failure) {
        runningStep = null;
        steps.add(new Step(name, startMs, durationMs, failure != null ? stackTrace(failure) : null));
        write();
    }

    synchronized void violation(String kind, String message, String dump) {
        violations.add(new Event(kind, uptimeMs(), message, dump));
        write();
    }

    synchronized void slowdown(String kind, String message, String dump) {
        slowdowns.add(new Event(kind, uptimeMs(), message, dump));
        write();
    }

    synchronized void metric(String name, Number value) {
        metrics.put(name, value);
    }

    synchronized void error(Throwable throwable) {
        error = error == null ? stackTrace(throwable) : error + '\n' + stackTrace(throwable);
        write();
    }

    synchronized void finish() {
        finished = true;
        write();
    }

    synchronized boolean passed() {
        return error == null && violations.isEmpty() && steps.stream().allMatch(step -> step.error == null);
    }

    private void write() {
        JsonObject json = new JsonObject();
        json.addProperty("suite", suite);
        json.addProperty("started", started);
        json.addProperty("finished", finished);
        json.addProperty("passed", passed());
        if (runningStep != null) {
            json.addProperty("runningStep", runningStep);
        }
        if (error != null) {
            json.addProperty("error", error);
        }
        JsonArray stepsJson = new JsonArray();
        for (Step step : steps) {
            JsonObject stepJson = new JsonObject();
            stepJson.addProperty("name", step.name);
            stepJson.addProperty("startMs", step.startMs);
            stepJson.addProperty("durationMs", step.durationMs);
            if (step.error != null) {
                stepJson.addProperty("error", step.error);
            }
            stepsJson.add(stepJson);
        }
        json.add("steps", stepsJson);
        json.add("violations", toJson(violations));
        json.add("slowdowns", toJson(slowdowns));
        JsonObject metricsJson = new JsonObject();
        metrics.forEach(metricsJson::addProperty);
        json.add("metrics", metricsJson);

        try {
            outDir.mkdirs();
            File file = new File(outDir, "results.json");
            File temp = new File(outDir, "results.json.tmp");
            Files.writeString(temp.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(json),
                StandardCharsets.UTF_8);
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException | RuntimeException ex) {
            // Not logged, a SEVERE record would come back here as a violation
            System.err.println("Release tests can't write their results: " + ex);
        }
    }

    private static JsonArray toJson(List<Event> events) {
        JsonArray array = new JsonArray();
        for (Event event : events) {
            JsonObject eventJson = new JsonObject();
            eventJson.addProperty("kind", event.kind);
            eventJson.addProperty("atMs", event.atMs);
            eventJson.addProperty("message", event.message);
            if (event.dump != null) {
                eventJson.addProperty("dump", event.dump);
            }
            array.add(eventJson);
        }
        return array;
    }

    private record Step(String name, long startMs, long durationMs, String error) {
    }

    private record Event(String kind, long atMs, String message, String dump) {
    }
}
