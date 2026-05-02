package org.gephi.visualization.api;

/**
 * Screenshot-related settings.
 *
 * @author Mathieu Bastian
 */
public interface ScreenshotModel {

    /**
     * Returns the visualization model associated with this screenshot model.
     *
     * @return the visualization model
     */
    VisualizationModel getVisualizationModel();

    /**
     * Returns the scale factor for screenshots.
     * <p>
     * Default is 1. A scale factor of 2 means the screenshot will have twice the size of the surface.
     *
     * @return the scale factor
     */
    int getScaleFactor();

    /**
     * Returns whether the screenshot background should be transparent.
     * <p>
     * Default is false.
     *
     * @return true if the background is transparent
     */
    boolean isTransparentBackground();

    /**
     * Returns whether the screenshot should be automatically saved to disk, or if a file chooser should be shown.
     * <p>
     * Default is false.
     *
     * @return true if the screenshot is auto-saved
     */
    boolean isAutoSave();

    /**
     * Returns the default directory for saving screenshots.
     * <p>
     * When auto-save is enabled, screenshots will be saved in this directory.
     *
     * @return the default directory path as a string
     */
    String getDefaultDirectory();
}
