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

package org.gephi.desktop.project;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.graph.api.Configuration;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.gephi.lib.validation.DialogDescriptorWithValidation;
import org.gephi.project.api.EmptyProjectFileException;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.api.LegacyGephiFormatException;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectListener;
import org.gephi.project.api.ProjectMetaData;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceMetaData;
import org.gephi.ui.project.NewWorkspace;
import org.gephi.ui.project.ProjectList;
import org.gephi.ui.project.ProjectPropertiesEditor;
import org.gephi.ui.project.WorkspacePropertiesEditor;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectListener.class)
public class ProjectControllerUIImpl implements ProjectListener {

    public static final String PROJECTS_PERSISTENCE_ENABLED = "ProjectsPersistence_Enabled";
    private static final boolean DEFAULT_PROJECTS_PERSISTENCE_ENABLED = true;
    private static final String PROJECTS_FOLDER = "projects";
    private static final String PROJECTS_FILE= "projects.xml";
    //Project
    private final ProjectController controller;
    private final ImportControllerUI importControllerUI;
    //Utilities
    private final LongTaskExecutor longTaskExecutor;
    //Actions
    private volatile ActionsState actionsState = ActionsState.forProject(null);
    //Last project the controller reported as saved. A save cancelled from the progress bar writes
    //nothing and reports no failure, so this is the only way to tell it from a successful one.
    private volatile Project lastSavedProject;

    public ProjectControllerUIImpl() {

        controller = Lookup.getDefault().lookup(ProjectController.class);
        importControllerUI = Lookup.getDefault().lookup(ImportControllerUI.class);

        //Project IO executor
        longTaskExecutor = new LongTaskExecutor(true, "Project IO");
        longTaskExecutor.setDefaultErrorHandler(t -> {
            unlockProjectActions();

            if (t instanceof LegacyGephiFormatException || t instanceof GephiFormatException) {
                NotifyDescriptor.Message msg =
                    new NotifyDescriptor.Message(t.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }

            if (!(t instanceof LegacyGephiFormatException || t instanceof EmptyProjectFileException)) {
                Exceptions.printStackTrace(t);
            }
        });
        //Safety net: a task may return early, or lock the actions without reaching the matching
        //unlock, and the actions would stay disabled. Recomputing the state is idempotent.
        longTaskExecutor.setLongTaskListener(task -> unlockProjectActions());
    }

    @Override
    public void lock() {
        lockProjectActions();
    }

    @Override
    public void unlock() {
        unlockProjectActions();
    }

    @Override
    public void saved(Project project) {
        lastSavedProject = project;
        SwingUtilities.invokeLater(() -> {
            //Status line
            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.status.saved",
                    project.getFileName()));
        });
        unlockProjectActions();
        updateTitleBar(project);

        //Persist projects so the last opened is refreshed
        saveProjects();
    }

    @Override
    public void error(Project project, Throwable t) {
        unlockProjectActions();

//        Exceptions.printStackTrace(throwable);
//        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
//            NbBundle.getMessage(ProjectControllerUIImpl.class, "OpenProject.defaulterror"),
//            NotifyDescriptor.WARNING_MESSAGE);
//        DialogDisplayer.getDefault().notify(msg);

        if (t instanceof LegacyGephiFormatException || t instanceof GephiFormatException) {
            NotifyDescriptor.Message msg =
                new NotifyDescriptor.Message(t.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
        }

        if (!(t instanceof LegacyGephiFormatException || t instanceof GephiFormatException)) {
            Exceptions.printStackTrace(t);
        }
        updateTitleBar(project);
    }

    @Override
    public void opened(Project project) {
        SwingUtilities.invokeLater(() -> {
            //Status line
            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.status.opened",
                    !project.getFileName().isEmpty() ? project.getFileName() : project.getName()));
        });
        unlockProjectActions();
        updateTitleBar(project);

        //Persist projects so the last opened is refreshed
        saveProjects();
    }

    @Override
    public void closed(Project project) {
        unlockProjectActions();
        updateTitleBar(project);
    }

    @Override
    public void changed(Project project) {
        unlockProjectActions();
        updateTitleBar(project);
    }

    private void updateTitleBar(Project project) {
        //Modifying Title bar
        SwingUtilities.invokeLater(() -> {
            JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
            String title;
            if (project == null || project.isClosed()) {
                title = getCurrentVersion();
            } else {
                title = getCurrentVersion() + " - " + project.getName();
            }
            if (!frame.getTitle().equals(title)) {
                frame.setTitle(title);
            }
        });
    }

    /**
     * Runs <code>runnable</code> on the Project IO thread, so the project controller never mutates
     * the model nor invokes its listeners on the Event Dispatch Thread. Returns as soon as the task
     * is submitted.
     * <p>
     * All the callers are on the Event Dispatch Thread. The operations that continue on the Project
     * IO thread, such as confirming and closing the current project, deliberately call their steps
     * directly rather than coming back through here: the executor runs a single task at a time, so
     * submitting from within a task would enqueue it behind the task it is part of.
     *
     * @param runnable the operation to perform on the Project IO thread
     */
    private void runInProjectIO(Runnable runnable) {
        longTaskExecutor.execute(null, runnable);
    }

    private Future<Void> saveProject(Project project, File file) {
        return longTaskExecutor.execute(null, () -> {
            controller.saveProject(project, file);
            return null;
        });
    }

    /**
     * Saves the current project, asking for a destination when it has none yet.
     * <p>
     * The returned future completes on the Project IO thread. Never wait on it from the Event
     * Dispatch Thread: the save notifies the project listeners synchronously, and one of them
     * marshalling to the interface would deadlock. Use it only from a background thread, or ignore
     * it and react to {@link #saved(org.gephi.project.api.Project)} instead.
     *
     * @return a future that completes once the save is done
     */
    public Future<Void> saveProject() {
        Project project = controller.getCurrentProject();
        if (project.hasFile()) {
            return saveProject(project, project.getFile());
        } else {
            return saveAsProject();
        }
    }

    /**
     * Asks for a destination and saves the current project to it.
     * <p>
     * Same caveat as {@link #saveProject()}: the returned future must not be waited on from the
     * Event Dispatch Thread.
     *
     * @return a future that completes once the save is done, already completed when the user gave up
     *     the destination
     */
    public Future<Void> saveAsProject() {
        File file = selectSaveAsFile();
        if (file != null) {
            return saveProject(controller.getCurrentProject(), file);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Asks the user where the current project should be written. Runs on the calling thread, which
     * has to be the Event Dispatch Thread.
     *
     * @return the destination file, or <code>null</code> if the user cancelled
     */
    private File selectSaveAsFile() {
        final String LAST_PATH = "SaveAsProject_Last_Path";
        final String LAST_PATH_DEFAULT = "SaveAsProject_Last_Path_Default";

        DialogFileFilter filter = new DialogFileFilter(
            NbBundle.getMessage(ProjectControllerUIImpl.class, "SaveAsProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH, lastPathDefault);

        File lastPathDir = null;
        if (lastPath != null) {
            lastPathDir = new File(lastPath).getParentFile();
            while (lastPathDir != null && !lastPathDir.exists()) {
                lastPathDir = lastPathDir.getParentFile();
            }
        }

        //File chooser
        final JFileChooser chooser = new JFileChooser(lastPathDir) {
            @Override
            public void approveSelection() {
                if (canExport(this)) {
                    super.approveSelection();
                }
            }
        };
        chooser.addChoosableFileFilter(filter);

        if (lastPathDir != null && lastPathDir.exists() && lastPathDir.isDirectory()) {
            chooser.setSelectedFile(new File(lastPath));
        }

        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);

            // Save last path
            NbPreferences.forModule(ProjectControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());

            return file;
        }
        return null;
    }

    private boolean canExport(JFileChooser chooser) {
        File file = chooser.getSelectedFile();

        if (!file.getPath().endsWith(".gephi")) {
            file = new File(file.getPath() + ".gephi");
            chooser.setSelectedFile(file);
        }

        //Note: when the file doesn't exist yet it must not be created here. Pre-creating the
        //destination would leave an empty .gephi file behind if the save is later interrupted,
        //defeating the temporary-file-then-move strategy used when writing the project. Real
        //write failures are reported by the save task itself.
        if (file.exists()) {
            String overwriteMsg = NbBundle.getMessage(
                ProjectControllerUIImpl.class,
                "SaveAsProject_Overwrite", new Object[] {file.getPath()});
            if (JOptionPane.showConfirmDialog(chooser, overwriteMsg) != JOptionPane.OK_OPTION) {
                return false;
            }
        }

        return true;
    }

    /**
     * Asks the user what to do with the current project before it gets closed, without touching the
     * model. All the dialogs happen here, so the caller can perform the decision on the Project IO
     * thread.
     * <p>
     * Blocks until the user answered. This is the only place where blocking is correct: a background
     * thread waits for the user, never the interface for a background thread.
     *
     * @return the user's decision, never <code>null</code>
     */
    private CloseDecision confirmCloseCurrentProject() {
        if (SwingUtilities.isEventDispatchThread()) {
            return askCloseCurrentProject();
        }
        final CloseDecision[] decision = new CloseDecision[1];
        try {
            SwingUtilities.invokeAndWait(() -> decision[0] = askCloseCurrentProject());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return CloseDecision.CANCEL;
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex.getCause() != null ? ex.getCause() : ex);
            return CloseDecision.CANCEL;
        }
        return decision[0];
    }

    /**
     * Shows the close confirmation. Runs on the Event Dispatch Thread only.
     */
    private CloseDecision askCloseCurrentProject() {
        Project project = controller.getCurrentProject();
        if (project == null) {
            return CloseDecision.close(null);
        }

        //Save ?
        String messageBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_message");
        String titleBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_title");
        String saveBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_save");
        String doNotSaveBundle =
            NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_doNotSave");
        String cancelBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_cancel");
        NotifyDescriptor msg = new NotifyDescriptor(messageBundle, titleBundle,
            NotifyDescriptor.YES_NO_CANCEL_OPTION,
            NotifyDescriptor.INFORMATION_MESSAGE,
            new Object[] {saveBundle, doNotSaveBundle, cancelBundle}, saveBundle);
        Object result = DialogDisplayer.getDefault().notify(msg);
        if (result == saveBundle) {
            //The destination is asked for now, so the close itself needs no dialog
            File file = project.hasFile() ? project.getFile() : selectSaveAsFile();
            if (file == null) {
                //Giving up the destination gives up the close, otherwise the project would be
                //closed without being saved
                return CloseDecision.CANCEL;
            }
            return CloseDecision.saveAndClose(project, file);
        } else if (result == cancelBundle) {
            return CloseDecision.CANCEL;
        }
        return CloseDecision.close(project);
    }

    /**
     * Performs a decision taken by {@link #confirmCloseCurrentProject()}. Shows no dialog: when the
     * project has to be saved, the save runs before the close on the calling thread, which has to be
     * the Project IO thread.
     *
     * @param decision the user's decision
     * @return <code>true</code> when the project has been closed and the operation that requested
     *     the close may continue
     */
    private boolean closeCurrentProject(CloseDecision decision) {
        if (decision.cancelled) {
            return false;
        }
        Project project = controller.getCurrentProject();
        if (project == null) {
            return true;
        }
        if (decision.project != null && decision.project != project) {
            //The current project changed while the user was being asked, so neither the answer nor
            //the destination it carries applies to what is open now
            return false;
        }
        if (decision.saveFile != null) {
            lastSavedProject = null;
            try {
                controller.saveProject(project, decision.saveFile);
            } catch (RuntimeException ex) {
                //The failure has already been reported to the user by the error() callback and
                //closing now would throw away what couldn't be written
                return false;
            }
            if (lastSavedProject != project) {
                //The save was cancelled from the progress bar, which writes nothing and reports no
                //failure. Closing now would throw the work away.
                return false;
            }
        }
        controller.closeCurrentProject();
        return true;
    }

    /**
     * Confirms and closes the current project, if any. Runs on the Project IO thread, the
     * confirmation being marshalled to the Event Dispatch Thread.
     *
     * @return <code>true</code> when the operation that requested the close may continue
     */
    private boolean confirmAndCloseCurrentProject() {
        if (!controller.hasCurrentProject()) {
            return true;
        }
        return closeCurrentProject(confirmCloseCurrentProject());
    }

    /**
     * Confirms closing the current project with the user and then closes it on the Project IO
     * thread. Made for the shutdown sequence, which needs the user's answer before it can decide
     * whether to proceed, while the close itself must not run on the Event Dispatch Thread.
     * <p>
     * Nothing happens when the user cancels.
     *
     * @param whenClosed executed on the Project IO thread once the project has been closed, not
     *                   called if the user cancelled or if the close failed
     */
    public void confirmAndCloseCurrentProject(Runnable whenClosed) {
        CloseDecision decision = confirmCloseCurrentProject();
        if (decision.cancelled) {
            return;
        }
        runInProjectIO(() -> {
            if (closeCurrentProject(decision) && whenClosed != null) {
                whenClosed.run();
            }
        });
    }

    public void openProject(Project project) {
        if (!project.hasFile()) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                NbBundle.getMessage(ProjectControllerUIImpl.class,
                    "ProjectControllerUI.error.noFileAssociated", project.getName()),
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        runInProjectIO(() -> {
            if (!confirmAndCloseCurrentProject()) {
                return;
            }
            controller.openProject(project);
        });
    }

    public void openProject(File file) {
        runInProjectIO(() -> {
            if (!confirmAndCloseCurrentProject()) {
                return;
            }
            controller.openProject(file);
        });
    }

    public void removeProject(Project project) {
        runInProjectIO(() -> {
            if (controller.getCurrentProject() == project && !confirmAndCloseCurrentProject()) {
                return;
            }
            controller.removeProject(project);
        });
    }

    public boolean canCloseProject() {
        return actionsState.closeProject;
    }

    public boolean canDeleteWorkspace() {
        return actionsState.deleteWorkspace;
    }

    public boolean canNewProject() {
        return actionsState.newProject;
    }

    public boolean canNewWorkspace() {
        return actionsState.newWorkspace;
    }

    public boolean canDuplicateWorkspace() {
        return actionsState.duplicateWorkspace;
    }

    public boolean canRenameWorkspace() {
        return actionsState.renameWorkspace;
    }

    public boolean canOpenFile() {
        return actionsState.openFile;
    }

    public boolean canSave() {
        return actionsState.saveProject;
    }

    public boolean canSaveAs() {
        return actionsState.saveAsProject;
    }

    public boolean canProjectProperties() {
        return actionsState.projectProperties;
    }

    private void lockProjectActions() {
        actionsState = ActionsState.LOCKED;
    }

    private void unlockProjectActions() {
        actionsState = ActionsState.forProject(controller.getCurrentProject());
    }

    public void projectProperties() {
        Project project = controller.getCurrentProject();
        ProjectPropertiesEditor panel = new ProjectPropertiesEditor();
        panel.load(project);

        DialogDescriptor dd = DialogDescriptorWithValidation.dialog(ProjectPropertiesEditor.createValidationPanel(panel),
            NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectProperties_dialog_title")) ;
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            String name = panel.getProjectName();
            String title = panel.getProjectTitle();
            String author = panel.getProjectAuthor();
            String keywords = panel.getProjectKeywords();
            String description = panel.getProjectDescription();
            runInProjectIO(() -> {
                if (!name.isEmpty() && !name.equals(project.getName())) {
                    controller.renameProject(project, name);
                }
                ProjectMetaData metaData = project.getLookup().lookup(ProjectMetaData.class);
                if (metaData != null) {
                    metaData.setTitle(title);
                    metaData.setAuthor(author);
                    metaData.setKeywords(keywords);
                    metaData.setDescription(description);
                }
            });
        }
    }

    public void workspaceProperties() {
        Workspace workspace = controller.getCurrentWorkspace();
        WorkspacePropertiesEditor panel = new WorkspacePropertiesEditor();
        panel.setup(workspace);

        DialogDescriptor dd = DialogDescriptorWithValidation
            .dialog(WorkspacePropertiesEditor.createValidationPanel(panel),
            NbBundle.getMessage(ProjectControllerUIImpl.class, "WorkspaceProperties_dialog_title"));
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            String name = panel.getWorkspaceName();
            String title = panel.getWorkspaceTitle();
            String description = panel.getWorkspaceDescription();
            runInProjectIO(() -> {
                WorkspaceMetaData metaData = workspace.getWorkspaceMetadata();
                if (!description.isEmpty() && !description.equals(metaData.getDescription())) {
                    metaData.setDescription(description);
                }
                if (!name.isEmpty() && !name.equals(workspace.getName())) {
                    controller.renameWorkspace(workspace, name);
                }
                if (!title.isEmpty() && !title.equals(metaData.getTitle())) {
                    metaData.setTitle(title);
                }
            });
        }
    }

    public void manageProjects() {
        ProjectList panel = new ProjectList();
        DialogDescriptor dd = new DialogDescriptor(panel,
            NbBundle.getMessage(ProjectControllerUIImpl.class, "ManageProjects_dialog_title"));
        dd.setOptions(new Object[] {NotifyDescriptor.CLOSED_OPTION});
        DialogDisplayer.getDefault().notify(dd);
    }

    public void openFile() {
        openFile(null);
    }

    public void openFile(FileImporterBuilder[] builders) {
        List<FileFilter> filters = new ArrayList<>();

        DialogFileFilter graphFilter =
            new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_graphfilter"));

        List<FileType> fileTypes;
        if (builders != null) {
            fileTypes = new ArrayList<>();

            for (FileImporterBuilder builder : builders) {
                fileTypes.addAll(Arrays.asList(builder.getFileTypes()));
            }
        } else {
            DialogFileFilter gephiFilter = new DialogFileFilter(
                NbBundle.getMessage(ProjectControllerUIImpl.class, "OpenProject_filechooser_filter"));
            gephiFilter.addExtension(".gephi");

            filters.add(gephiFilter);

            graphFilter.addExtension(".gephi");
            fileTypes = Arrays.asList(importControllerUI.getImportController().getFileTypes());
        }

        for (FileType fileType : fileTypes) {
            DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
            dialogFileFilter.addExtensions(fileType.getExtensions());
            filters.add(dialogFileFilter);

            graphFilter.addExtensions(fileType.getExtensions());
        }
        DialogFileFilter zipFileFilter =
            new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_zipfilter"));
        zipFileFilter.addExtensions(new String[] {".zip", ".gz", ".bz2"});

        filters.add(graphFilter);
        filters.add(zipFileFilter);

        openFile(filters.toArray(new FileFilter[0]), null);
    }

    private void openFile(FileFilter[] fileFilters, FileFilter initialFilter) {
        final String LAST_PATH = "OpenFile_Last_Path";
        final String LAST_PATH_DEFAULT = "OpenFile_Last_Path_Default";

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH, lastPathDefault);

        //Init dialog
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setMultiSelectionEnabled(true);

        for (FileFilter fileFilter : fileFilters) {
            chooser.addChoosableFileFilter(fileFilter);
        }

        if (initialFilter != null) {
            chooser.setFileFilter(initialFilter);
        }

        //Open dialog
        int returnFile = chooser.showOpenDialog(null);

        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            List<FileObject> fileObjects = new ArrayList<>();

            File gephiFile = null;
            for (File file : files) {
                file = FileUtil.normalizeFile(file);
                FileObject fileObject = FileUtil.toFileObject(file);

                if (fileObject == null) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle
                        .getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.error.fileNotAccessible",
                            file.getName()), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }

                fileObjects.add(fileObject);

                if (fileObject.getExt().equalsIgnoreCase("gephi")) {
                    if (gephiFile != null) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle
                            .getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.error.multipleGephi"),
                            NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                        return;
                    } else {
                        gephiFile = file;
                    }
                }

                //Save last path
                NbPreferences.forModule(ProjectControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());
            }


            if (gephiFile != null) {
                //Project
                openProject(gephiFile);
            } else {
                //Import
                importControllerUI.importFiles(fileObjects.toArray(new FileObject[0]));
            }
        }
    }

    public Project getCurrentProject() {
        return controller.getCurrentProject();
    }

    public void newProject() {
        runInProjectIO(() -> {
            if (confirmAndCloseCurrentProject()) {
                controller.newProject();
            }
        });
    }

    public void closeProject() {
        runInProjectIO(() -> confirmAndCloseCurrentProject());
    }

    public void newWorkspace() {
        runInProjectIO(() -> controller.newWorkspace(controller.getCurrentProject()));
    }

    public void newWorkspaceWithSettings() {
        NewWorkspace panel = new NewWorkspace();
        panel.setup();

        DialogDescriptor dd = DialogDescriptorWithValidation
            .dialog(NewWorkspace.createValidationPanel(panel),
                NbBundle.getMessage(ProjectControllerUIImpl.class, "NewWorkspace_dialog_title"));
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            Configuration configuration = panel.unsetup();
            String name = panel.getWorkspaceName();
            runInProjectIO(() -> {
                Workspace workspace = controller.newWorkspace(controller.getCurrentProject(), configuration);
                controller.renameWorkspace(workspace, name);
            });
        }
    }

    /**
     * Makes the given workspace the current one. Returns immediately when called from the Event
     * Dispatch Thread, the workspace being opened on the Project IO thread.
     *
     * @param workspace the workspace to open
     */
    public void openWorkspace(Workspace workspace) {
        runInProjectIO(() -> controller.openWorkspace(workspace));
    }

    public void deleteWorkspace() {
        deleteWorkspace(controller.getCurrentWorkspace());
    }

    public void deleteWorkspace(Workspace workspace) {
        String message =
            NbBundle.getMessage(ProjectControllerUIImpl.class, "DeleteWorkspace_confirm_message");
        String title = NbBundle.getMessage(ProjectControllerUIImpl.class, "DeleteWorkspace_confirm_title");
        NotifyDescriptor dd = new NotifyDescriptor(message, title,
            NotifyDescriptor.YES_NO_OPTION,
            NotifyDescriptor.QUESTION_MESSAGE, null, null);
        Object retType = DialogDisplayer.getDefault().notify(dd);
        if (retType == NotifyDescriptor.YES_OPTION) {
            runInProjectIO(() -> controller.deleteWorkspace(workspace));
        }
    }

    public void deleteWorkspaces(List<Workspace> workspaces) {
        String message =
            NbBundle.getMessage(ProjectControllerUIImpl.class, "DeleteWorkspaces_confirm_message", workspaces.size());
        String title = NbBundle.getMessage(ProjectControllerUIImpl.class, "DeleteWorkspaces_confirm_title");
        NotifyDescriptor dd = new NotifyDescriptor(message, title,
            NotifyDescriptor.YES_NO_OPTION,
            NotifyDescriptor.QUESTION_MESSAGE, null, null);
        Object retType = DialogDisplayer.getDefault().notify(dd);
        if (retType == NotifyDescriptor.YES_OPTION) {
            List<Workspace> toDelete = new ArrayList<>(workspaces);
            runInProjectIO(() -> {
                for (Workspace workspace : toDelete) {
                    controller.deleteWorkspace(workspace);
                }
            });
        }
    }

    public void renameWorkspace(String name) {
        Workspace workspace = controller.getCurrentWorkspace();
        runInProjectIO(() -> controller.renameWorkspace(workspace, name));
    }

    public void duplicateWorkspace() {
        runInProjectIO(() -> controller.duplicateWorkspace(controller.getCurrentWorkspace()));
    }

    private String getCurrentVersion() {
        return NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion")
            .replaceAll("( [0-9]{12})$", "");
    }

    private File getProjectsFile() {
        File folder = new File(Places.getUserDirectory(), PROJECTS_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, PROJECTS_FILE);
    }

    public void loadProjects() {
        if(NbPreferences.forModule(Installer.class).getBoolean(PROJECTS_PERSISTENCE_ENABLED, DEFAULT_PROJECTS_PERSISTENCE_ENABLED)) {
            File file = getProjectsFile();
            if (file.exists()) {
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                try {
                    pc.getProjects().loadProjects(file);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    public void saveProjects() {
        if(NbPreferences.forModule(Installer.class).getBoolean(PROJECTS_PERSISTENCE_ENABLED, DEFAULT_PROJECTS_PERSISTENCE_ENABLED)) {
            File file = getProjectsFile();
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            try {
                pc.getProjects().saveProjects(file);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    /**
     * Immutable snapshot of the actions currently enabled. The Project IO thread replaces the whole
     * snapshot at once while the Event Dispatch Thread reads it from <code>Action.isEnabled()</code>,
     * so the interface can't observe a half-applied enablement.
     */
    private static final class ActionsState {

        private static final ActionsState LOCKED = new ActionsState(false, false, false);

        private final boolean newProject;
        private final boolean openFile;
        private final boolean saveProject;
        private final boolean saveAsProject;
        private final boolean projectProperties;
        private final boolean closeProject;
        private final boolean newWorkspace;
        private final boolean deleteWorkspace;
        private final boolean duplicateWorkspace;
        private final boolean renameWorkspace;

        private ActionsState(boolean fileActions, boolean projectActions, boolean workspaceActions) {
            this.newProject = fileActions;
            this.openFile = fileActions;
            this.saveProject = projectActions;
            this.saveAsProject = projectActions;
            this.projectProperties = projectActions;
            this.closeProject = projectActions;
            this.newWorkspace = projectActions;
            this.deleteWorkspace = workspaceActions;
            this.duplicateWorkspace = workspaceActions;
            this.renameWorkspace = workspaceActions;
        }

        private static ActionsState forProject(Project project) {
            return new ActionsState(true, project != null, project != null && project.hasCurrentWorkspace());
        }
    }

    /**
     * The user's answer to the close confirmation. <code>saveFile</code> is the destination the
     * project has to be written to before being closed, <code>null</code> when it has to be closed
     * without being saved.
     */
    private static final class CloseDecision {

        private static final CloseDecision CANCEL = new CloseDecision(null, true, null);

        /**
         * Project the user answered about, <code>null</code> when there was nothing to answer for.
         * The answer is acted upon later, on the Project IO thread, by which time another project
         * may have become the current one.
         */
        private final Project project;
        private final boolean cancelled;
        private final File saveFile;

        private CloseDecision(Project project, boolean cancelled, File saveFile) {
            this.project = project;
            this.cancelled = cancelled;
            this.saveFile = saveFile;
        }

        private static CloseDecision close(Project project) {
            return new CloseDecision(project, false, null);
        }

        private static CloseDecision saveAndClose(Project project, File file) {
            return new CloseDecision(project, false, file);
        }
    }
}
