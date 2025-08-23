package org.gephi.visualization.api;


public interface ScreenshotModel {

    VisualisationModel getVisualisationModel();

    int getAntiAliasing();

    int getWidth();

    int getHeight();

    boolean isTransparentBackground();

    boolean isAutoSave();

    String getDefaultDirectory();
}
