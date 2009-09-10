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

import java.util.ArrayList;
import org.gephi.io.exporter.ExportController;
import org.gephi.io.exporter.Exporter;
import org.gephi.io.exporter.FileFormatExporter;
import org.gephi.io.exporter.FileType;
import org.gephi.ui.exporter.ExporterUI;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopExportController implements ExportController {

    private LongTaskExecutor executor;
    private FileFormatExporter[] fileFormatExporters;

    public DesktopExportController() {

        //Get FileFormatExporters
        fileFormatExporters = new FileFormatExporter[0];
        fileFormatExporters = Lookup.getDefault().lookupAll(FileFormatExporter.class).toArray(fileFormatExporters);

        executor = new LongTaskExecutor(true, "Exporter", 10);
    }

    public FileFormatExporter[] getFileFormatExporters() {
        return fileFormatExporters;
    }

    public ExporterUI getUI(Exporter exporter) {
        Exporter[] exs = Lookup.getDefault().lookupAll(Exporter.class).toArray(new Exporter[0]);
        return null;
    }

    public FileType[] getFileTypes() {
        ArrayList<FileType> list = new ArrayList<FileType>();
        for (FileFormatExporter im : fileFormatExporters) {
            for (FileType ft : im.getFileTypes()) {
                list.add(ft);
            }
        }
        return list.toArray(new FileType[0]);
    }
}
