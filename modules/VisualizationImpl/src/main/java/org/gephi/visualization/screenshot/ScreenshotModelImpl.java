package org.gephi.visualization.screenshot;

import java.io.File;
import org.gephi.visualization.api.ScreenshotModel;
import org.gephi.visualization.api.VisualisationModel;
import org.openide.util.NbPreferences;

public class ScreenshotModelImpl implements ScreenshotModel {

    private static final String LAST_PATH = "ScreenshotMaker_Last_Path";
    private static final String LAST_PATH_DEFAULT = "ScreenshotMaker_Last_Path_Default";
    // Model
    private final VisualisationModel visualisationModel;
    // Settings
    private int antiAliasing = 2;
    private int width = 1024;
    private int height = 768;
    private boolean transparentBackground = false;
    private boolean autoSave = false;
    private String defaultDirectory;

    public ScreenshotModelImpl(VisualisationModel visualisationModel) {
        this.visualisationModel = visualisationModel;
        String lastPathDefault = NbPreferences.forModule(ScreenshotControllerImpl.class).get(LAST_PATH_DEFAULT, null);
        defaultDirectory = NbPreferences.forModule(ScreenshotControllerImpl.class).get(LAST_PATH, lastPathDefault);
    }

    @Override
    public VisualisationModel getVisualisationModel() {
        return visualisationModel;
    }


    @Override
    public int getAntiAliasing() {
        return antiAliasing;
    }

    void setAntiAliasing(int antiAliasing) {
        this.antiAliasing = antiAliasing;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    @Override
    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    @Override
    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public void setDefaultDirectory(File directory) {
        if (directory != null && directory.exists()) {
            defaultDirectory = directory.getAbsolutePath();
            NbPreferences.forModule(ScreenshotControllerImpl.class).put(LAST_PATH, defaultDirectory);
        }
    }
}
