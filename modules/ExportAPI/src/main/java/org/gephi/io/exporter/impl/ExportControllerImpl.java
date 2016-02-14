/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.io.exporter.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
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

    @Override
    public void exportFile(File file) throws IOException {
        Exporter fileExporter = getFileExporter(file);
        if (fileExporter == null) {
            throw new RuntimeException(NbBundle.getMessage(ExportControllerImpl.class, "ExportControllerImpl.error.nomatchingexporter"));
        }
        exportFile(file, fileExporter);
    }

    @Override
    public void exportFile(File file, Workspace workspace) throws IOException {
        Exporter fileExporter = getFileExporter(file);
        if (fileExporter == null) {
            throw new RuntimeException(NbBundle.getMessage(ExportControllerImpl.class, "ExportControllerImpl.error.nomatchingexporter"));
        }
        fileExporter.setWorkspace(workspace);
        exportFile(file, fileExporter);
    }

    @Override
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
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
