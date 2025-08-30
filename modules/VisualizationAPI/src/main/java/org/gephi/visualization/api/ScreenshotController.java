package org.gephi.visualization.api;

import java.io.File;
import org.gephi.project.spi.Controller;
import org.openide.util.NbPreferences;

public interface ScreenshotController {

    void setAntiAliasing(int antiAliasing);

    void setWidth(int width);

    void setHeight(int height);

    void setTransparentBackground(boolean transparentBackground);

    void setAutoSave(boolean autoSave);

    void setDefaultDirectory(File directory);
}
