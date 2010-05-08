/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.DatabaseType;
import org.gephi.io.importer.spi.FileFormatImporter;
import org.gephi.io.importer.spi.StreamImporter;
import org.gephi.io.importer.spi.TextImporter;
import org.gephi.io.importer.spi.XMLImporter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.Scaler;
import org.gephi.project.api.Workspace;
import org.gephi.utils.CharsetToolkit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ImportController.class)
public class ImportControllerImpl implements ImportController {

    private FileFormatImporter[] fileFormatImporters;
    private DatabaseImporter[] databaseImporters;
    private DatabaseType[] databaseTypes;

    public ImportControllerImpl() {
        //Get FileFormatImporters
        fileFormatImporters = new FileFormatImporter[0];
        fileFormatImporters = Lookup.getDefault().lookupAll(FileFormatImporter.class).toArray(fileFormatImporters);

        //Get DatabaseImporters
        databaseImporters = new DatabaseImporter[0];
        databaseImporters = Lookup.getDefault().lookupAll(DatabaseImporter.class).toArray(databaseImporters);

        //Get DatabaseTypes
        databaseTypes = new DatabaseType[0];
        databaseTypes = Lookup.getDefault().lookupAll(DatabaseType.class).toArray(databaseTypes);
    }

    public FileFormatImporter getFileImporter(FileObject fileObject) {
        fileObject = getArchivedFile(fileObject);   //Unzip and return content file
        return getMatchingImporter(fileObject);
    }

    public FileFormatImporter getFileImporter(String importerName) {
        return getMatchingImporter(importerName);
    }

    public DatabaseImporter getDatabaseImporter(Database database) {
        DatabaseType type = getDatabaseType(database);
        if (type != null) {
            return getMatchingImporter(type);
        }
        return null;
    }

    public Container importFile(InputStream stream, FileFormatImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        try {

            if (importer instanceof XMLImporter) {
                Document document = getDocument(stream);
                if (((XMLImporter) importer).importData(document, container.getLoader(), report)) {
                    return container;
                }
            } else if (importer instanceof TextImporter) {
                LineNumberReader reader = getTextReader(stream);
                if (((TextImporter) importer).importData(reader, container.getLoader(), report)) {
                    return container;
                }
            } else if (importer instanceof StreamImporter) {
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    public Container importDatabase(Database database, DatabaseImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);
        try {
            if (importer.importData(database, container.getLoader(), report)) {
                return container;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return null;
    }

    public void process(Container container) {
        Processor processor = Lookup.getDefault().lookup(Processor.class);
        if (processor == null) {
            throw new RuntimeException("Impossible to find Default Processor");
        }
        process(container, processor, null);
    }

    public void process(Container container, Processor processor, Workspace workspace) {
        container.closeLoader();
        if (container.isAutoScale()) {
            Scaler scaler = Lookup.getDefault().lookup(Scaler.class);
            if (scaler != null) {
                scaler.doScale(container);
            }
        }
        processor.process(container.getUnloader(), workspace);
    }

    private LineNumberReader getTextReader(FileObject fileObject) throws RuntimeException {
        try {
            return getTextReader(fileObject.getInputStream());
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_file_not_found"));
        }
    }

    private LineNumberReader getTextReader(InputStream stream) throws RuntimeException {
        try {
            LineNumberReader reader;
            CharsetToolkit charsetToolkit = new CharsetToolkit(stream);
            reader = (LineNumberReader) charsetToolkit.getReader();
            return reader;
        } catch (IOException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_io"));
        }
    }

    private Document getDocument(InputStream stream) throws RuntimeException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            return document;
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_missing_document_instance_factory"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_file_not_found"));
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_sax"));
        } catch (IOException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_io"));
        }
    }

    private Document getDocument(FileObject fileObject) throws RuntimeException {
        try {
            InputStream stream = fileObject.getInputStream();
            return getDocument(stream);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "ImportControllerImpl.error_file_not_found"));
        }
    }

    private FileObject getArchivedFile(FileObject fileObject) {
        if (FileUtil.isArchiveFile(fileObject)) {
            //Unzip
            fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
        }
        return fileObject;
    }

    private FileFormatImporter getMatchingImporter(FileObject fileObject) {
        for (FileFormatImporter im : fileFormatImporters) {
            if (im.isMatchingImporter(fileObject)) {
                return im;
            }
        }
        return null;
    }

    private FileFormatImporter getMatchingImporter(String extension) {
        for (FileFormatImporter im : fileFormatImporters) {
            for (FileType ft : im.getFileTypes()) {
                for (String ext : ft.getExtensions()) {
                    if (ext.equalsIgnoreCase(extension)) {
                        return im;
                    }
                }
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

    public DatabaseType getDatabaseType(Database database) {
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

    public boolean isFileSupported(FileObject fileObject) {
        for (FileFormatImporter im : fileFormatImporters) {
            if (im.isMatchingImporter(fileObject)) {
                return true;
            }
        }
        if (fileObject.hasExt("zip") || fileObject.hasExt("ZIP")) {
            return true;
        }
        return false;
    }
}
