package org.gephi.viz.engine.util.gl;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight, process-wide counters for GPU data transfers, used to measure and validate the
 * large-graph upload optimizations (idle frames should perform zero texture/buffer uploads).
 * <p>
 * Counters are cumulative; callers interested in per-frame rates should snapshot
 * {@link #snapshot()} and diff successive snapshots. Incrementing is cheap (atomic adds) and the
 * counters are only touched on the GL/render thread, but {@link AtomicLong} keeps reads safe from
 * any thread (e.g. a performance HUD).
 *
 * @author Eduardo Ramos
 */
public final class DataUploadStats {

    private static final AtomicLong TEXTURE_UPLOADS = new AtomicLong();
    private static final AtomicLong TEXTURE_UPLOADS_SKIPPED = new AtomicLong();
    private static final AtomicLong TEXTURE_BYTES = new AtomicLong();

    private static final AtomicLong BUFFER_UPLOADS = new AtomicLong();
    private static final AtomicLong BUFFER_UPLOADS_SKIPPED = new AtomicLong();
    private static final AtomicLong BUFFER_BYTES = new AtomicLong();

    private static final AtomicLong WORLD_UPDATES = new AtomicLong();
    private static final AtomicLong WORLD_UPDATES_SKIPPED = new AtomicLong();

    private DataUploadStats() {
    }

    public static void recordTextureUpload(long bytes) {
        TEXTURE_UPLOADS.incrementAndGet();
        TEXTURE_BYTES.addAndGet(bytes);
    }

    public static void recordTextureUploadSkipped() {
        TEXTURE_UPLOADS_SKIPPED.incrementAndGet();
    }

    public static void recordBufferUpload(long bytes) {
        BUFFER_UPLOADS.incrementAndGet();
        BUFFER_BYTES.addAndGet(bytes);
    }

    public static void recordBufferUploadSkipped() {
        BUFFER_UPLOADS_SKIPPED.incrementAndGet();
    }

    public static void recordWorldUpdate() {
        WORLD_UPDATES.incrementAndGet();
    }

    public static void recordWorldUpdateSkipped() {
        WORLD_UPDATES_SKIPPED.incrementAndGet();
    }

    public static void reset() {
        TEXTURE_UPLOADS.set(0);
        TEXTURE_UPLOADS_SKIPPED.set(0);
        TEXTURE_BYTES.set(0);
        BUFFER_UPLOADS.set(0);
        BUFFER_UPLOADS_SKIPPED.set(0);
        BUFFER_BYTES.set(0);
        WORLD_UPDATES.set(0);
        WORLD_UPDATES_SKIPPED.set(0);
    }

    public static Snapshot snapshot() {
        return new Snapshot(
            TEXTURE_UPLOADS.get(), TEXTURE_UPLOADS_SKIPPED.get(), TEXTURE_BYTES.get(),
            BUFFER_UPLOADS.get(), BUFFER_UPLOADS_SKIPPED.get(), BUFFER_BYTES.get(),
            WORLD_UPDATES.get(), WORLD_UPDATES_SKIPPED.get());
    }

    /**
     * Immutable snapshot of the cumulative counters at a point in time.
     */
    public record Snapshot(
        long textureUploads,
        long textureUploadsSkipped,
        long textureBytes,
        long bufferUploads,
        long bufferUploadsSkipped,
        long bufferBytes,
        long worldUpdates,
        long worldUpdatesSkipped
    ) {
    }
}
