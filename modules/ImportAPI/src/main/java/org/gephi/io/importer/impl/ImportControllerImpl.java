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
package org.gephi.io.importer.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
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
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.io.importer.spi.WizardImporterBuilder;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.Scaler;
import org.gephi.project.api.Workspace;
import org.gephi.utils.TempDirUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.io.ReaderInputStream;
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
    private final WizardImporterBuilder[] wizardImporterBuilders;
    private final ImporterUI[] uis;
    private final ImporterWizardUI[] wizardUis;

    public ImportControllerImpl() {
        //Get FileFormatImporters
        fileImporterBuilders = Lookup.getDefault().lookupAll(FileImporterBuilder.class).toArray(new FileImporterBuilder[0]);

        //Get DatabaseImporters
        databaseImporterBuilders = Lookup.getDefault().lookupAll(DatabaseImporterBuilder.class).toArray(new DatabaseImporterBuilder[0]);

        //Get Wizards
        wizardImporterBuilders = Lookup.getDefault().lookupAll(WizardImporterBuilder.class).toArray(new WizardImporterBuilder[0]);

        //Get UIS
        uis = Lookup.getDefault().lookupAll(ImporterUI.class).toArray(new ImporterUI[0]);
        wizardUis = Lookup.getDefault().lookupAll(ImporterWizardUI.class).toArray(new ImporterWizardUI[0]);
    }

    @Override
    public FileImporter getFileImporter(File file) {
        if (file != null) {
            return getFileImporter(FileUtil.toFileObject(file));
        }

        return null;
    }

    @Override
    public FileImporter getFileImporter(FileObject fileObject) {
        if (fileObject != null) {
            fileObject = ImportUtils.getArchivedFile(fileObject);   //Unzip and return content file
            FileImporterBuilder builder = getMatchingImporter(fileObject);
            if (fileObject != null && builder != null) {
                FileImporter fi = builder.buildImporter();
                return fi;
            }
        }
        
        return null;
    }

    @Override
    public FileImporter getFileImporter(String importerName) {
        FileImporterBuilder builder = getMatchingImporter(importerName);
        if (builder != null) {
            return builder.buildImporter();
        }
        return null;
    }

    @Override
    public Container importFile(File file) throws FileNotFoundException {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null) {
            fileObject = ImportUtils.getArchivedFile(fileObject);   //Unzip and return content file
            file = FileUtil.toFile(fileObject);
            FileImporterBuilder builder = getMatchingImporter(fileObject);
            if (fileObject != null && builder != null) {
                Container c = importFile(fileObject.getInputStream(), builder.buildImporter(), file);
                return c;
            }
        }
        return null;
    }

    @Override
    public Container importFile(File file, FileImporter importer) throws FileNotFoundException {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject != null) {
            fileObject = ImportUtils.getArchivedFile(fileObject);   //Unzip and return content file
            file = FileUtil.toFile(fileObject);
            if (fileObject != null) {
                Container c = importFile(fileObject.getInputStream(), importer, file);
                return c;
            }
        }
        return null;
    }

    @Override
    public Container importFile(Reader reader, FileImporter importer) {
        return importFile(reader, importer, null);
    }

    public Container importFile(Reader reader, FileImporter importer, File file) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        if (importer instanceof FileImporter.FileAware) {
            if (file == null) {
                //There is no source file but the importer needs it, create temporary copy:
                try {
                    file = TempDirUtils.createTempDir().createFile("file_copy");
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        FileUtil.copy(new ReaderInputStream(reader, "UTF-8"), fos);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            
            ((FileImporter.FileAware) importer).setFile(file);
        } else {
            importer.setReader(reader);
        }

        try {
            if (importer.execute(container.getLoader())) {
                if (importer.getReport() != null && importer.getReport() != report) {
                    report.append(importer.getReport());
                }
                report.close();
                return container;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                //NOOP
            }
        }
        return null;
    }

    @Override
    public Container importFile(InputStream stream, FileImporter importer) {
        return importFile(stream, importer, null);
    }

    public Container importFile(InputStream stream, FileImporter importer, File file) {
        try {
            Reader reader = ImportUtils.getTextReader(stream);
            return importFile(reader, importer, file);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public Container importDatabase(Database database, DatabaseImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        importer.setDatabase(database);

        try {
            if (importer.execute(container.getLoader())) {
                if (importer.getReport() != null && importer.getReport() != report) {
                    report.append(importer.getReport());
                }
                report.close();
                return container;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    @Override
    public Container importWizard(WizardImporter importer) {
        //Create Container
        final Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();

        //Report
        Report report = new Report();
        container.setReport(report);

        try {
            if (importer.execute(container.getLoader())) {
                if (importer.getReport() != null) {
                    report.append(importer.getReport());
                }
                report.close();
                return container;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    @Override
    public void process(Container container) {
        Processor processor = Lookup.getDefault().lookup(Processor.class);
        if (processor == null) {
            throw new RuntimeException("Impossible to find Default Processor");
        }
        process(container, processor, null);
    }

    @Override
    public void process(Container container, Processor processor, Workspace workspace) {
        container.closeLoader();
        if (container.getUnloader().isAutoScale()) {
            Scaler scaler = Lookup.getDefault().lookup(Scaler.class);
            if (scaler != null) {
                scaler.doScale(container);
            }
        }
        processor.setContainers(new ContainerUnloader[]{container.getUnloader()});
        processor.setWorkspace(workspace);
        processor.process();
    }

    @Override
    public void process(Container[] containers, Processor processor, Workspace workspace) {
        ContainerUnloader[] unloaders = new ContainerUnloader[containers.length];
        int i = 0;
        for (Container container : containers) {
            container.closeLoader();
            if (container.getUnloader().isAutoScale()) {
                Scaler scaler = Lookup.getDefault().lookup(Scaler.class);
                if (scaler != null) {
                    scaler.doScale(container);
                }
            }
            unloaders[i++] = container.getUnloader();
        }
        processor.setContainers(unloaders);
        processor.setWorkspace(workspace);
        processor.process();
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
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        for (FileImporterBuilder im : fileImporterBuilders) {
            for (FileType ft : im.getFileTypes()) {
                for (String ext : ft.getExtensions()) {
                    if (ext.startsWith(".")) {
                        ext = ext.substring(1);
                    }
                    if (ext.equalsIgnoreCase(extension)) {
                        return im;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public FileType[] getFileTypes() {
        ArrayList<FileType> list = new ArrayList<>();
        for (FileImporterBuilder im : fileImporterBuilders) {
            for (FileType ft : im.getFileTypes()) {
                list.add(ft);
            }
        }
        return list.toArray(new FileType[0]);
    }

    @Override
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

    @Override
    public ImporterUI getUI(Importer importer) {
        for (ImporterUI ui : uis) {
            if (ui.isUIForImporter(importer)) {
                return ui;
            }
        }
        return null;
    }

    @Override
    public ImporterWizardUI getWizardUI(Importer importer) {
        for (ImporterWizardUI ui : wizardUis) {
            if (ui.isUIForImporter(importer)) {
                return ui;
            }
        }
        return null;
    }
}
