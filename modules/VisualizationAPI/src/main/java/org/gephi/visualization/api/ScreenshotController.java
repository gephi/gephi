package org.gephi.visualization.api;

import java.io.File;

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
