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

import java.awt.Dialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.ProcessorUI;
import org.gephi.lib.validation.DialogDescriptorWithValidation;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.CharsetToolkit;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 * @author Sebastien Heymann
 */
@ServiceProvider(service = ImportControllerUI.class)
public class DesktopImportControllerUI implements ImportControllerUI {

    private final LongTaskExecutor executor;
    private final LongTaskErrorHandler defaultErrorHandler;
    private final ImportController controller;

    public DesktopImportControllerUI() {
        controller = Lookup.getDefault().lookup(ImportController.class);
        defaultErrorHandler = new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                if (t instanceof OutOfMemoryError) {
                    return;
                }
                Exceptions.printStackTrace(t);
            }
        };
        executor = new LongTaskExecutor(true, "Importer", 10);
    }

    @Override
    public void importFile(FileObject fileObject) {
        importFiles(new FileObject[] {fileObject});
    }

    @Override
    public void importFiles(FileObject[] fileObjects) {
        MostRecentFiles mostRecentFiles = Lookup.getDefault().lookup(MostRecentFiles.class);

        fileObjects = Arrays.copyOf(fileObjects, fileObjects.length);

        //Extract files if they are zipped:
        for (int i = 0; i < fileObjects.length; i++) {
            fileObjects[i] = ImportUtils.getArchivedFile(fileObjects[i]);
            if (FileUtil.isArchiveArtifact(fileObjects[i])) {
                try {
                    //Copy the archived file so we never have problems converting it to a simple File during import:
                    File tempDir = TempDirUtils.createTempDirectory();
                    fileObjects[i] =
                        FileUtil.copyFile(fileObjects[i], FileUtil.toFileObject(tempDir), fileObjects[i].getName());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        Reader[] readers = new Reader[fileObjects.length];
        FileImporter[] importers = new FileImporter[fileObjects.length];
        try {
            for (int i = 0; i < fileObjects.length; i++) {
                FileObject fileObject = fileObjects[i];
                importers[i] = controller.getFileImporter(fileObject);

                if (importers[i] == null) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"),
                        NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }

                readers[i] = ImportUtils.getTextReader(fileObject);

                //MRU
                mostRecentFiles.addFile(fileObject.getPath());
            }

            importFiles(readers, importers, fileObjects, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void importStream(final InputStream stream, String importerName) {
        importStream(stream, null, importerName);
    }

    @Override
    public void importStream(final InputStream stream, String streamName, String importerName) {
        final FileImporter importer = controller.getFileImporter(importerName);
        if (importer == null) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"),
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        try {
            Reader reader = ImportUtils.getTextReader(stream);
            importFile(reader, streamName, importer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void importFile(final Reader reader, String importerName) {
        importFile(reader, null, importerName);
    }

    @Override
    public void importFile(final Reader reader, String fileName, String importerName) {
        final FileImporter importer = controller.getFileImporter(importerName);
        if (importer == null) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                NbBundle.getMessage(getClass(), "DesktopImportControllerUI.error_no_matching_file_importer"),
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        importFile(reader, fileName, importer);
    }

    private void importFile(final Reader reader, final String fileName, final FileImporter importer) {
        importFiles(new Reader[] {reader}, new FileImporter[] {importer}, new String[] {fileName});
    }

    private void importFiles(final Reader[] readers, final FileImporter[] importers, final String[] names) {
        importFiles(readers, importers, null, names);
    }

    private void importFiles(final Reader[] readers, final FileImporter[] importers, FileObject[] fileObjects, String[] names) {
        try {
            File[] files = new File[readers.length];

            Map<ImporterUI, List<FileImporter>> importerUIs = new HashMap<>();
            for (int i = 0; i < importers.length; i++) {
                FileImporter importer = importers[i];
                ImporterUI ui = controller.getUI(importer);
                if (ui != null) {
                    List<FileImporter> l = importerUIs.get(ui);
                    if (l == null) {
                        l = new ArrayList<>();
                        importerUIs.put(ui, l);
                    }
                    l.add(importer);
                }

                if (importer instanceof FileImporter.FileAware) {
                    try (Reader reader = readers[i]) {
                        File file = null;
                        if (fileObjects != null) {
                            file = FileUtil.toFile(fileObjects[i]);
                        }

                        if (file == null) {
                            //There is no source file but the importer needs it, create temporary copy:
                            String fileName = "tmp_file" + 1;
                            String charset = "UTF-8";
                            if (fileObjects != null &&
                                fileObjects[i] != null) {//Netbeans FileUtil.toFile bug returning null?? Try to recover:
                                fileName = fileObjects[i].getNameExt();
                                CharsetToolkit charsetToolkit = new CharsetToolkit(fileObjects[i].getInputStream());
                                charset = charsetToolkit.getCharset().name();
                            }

                            file = TempDirUtils.createTempDir().createFile(fileName);
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                FileUtil.copy(new ReaderInputStream(reader, charset), fos);
                            }
                        }

                        files[i] = file;
                        ((FileImporter.FileAware) importer).setFile(file);
                    }
                }
            }

            for (Map.Entry<ImporterUI, List<FileImporter>> entry : importerUIs.entrySet()) {
                ImporterUI ui = entry.getKey();
                String title = NbBundle
                    .getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.file.ui.dialog.title",
                        ui.getDisplayName());
                JPanel panel = ui.getPanel();

                FileImporter[] fi = entry.getValue()
                    .toArray((FileImporter[]) Array.newInstance(entry.getValue().get(0).getClass(), 0));
                ui.setup(fi);

                if (panel != null) {
                    final DialogDescriptor dd = DialogDescriptorWithValidation.dialog(panel, title);
                    Object result = DialogDisplayer.getDefault().notify(dd);
                    if (!result.equals(NotifyDescriptor.OK_OPTION)) {
                        ui.unsetup(false);
                        return;
                    }
                }

                if (ui instanceof ImporterUI.WithWizard) {
                    boolean finishedOk = showWizard(ui, ((ImporterUI.WithWizard) ui).getWizardDescriptor());
                    if (!finishedOk) {
                        ui.unsetup(false);
                        return;
                    }
                }

                ui.unsetup(true);
            }

            ImportErrorHandler errorHandler = new ImportErrorHandler();
            final List<Container> results = new ArrayList<>();
            for (int i = 0; i < importers.length; i++) {
                doImport(results, readers[i], names != null ? names[0] : null, fileObjects != null ? fileObjects[i] : null, files[i], importers[i],
                    errorHandler);
            }

            String taskName = null;
            if (importers.length == 1) {
                taskName = NbBundle
                    .getMessage(DesktopImportControllerUI.class,
                        "DesktopImportControllerUI.finishingImport",
                        fileObjects != null ? fileObjects[0].getNameExt() : (names != null ? names[0] : null));
            } else {
                taskName = NbBundle
                    .getMessage(DesktopImportControllerUI.class,
                        "DesktopImportControllerUI.multiImport.finishingImport",
                        importers.length);
            }
            FinishImport finishImport = new FinishImport(results, errorHandler);
            executor.execute(finishImport, finishImport, taskName, defaultErrorHandler);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void doImport(final List<Container> results, final Reader reader, final String containerName, final FileObject fileObject,
                          final File file,
                          final FileImporter importer,
                          final ImportErrorHandler errorHandler) {
        LongTask task = null;
        if (importer instanceof LongTask) {
            task = (LongTask) importer;
        }

        if (file == null && reader == null) {
            throw new NullPointerException("Null file and reader!");
        }

        if (importer == null) {
            throw new NullPointerException("Null importer!");
        }

        //Execute task
        final String containerSource = fileObject != null ? fileObject.getNameExt() : (containerName != null ? containerName : NbBundle
            .getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.streamSource",
                importer.getClass().getSimpleName()));
        String taskName =
            NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
        executor.execute(task, () -> {
            try {
                Container container;
                if (importer instanceof FileImporter.FileAware && file != null) {
                    container = controller.importFile(file, importer);
                } else {
                    container = controller.importFile(reader, importer);
                }

                if (container != null) {
                    container.setSource(containerSource);
                    results.add(container);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, taskName, errorHandler.createHandler(containerSource));
    }

    private boolean showWizard(ImporterUI importer, WizardDescriptor wizardDescriptor) {
        if (wizardDescriptor == null) {
            return true;//Nothing to show
        }

        wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
        wizardDescriptor.setTitle(importer.getDisplayName());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();

        return wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION;
    }

    @Override
    public void importDatabase(DatabaseImporter importer) {
        importDatabase(null, importer);
    }

    @Override
    public void importDatabase(Database database, final DatabaseImporter importer) {
        try {
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle
                    .getMessage(DesktopImportControllerUI.class,
                        "DesktopImportControllerUI.error_no_matching_db_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                String title = NbBundle
                    .getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.database.ui.dialog.title");
                JPanel panel = ui.getPanel();
                ui.setup(new DatabaseImporter[] {importer});
                final DialogDescriptor dd = DialogDescriptorWithValidation.dialog(panel, title);

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
            final String containerSource = database != null ? database.getName() :
                (ui != null ? ui.getDisplayName() : importer.getClass().getSimpleName());
            final Database db = database;
            String taskName = NbBundle
                .getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importDatabase(db, importer);
                        if (container != null) {
                            container.setSource(containerSource);
                            new FinishImport(Collections.singletonList(container), null).run();
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, defaultErrorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.SEVERE, "", ex);
        }
    }

    @Override
    public void importWizard(final WizardImporter importer) {
        try {
            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle
                    .getMessage(DesktopImportControllerUI.class,
                        "DesktopImportControllerUI.error_no_matching_db_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            String containerSource =
                NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.wizardSource", "");
            ImporterUI ui = controller.getUI(importer);
            if (ui != null) {
                String title = NbBundle
                    .getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.wizard.ui.dialog.title",
                        ui.getDisplayName());
                JPanel panel = ui.getPanel();
                ui.setup(new WizardImporter[] {importer});
                final DialogDescriptor dd = DialogDescriptorWithValidation.dialog(panel, title);
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
            String taskName = NbBundle
                .getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.taskName", containerSource);
            executor.execute(task, new Runnable() {
                @Override
                public void run() {
                    try {
                        Container container = controller.importWizard(importer);
                        if (container != null) {
                            container.setSource(source);
                            new FinishImport(Collections.singletonList(container), null).run();
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, taskName, defaultErrorHandler);
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.WARNING, "", ex);
        }
    }

    private void showProcessorIssues(Report report) {
        ProcessorIssuesReportPanel issuesReport = new ProcessorIssuesReportPanel();
        issuesReport.setData(report);

        DialogDescriptor dd = new DialogDescriptor(issuesReport,
            NbBundle.getMessage(DesktopImportControllerUI.class, "ProcessorIssuesReportPanel.title"));
        dd.setOptions(
            new Object[] {NbBundle.getMessage(DesktopImportControllerUI.class, "ProcessorIssuesReportPanel.close")});

        DialogDisplayer.getDefault().notify(dd);
        issuesReport.destroy();
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

    private static class ImportErrorHandler {
        private final Report errorReport = new Report("temperrorhandler");
        private final AtomicInteger count = new AtomicInteger();

        public LongTaskErrorHandler createHandler(String source) {
            return new LongTaskErrorHandler() {
                @Override
                public void fatalError(Throwable t) {
                    handleError(source, t);
                }
            };
        }

        public void handleError(String source, Throwable t) {
            if (t instanceof OutOfMemoryError) {
                return;
            }
            count.incrementAndGet();
            String msg =
                NbBundle.getMessage(DesktopImportControllerUI.class, "DesktopImportControllerUI.errorHandler.critical",
                    source, t.getMessage());
            errorReport.logIssue(new Issue(msg, Issue.Level.SEVERE, t));
            Exceptions.printStackTrace(t);
        }

        public int countErrors() {
            return count.get();
        }

        public Report closeAndGetReport() {
            errorReport.close();
            return errorReport;
        }
    }

    private static class ValidResult {

        private boolean result = true;

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }
    }

    private class FinishImport implements LongTask, Runnable {

        private final List<Container> containers;
        private final ImportErrorHandler errorHandler;
        private ProgressTicket progressTicket;

        public FinishImport(List<Container> containers, ImportErrorHandler errorHandler) {
            this.containers = containers;
            this.errorHandler = errorHandler;
        }

        @Override
        public void run() {
            // If exceptions were thrown we show them in the processor panel
            if (errorHandler != null) {
                Report errorReport = errorHandler.closeAndGetReport();
                if (errorHandler.countErrors() > 0) {
                    showProcessorIssues(errorReport);
                    return;
                }
            }

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
            if (!containers.isEmpty()) {
                ReportPanel reportPanel = new ReportPanel();
                reportPanel.setData(finalReport, containers.toArray(new Container[0]));
                DialogDescriptor dd = new DialogDescriptor(reportPanel,
                    NbBundle.getMessage(DesktopImportControllerUI.class, "ReportPanel.title"));
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
                processor.setProgressTicket(progressTicket);

                //Project
                Workspace workspace = null;
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                if (pc.getCurrentProject() == null) {
                    Project project = pc.newProject();
                    workspace = project.getCurrentWorkspace();
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
                                    String title = NbBundle.getMessage(DesktopImportControllerUI.class,
                                        "DesktopImportControllerUI.processor.ui.dialog.title");

                                    pui.setup(processor);
                                    final DialogDescriptor dd2 = DialogDescriptorWithValidation.dialog(panel, title);
                                    Object result = DialogDisplayer.getDefault().notify(dd2);
                                    if (result.equals(NotifyDescriptor.CANCEL_OPTION) ||
                                        result.equals(NotifyDescriptor.CLOSED_OPTION)) {
                                        validResult.setResult(false);
                                    } else {
                                        pui.unsetup(); //true
                                        validResult.setResult(true);
                                    }
                                }
                            });
                        }
                    } catch (InterruptedException | InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (validResult.isResult()) {
                    controller.process(containers.toArray(new Container[0]), processor, workspace);

                    Report report = processor.getReport();
                    if (report != null && !report.isEmpty()) {
                        showProcessorIssues(report);
                    }

                    //StatusLine notify
                    StatusDisplayer.getDefault().setStatusText(NbBundle
                        .getMessage(DesktopImportControllerUI.class,
                            "DesktopImportControllerUI.status.multiImportSuccess",
                            containers.size()));
                }
            }
        }

        @Override
        public boolean cancel() {
            return false;
        }

        @Override
        public void setProgressTicket(ProgressTicket progressTicket) {
            this.progressTicket = progressTicket;
        }
    }
}
