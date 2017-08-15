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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectInformation;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.project.spi.ProjectPropertiesUI;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ProjectControllerUI.class)
public class ProjectControllerUIImpl implements ProjectControllerUI {

    //Actions
    private boolean openProject = true;
    private boolean newProject = true;
    private boolean openFile = true;
    private boolean saveProject = false;
    private boolean saveAsProject = false;
    private boolean projectProperties = false;
    private boolean closeProject = false;
    private boolean newWorkspace = false;
    private boolean deleteWorkspace = false;
    private boolean duplicateWorkspace = false;
    private boolean renameWorkspace = false;
    //Project
    private final ProjectController controller;
    private final ImportControllerUI importControllerUI;
    //Utilities
    private final LongTaskExecutor longTaskExecutor;

    public ProjectControllerUIImpl() {

        controller = Lookup.getDefault().lookup(ProjectController.class);
        importControllerUI = Lookup.getDefault().lookup(ImportControllerUI.class);

        //Project IO executor
        longTaskExecutor = new LongTaskExecutor(true, "Project IO");
        longTaskExecutor.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                unlockProjectActions();
//                String txt = NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.error.open");
//                String message = txt + "\n\n" + t.getMessage();
//                if (t.getCause() != null) {
//                    message = txt + "\n\n" + t.getCause().getClass().getSimpleName() + " - " + t.getCause().getMessage();
//                }
//                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
//                DialogDisplayer.getDefault().notify(msg);

                Exceptions.printStackTrace(t);
            }
        });
        longTaskExecutor.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskFinished(LongTask task) {
                unlockProjectActions();
            }
        });
    }

    private void saveProject(Project project, File file) {
        lockProjectActions();

        final Runnable saveTask = controller.saveProject(project, file);
        final String fileName = file.getName();
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                saveTask.run();
                //Status line
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.status.saved", fileName));
            }
        };
        if (saveTask instanceof LongTask) {
            longTaskExecutor.execute((LongTask) saveTask, saveRunnable);
        } else {
            longTaskExecutor.execute(null, saveRunnable);
        }

        //Save MRU
        MostRecentFiles mostRecentFiles = Lookup.getDefault().lookup(MostRecentFiles.class);
        mostRecentFiles.addFile(file.getAbsolutePath());
    }

    @Override
    public void saveProject() {
        Project project = controller.getCurrentProject();
        if (project.getLookup().lookup(ProjectInformation.class).hasFile()) {
            File file = project.getLookup().lookup(ProjectInformation.class).getFile();
            saveProject(project, file);
        } else {
            saveAsProject();
        }
    }

    @Override
    public void saveAsProject() {
        final String LAST_PATH = "SaveAsProject_Last_Path";
        final String LAST_PATH_DEFAULT = "SaveAsProject_Last_Path_Default";

        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(ProjectControllerUIImpl.class, "SaveAsProject_filechooser_filter"));
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

            //Save last path
            NbPreferences.forModule(ProjectControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());

            //File management
            try {
                final String SaveAsFileName = file.getName();
                //File exist now, Save project
                Project project = controller.getCurrentProject();
                saveProject(project, file);

                //Modifying Title bar
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                        String title = frame.getTitle();
                        title = title.substring(0, title.indexOf(" - ")) + " - " + SaveAsFileName;
                        frame.setTitle(title);
                    }
                });

            } catch (Exception e) {
                Logger.getLogger("").log(Level.WARNING, "", e);
            }
        }
    }

    private boolean canExport(JFileChooser chooser) {
        File file = chooser.getSelectedFile();

        if (!file.getPath().endsWith(".gephi")) {
            file = new File(file.getPath() + ".gephi");
            chooser.setSelectedFile(file);
        }

        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    String failMsg = NbBundle.getMessage(
                            ProjectControllerUIImpl.class,
                            "SaveAsProject_SaveFailed", new Object[]{file.getPath()});
                    JOptionPane.showMessageDialog(null, failMsg);
                    return false;
                }
            } else {
                String overwriteMsg = NbBundle.getMessage(
                        ProjectControllerUIImpl.class,
                        "SaveAsProject_Overwrite", new Object[]{file.getPath()});
                if (JOptionPane.showConfirmDialog(null, overwriteMsg) != JOptionPane.OK_OPTION) {
                    return false;
                }
            }
        } catch (IOException ex) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
            return false;
        }

        return true;
    }

    public boolean closeCurrentProject() {
        if (controller.getCurrentProject() != null) {

            //Save ?
            String messageBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_message");
            String titleBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_title");
            String saveBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_save");
            String doNotSaveBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_doNotSave");
            String cancelBundle = NbBundle.getMessage(ProjectControllerUIImpl.class, "CloseProject_confirm_cancel");
            NotifyDescriptor msg = new NotifyDescriptor(messageBundle, titleBundle,
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE,
                    new Object[]{saveBundle, doNotSaveBundle, cancelBundle}, saveBundle);
            Object result = DialogDisplayer.getDefault().notify(msg);
            if (result == saveBundle) {
                saveProject();
            } else if (result == cancelBundle) {
                return false;
            }

            controller.closeCurrentProject();

            //Actions
            saveProject = false;
            saveAsProject = false;
            projectProperties = false;
            closeProject = false;
            newWorkspace = false;
            deleteWorkspace = false;
            duplicateWorkspace = false;
            renameWorkspace = false;

            //Title bar
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle();
                    title = title.substring(0, title.indexOf(" - "));
                    frame.setTitle(title);
                }
            });
        }
        return true;
    }

    @Override
    public void openProject(File file) {
        if (controller.getCurrentProject() != null) {
            if (!closeCurrentProject()) {
                return;
            }
        }
        loadProject(file);
    }

    private void loadProject(File file) {
        lockProjectActions();

        final Runnable loadTask = controller.openProject(file);
        final String fileName = file.getName();
        Runnable loadRunnable = new Runnable() {
            @Override
            public void run() {
                loadTask.run();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                        String title = frame.getTitle() + " - " + fileName;
                        frame.setTitle(title);
                    }
                });
                //Status line
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.status.opened", fileName));
            }
        };
        if (loadTask instanceof LongTask) {
            longTaskExecutor.execute((LongTask) loadTask, loadRunnable);
        } else {
            longTaskExecutor.execute(null, loadRunnable);
        }

        //Save MRU
        MostRecentFiles mostRecentFiles = Lookup.getDefault().lookup(MostRecentFiles.class);
        mostRecentFiles.addFile(file.getAbsolutePath());
    }

    @Override
    public void renameProject(final String name) {
        controller.renameProject(controller.getCurrentProject(), name);

        //Title bar
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                String title = frame.getTitle();
                title = title.substring(0, title.indexOf(" - "));
                title += " - " + name;
                frame.setTitle(title);
            }
        });
    }

    @Override
    public boolean canCloseProject() {
        return closeProject;
    }

    @Override
    public boolean canDeleteWorkspace() {
        return deleteWorkspace;
    }

    @Override
    public boolean canNewProject() {
        return newProject;
    }

    @Override
    public boolean canNewWorkspace() {
        return newWorkspace;
    }

    @Override
    public boolean canDuplicateWorkspace() {
        return duplicateWorkspace;
    }

    @Override
    public boolean canRenameWorkspace() {
        return renameWorkspace;
    }

    @Override
    public boolean canOpenFile() {
        return openFile;
    }

    @Override
    public boolean canSave() {
        return saveProject;
    }

    @Override
    public boolean canSaveAs() {
        return saveAsProject;
    }

    @Override
    public boolean canProjectProperties() {
        return projectProperties;
    }

    private void lockProjectActions() {
        saveProject = false;
        saveAsProject = false;
        openProject = false;
        closeProject = false;
        newProject = false;
        openFile = false;
        newWorkspace = false;
        deleteWorkspace = false;
        duplicateWorkspace = false;
        renameWorkspace = false;
    }

    private void unlockProjectActions() {
        if (controller.getCurrentProject() != null) {
            saveProject = true;
            saveAsProject = true;
            closeProject = true;
            newWorkspace = true;
            projectProperties = true;
            if (controller.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).hasCurrentWorkspace()) {
                deleteWorkspace = true;
                duplicateWorkspace = true;
                renameWorkspace = true;
            }
        }
        openProject = true;
        newProject = true;
        openFile = true;
    }

    @Override
    public void projectProperties() {
        Project project = controller.getCurrentProject();
        ProjectPropertiesUI ui = Lookup.getDefault().lookup(ProjectPropertiesUI.class);
        if (ui != null) {
            JPanel panel = ui.getPanel();
            ui.setup(project);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectProperties_dialog_title"));
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result == NotifyDescriptor.OK_OPTION) {
                ui.unsetup(project);
            }
        }
    }

    @Override
    public void openFile() {
        openFile(null);
    }

    @Override
    public void openFile(FileImporterBuilder[] builders) {
        List<FileFilter> filters = new ArrayList<>();

        DialogFileFilter graphFilter = new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_graphfilter"));

        List<FileType> fileTypes;
        if (builders != null) {
            fileTypes = new ArrayList<>();

            for (FileImporterBuilder builder : builders) {
                fileTypes.addAll(Arrays.asList(builder.getFileTypes()));
            }
        } else {
            DialogFileFilter gephiFilter = new DialogFileFilter(NbBundle.getMessage(ProjectControllerUIImpl.class, "OpenProject_filechooser_filter"));
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
        DialogFileFilter zipFileFilter = new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_zipfilter"));
        zipFileFilter.addExtensions(new String[]{".zip", ".gz", ".bz2"});

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
            FileObject[] fileObjects = new FileObject[files.length];

            int i = 0;
            File gephiFile = null;
            for (File file : files) {
                file = FileUtil.normalizeFile(file);
                FileObject fileObject = FileUtil.toFileObject(file);
                fileObjects[i++] = fileObject;

                if (fileObject.getExt().equalsIgnoreCase("gephi")) {
                    if (gephiFile != null) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.error.multipleGephi"), NotifyDescriptor.ERROR_MESSAGE);
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
                if (controller.getCurrentProject() != null) {
                    if (!closeCurrentProject()) {
                        return;
                    }
                }

                try {
                    loadProject(gephiFile);
                } catch (Exception ew) {
                    Exceptions.printStackTrace(ew);
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(ProjectControllerUIImpl.class, "OpenProject.defaulterror"), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                }
            } else {
                //Import
                importControllerUI.importFiles(fileObjects);
            }
        }
    }

    @Override
    public Project getCurrentProject() {
        return controller.getCurrentProject();
    }

    @Override
    public Project newProject() {
        if (closeCurrentProject()) {
            controller.newProject();
            final Project project = controller.getCurrentProject();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle() + " - " + project.getLookup().lookup(ProjectInformation.class).getName();
                    frame.setTitle(title);
                }
            });

            unlockProjectActions();
            return project;
        }
        return null;
    }

    @Override
    public void closeProject() {
        if (closeCurrentProject()) {
            controller.closeCurrentProject();
        }
    }

    @Override
    public Workspace newWorkspace() {
        return controller.newWorkspace(controller.getCurrentProject());
    }

    @Override
    public void deleteWorkspace() {
        if (controller.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces().length == 1) {
            //Close project
            //Actions
            saveProject = false;
            saveAsProject = false;
            projectProperties = false;
            closeProject = false;
            newWorkspace = false;
            deleteWorkspace = false;
            duplicateWorkspace = false;
            renameWorkspace = false;

            //Title bar
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle();
                    title = title.substring(0, title.indexOf(" - "));
                    frame.setTitle(title);
                }
            });
        }
        controller.deleteWorkspace(controller.getCurrentWorkspace());
    }

    @Override
    public void renameWorkspace(String name) {
        controller.renameWorkspace(controller.getCurrentWorkspace(), name);
    }

    @Override
    public Workspace duplicateWorkspace() {
        return controller.duplicateWorkspace(controller.getCurrentWorkspace());
    }
}
