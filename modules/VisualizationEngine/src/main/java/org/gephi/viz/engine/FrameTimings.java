package org.gephi.viz.engine;

import java.util.Map;

/**
 * CPU-side timings (in nanoseconds) captured for the most recent
 * {@link VizEngine#display()} call.
 *
 * <p>Each field captures the wall-clock time spent in a particular phase of
 * a single frame, as observed on the rendering thread. These are CPU
 * latencies (the time spent submitting work) rather than GPU execution
 * times — the latter would require explicit GL timer queries.</p>
 *
 * @param totalNs                 total time spent inside {@link VizEngine#display()}
 * @param worldUpdateNs           time spent producing/consuming world data
 *                                (including {@code worldUpdated()} calls on renderers)
 * @param renderNs                time spent in the render pass loop
 * @param perRendererCategoryNs   sum of {@code renderer.render(...)} times per
 *                                {@link org.gephi.viz.engine.spi.PipelinedExecutor#getCategory() category},
 *                                across all rendering layers
 */
public record FrameTimings(
    long totalNs,
    long worldUpdateNs,
    long renderNs,
    Map<String, Long> perRendererCategoryNs
) {

    public static final FrameTimings EMPTY = new FrameTimings(0L, 0L, 0L, Map.of());
}
