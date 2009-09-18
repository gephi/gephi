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
package org.gephi.io.desktop;

import org.gephi.io.database.DatabaseType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.io.container.Container;
import org.gephi.io.container.ContainerFactory;
import org.gephi.io.database.Database;
import org.gephi.io.importer.DatabaseImporter;
import org.gephi.io.importer.FileFormatImporter;
import org.gephi.io.importer.FileType;
import org.gephi.io.importer.ImportController;
import org.gephi.io.importer.ImportException;
import org.gephi.io.importer.Importer;
import org.gephi.io.importer.StreamImporter;
import org.gephi.io.importer.TextImporter;
import org.gephi.io.importer.XMLImporter;
import org.gephi.io.logging.Report;
import org.gephi.io.processor.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.ui.database.DatabaseTypeUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskErrorHandler;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Mathieu Bastian
 */
public class DesktopImportController implements ImportController {

    private LongTaskExecutor executor;
    private FileFormatImporter[] fileFormatImporters;
    private DatabaseImporter[] databaseImporters;
    private DatabaseType[] databaseTypes;

    public DesktopImportController() {
        //Get FileFormatImporters
        fileFormatImporters = new FileFormatImporter[0];
        fileFormatImporters = Lookup.getDefault().lookupAll(FileFormatImporter.class).toArray(fileFormatImporters);

        //Get DatabaseImporters
        databaseImporters = new DatabaseImporter[0];
        databaseImporters = Lookup.getDefault().lookupAll(DatabaseImporter.class).toArray(databaseImporters);

        //Get DatabaseTypes
        databaseTypes = new DatabaseType[0];
        databaseTypes = Lookup.getDefault().lookupAll(DatabaseType.class).toArray(databaseTypes);

        executor = new LongTaskExecutor(true, "Importer", 10);
    }

    public void doImport(FileObject fileObject) {
        try {
            FileFormatImporter im = getMatchingImporter(fileObject);
            if (im == null) {
                throw new ImportException(NbBundle.getMessage(getClass(), "error_no_matching_file_importer"));
            }

            //Create Container
            final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
            container.setSource("" + im.getClass());

            //Report
            Report report = new Report();
            container.setReport(report);

            if (im instanceof XMLImporter) {
                importXML(fileObject, im, container);
            } else if (im instanceof TextImporter) {
                importText(fileObject, im, container);
            } else if (im instanceof StreamImporter) {
            }

        } catch (Exception ex) {
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
            ex.printStackTrace();
        }
    }

    private void importXML(FileObject fileObject, Importer importer, final Container container) {
        final Document document = getDocument(fileObject);
        final XMLImporter xmlImporter = (XMLImporter) importer;
        final Report report = container.getReport();
        LongTask task = null;
        if (importer instanceof LongTask) {
            task = (LongTask) importer;
        }

        //ErrorHandler
        final LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(t);
                DialogDisplayer.getDefault().notify(ex);
                t.printStackTrace();
            }
        };

        //Execute task
        executor.execute(task, new Runnable() {

            public void run() {
                try {
                    xmlImporter.importData(document, container.getLoader(), report);
                    finishImport(container);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, "Import " + fileObject.getNameExt(), errorHandler);
    }

    private void importText(FileObject fileObject, Importer importer, final Container container) {
        final BufferedReader reader = getTextReader(fileObject);
        final TextImporter textImporter = (TextImporter) importer;
        final Report report = container.getReport();
        LongTask task = null;
        if (importer instanceof LongTask) {
            task = (LongTask) importer;
        }

        //ErrorHandler
        final LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(t);
                DialogDisplayer.getDefault().notify(ex);
                t.printStackTrace();
            }
        };

        //Execute task
        executor.execute(task, new Runnable() {

            public void run() {
                try {
                    textImporter.importData(reader, container.getLoader(), report);
                    finishImport(container);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, "Import " + fileObject.getNameExt(), errorHandler);
    }

    public void doImport(Database database) {
        try {
            DatabaseType type = getDatabaseType(database);
            if (type == null) {
                throw new ImportException(NbBundle.getMessage(getClass(), "error_no_matching_db_importer"));
            }
            final DatabaseImporter importer = getMatchingImporter(type);
            if (importer == null) {
                throw new ImportException(NbBundle.getMessage(getClass(), "error_no_matching_db_importer"));
            }

            DatabaseTypeUI ui = type.getUI();
            if (ui != null) {
                ui.setup(type);
                String title = "Database settings";
                JPanel panel = ui.getPanel();
                if (panel instanceof ValidationPanel) {
                    ValidationPanel validationPanel = (ValidationPanel) panel;
                    if (!validationPanel.showOkCancelDialog(title)) {
                        return;
                    }
                } else {
                    DialogDescriptor dd = new DialogDescriptor(panel, title);
                    if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.CANCEL_OPTION)) {
                        return;
                    }
                }
                ui.unsetup();
                database = ui.getDatabase();
            }

            //Create Container
            final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
            container.setSource("" + importer.getClass());

            //Report
            final Report report = new Report();
            container.setReport(report);

            LongTask task = null;
            if (importer instanceof LongTask) {
                task = (LongTask) importer;
            }

            //ErrorHandler
            final LongTaskErrorHandler errorHandler = new LongTaskErrorHandler() {

                public void fatalError(Throwable t) {
                    NotifyDescriptor.Exception ex = new NotifyDescriptor.Exception(t);
                    DialogDisplayer.getDefault().notify(ex);
                }
            };

            //Execute task
            final Database db = database;
            executor.execute(task, new Runnable() {

                public void run() {
                    try {
                        importer.importData(db, container.getLoader(), report);
                        finishImport(container);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            }, "Import database", errorHandler);

        } catch (Exception ex) {
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
            ex.printStackTrace();
        }
    }

    private void finishImport(Container container) {

        Report report = container.getReport();

        //Report panel
        ReportPanel reportPanel = new ReportPanel();
        reportPanel.setData(report, container);
        DialogDescriptor dd = new DialogDescriptor(reportPanel, "Import report");
        if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.CANCEL_OPTION)) {
            reportPanel.destroy();
            return;
        }
        reportPanel.destroy();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace;
        if (pc.getCurrentProject() == null) {
            pc.newProject();
            workspace = pc.getCurrentWorkspace();
        } else {
            if (reportPanel.getProcessorStrategy().equals(ProcessorStrategyEnum.FULL)) {
                //New workspace
                workspace = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            } else if (pc.getCurrentWorkspace() == null) {
                //Append mode but no workspace
                workspace = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            } else {
                //Append mode, current workspace is fine
                workspace = pc.getCurrentWorkspace();
            }
        }
        if (container.getSource() != null) {
            workspace.setSource(container.getSource());
        }

        Lookup.getDefault().lookup(Processor.class).process(container.getUnloader());
    }

    private BufferedReader getTextReader(FileObject fileObject) throws ImportException {
        File file = FileUtil.toFile(fileObject);
        try {
            if (file == null) {
                throw new FileNotFoundException();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            return reader;
        } catch (FileNotFoundException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_file_not_found"));
        }
    }

    private Document getDocument(FileObject fileObject) throws ImportException {
        File file = FileUtil.toFile(fileObject);
        try {
            if (file == null) {
                throw new FileNotFoundException();
            }
            InputStream stream = new FileInputStream(file);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            return document;
        } catch (ParserConfigurationException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_missing_document_instance_factory"));
        } catch (FileNotFoundException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_file_not_found"));
        } catch (SAXException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_sax"));
        } catch (IOException ex) {
            throw new ImportException(NbBundle.getMessage(getClass(), "error_io"));
        }
    }

    private FileFormatImporter getMatchingImporter(FileObject fileObject) {
        for (FileFormatImporter im : fileFormatImporters) {
            if (im.isMatchingImporter(fileObject)) {
                return im;
            }
        }
        return null;
    }

    private DatabaseImporter getMatchingImporter(DatabaseType type) {
        for (DatabaseImporter im : databaseImporters) {
            if (im.isMatchingImporter(type)) {
                return im;
            }
        }

        return null;
    }

    private DatabaseType getDatabaseType(Database database) {
        for (DatabaseType dbt : databaseTypes) {
            if (dbt.getDatabaseClass().isAssignableFrom(database.getClass())) {
                return dbt;
            }
        }
        return null;
    }

    public FileType[] getFileTypes() {
        ArrayList<FileType> list = new ArrayList<FileType>();
        for (FileFormatImporter im : fileFormatImporters) {
            for (FileType ft : im.getFileTypes()) {
                list.add(ft);
            }
        }
        return list.toArray(new FileType[0]);
    }

    public DatabaseType[] getDatabaseTypes() {
        return databaseTypes;
    }

    public Database[] getDatabases(DatabaseType type) {
        Database[] dbs = new Database[0];
        return Lookup.getDefault().lookupAll(type.getDatabaseClass()).toArray(dbs);
    }
}
