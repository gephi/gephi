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
package org.gephi.io.exporter.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.project.api.Workspace;

/**
 * Manage exporting tasks.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>ExportController ec = Lookup.getDefault().lookup(ExportController.class);</pre>
 *
 * @author Mathieu Bastian
 * @see Exporter
 */
public interface ExportController {

    public void exportFile(File file) throws IOException;

    public void exportFile(File file, Workspace workspace) throws IOException;

    public void exportFile(File file, Exporter fileExporter) throws IOException;

    public void exportWriter(Writer writer, CharacterExporter characterExporter);

    public void exportStream(OutputStream stream, ByteExporter byteExporter);

    public Exporter getFileExporter(File file);

    public Exporter getExporter(String exporterName);

    public ExporterUI getUI(Exporter exporter);
}
