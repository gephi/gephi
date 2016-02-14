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
package org.gephi.desktop.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.ProcessorUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
@ServiceProvider(service = ImportControllerUI.class)
public class DesktopImportControllerUI implements ImportControllerUI {

    private final LongTaskExecutor executor;
    private final LongTaskErrorHandler errorHandler;
    private final ImportController controller;

    public DesktopImportControllerUI() {
        controller = Lookup.getDefault().lookup(ImportController.class);
        errorHandler = new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                if (t instanceof OutOfMemoryError) {
                    return;
                }
//                t.printStackTrace();
//                String message = t.getCause().getMessage();
//                if (message == null || message.isEmpty()) {
//                    message = t.getMessage();
//                }
//                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
//                DialogDisplayer.getDefault().notify(msg);
                Exceptions.printStackTrace(t);
                t.printStackTrace();
            }
        };
        executor = new LongTaskExecutor(true, "Importer", 10);
    }

    @Override
    public void importFile(FileObject fileObject) {
        try {
            final FileImporter importer = controller.getFileImporter(FileUtil.toFile(fileObject));
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            //MRU
            MostRecentFiles mostRecentFiles = Lookup.getDefault().lookup(MostRecentFiles.class);
            mostRecentFiles.addFile(fileObject.getPath());

            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.file.ui.dialog.title", ui.getDisplayName());
                JPanel panel = ui.getPanel();
                FileImporter[] fi = (FileImporter[]) Array.newInstance(importer.getClass(), 1);
                fi[0] = importer;
                ui.setup(fi);
                final DialogDescriptor dd = new DialogDescriptor(panel, title);
                if (panel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) panel;
                    vp.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                Object result = DialogDisplayer.getDefault().notify(dd);
                if (!result.equals(NotifyDescriptor.OK_OPTION)) {
                    ui.unsetup(false);
                    return;
                }
                ui.unsetup(true);
            }

            LongTask task = null;
            if (importer instanceof LongTask) {
                task = (LongTask) importer;
            }

            //Execute task
            fileObject = getArchivedFile(fileObject);
            final String containerSource = fileObject.getNameExt();
            final InputStream stream = fileObject.getInputStream();
            String taskName = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importFile(stream, importer);
                        if (container != null) {
                            container.setSource(containerSource);
                            finishImport(container);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, errorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    @Override
    public void importFiles(FileObject[] fileObjects) {
        try {
            Map<ImporterUI, List<FileImporter>> importerUIs = new HashMap<>();
            List<FileImporter> importers = new ArrayList<>();
            for (FileObject fileObject : fileObjects) {
                FileImporter importer = controller.getFileImporter(FileUtil.toFile(fileObject));
                if (importer == null) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }
                importers.add(importer);
                ImporterUI ui = controller.getUI(importer);
                if (ui != null) {
                    List<FileImporter> l = importerUIs.get(ui);
                    if (l == null) {
                        l = new ArrayList<>();
                        importerUIs.put(ui, l);
                    }
                    l.add(importer);
                }

                //MRU
                MostRecentFiles mostRecentFiles = Lookup.getDefault().lookup(MostRecentFiles.class);
                mostRecentFiles.addFile(fileObject.getPath());
            }

            for (Map.Entry<ImporterUI, List<FileImporter>> entry : importerUIs.entrySet()) {
                ImporterUI ui = entry.getKey();
                String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.file.ui.dialog.title", ui.getDisplayName());
                JPanel panel = ui.getPanel();
                FileImporter[] fi = (FileImporter[]) entry.getValue().toArray((FileImporter[]) Array.newInstance(entry.getValue().get(0).getClass(), 0));
                ui.setup(fi);
                final DialogDescriptor dd = new DialogDescriptor(panel, title);
                if (panel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) panel;
                    vp.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                Object result = DialogDisplayer.getDefault().notify(dd);
                if (!result.equals(NotifyDescriptor.OK_OPTION)) {
                    ui.unsetup(false);
                    return;
                }
                ui.unsetup(true);
            }

            final List<Container> result = new ArrayList<>();
            for (int i = 0; i < importers.size(); i++) {
                final FileImporter importer = importers.get(i);
                FileObject fileObject = fileObjects[i];
                LongTask task = null;
                if (importer instanceof LongTask) {
                    task = (LongTask) importer;
                }

                //Execute task
                fileObject = getArchivedFile(fileObject);
                final String containerSource = fileObject.getNameExt();
                final InputStream stream = fileObject.getInputStream();
                String taskName = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
                executor.execute(task, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Container container = controller.importFile(stream, importer);
                            if (container != null) {
                                container.setSource(containerSource);
                                result.add(container);
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }, taskName, errorHandler);
            }
            executor.execute(null, new Runnable() {

                @Override
                public void run() {
                    if (!result.isEmpty()) {
                        finishImport(result.toArray(new Container[0]));
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    @Override
    public void importStream(final InputStream stream, String importerName) {
        try {
            final FileImporter importer = controller.getFileImporter(importerName);
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.file.ui.dialog.title", ui.getDisplayName());
                JPanel panel = ui.getPanel();
                FileImporter[] fi = (FileImporter[]) Array.newInstance(importer.getClass(), 1);
                fi[0] = importer;
                ui.setup(fi);
                final DialogDescriptor dd = new DialogDescriptor(panel, title);
                if (panel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) panel;
                    vp.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                Object result = DialogDisplayer.getDefault().notify(dd);
                if (!result.equals(NotifyDescriptor.OK_OPTION)) {
                    ui.unsetup(false);
                    return;
                }
                ui.unsetup(true);
            }

            LongTask task = null;
            if (importer instanceof LongTask) {
                task = (LongTask) importer;
            }

            //Execute task
            final String containerSource = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.streamSource", importerName);
            String taskName = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importFile(stream, importer);
                        if (container != null) {
                            container.setSource(containerSource);
                            finishImport(container);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, errorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    @Override
    public void importFile(final Reader reader, String importerName) {
        try {
            final FileImporter importer = controller.getFileImporter(importerName);
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                FileImporter[] fi = (FileImporter[]) Array.newInstance(importer.getClass(), 1);
                fi[0] = importer;
                ui.setup(fi);
                String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.file.ui.dialog.title", ui.getDisplayName());
                JPanel panel = ui.getPanel();
                final DialogDescriptor dd = new DialogDescriptor(panel, title);
                if (panel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) panel;
                    vp.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                Object result = DialogDisplayer.getDefault().notify(dd);
                if (!result.equals(NotifyDescriptor.OK_OPTION)) {
                    ui.unsetup(false);
                    return;
                }
                ui.unsetup(true);
            }

            LongTask task = null;
            if (importer instanceof LongTask) {
                task = (LongTask) importer;
            }

            //Execute task
            final String containerSource = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.streamSource", importerName);
            String taskName = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importFile(reader, importer);
                        if (container != null) {
                            container.setSource(containerSource);
                            finishImport(container);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, errorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    @Override
    public void importDatabase(DatabaseImporter importer) {
        importDatabase(null, importer);
    }

    @Override
    public void importDatabase(Database database, final DatabaseImporter importer) {
        try {
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.error_no_matching_db_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.database.ui.dialog.title");
                JPanel panel = ui.getPanel();
                ui.setup(new DatabaseImporter[]{importer});
                final DialogDescriptor dd = new DialogDescriptor(panel, title);
                if (panel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) panel;
                    vp.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                Object result = DialogDisplayer.getDefault().notify(dd);
                if (result.equals(NotifyDescriptor.CANCEL_OPTION) || result.equals(NotifyDescriptor.CLOSED_OPTION)) {
                    ui.unsetup(false);
                    return;
                }
                ui.unsetup(true);
                if (database == null) {
                    database = importer.getDatabase();
                }
            }

            LongTask task = null;
            if (importer instanceof LongTask) {
                task = (LongTask) importer;
            }

            //Execute task
            final String containerSource = database != null ? database.getName() : (ui != null ? ui.getDisplayName() : importer.getClass().getSimpleName());
            final Database db = database;
            String taskName = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importDatabase(db, importer);
                        if (container != null) {
                            container.setSource(containerSource);
                            finishImport(container);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, errorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    @Override
    public void importWizard(final WizardImporter importer) {
        try {
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.error_no_matching_db_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            String containerSource = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.wizardSource", "");
            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.wizard.ui.dialog.title", ui.getDisplayName());
                JPanel panel = ui.getPanel();
                ui.setup(new WizardImporter[]{importer});
                final DialogDescriptor dd = new DialogDescriptor(panel, title);
                if (panel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) panel;
                    vp.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                Object result = DialogDisplayer.getDefault().notify(dd);
                if (result.equals(NotifyDescriptor.CANCEL_OPTION) || result.equals(NotifyDescriptor.CLOSED_OPTION)) {
                    ui.unsetup(false);
                    return;
                }
                ui.unsetup(true);
                containerSource = ui.getDisplayName();
            }
            ImporterWizardUI wizardUI = controller.getWizardUI(importer);
            if (wizardUI != null) {
                containerSource = wizardUI.getCategory() + ":" + wizardUI.getDisplayName();
            }

            LongTask task = null;
            if (importer instanceof LongTask) {
                task = (LongTask) importer;
            }

            //Execute task
            final String source = containerSource;
            String taskName = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importWizard(importer);
                        if (container != null) {
                            container.setSource(source);
                            finishImport(container);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, errorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    private void finishImport(Container container) {
        finishImport(new Container[]{container});
    }

    private void finishImport(Container[] containers) {
        Report finalReport = new Report();
        for (Container container : containers) {
            if (container.verify()) {
                Report report = container.getReport();
                report.close();
                finalReport.append(report);
            } else {
                //TODO
            }
        }
        finalReport.close();

        //Report panel
        ReportPanel reportPanel = new ReportPanel();
        reportPanel.setData(finalReport, containers);
        DialogDescriptor dd = new DialogDescriptor(reportPanel, NbBundle.getMessage(DesktopImportControllerUI.class, "ReportPanel.title"));
        Object response = DialogDisplayer.getDefault().notify(dd);
        reportPanel.destroy();
        finalReport.clean();
        for (Container c : containers) {
            c.getReport().clean();
        }
        if (!response.equals(NotifyDescriptor.OK_OPTION)) {
            return;
        }
        final Processor processor = reportPanel.getProcessor();

        //Project
        Workspace workspace = null;
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        ProjectControllerUI pcui = Lookup.getDefault().lookup(ProjectControllerUI.class);
        if (pc.getCurrentProject() == null) {
            pcui.newProject();
            workspace = pc.getCurrentWorkspace();
        }

        //Process
        final ProcessorUI pui = getProcessorUI(processor);
        final ValidResult validResult = new ValidResult();
        if (pui != null) {
            try {
                final JPanel panel = pui.getPanel();
                if (panel != null) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            String title = NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.processor.ui.dialog.title");

                            pui.setup(processor);
                            final DialogDescriptor dd2 = new DialogDescriptor(panel, title);
                            if (panel instanceof ValidationPanel) {
                                ValidationPanel vp = (ValidationPanel) panel;
                                vp.addChangeListener(new ChangeListener() {
                                    @Override
                                    public void stateChanged(ChangeEvent e) {
                                        dd2.setValid(!((ValidationPanel) e.getSource()).isProblem());
                                    }
                                });
                                dd2.setValid(!vp.isProblem());
                            }
                            Object result = DialogDisplayer.getDefault().notify(dd2);
                            if (result.equals(NotifyDescriptor.CANCEL_OPTION) || result.equals(NotifyDescriptor.CLOSED_OPTION)) {
                                validResult.setResult(false);
                            } else {
                                pui.unsetup(); //true
                                validResult.setResult(true);
                            }
                        }
                    });
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (validResult.isResult()) {
            controller.process(containers, processor, workspace);

            //StatusLine notify
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.status.multiImportSuccess", containers.length));
        }
    }

    private static class ValidResult {

        private boolean result = true;

        public void setResult(boolean result) {
            this.result = result;
        }

        public boolean isResult() {
            return result;
        }
    }

    private FileObject getArchivedFile(FileObject fileObject) {
        // ZIP and JAR archives
        if (FileUtil.isArchiveFile(fileObject)) {
            try {
                fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
            } catch (Exception e) {
                throw new RuntimeException("The archive can't be opened, be sure it has no password and contains a single file, without folders");
            }
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
                            tempFile = getGzFile(fileObject, tempFile, true);
                        } else {
                            tempFile = getBzipFile(fileObject, tempFile, true);
                        }
                    } else {
                        String fname = fileObject.getName();
                        fname = fname.replace(fileExt1, "");
                        tempFile = File.createTempFile(fname, "." + fileExt1);
                        // Unzip
                        if (isGz) {
                            tempFile = getGzFile(fileObject, tempFile, false);
                        } else {
                            tempFile = getBzipFile(fileObject, tempFile, false);
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

    @Override
    public ImportController getImportController() {
        return controller;
    }

    private ProcessorUI getProcessorUI(Processor processor) {
        for (ProcessorUI pui : Lookup.getDefault().lookupAll(ProcessorUI.class)) {
            if (pui.isUIFoProcessor(processor)) {
                return pui;
            }
        }
        return null;
    }

    /**
     * Uncompress a Bzip2 file.
     */
    private static File getBzipFile(FileObject in, File out, boolean isTar) throws IOException {

        // Stream buffer
        final int BUFF_SIZE = 8192;
        final byte[] buffer = new byte[BUFF_SIZE];

        BZip2CompressorInputStream inputStream = null;
        FileOutputStream outStream = null;

        try {
            FileInputStream is = new FileInputStream(in.getPath());
            inputStream = new BZip2CompressorInputStream(is);
            outStream = new FileOutputStream(out.getAbsolutePath());

            if (isTar) {
                // Read Tar header
                int remainingBytes = readTarHeader(inputStream);

                // Read content
                ByteBuffer bb = ByteBuffer.allocateDirect(4 * BUFF_SIZE);
                byte[] tmpCache = new byte[BUFF_SIZE];
                int nRead, nGet;
                while ((nRead = inputStream.read(tmpCache)) != -1) {
                    if (nRead == 0) {
                        continue;
                    }
                    bb.put(tmpCache);
                    bb.position(0);
                    bb.limit(nRead);
                    while (bb.hasRemaining() && remainingBytes > 0) {
                        nGet = Math.min(bb.remaining(), BUFF_SIZE);
                        nGet = Math.min(nGet, remainingBytes);
                        bb.get(buffer, 0, nGet);
                        outStream.write(buffer, 0, nGet);
                        remainingBytes -= nGet;
                    }
                    bb.clear();
                }
            } else {
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, len);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }

        return out;
    }

    /**
     * Uncompress a GZIP file.
     */
    private static File getGzFile(FileObject in, File out, boolean isTar) throws IOException {

        // Stream buffer
        final int BUFF_SIZE = 8192;
        final byte[] buffer = new byte[BUFF_SIZE];

        GZIPInputStream inputStream = null;
        FileOutputStream outStream = null;

        try {
            inputStream = new GZIPInputStream(new FileInputStream(in.getPath()));
            outStream = new FileOutputStream(out);

            if (isTar) {
                // Read Tar header
                int remainingBytes = readTarHeader(inputStream);

                // Read content
                ByteBuffer bb = ByteBuffer.allocateDirect(4 * BUFF_SIZE);
                byte[] tmpCache = new byte[BUFF_SIZE];
                int nRead, nGet;
                while ((nRead = inputStream.read(tmpCache)) != -1) {
                    if (nRead == 0) {
                        continue;
                    }
                    bb.put(tmpCache);
                    bb.position(0);
                    bb.limit(nRead);
                    while (bb.hasRemaining() && remainingBytes > 0) {
                        nGet = Math.min(bb.remaining(), BUFF_SIZE);
                        nGet = Math.min(nGet, remainingBytes);
                        bb.get(buffer, 0, nGet);
                        outStream.write(buffer, 0, nGet);
                        remainingBytes -= nGet;
                    }
                    bb.clear();
                }
            } else {
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, len);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }

        return out;
    }

    private static int readTarHeader(InputStream inputStream) throws IOException {
        // Tar bytes
        final int FILE_SIZE_OFFSET = 124;
        final int FILE_SIZE_LENGTH = 12;
        final int HEADER_LENGTH = 512;

        ignoreBytes(inputStream, FILE_SIZE_OFFSET);
        String fileSizeLengthOctalString = readString(inputStream, FILE_SIZE_LENGTH).trim();
        final int fileSize = Integer.parseInt(fileSizeLengthOctalString, 8);

        ignoreBytes(inputStream, HEADER_LENGTH - (FILE_SIZE_OFFSET + FILE_SIZE_LENGTH));

        return fileSize;
    }

    private static void ignoreBytes(InputStream inputStream, int numberOfBytes) throws IOException {
        for (int counter = 0; counter < numberOfBytes; counter++) {
            inputStream.read();
        }
    }

    private static String readString(InputStream inputStream, int numberOfBytes) throws IOException {
        return new String(readBytes(inputStream, numberOfBytes));
    }

    private static byte[] readBytes(InputStream inputStream, int numberOfBytes) throws IOException {
        byte[] readBytes = new byte[numberOfBytes];
        inputStream.read(readBytes);

        return readBytes;
    }
}
