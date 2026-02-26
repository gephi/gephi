package org.gephi.viz.engine.profiling;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EngineProfiler {

    private static final Logger LOG = Logger.getLogger(EngineProfiler.class.getSimpleName());
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int WRITER_BUFFER_SIZE = 65536;

    private volatile boolean enabled = false;
    private final Path outputPath;
    private final int batchSize;

    // Per-frame state (only touched by GL thread)
    private long frameStartNanos;
    private long frameCpuStartNanos;
    private long frameNumber;
    private final Map<String, Long> stageStartNanos = new LinkedHashMap<>();
    private final Map<String, Double> currentFrameMetrics = new LinkedHashMap<>();
    private final StringBuilder jsonBuilder = new StringBuilder(1024);

    // GPU timer query state (double-buffered)
    private boolean gpuTimerSupported;
    private boolean gpuTimerInitialized;
    private final int[] gpuQueryIds = new int[2];
    private int currentQuerySlot;
    private boolean previousQueryPending;
    private final long[] gpuQueryResult = new long[1];
    private final int[] gpuQueryAvailable = new int[1];

    // CPU time tracking
    private final ThreadMXBean threadMXBean;
    private final boolean cpuTimeSupported;

    // Batched async writer
    private List<String> currentBatch;
    private final ExecutorService writerExecutor;
    private BufferedWriter writer;

    public EngineProfiler(Path outputPath) {
        this(outputPath, DEFAULT_BATCH_SIZE);
    }

    public EngineProfiler(Path outputPath, int batchSize) {
        this.outputPath = appendTimestamp(outputPath);
        this.batchSize = batchSize;
        this.currentBatch = new ArrayList<>(batchSize);
        this.writerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Profiler Writer");
            t.setDaemon(true);
            return t;
        });
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.cpuTimeSupported = threadMXBean.isCurrentThreadCpuTimeSupported();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void beginFrame(GL gl) {
        if (!enabled) {
            return;
        }
        frameStartNanos = System.nanoTime();
        frameCpuStartNanos = cpuTimeSupported ? threadMXBean.getCurrentThreadCpuTime() : 0;
        currentFrameMetrics.clear();
        stageStartNanos.clear();

        initGpuTimerIfNeeded(gl);
        collectPreviousGpuQuery(gl);
        beginGpuQuery(gl);
    }

    public void endFrame(GL gl) {
        if (!enabled) {
            return;
        }
        endGpuQuery(gl);

        double frameTotalMs = nanosToMs(System.nanoTime() - frameStartNanos);
        currentFrameMetrics.put("frame_total_ms", frameTotalMs);

        if (cpuTimeSupported) {
            double cpuTimeMs = nanosToMs(threadMXBean.getCurrentThreadCpuTime() - frameCpuStartNanos);
            currentFrameMetrics.put("frame_cpu_ms", cpuTimeMs);
        }

        String json = buildJson();
        frameNumber++;

        currentBatch.add(json);
        if (currentBatch.size() >= batchSize) {
            flushBatch();
        }
    }

    public void beginStage(String name) {
        if (!enabled) {
            return;
        }
        stageStartNanos.put(name, System.nanoTime());
    }

    public void endStage(String name) {
        if (!enabled) {
            return;
        }
        Long startNanos = stageStartNanos.remove(name);
        if (startNanos != null) {
            currentFrameMetrics.put(name, nanosToMs(System.nanoTime() - startNanos));
        }
    }

    /**
     * Records a detail timing to be nested inside a dict key.
     * For example, recordDetail("world_update_detail", "Nodes (Instanced)", 0.3) adds to the dict.
     */
    public void recordDetail(String dictKey, String entryName, double valueMs) {
        if (!enabled) {
            return;
        }
        currentFrameMetrics.put(dictKey + "\0" + entryName, valueMs);
    }

    public void dispose() {
        if (!currentBatch.isEmpty()) {
            flushBatch();
        }

        writerExecutor.shutdown();
        try {
            if (!writerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            writerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        closeWriter();
    }

    // --- GPU Timer Queries ---

    private void initGpuTimerIfNeeded(GL gl) {
        if (gpuTimerInitialized) {
            return;
        }
        gpuTimerInitialized = true;

        if (!(gl instanceof GL2ES3)) {
            gpuTimerSupported = false;
            return;
        }

        try {
            GL2ES3 gl3 = (GL2ES3) gl;
            gl3.glGenQueries(2, gpuQueryIds, 0);
            if (gl3.glGetError() == GL.GL_NO_ERROR && gpuQueryIds[0] != 0) {
                gpuTimerSupported = true;
                currentQuerySlot = 0;
                previousQueryPending = false;
            } else {
                gpuTimerSupported = false;
            }
        } catch (Exception e) {
            gpuTimerSupported = false;
            LOG.log(Level.FINE, "GPU timer queries not available", e);
        }
    }

    private void beginGpuQuery(GL gl) {
        if (!gpuTimerSupported) {
            return;
        }
        GL2ES3 gl3 = (GL2ES3) gl;
        gl3.glBeginQuery(GL2ES3.GL_TIME_ELAPSED, gpuQueryIds[currentQuerySlot]);
    }

    private void endGpuQuery(GL gl) {
        if (!gpuTimerSupported) {
            return;
        }
        GL2ES3 gl3 = (GL2ES3) gl;
        gl3.glEndQuery(GL2ES3.GL_TIME_ELAPSED);
        previousQueryPending = true;
        currentQuerySlot = 1 - currentQuerySlot;
    }

    private void collectPreviousGpuQuery(GL gl) {
        if (!gpuTimerSupported || !previousQueryPending) {
            return;
        }
        GL2ES3 gl3 = (GL2ES3) gl;
        int prevSlot = 1 - currentQuerySlot;

        gl3.glGetQueryObjectiv(gpuQueryIds[prevSlot], GL2ES3.GL_QUERY_RESULT_AVAILABLE, gpuQueryAvailable, 0);
        if (gpuQueryAvailable[0] != 0) {
            gl3.glGetQueryObjecti64v(gpuQueryIds[prevSlot], GL2ES3.GL_QUERY_RESULT, gpuQueryResult, 0);
            currentFrameMetrics.put("frame_gpu_ms", nanosToMs(gpuQueryResult[0]));
        }
    }

    // --- JSON building ---

    private String buildJson() {
        jsonBuilder.setLength(0);
        jsonBuilder.append('{');

        appendLong("timestamp_ms", System.currentTimeMillis());
        jsonBuilder.append(',');
        appendLong("frame_number", frameNumber);

        Map<String, Map<String, Double>> dicts = new LinkedHashMap<>();

        for (Map.Entry<String, Double> entry : currentFrameMetrics.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();

            int sep = key.indexOf('\0');
            if (sep >= 0) {
                String dictKey = key.substring(0, sep);
                String entryName = key.substring(sep + 1);
                dicts.computeIfAbsent(dictKey, k -> new LinkedHashMap<>()).put(entryName, value);
            } else {
                jsonBuilder.append(',');
                appendDouble(key, value);
            }
        }

        for (Map.Entry<String, Map<String, Double>> dictEntry : dicts.entrySet()) {
            jsonBuilder.append(',');
            appendString(dictEntry.getKey());
            jsonBuilder.append(":{");
            boolean first = true;
            for (Map.Entry<String, Double> inner : dictEntry.getValue().entrySet()) {
                if (!first) {
                    jsonBuilder.append(',');
                }
                appendDouble(inner.getKey(), inner.getValue());
                first = false;
            }
            jsonBuilder.append('}');
        }

        jsonBuilder.append('}');
        return jsonBuilder.toString();
    }

    private void appendLong(String key, long value) {
        appendString(key);
        jsonBuilder.append(':').append(value);
    }

    private void appendDouble(String key, double value) {
        appendString(key);
        jsonBuilder.append(':');
        long rounded = Math.round(value * 1000.0);
        jsonBuilder.append(rounded / 1000L).append('.');
        long frac = Math.abs(rounded % 1000);
        if (frac < 10) {
            jsonBuilder.append("00");
        } else if (frac < 100) {
            jsonBuilder.append('0');
        }
        jsonBuilder.append(frac);
    }

    private void appendString(String value) {
        jsonBuilder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                jsonBuilder.append('\\');
            }
            jsonBuilder.append(c);
        }
        jsonBuilder.append('"');
    }

    // --- Batched async I/O ---

    private void flushBatch() {
        List<String> batch = currentBatch;
        currentBatch = new ArrayList<>(batchSize);
        writerExecutor.submit(() -> writeBatch(batch));
    }

    private void writeBatch(List<String> batch) {
        try {
            ensureWriterOpen();
            for (String line : batch) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to write profiling data", e);
        }
    }

    private void ensureWriterOpen() throws IOException {
        if (writer == null) {
            writer = Files.newBufferedWriter(outputPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);
        }
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to close profiling writer", e);
            }
            writer = null;
        }
    }

    private static double nanosToMs(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static final DateTimeFormatter TIMESTAMP_FMT =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneOffset.UTC);

    private static Path appendTimestamp(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String stem = dot >= 0 ? fileName.substring(0, dot) : fileName;
        String ext = dot >= 0 ? fileName.substring(dot) : "";
        String timestamped = stem + "_" + TIMESTAMP_FMT.format(Instant.now()) + ext;
        Path parent = path.getParent();
        return parent != null ? parent.resolve(timestamped) : Path.of(timestamped);
    }
}
