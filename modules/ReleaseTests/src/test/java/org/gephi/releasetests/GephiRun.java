package org.gephi.releasetests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Assert;

/**
 * Runs a release test suite in a Gephi distribution. The driver module is installed into a fresh user directory, as a
 * plugin would be, and Gephi is started with the suite to run. A run that doesn't exit in time gets a thread dump and
 * is killed.
 * <p>
 * Reads the <code>gephi.dist</code>, <code>releasetests.nbm</code> and <code>releasetests.workdir</code> system
 * properties, which the module's <code>release-tests</code> profile sets.
 */
final class GephiRun {

    /**
     * How long the driver has to start once Gephi is launched. It doesn't when its module can't be loaded, typically
     * because it was built from another version than the distribution.
     */
    static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    private static final String NBM_PREFIX = "netbeans/";

    private GephiRun() {
    }

    static Result run(String suite, Duration timeout) throws IOException, InterruptedException {
        File dist = new File(requiredProperty("gephi.dist"));
        File home = dist.getName().endsWith(".app") ? new File(dist, "Contents/Resources/gephi") : dist;
        File launcher = new File(home, isWindows() ? "bin/gephi64.exe" : "bin/gephi");
        Assert.assertTrue("No Gephi distribution at " + dist + ". Build it with mvn install, or pass -Dgephi.dist",
            launcher.isFile());

        File runDir = new File(requiredProperty("releasetests.workdir"), suite);
        deleteRecursively(runDir.toPath());
        File userdir = new File(runDir, "userdir");
        File cachedir = new File(runDir, "cachedir");
        File outDir = new File(runDir, "out");
        Files.createDirectories(outDir.toPath());
        installNbm(new File(requiredProperty("releasetests.nbm")), userdir);
        writePreferences(userdir);

        // Not --nosplash, Gephi then starts without its FlatLaf look and feel
        List<String> command = new ArrayList<>(List.of(launcher.getPath(),
            "--userdir", userdir.getPath(), "--cachedir", cachedir.getPath()));
        if (!hasJdkHome(home)) {
            command.add("--jdkhome");
            command.add(System.getProperty("java.home"));
        }
        for (String option : jvmOptions(suite, outDir)) {
            command.add("-J" + option);
        }
        Process process = new ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(new File(outDir, "console.log"))
            .start();
        // Gephi mustn't outlive these tests, if they're interrupted
        Thread killer = new Thread(() -> kill(process));
        Runtime.getRuntime().addShutdownHook(killer);
        File resultsFile = new File(outDir, "results.json");
        long startNs = System.nanoTime();
        boolean exited = false;
        boolean started = false;
        try {
            while (!exited) {
                exited = process.waitFor(1, TimeUnit.SECONDS);
                started = started || resultsFile.isFile();
                long elapsedNs = System.nanoTime() - startNs;
                if (!exited && (elapsedNs > timeout.toNanos() || !started && elapsedNs > STARTUP_TIMEOUT.toNanos())) {
                    dumpHang(process, outDir);
                    kill(process);
                    process.waitFor(30, TimeUnit.SECONDS);
                    break;
                }
            }
        } finally {
            Runtime.getRuntime().removeShutdownHook(killer);
        }

        File log = new File(userdir, "var/log/messages.log");
        if (log.isFile()) {
            Files.copy(log.toPath(), new File(outDir, "messages.log").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return new Result(suite, timeout, outDir, exited ? process.exitValue() : -1, !exited,
            started || resultsFile.isFile(), readResults(outDir));
    }

    /**
     * Options for the Gephi JVM: the suite to run, and settings keeping a test run from showing dialogs, checking
     * for updates or sending reports.
     */
    private static List<String> jvmOptions(String suite, File outDir) {
        return List.of(
            "-Dgephi.releasetests.suite=" + suite,
            "-Dgephi.releasetests.out=" + outDir.getAbsolutePath(),
            "-Dorg.gephi.crashReporter.enabled=false",
            "-Dorg.gephi.settingsUpgrader.enabled=false",
            "-Dplugin.manager.check.interval=NEVER",
            "-Dnetbeans.exception.report.min.level=99999",
            "-Dnetbeans.exception.alert.min.level=99999",
            "-XX:StartFlightRecording=filename=" + new File(outDir, "recording.jfr").getAbsolutePath()
                + ",dumponexit=true");
    }

    /**
     * Installs the driver module the way the plugin manager does, from the files under <code>netbeans/</code> in
     * the NBM.
     */
    private static void installNbm(File nbm, File userdir) throws IOException {
        Assert.assertTrue("No driver module at " + nbm, nbm.isFile());
        Path root = userdir.toPath().toAbsolutePath().normalize();
        try (ZipFile zip = new ZipFile(nbm)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(NBM_PREFIX)) {
                    continue;
                }
                Path target = root.resolve(entry.getName().substring(NBM_PREFIX.length())).normalize();
                Assert.assertTrue("Entry outside the user directory: " + entry.getName(), target.startsWith(root));
                Files.createDirectories(target.getParent());
                try (InputStream stream = zip.getInputStream(entry)) {
                    Files.copy(stream, target);
                }
            }
        }
    }

    /**
     * Turns off what a fresh user directory shows or sends at startup: the welcome window, the analytics consent
     * popup, usage and crash tracking, and the check for a newer release.
     */
    private static void writePreferences(File userdir) throws IOException {
        writeProperties(new File(userdir, "config/Preferences/org/gephi/desktop/branding.properties"),
            "disable_all_tracking=true\nanalytics_consent_shown=true\ncheck_latest_version=false\n");
        writeProperties(new File(userdir, "config/Preferences/org/gephi/welcome/screen.properties"),
            "WelcomeScreen_Open_Startup=false\n");
    }

    private static void writeProperties(File file, String content) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        Files.writeString(file.toPath(), content, StandardCharsets.ISO_8859_1);
    }

    /**
     * Whether the distribution's configuration names the JDK to run on, like the bundled one. Otherwise the JDK
     * running the tests is used.
     */
    private static boolean hasJdkHome(File home) throws IOException {
        File conf = new File(home, "etc/gephi.conf");
        try (Stream<String> lines = Files.lines(conf.toPath())) {
            return lines.anyMatch(line -> line.trim().matches("jdkhome=\"?[^\"]+\"?"));
        }
    }

    /**
     * Writes a thread dump and the flight recording of the Java processes the launcher started, with the
     * <code>jcmd</code> of the JDK running the tests. Every process is tried, the launcher too, as on Windows it runs
     * the JVM itself. Falls back to <code>kill -3</code>, which prints the thread dump to the console log.
     */
    private static void dumpHang(Process process, File outDir) throws InterruptedException {
        String jcmd = new File(System.getProperty("java.home"), "bin/jcmd").getPath();
        List<ProcessHandle> processes = Stream.concat(Stream.of(process.toHandle()), process.descendants()).toList();
        boolean dumped = false;
        for (ProcessHandle handle : processes) {
            String pid = String.valueOf(handle.pid());
            File dump = new File(outDir, "hang-threads-" + pid + ".txt");
            if (run(dump, jcmd, pid, "Thread.print", "-l") == 0) {
                dumped = true;
                run(new File(outDir, "hang-jfr-dump.txt"), jcmd, pid, "JFR.dump", "name=1",
                    "filename=" + new File(outDir, "recording.jfr").getAbsolutePath());
            } else {
                dump.delete();
            }
        }
        if (!dumped && !isWindows()) {
            for (ProcessHandle handle : processes) {
                if (handle.info().command().map(command -> command.endsWith("java")).orElse(false)) {
                    run(new File(outDir, "hang-kill.txt"), "kill", "-3", String.valueOf(handle.pid()));
                }
            }
        }
    }

    private static void kill(Process process) {
        process.descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static int run(File output, String... command) throws InterruptedException {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output).start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return -1;
            }
            return process.exitValue();
        } catch (IOException ex) {
            return -1;
        }
    }

    private static JsonObject readResults(File outDir) throws IOException {
        File file = new File(outDir, "results.json");
        if (!file.isFile()) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        Assert.assertNotNull("The " + name + " system property isn't set, run with -P release-tests", value);
        return value;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }

    /**
     * How a run ended, and what the driver's <code>results.json</code> recorded, if anything.
     */
    record Result(String suite, Duration timeout, File outDir, int exitCode, boolean timedOut, boolean started,
                  JsonObject results) {

        List<JsonObject> violations() {
            List<JsonObject> violations = new ArrayList<>();
            if (results != null) {
                results.getAsJsonArray("violations").forEach(element -> violations.add(element.getAsJsonObject()));
            }
            return violations;
        }

        void assertPassed() {
            // A suite that didn't finish exited early, maybe with status 0, while what it had done so far passed
            boolean finished = results != null && results.get("finished").getAsBoolean();
            if (!timedOut && exitCode == 0 && finished && results.get("passed").getAsBoolean()) {
                return;
            }
            StringBuilder message = new StringBuilder("Release test suite '").append(suite).append("' ");
            if (!started) {
                message.append("never started")
                    .append(timedOut ? "" : ", Gephi exited with status " + exitCode)
                    .append(". The driver module may not have loaded, it must be built from the same version as the"
                        + " distribution");
            } else if (timedOut) {
                message.append("didn't exit within ").append(timeout.toSeconds())
                    .append(" s, see the hang-threads dumps");
            } else if (results != null && !finished) {
                message.append("exited with status ").append(exitCode).append(" before finishing");
            } else {
                message.append("exited with status ").append(exitCode);
            }
            message.append(". Files in ").append(outDir).append('\n');
            if (!started) {
                appendModuleMessages(message);
            } else if (results == null) {
                message.append("No results.json, see console.log and messages.log\n");
            } else {
                appendResults(message);
            }
            Assert.fail(message.toString());
        }

        /**
         * Appends what <code>messages.log</code> says about the driver module, like why it couldn't be installed.
         */
        private void appendModuleMessages(StringBuilder message) {
            File log = new File(outDir, "messages.log");
            try (Stream<String> lines = Files.lines(log.toPath())) {
                lines.filter(line -> line.contains("Release Tests") || line.contains("org.gephi.release.tests"))
                    .forEach(line -> message.append(line.trim()).append('\n'));
            } catch (IOException | UncheckedIOException ex) {
                message.append("Can't read ").append(log).append(": ").append(ex).append('\n');
            }
        }

        private void appendResults(StringBuilder message) {
            if (results.has("runningStep")) {
                message.append("Running step: ").append(results.get("runningStep").getAsString()).append('\n');
            }
            if (results.has("error")) {
                message.append("Error: ").append(results.get("error").getAsString()).append('\n');
            }
            for (JsonElement element : results.getAsJsonArray("steps")) {
                JsonObject step = element.getAsJsonObject();
                message.append("Step '").append(step.get("name").getAsString()).append("' ");
                if (step.has("error")) {
                    message.append("failed: ").append(step.get("error").getAsString()).append('\n');
                } else {
                    message.append("passed in ").append(step.get("durationMs").getAsLong()).append(" ms\n");
                }
            }
            for (JsonObject violation : violations()) {
                message.append("Violation [").append(violation.get("kind").getAsString()).append("] at ")
                    .append(violation.get("atMs").getAsLong()).append(" ms: ")
                    .append(violation.get("message").getAsString());
                if (violation.has("dump")) {
                    message.append(" (").append(violation.get("dump").getAsString()).append(')');
                }
                message.append('\n');
            }
        }
    }
}
