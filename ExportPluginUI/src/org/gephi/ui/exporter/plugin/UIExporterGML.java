/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.exporter.plugin;

import javax.swing.JPanel;
import org.gephi.io.exporter.plugin.ExporterGML;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterGML implements ExporterUI {

    ValidationPanel validationPanel;
    UIExporterGMLPanel panel;
    ExporterGML exporter;
    ExporterGMLSettings settings = new ExporterGMLSettings();

    public JPanel getPanel() {
        panel = new UIExporterGMLPanel();
        validationPanel = UIExporterGMLPanel.createValidationPanel(panel);
        return validationPanel;
    }

    public void setup(Exporter exporter) {
        this.exporter = (ExporterGML) exporter;
        settings.load(this.exporter);
        panel.setup(this.exporter);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporter);
            settings.save(exporter);
        }
        panel = null;
        exporter = null;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterGML;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterGEXF.class, "UIExporterGML.name");
    }

    private static class ExporterGMLSettings {

        private boolean exportLabel = true;
        private boolean exportCoordinates = true;
        private boolean exportNodeSize = true;
        private boolean exportEdgeSize = true;
        private boolean exportColor = true;
        private boolean exportNotRecognizedElements = true;
        private boolean normalize = false;
        private int spaces = 2;

        private void load(ExporterGML exporter) {
            exporter.setExportColor(exportColor);
            exporter.setExportCoordinates(exportCoordinates);
            exporter.setExportEdgeSize(exportEdgeSize);
            exporter.setExportLabel(exportLabel);
            exporter.setExportNodeSize(exportNodeSize);
            exporter.setExportNotRecognizedElements(exportNotRecognizedElements);
            exporter.setNormalize(normalize);
            exporter.setSpaces(spaces);
        }

        private void save(ExporterGML exporter) {
            exportColor = exporter.isExportColor();
            exportCoordinates = exporter.isExportCoordinates();
            exportEdgeSize = exporter.isExportEdgeSize();
            exportLabel = exporter.isExportLabel();
            exportNodeSize = exporter.isExportNodeSize();
            exportNotRecognizedElements = exporter.isExportNotRecognizedElements();
            normalize = exporter.isNormalize();
            spaces = exporter.getSpaces();
        }
    }
}