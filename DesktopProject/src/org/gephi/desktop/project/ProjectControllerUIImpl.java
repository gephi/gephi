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
package org.gephi.desktop.project;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.io.importer.api.FileType;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectInformation;
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
    private boolean cleanWorkspace = false;
    private boolean duplicateWorkspace = false;
    //Project
    private ProjectController controller;
    //Utilities
    private final LongTaskExecutor longTaskExecutor;

    public ProjectControllerUIImpl() {

        controller = Lookup.getDefault().lookup(ProjectController.class);

        //Project IO executor
        longTaskExecutor = new LongTaskExecutor(true, "Project IO");
        longTaskExecutor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                unlockProjectActions();
                String txt = NbBundle.getMessage(ProjectControllerUIImpl.class, "ProjectControllerUI.error.open");
                String message = txt + "\n\n" + t.getMessage();
                if (t.getCause() != null) {
                    message = txt + "\n\n" + t.getCause().getClass().getSimpleName() + " - " + t.getCause().getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        });
        longTaskExecutor.setLongTaskListener(new LongTaskListener() {

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

    public void saveProject() {
        Project project = controller.getCurrentProject();
        if (project.getLookup().lookup(ProjectInformation.class).hasFile()) {
            File file = project.getLookup().lookup(ProjectInformation.class).getFile();
            saveProject(project, file);
        } else {
            saveAsProject();
        }
    }

    public void saveAsProject() {
        final String LAST_PATH = "SaveAsProject_Last_Path";
        final String LAST_PATH_DEFAULT = "SaveAsProject_Last_Path_Default";

        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(ProjectControllerUIImpl.class, "SaveAsProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH, lastPathDefault);

        //File chooser
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.addChoosableFileFilter(filter);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            //Save last path
            NbPreferences.forModule(ProjectControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());

            //File management
            try {
                if (!file.getPath().endsWith(".gephi")) {
                    file = new File(file.getPath() + ".gephi");
                }
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        String failMsg = NbBundle.getMessage(
                                ProjectControllerUIImpl.class,
                                "SaveAsProject_SaveFailed", new Object[]{file.getPath()});
                        JOptionPane.showMessageDialog(null, failMsg);
                        return;
                    }
                } else {
                    String overwriteMsg = NbBundle.getMessage(
                            ProjectControllerUIImpl.class,
                            "SaveAsProject_Overwrite", new Object[]{file.getPath()});
                    if (JOptionPane.showConfirmDialog(null, overwriteMsg) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }
                file = FileUtil.normalizeFile(file);

                //File exist now, Save project
                Project project = controller.getCurrentProject();
                saveProject(project, file);

            } catch (Exception e) {
                Logger.getLogger("").log(Level.WARNING, "", e);
            }
        }
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
            cleanWorkspace = false;
            duplicateWorkspace = false;

            //Title bar
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle();
                    title = title.substring(0, title.indexOf('-') - 1);
                    frame.setTitle(title);
                }
            });
        }
        return true;
    }

    public void openProject(File file) {
        if (controller.getCurrentProject() != null) {
            if (!closeCurrentProject()) {
                return;
            }
        }
        loadProject(file);
    }

    public void openProject() {
        final String LAST_PATH = "OpenProject_Last_Path";
        final String LAST_PATH_DEFAULT = "OpenProject_Last_Path_Default";

        if (controller.getCurrentProject() != null) {
            if (!closeCurrentProject()) {
                return;
            }
        }

        //Open Dialog
        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(ProjectControllerUIImpl.class, "OpenProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH, lastPathDefault);

        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.addChoosableFileFilter(filter);

        int returnFile = chooser.showOpenDialog(null);

        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);

            //Save last path
            NbPreferences.forModule(ProjectControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());

            try {
                loadProject(file);
            } catch (Exception ew) {
                ew.printStackTrace();
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(ProjectControllerUIImpl.class, "OpenProject.defaulterror"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        }
    }

    private void loadProject(File file) {
        lockProjectActions();

        final Runnable loadTask = controller.openProject(file);
        final String fileName = file.getName();
        Runnable loadRunnable = new Runnable() {

            public void run() {
                loadTask.run();
                SwingUtilities.invokeLater(new Runnable() {

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

    public void renameProject(final String name) {
        controller.renameProject(controller.getCurrentProject(), name);

        //Title bar
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                String title = frame.getTitle();
                title = title.substring(0, title.indexOf('-') - 1);
                title += " - " + name;
                frame.setTitle(title);
            }
        });
    }

    public boolean canCleanWorkspace() {
        return cleanWorkspace;
    }

    public boolean canCloseProject() {
        return closeProject;
    }

    public boolean canDeleteWorkspace() {
        return deleteWorkspace;
    }

    public boolean canNewProject() {
        return newProject;
    }

    public boolean canNewWorkspace() {
        return newWorkspace;
    }

    public boolean canOpenFile() {
        return openFile;
    }

    public boolean canOpenProject() {
        return openProject;
    }

    public boolean canSave() {
        return saveProject;
    }

    public boolean canSaveAs() {
        return saveAsProject;
    }

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
        cleanWorkspace = false;
        duplicateWorkspace = false;
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
                cleanWorkspace = true;
                duplicateWorkspace = true;
            }
        }
        openProject = true;
        newProject = true;
        openFile = true;
    }

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

    public void openFile() {
        final String LAST_PATH = "OpenFile_Last_Path";
        final String LAST_PATH_DEFAULT = "OpenFile_Last_Path_Default";

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(ProjectControllerUIImpl.class).get(LAST_PATH, lastPathDefault);

        //Init dialog
        final JFileChooser chooser = new JFileChooser(lastPath);
        DialogFileFilter graphFilter = new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_graphfilter"));

        ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
        for (FileType fileType : importController.getImportController().getFileTypes()) {
            DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
            dialogFileFilter.addExtensions(fileType.getExtensions());
            chooser.addChoosableFileFilter(dialogFileFilter);

            graphFilter.addExtensions(fileType.getExtensions());
        }
        DialogFileFilter zipFileFilter = new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_zipfilter"));
        zipFileFilter.addExtensions(new String[]{".zip"});
        chooser.addChoosableFileFilter(zipFileFilter);
        chooser.addChoosableFileFilter(graphFilter);

        //Open dialog
        int returnFile = chooser.showOpenDialog(null);

        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            //Save last path
            NbPreferences.forModule(ProjectControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());

            importController.importFile(fileObject);
        }
    }

    public void newProject() {
        if (closeCurrentProject()) {
            controller.newProject();
            final Project project = controller.getCurrentProject();

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                    String title = frame.getTitle() + " - " + project.getLookup().lookup(ProjectInformation.class).getName();
                    frame.setTitle(title);
                }
            });

            unlockProjectActions();
        }
    }

    public void closeProject() {
        if (closeCurrentProject()) {
            controller.closeCurrentProject();
        }
    }

    public void newWorkspace() {
        controller.newWorkspace(controller.getCurrentProject());
    }

    public void cleanWorkspace() {
        controller.cleanWorkspace(controller.getCurrentWorkspace());
    }

    public void deleteWorkspace() {
        controller.deleteWorkspace(controller.getCurrentWorkspace());
    }
}
