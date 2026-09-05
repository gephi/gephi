package org.gephi.visualization.screenshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;

public class ScreenshotToFileTask implements LongTask, Callable<File> {

    private final JOGLRenderingTarget renderingTarget;
    private final int scaleFactor;
    private final boolean transparentBackground;
    private final File file;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final BooleanSupplier isCancelled = cancelled::get;
    private ProgressTicket progressTicket;

    public ScreenshotToFileTask(VizEngine<JOGLRenderingTarget, ?> engine, int scaleFactor,
                                boolean transparentBackground, File file) {
        this.renderingTarget = engine.getRenderingTarget();
        this.scaleFactor = scaleFactor;
        this.transparentBackground = transparentBackground;
        this.file = file;
    }

    @Override
    public File call() throws Exception {
        BufferedImage image =
            renderingTarget.requestScreenshot(scaleFactor, transparentBackground, isCancelled).get();
        if (!ImageIO.write(image, "png", file)) {
            throw new IOException("No image writer found for PNG format");
        }
        return file;
    }

    @Override
    public boolean cancel() {
        cancelled.set(true);
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
