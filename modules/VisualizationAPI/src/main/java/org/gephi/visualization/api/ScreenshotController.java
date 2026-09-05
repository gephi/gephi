package org.gephi.visualization.api;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Controller for taking screenshots of the visualization.
 *
 * @author Mathieu Bastian
 */
public interface ScreenshotController {

    /**
     * Triggers the screenshot task in a background thread. This method returns immediately.
     */
    void takeScreenshot();

    /**
     * Takes a screenshot in a background thread and writes it to the given file as a PNG. This method returns
     * immediately.
     * <p>
     * Unlike {@link #takeScreenshot()}, this method is independent of the controller's configured settings
     * (scale factor, transparent background, auto-save, default directory) and never shows a file chooser: the
     * parameters passed here are used as-is and the screenshot is always written to {@code file}.
     * <p>
     * The returned future completes with {@code file} once the screenshot has been written to disk, or completes
     * exceptionally if the screenshot could not be taken or the file could not be written. Do not block on the
     * returned future from the AWT event dispatch thread.
     *
     * @param scaleFactor            the scale factor to apply
     * @param transparentBackground true if the background should be transparent
     * @param file                   the file to write the screenshot to
     * @return a future that completes with the file once the screenshot has been written
     */
    Future<File> takeScreenshot(int scaleFactor, boolean transparentBackground, File file);

    /**
     * Sets the scale factor for screenshots.
     *
     * @param scaleFactor the scale factor
     */
    void setScaleFactor(int scaleFactor);

    /**
     * Sets whether the screenshot background should be transparent.
     *
     * @param transparentBackground true if the background is transparent
     */
    void setTransparentBackground(boolean transparentBackground);

    /**
     * Sets whether the screenshot should be automatically saved to disk, or if a file chooser should be shown.
     *
     * @param autoSave true if the screenshot is auto-saved
     */
    void setAutoSave(boolean autoSave);

    /**
     * Sets the default directory for saving screenshots.
     *
     * @param directory the default directory
     */
    void setDefaultDirectory(File directory);
}
