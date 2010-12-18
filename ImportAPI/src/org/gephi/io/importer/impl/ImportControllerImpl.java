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
package org.gephi.io.importer.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.importer.spi.SpigotImporterBuilder;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.Scaler;
import org.gephi.project.api.Workspace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
@ServiceProvider(service = ImportController.class)
public class ImportControllerImpl implements ImportController {

    private final FileImporterBuilder[] fileImporterBuilders;
    private final DatabaseImporterBuilder[] databaseImporterBuilders;
    private final SpigotImporterBuilder[] spigotImporterBuilders;
    private final ImporterUI[] uis;
    private final ImporterWizardUI[] wizardUis;

    public ImportControllerImpl() {
        //Get FileFormatImporters
        fileImporterBuilders = Lookup.getDefault().lookupAll(FileImporterBuilder.class).toArray(new FileImporterBuilder[0]);

        //Get DatabaseImporters
        databaseImporterBuilders = Lookup.getDefault().lookupAll(DatabaseImporterBuilder.class).toArray(new DatabaseImporterBuilder[0]);

        //Get Spigots
        spigotImporterBuilders = Lookup.getDefault().lookupAll(SpigotImporterBuilder.class).toArray(new SpigotImporterBuilder[0]);

        //Get UIS
        uis = Lookup.getDefault().lookupAll(ImporterUI.class).toArray(new ImporterUI[0]);
        wizardUis = Lookup.getDefault().lookupAll(ImporterWizardUI.class).toArray(new ImporterWizardUI[0]);
    }

    public FileImporter getFileImporter(File file) {
        FileObject fileObject = FileUtil.toFileObject(file);
        fileObject = getArchivedFile(fileObject);   //Unzip and return content file
        FileImporterBuilder builder = getMatchingImporter(fileObject);
        if (fileObject != null && builder != null) {
            FileImporter fi = builder.buildImporter();
            if (fileObject.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                try {
                    fileObject.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return fi;
        }
        return null;
    }

    public FileImporter getFileImporter(String importerName) {
        FileImporterBuilder builder = getMatchingImporter(importerName);
        if (builder != null) {
            return builder.buildImporter();
        }
        return null;
    }

    public Container importFile(File file) throws FileNotFoundException {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null) {
            fileObject = getArchivedFile(fileObject);   //Unzip and return content file
            FileImporterBuilder builder = getMatchingImporter(fileObject);
            if (fileObject != null && builder != null) {
                Container c = importFile(fileObject.getInputStream(), builder.buildImporter());
                if (fileObject.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                    try {
                        fileObject.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return c;
            }
        }
        return null;
    }

    public Container importFile(File file, FileImporter importer) throws FileNotFoundException {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null) {
            fileObject = getArchivedFile(fileObject);   //Unzip and return content file
            if (fileObject != null) {
                Container c = importFile(fileObject.getInputStream(), importer);
                if (fileObject.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                    try {
                        fileObject.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return c;
            }
        }
        return null;
    }

    public Container importFile(Reader reader, FileImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        importer.setReader(reader);

        try {
            if (importer.execute(container.getLoader())) {
                if (importer.getReport() != null) {
                    report.append(importer.getReport());
                }
                return container;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    public Container importFile(InputStream stream, FileImporter importer) {
        try {
            Reader reader = ImportUtils.getTextReader(stream);
            return importFile(reader, importer);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Container importDatabase(Database database, DatabaseImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        importer.setDatabase(database);

        try {
            if (importer.execute(container.getLoader())) {
                if (importer.getReport() != null) {
                    report.append(importer.getReport());
                }
                return container;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    public Container importSpigot(SpigotImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        try {
            if (importer.execute(container.getLoader())) {
                if (importer.getReport() != null) {
                    report.append(importer.getReport());
                }
                return container;
            }
        } catch (RuntimeException ex) {
            throw ex;
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
        processor.setContainer(container.getUnloader());
        processor.setWorkspace(workspace);
        processor.process();
    }

    private FileObject getArchivedFile(FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        // ZIP and JAR archives
        if (FileUtil.isArchiveFile(fileObject)) {
            fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
        } else { // GZ or BZIP2 archives
            boolean isGz = fileObject.getExt().equalsIgnoreCase("gz");
            boolean isBzip = fileObject.getExt().equalsIgnoreCase("bz2");
            if (isGz || isBzip) {
                try {
                    String[] splittedFileName = fileObject.getName().split("\\.");
                    if (splittedFileName.length < 2) {
                        return fileObject;
                    }

                    String fileExt1 = splittedFileName[splittedFileName.length - 1];
                    String fileExt2 = splittedFileName[splittedFileName.length - 2];

                    File tempFile = null;
                    if (fileExt1.equalsIgnoreCase("tar")) {
                        String fname = fileObject.getName().replaceAll("\\.tar$", "");
                        fname = fname.replace(fileExt2, "");
                        tempFile = File.createTempFile(fname, "." + fileExt2);
                        // Untar & unzip
                        if (isGz) {
                            tempFile = ImportUtils.getGzFile(fileObject, tempFile, true);
                        } else {
                            tempFile = ImportUtils.getBzipFile(fileObject, tempFile, true);
                        }
                    } else {
                        String fname = fileObject.getName();
                        fname = fname.replace(fileExt1, "");
                        tempFile = File.createTempFile(fname, "." + fileExt1);
                        // Unzip
                        if (isGz) {
                            tempFile = ImportUtils.getGzFile(fileObject, tempFile, false);
                        } else {
                            tempFile = ImportUtils.getBzipFile(fileObject, tempFile, false);
                        }
                    }
                    tempFile.deleteOnExit();
                    tempFile = FileUtil.normalizeFile(tempFile);
                    fileObject = FileUtil.toFileObject(tempFile);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return fileObject;
    }

    private FileImporterBuilder getMatchingImporter(FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        for (FileImporterBuilder im : fileImporterBuilders) {
            if (im.isMatchingImporter(fileObject)) {
                return im;
            }
        }
        return null;
    }

    private FileImporterBuilder getMatchingImporter(String extension) {
        for (FileImporterBuilder im : fileImporterBuilders) {
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

    public FileType[] getFileTypes() {
        ArrayList<FileType> list = new ArrayList<FileType>();
        for (FileImporterBuilder im : fileImporterBuilders) {
            for (FileType ft : im.getFileTypes()) {
                list.add(ft);
            }
        }
        return list.toArray(new FileType[0]);
    }

    public boolean isFileSupported(File file) {
        FileObject fileObject = FileUtil.toFileObject(file);
        for (FileImporterBuilder im : fileImporterBuilders) {
            if (im.isMatchingImporter(fileObject)) {
                return true;
            }
        }
        if (fileObject.getExt().equalsIgnoreCase("zip")
                || fileObject.getExt().equalsIgnoreCase("gz")
                || fileObject.getExt().equalsIgnoreCase("bz2")) {
            return true;
        }
        return false;
    }

    public ImporterUI getUI(Importer importer) {
        for (ImporterUI ui : uis) {
            if (ui.isUIForImporter(importer)) {
                return ui;
            }
        }
        return null;
    }

    public ImporterWizardUI getWizardUI(Importer importer) {
        for (ImporterWizardUI ui : wizardUis) {
            if (ui.isUIForImporter(importer)) {
                return ui;
            }
        }
        return null;
    }
}
