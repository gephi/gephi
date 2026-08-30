package org.gephi.viz.engine.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

/**
 * Floating HUD shown at the top-right of an owner {@link JFrame} that displays
 * {@code p50} / {@code p99} of multiple rendering metric series, computed over
 * the last {@value #WINDOW_SECONDS_HUMAN} of samples. The first registered
 * series is also drawn as a sparkline at the bottom of the HUD.
 *
 * <p>Series are dynamically registered on first {@link #recordSample(String, long)}
 * call so the demo can simply forward whatever phases the engine reports.
 * Initial series can also be registered via {@link #ensureSeries(String...)}
 * to fix their display order.</p>
 *
 * <p>Reads/writes of the underlying ring buffers are synchronized per-series,
 * so {@link #recordSample(String, long)} is safe to call from the render thread
 * while the EDT repaints.</p>
 */
public final class RenderingMetricsHud {

    /** Length of the sliding window over which p50/p99 are computed. */
    private static final long WINDOW_NANOS = 10L * 1_000_000_000L;
    private static final String WINDOW_SECONDS_HUMAN = "10s";

    /** Per-series ring buffer capacity (≈400 FPS × 10 s, generous). */
    private static final int CAPACITY = 4_096;

    private static final int HUD_WIDTH = 280;
    private static final int HUD_HEIGHT = 220;
    private static final int HUD_MARGIN = 10;
    private static final int REPAINT_INTERVAL_MS = 50;

    /** Reference frame rate. Sparkline anchors this at the chart's vertical middle. */
    private static final double TARGET_FPS = 60.0;
    private static final double TARGET_FRAME_MS = 1000.0 / TARGET_FPS;
    /** Top-of-chart frame rate. Picked so 60 FPS lands at the middle of the chart. */
    private static final double SPARKLINE_MAX_FPS = TARGET_FPS * 2.0;

    /**
     * Series whose samples drive the sparkline at the bottom. Set on first
     * {@link #ensureSeries(String...)} call (the first name passed) or, if
     * none was registered up front, on the first {@link #recordSample}.
     */
    private volatile String sparklineSeries;

    /** Insertion-ordered map; iterated for layout, mutated under its own lock. */
    private final Map<String, Series> seriesByName = new LinkedHashMap<>();
    private final Object seriesMapLock = new Object();

    private final JFrame anchorFrame;
    private final JWindow window;
    private final ChartPanel chartPanel;
    private final Timer repaintTimer;
    private final ComponentAdapter anchorListener;

    public RenderingMetricsHud(JFrame anchorFrame) {
        this.anchorFrame = anchorFrame;

        this.chartPanel = new ChartPanel();
        this.window = new JWindow(anchorFrame);
        this.window.setContentPane(chartPanel);
        this.window.setSize(HUD_WIDTH, HUD_HEIGHT);
        this.window.setFocusableWindowState(false);
        this.window.setAutoRequestFocus(false);
        this.window.setAlwaysOnTop(true);

        this.anchorListener = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                repositionToTopRight();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                repositionToTopRight();
            }
        };
        anchorFrame.addComponentListener(anchorListener);

        this.repaintTimer = new Timer(REPAINT_INTERVAL_MS, e -> chartPanel.repaint());
        this.repaintTimer.setCoalesce(true);
    }

    /**
     * Registers (or no-ops on already-known) series in the order given.
     * Useful to lock down a stable display order. The first name becomes
     * the sparkline series.
     */
    public void ensureSeries(String... names) {
        synchronized (seriesMapLock) {
            for (String name : names) {
                seriesByName.computeIfAbsent(name, n -> new Series());
                if (sparklineSeries == null) {
                    sparklineSeries = name;
                }
            }
        }
    }

    public void start() {
        repositionToTopRight();
        window.setVisible(true);
        repaintTimer.start();
    }

    public void dispose() {
        repaintTimer.stop();
        anchorFrame.removeComponentListener(anchorListener);
        window.dispose();
    }

    /**
     * Records a sample for the given series (creating it on first sight).
     * Safe to call from the rendering thread.
     */
    public void recordSample(String seriesName, long durationNs) {
        Series s;
        synchronized (seriesMapLock) {
            s = seriesByName.get(seriesName);
            if (s == null) {
                s = new Series();
                seriesByName.put(seriesName, s);
                if (sparklineSeries == null) {
                    sparklineSeries = seriesName;
                }
            }
        }
        s.record(durationNs);
    }

    private void repositionToTopRight() {
        final int x = anchorFrame.getX() + anchorFrame.getWidth()
            - HUD_WIDTH - HUD_MARGIN - anchorFrame.getInsets().right;
        final int y = anchorFrame.getY() + anchorFrame.getInsets().top + HUD_MARGIN;
        window.setLocation(x, y);
    }

    /**
     * Returns the value at the given percentile {@code p} (in {@code [0, 1]})
     * of an already-sorted-ascending array, expressed in milliseconds.
     * Uses nearest-rank ordering.
     */
    private static double percentileMs(long[] sortedNs, double p) {
        if (sortedNs.length == 0) {
            return 0.0;
        }
        final int rank = (int) Math.ceil(p * sortedNs.length);
        final int idx = Math.max(0, Math.min(sortedNs.length - 1, rank - 1));
        return sortedNs[idx] / 1_000_000.0;
    }

    /**
     * One named time series of frame samples, with timestamps and durations
     * stored in parallel ring buffers. Pruning happens on insert.
     */
    private static final class Series {

        private final long[] timestampsNs = new long[CAPACITY];
        private final long[] durationsNs = new long[CAPACITY];
        private int head = 0;
        private int tail = 0;
        private int size = 0;

        synchronized void record(long durationNs) {
            final long now = System.nanoTime();
            if (size == CAPACITY) {
                head = (head + 1) % CAPACITY;
                size--;
            }
            timestampsNs[tail] = now;
            durationsNs[tail] = durationNs;
            tail = (tail + 1) % CAPACITY;
            size++;
            final long cutoff = now - WINDOW_NANOS;
            while (size > 0 && timestampsNs[head] < cutoff) {
                head = (head + 1) % CAPACITY;
                size--;
            }
        }

        synchronized long[] snapshotDurations() {
            final long[] out = new long[size];
            for (int i = 0; i < size; i++) {
                out[i] = durationsNs[(head + i) % CAPACITY];
            }
            return out;
        }
    }

    /**
     * Lightweight value type used during paint to avoid holding the series
     * lock while rendering text.
     */
    private record SeriesSnapshot(String name, double p50Ms, double p99Ms, long[] durations) {
    }

    private List<SeriesSnapshot> collectSnapshots() {
        final List<Map.Entry<String, Series>> entries;
        synchronized (seriesMapLock) {
            entries = new ArrayList<>(seriesByName.entrySet());
        }
        final List<SeriesSnapshot> snaps = new ArrayList<>(entries.size());
        for (Map.Entry<String, Series> e : entries) {
            final long[] samples = e.getValue().snapshotDurations();
            if (samples.length == 0) {
                snaps.add(new SeriesSnapshot(e.getKey(), 0.0, 0.0, samples));
                continue;
            }
            final long[] sorted = samples.clone();
            Arrays.sort(sorted);
            snaps.add(new SeriesSnapshot(
                e.getKey(),
                percentileMs(sorted, 0.50),
                percentileMs(sorted, 0.99),
                samples));
        }
        return snaps;
    }

    private final class ChartPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        ChartPanel() {
            setOpaque(true);
            setBackground(new Color(0, 0, 0, 220));
            setPreferredSize(new Dimension(HUD_WIDTH, HUD_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                final int w = getWidth();
                final int h = getHeight();

                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

                final List<SeriesSnapshot> snaps = collectSnapshots();
                if (snaps.isEmpty()) {
                    g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawString("Collecting frame samples...", 8, h / 2 + 4);
                    return;
                }

                drawTable(g2, snaps, w);

                final SeriesSnapshot sparkline = findSeriesSnapshot(snaps, sparklineSeries);
                if (sparkline != null && sparkline.durations().length > 1) {
                    drawSparklineSection(g2, sparkline, w, h);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawTable(Graphics2D g2, List<SeriesSnapshot> snaps, int w) {
            final Font headerFont = getFont().deriveFont(Font.BOLD, 11f);
            final Font rowFont = getFont().deriveFont(Font.PLAIN, 11f);

            g2.setFont(headerFont);
            g2.setColor(new Color(220, 220, 220));
            final int rightCol = w - 8;
            g2.drawString("p50 / p99 (ms, " + WINDOW_SECONDS_HUMAN + ")", 8, 14);

            g2.setFont(rowFont);
            final FontMetrics fm = g2.getFontMetrics();
            int y = 30;
            for (SeriesSnapshot s : snaps) {
                final boolean perRenderer = s.name().contains(":");
                g2.setColor(perRenderer ? new Color(180, 200, 230) : Color.WHITE);
                final String displayName = perRenderer ? "  " + s.name().substring(s.name().indexOf(':') + 1)
                    : s.name();
                g2.drawString(displayName, 8, y);

                final String values = String.format("%5.2f / %5.2f", s.p50Ms(), s.p99Ms());
                final int valuesWidth = fm.stringWidth(values);
                g2.setColor(s.p99Ms() > TARGET_FRAME_MS * 1.5
                    ? new Color(255, 170, 80) : new Color(180, 220, 255));
                g2.drawString(values, rightCol - valuesWidth, y);
                y += 13;
                if (y > getHeight() - 60) {
                    break;
                }
            }
        }

        private SeriesSnapshot findSeriesSnapshot(List<SeriesSnapshot> snaps, String name) {
            if (name == null) {
                return null;
            }
            for (SeriesSnapshot s : snaps) {
                if (name.equals(s.name())) {
                    return s;
                }
            }
            return null;
        }

        private void drawSparklineSection(Graphics2D g2, SeriesSnapshot s, int w, int h) {
            final int chartX = 8;
            final int chartW = w - 16;
            final int chartH = 44;
            final int chartY = h - chartH - 8;

            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawLine(chartX, chartY - 4, chartX + chartW, chartY - 4);

            g2.setFont(getFont().deriveFont(Font.PLAIN, 9f));
            g2.setColor(new Color(255, 255, 255, 160));
            g2.drawString("FPS sparkline", chartX, chartY - 6);

            // Linear FPS scale with 0 at the bottom and SPARKLINE_MAX_FPS at the top,
            // so 60 FPS lands at the chart's vertical middle: above when the engine
            // is faster than 60 FPS, below when it stutters.
            final int y60 = fpsToY(TARGET_FPS, chartY, chartH);
            g2.setColor(new Color(0, 200, 120, 120));
            g2.drawLine(chartX, y60, chartX + chartW, y60);
            g2.drawString("60 FPS", chartX + chartW - 36, y60 - 2);

            drawFpsSparkline(g2, s.durations(), chartX, chartY, chartW, chartH);
        }

        /**
         * Draws the sparkline as an FPS curve. Each chart pixel is min-pooled
         * over the samples that fall into it (i.e. the worst FPS in the bin),
         * so frame-rate dips stay visible at any window length.
         */
        private void drawFpsSparkline(Graphics2D g2, long[] samples,
                                      int chartX, int chartY, int chartW, int chartH) {
            if (chartW <= 0 || samples.length == 0) {
                return;
            }
            final int bins = chartW;
            final double[] binMinFps = new double[bins];
            Arrays.fill(binMinFps, Double.NaN);

            for (int i = 0; i < samples.length; i++) {
                int bin = (int) ((long) i * (bins - 1) / Math.max(1, samples.length - 1));
                if (bin < 0) {
                    bin = 0;
                } else if (bin >= bins) {
                    bin = bins - 1;
                }
                final double ms = samples[i] / 1_000_000.0;
                final double fps = ms > 0 ? 1000.0 / ms : SPARKLINE_MAX_FPS;
                if (Double.isNaN(binMinFps[bin]) || fps < binMinFps[bin]) {
                    binMinFps[bin] = fps;
                }
            }

            g2.setColor(new Color(80, 180, 255, 230));
            int prevX = -1;
            int prevY = -1;
            for (int b = 0; b < bins; b++) {
                if (Double.isNaN(binMinFps[b])) {
                    continue;
                }
                final int x = chartX + b;
                final int y = fpsToY(binMinFps[b], chartY, chartH);
                if (prevX >= 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }
        }

        /** Maps an FPS value to a Y screen coordinate inside the chart band. */
        private int fpsToY(double fps, int chartY, int chartH) {
            final double clamped = Math.max(0.0, Math.min(fps, SPARKLINE_MAX_FPS));
            return chartY + chartH
                - (int) Math.round(clamped / SPARKLINE_MAX_FPS * chartH);
        }
    }
}
