/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.io.exporter.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.FileExporterBuilder;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.gephi.io.exporter.spi.VectorFileExporterBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExportController.class)
public class ExportControllerImpl implements ExportController {

    private final FileExporterBuilder[] fileExporterBuilders;
    private final ExporterUI[] uis;

    public ExportControllerImpl() {
        Lookup.getDefault().lookupAll(GraphFileExporterBuilder.class);
        Lookup.getDefault().lookupAll(VectorFileExporterBuilder.class);
        fileExporterBuilders = Lookup.getDefault().lookupAll(FileExporterBuilder.class).toArray(new FileExporterBuilder[0]);
        uis = Lookup.getDefault().lookupAll(ExporterUI.class).toArray(new ExporterUI[0]);
    }

    public void exportFile(File file) throws IOException {
        Exporter fileExporter = getFileExporter(file);
        if (fileExporter == null) {
            throw new RuntimeException(NbBundle.getMessage(ExportControllerImpl.class, "ExportControllerImpl.error.nomatchingexporter"));
        }
        exportFile(file, fileExporter);
    }

    public void exportFile(File file, Workspace workspace) throws IOException {
        Exporter fileExporter = getFileExporter(file);
        if (fileExporter == null) {
            throw new RuntimeException(NbBundle.getMessage(ExportControllerImpl.class, "ExportControllerImpl.error.nomatchingexporter"));
        }
        fileExporter.setWorkspace(workspace);
        exportFile(file, fileExporter);
    }

    public void exportFile(File file, Exporter fileExporter) throws IOException {
        if (fileExporter.getWorkspace() == null) {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace = projectController.getCurrentWorkspace();
            fileExporter.setWorkspace(workspace);
        }
        if (fileExporter instanceof ByteExporter) {
            OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
            ((ByteExporter) fileExporter).setOutputStream(stream);
            try {
                fileExporter.execute();
            } catch (Exception ex) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException exe) {
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new RuntimeException(ex);
            }
            try {
                stream.flush();
                stream.close();
            } catch (IOException ex) {
            }
        } else if (fileExporter instanceof CharacterExporter) {
            Writer writer = new BufferedWriter(new FileWriter(file));
            ((CharacterExporter) fileExporter).setWriter(writer);
            try {
                fileExporter.execute();
            } catch (Exception ex) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException exe) {
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new RuntimeException(ex);
            }
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
            }
        }
    }

    public void exportStream(OutputStream stream, ByteExporter byteExporter) {
        if (byteExporter.getWorkspace() == null) {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace = projectController.getCurrentWorkspace();
            byteExporter.setWorkspace(workspace);
        }
        byteExporter.setOutputStream(stream);
        try {
            byteExporter.execute();
        } catch (Exception ex) {
            try {
                stream.flush();
                stream.close();
            } catch (IOException exe) {
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
        try {
            stream.flush();
            stream.close();
        } catch (IOException ex) {
        }
    }

    public void exportWriter(Writer writer, CharacterExporter characterExporter) {
        if (characterExporter.getWorkspace() == null) {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace = projectController.getCurrentWorkspace();
            characterExporter.setWorkspace(workspace);
        }
        characterExporter.setWriter(writer);
        try {
            characterExporter.execute();
        } catch (Exception ex) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException exe) {
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException ex) {
        }
    }

    public Exporter getFileExporter(File file) {
        for (FileExporterBuilder im : fileExporterBuilders) {
            for (FileType ft : im.getFileTypes()) {
                for (String ex : ft.getExtensions()) {
                    if (hasExt(file, ex)) {
                        return im.buildExporter();
                    }
                }
            }
        }
        return null;
    }

    public Exporter getExporter(String exporterName) {
        for (FileExporterBuilder im : fileExporterBuilders) {
            if (im.getName().equalsIgnoreCase(exporterName)) {
                return im.buildExporter();
            }
        }
        for (FileExporterBuilder im : fileExporterBuilders) {
            for (FileType ft : im.getFileTypes()) {
                for (String ex : ft.getExtensions()) {
                    if (ex.equalsIgnoreCase(exporterName)) {
                        return im.buildExporter();
                    }
                }
            }
        }
        return null;
    }

    public ExporterUI getUI(Exporter exporter) {
        for (ExporterUI ui : uis) {
            if (ui.isUIForExporter(exporter)) {
                return ui;
            }
        }
        return null;
    }

    private boolean hasExt(File file, String ext) {
        if (ext == null || ext.isEmpty()) {
            return false;
        }

        /** period at first position is not considered as extension-separator */
        if ((file.getName().length() - ext.length()) <= 1) {
            return false;
        }

        boolean ret = file.getName().endsWith(ext);

        if (!ret) {
            return false;
        }

        return true;
    }
}
