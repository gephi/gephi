/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.io.export;

import org.gephi.io.exporter.ExportController;
import org.gephi.io.exporter.Exporter;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.ui.exporter.ExporterUI;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopExportController implements ExportController {

    private LongTaskExecutor executor;
    private GraphFileExporter[] fileFormatExporters;

    public DesktopExportController() {

        //Get FileFormatExporters
        fileFormatExporters = new GraphFileExporter[0];
        fileFormatExporters = Lookup.getDefault().lookupAll(GraphFileExporter.class).toArray(fileFormatExporters);

        executor = new LongTaskExecutor(true, "Exporter", 10);
    }

    public GraphFileExporter[] getGraphFileExporters() {
        return fileFormatExporters;
    }

    public boolean hasUI(Exporter exporter) {
        ExporterUI[] exporterUIs = Lookup.getDefault().lookupAll(ExporterUI.class).toArray(new ExporterUI[0]);
        for (ExporterUI ui : exporterUIs) {
            if (ui.isMatchingExporter(exporter)) {
                return true;
            }
        }
        return false;
    }

    public ExporterUI getUI(Exporter exporter) {
        ExporterUI[] exporterUIs = Lookup.getDefault().lookupAll(ExporterUI.class).toArray(new ExporterUI[0]);
        for (ExporterUI ui : exporterUIs) {
            if (ui.isMatchingExporter(exporter)) {
                return ui;
            }
        }
        return null;
    }
}
