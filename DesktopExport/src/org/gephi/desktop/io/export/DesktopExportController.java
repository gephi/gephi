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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.io.exporter.ExportController;
import org.gephi.io.exporter.Exporter;
import org.gephi.io.exporter.FileExporter;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.io.exporter.TextExporter;
import org.gephi.io.exporter.XMLExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.ui.exporter.ExporterUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskErrorHandler;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.workspace.api.Workspace;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopExportController implements ExportController {

    private LongTaskExecutor executor;
    private GraphFileExporter[] graphFileExporters;

    public DesktopExportController() {

        //Get FileFormatExporters
        graphFileExporters = new GraphFileExporter[0];
        graphFileExporters = Lookup.getDefault().lookupAll(GraphFileExporter.class).toArray(graphFileExporters);

        executor = new LongTaskExecutor(true, "Exporter", 10);
    }

    public GraphFileExporter[] getGraphFileExporters() {
        return graphFileExporters;
    }

    public void doExport(Exporter exporter, FileObject fileObject) {
        try {

            Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getDirectedGraph();

            if (exporter instanceof TextExporter) {
                exportText(exporter, fileObject, graph);
            } else if (exporter instanceof XMLExporter) {
                exportXML(exporter, fileObject, graph);
            }

        } catch (Exception ex) {
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
            ex.printStackTrace();
        }
    }

    public void doExport(FileObject fileObject) {
        FileExporter exporter = getMatchingExporter(fileObject);
        if (exporter == null) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_no_matching_file_importer"));
        }
        doExport(exporter, fileObject);
    }

    private void exportText(Exporter exporter, FileObject fileObject, final Graph graph) {
        final TextExporter textExporter = (TextExporter) exporter;

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
                    NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(t);
                    DialogDisplayer.getDefault().notify(ex);
                }
            };

            //Export, execute task
            executor.execute(task, new Runnable() {

                public void run() {
                    try {
                        textExporter.exportData(bufferedWriter, graph);
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

    private void exportXML(Exporter exporter, FileObject fileObject, final Graph graph) {
        final XMLExporter xmlExporter = (XMLExporter) exporter;

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
                    NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(t);
                    DialogDisplayer.getDefault().notify(ex);
                }
            };

            //Export, execute task
            executor.execute(task, new Runnable() {

                public void run() {
                    try {
                        if (xmlExporter.exportData(document, graph)) {
                            //Write XML Document
                            Source source = new DOMSource(document);
                            Result result = new StreamResult(outputFile);
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.transform(source, result);
                        }
                    } catch (TransformerConfigurationException ex) {
                        throw new RuntimeException(NbBundle.getMessage(getClass(), "error_transformer"));
                    } catch (TransformerException ex) {
                        throw new RuntimeException(NbBundle.getMessage(getClass(), "error_transformer"));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, "Export to " + fileObject.getNameExt(), errorHandler);

        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_missing_document_instance_factory"));
        }
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
        for (GraphFileExporter im : graphFileExporters) {
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
