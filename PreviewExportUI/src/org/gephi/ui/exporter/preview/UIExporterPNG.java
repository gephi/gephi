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
package org.gephi.ui.exporter.preview;

import org.gephi.io.exporter.preview.PNGExporter;
import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Taras Klaskovsky
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterPNG implements ExporterUI {

    private UIExporterPNGPanel panel;
    private PNGExporter exporter;
    private ExporterPNGSettings settings = new ExporterPNGSettings();
    private ValidationPanel validationPanel;

    @Override
    public JPanel getPanel() {
        panel = new UIExporterPNGPanel();
        validationPanel = UIExporterPNGPanel.createValidationPanel(panel);
        return validationPanel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (PNGExporter) exporter;
        settings.load(this.exporter);
        panel.setup(this.exporter);
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporter);
            settings.save(exporter);
        }

        panel = null;
        exporter = null;
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof PNGExporter;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterPDF.class, "UIExporterPNG.name");
    }

    private static class ExporterPNGSettings {

        private int width = 1024;
        private int height = 1024;
        private int margin = 4;
        private boolean transparentBackground;

        void load(PNGExporter exporter) {
            exporter.setHeight(height);
            exporter.setWidth(width);
            exporter.setMargin(margin);
            exporter.setTransparentBackground(transparentBackground);
        }

        void save(PNGExporter exporter) {
            height = exporter.getHeight();
            width = exporter.getWidth();
            margin = exporter.getMargin();
            transparentBackground = exporter.isTransparentBackground();
        }
    }
}
