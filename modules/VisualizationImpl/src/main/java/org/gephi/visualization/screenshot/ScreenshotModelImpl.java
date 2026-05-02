package org.gephi.visualization.screenshot;

import java.io.File;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.visualization.VizConfig;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.ScreenshotModel;
import org.gephi.visualization.api.VisualizationModel;
import org.openide.util.NbPreferences;

public class ScreenshotModelImpl implements ScreenshotModel {

    protected static final String LAST_PATH = "ScreenshotMaker_Last_Path";
    protected static final String LAST_PATH_DEFAULT = "ScreenshotMaker_Last_Path_Default";
    // Model
    private final VizModel vizModel;
    // Settings
    private int scaleFactor;
    private boolean transparentBackground;
    private boolean autoSave;
    private String defaultDirectory;

    public ScreenshotModelImpl(VizModel vizModel) {
        this.vizModel = vizModel;
        String lastPathDefault = NbPreferences.forModule(ScreenshotControllerImpl.class).get(LAST_PATH_DEFAULT, null);
        defaultDirectory = NbPreferences.forModule(ScreenshotControllerImpl.class).get(LAST_PATH, lastPathDefault);

        scaleFactor = VizConfig.getDefaultScreenshotScaleFactor();
        transparentBackground = VizConfig.isDefaultScreenshotTransparentBackground();
        autoSave = VizConfig.isDefaultScreenshotAutoSave();
    }

    @Override
    public VisualizationModel getVisualizationModel() {
        return vizModel;
    }

    @Override
    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
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

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();
            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("scaleFactor".equalsIgnoreCase(name)) {
                        scaleFactor = Integer.parseInt(reader.getAttributeValue(null, "value"));
                    } else if ("transparentBackground".equalsIgnoreCase(name)) {
                        transparentBackground = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("autoSave".equalsIgnoreCase(name)) {
                        autoSave = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("defaultDirectory".equalsIgnoreCase(name)) {
                        String path = reader.getAttributeValue(null, "value");
                        if (path != null && !path.isEmpty()) {
                            File dir = new File(path);
                            if (dir.exists()) {
                                defaultDirectory = path;
                            }
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("screenshotModel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("scaleFactor");
        writer.writeAttribute("value", String.valueOf(scaleFactor));
        writer.writeEndElement();

        writer.writeStartElement("transparentBackground");
        writer.writeAttribute("value", String.valueOf(transparentBackground));
        writer.writeEndElement();

        writer.writeStartElement("autoSave");
        writer.writeAttribute("value", String.valueOf(autoSave));
        writer.writeEndElement();

        if (defaultDirectory != null && !defaultDirectory.isEmpty()) {
            writer.writeStartElement("defaultDirectory");
            writer.writeAttribute("value", defaultDirectory);
            writer.writeEndElement();
        }
    }
}
