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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.FileExporter;
import org.gephi.io.exporter.spi.GraphFileExporter;
import org.gephi.io.exporter.spi.GraphFileExporterSettings;
import org.gephi.io.exporter.spi.TextGraphFileExporter;
import org.gephi.io.exporter.spi.VectorialFileExporter;
import org.gephi.io.exporter.spi.XMLGraphFileExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.workspace.api.Workspace;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExportController.class)
public class DesktopExportController implements ExportController {

    private LongTaskExecutor executor;
    private GraphFileExporter[] graphFileExporters;
    private VectorialFileExporter[] vectorialExporters;

    public DesktopExportController() {

        //Get exportes
        graphFileExporters = new GraphFileExporter[0];
        graphFileExporters = Lookup.getDefault().lookupAll(GraphFileExporter.class).toArray(graphFileExporters);
        vectorialExporters = new VectorialFileExporter[0];
        vectorialExporters = Lookup.getDefault().lookupAll(VectorialFileExporter.class).toArray(vectorialExporters);

        executor = new LongTaskExecutor(true, "Exporter", 10);
    }

    public GraphFileExporter[] getGraphFileExporters() {
        return graphFileExporters;
    }

    public VectorialFileExporter[] getVectorialFileExporters() {
        return vectorialExporters;
    }

    public void doExport(GraphFileExporter exporter, FileObject fileObject, boolean visibleGraphOnly) {
        try {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace = projectController.getCurrentWorkspace();
            GraphFileExporterSettings settings = new GraphFileExporterSettings(workspace, visibleGraphOnly);

            if (exporter instanceof TextGraphFileExporter) {
                exportText(exporter, fileObject, settings);
            } else if (exporter instanceof XMLGraphFileExporter) {
                exportXML(exporter, fileObject, settings);
            }
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    public void doExport(VectorialFileExporter exporter, FileObject fileObject) {
        try {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace = projectController.getCurrentWorkspace();

            exportVectorial(exporter, fileObject, workspace);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    public void doExport(FileObject fileObject) {
        FileExporter exporter = getMatchingExporter(fileObject);
        if (exporter == null) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_no_matching_file_importer"));
        }
        if (exporter instanceof GraphFileExporter) {
            doExport((GraphFileExporter) exporter, fileObject, true);
        } else if (exporter instanceof VectorialFileExporter) {
            doExport((VectorialFileExporter) exporter, fileObject);
        } else {
        }
    }

    private void exportText(Exporter exporter, final FileObject fileObject, final GraphFileExporterSettings settings) {
        final TextGraphFileExporter textExporter = (TextGraphFileExporter) exporter;

        try {
            //Create Writer
            File outputFile = FileUtil.toFile(fileObject);
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

            //Export Task
            LongTask task = null;
            if (textExporter instanceof LongTask) {
                task = (LongTask) textExporter;
            }
            final LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

                public void fatalError(Throwable t) {
                    Logger.getLogger("").log(Level.WARNING, "", t.getCause());
                }
            };

            //Export, execute task
            executor.execute(task, new Runnable() {

                public void run() {
                    try {
                        if (textExporter.exportData(bufferedWriter, settings)) {
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DesktopExportController.class, "DesktopExportController.status.exportSuccess", fileObject.getNameExt()));
                        }
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, "Export to " + fileObject.getNameExt(), errorHandler);

        } catch (IOException e) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_io"));
        }
    }

    private void exportXML(Exporter exporter, final FileObject fileObject, final GraphFileExporterSettings settings) {
        final XMLGraphFileExporter xmlExporter = (XMLGraphFileExporter) exporter;

        try {
            //Create document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            final File outputFile = FileUtil.toFile(fileObject);

            //Export Task
            LongTask task = null;
            if (xmlExporter instanceof LongTask) {
                task = (LongTask) xmlExporter;
            }
            final LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

                public void fatalError(Throwable t) {
                    Logger.getLogger("").log(Level.WARNING, "", t.getCause());
                }
            };

            //Export, execute task
            executor.execute(task, new Runnable() {

                public void run() {
                    try {
                        if (xmlExporter.exportData(document, settings)) {
                            //Write XML Document
                            Source source = new DOMSource(document);
                            Result result = new StreamResult(outputFile);
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.transform(source, result);
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DesktopExportController.class, "DesktopExportController.status.exportSuccess", fileObject.getNameExt()));
                        }
                    } catch (TransformerConfigurationException ex) {
                        throw new RuntimeException(NbBundle.getMessage(getClass(), "error_transformer"), ex);
                    } catch (TransformerException ex) {
                        throw new RuntimeException(NbBundle.getMessage(getClass(), "error_transformer"), ex);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, "Export to " + fileObject.getNameExt(), errorHandler);

        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_missing_document_instance_factory"));
        }
    }

    private void exportVectorial(final VectorialFileExporter exporter, final FileObject fileObject, final Workspace workspace) {
        final File outputFile = FileUtil.toFile(fileObject);
        LongTask task = null;
        if (exporter instanceof LongTask) {
            task = (LongTask) exporter;
        }
        final LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.WARNING, "", t.getCause());
            }
        };

        //Export, execute task
        executor.execute(task, new Runnable() {

            public void run() {
                try {
                    if (exporter.exportData(outputFile, workspace)) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DesktopExportController.class, "DesktopExportController.status.exportSuccess", fileObject.getNameExt()));
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, "Export to " + fileObject.getNameExt(), errorHandler);
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

    private FileExporter getMatchingExporter(FileObject fileObject) {
        for (FileExporter im : graphFileExporters) {
            for (FileType ft : im.getFileTypes()) {
                for (String ex : ft.getExtensions()) {
                    if (fileObject.hasExt(ex)) {
                        return im;
                    }
                }
            }
        }
        return null;
    }
}
