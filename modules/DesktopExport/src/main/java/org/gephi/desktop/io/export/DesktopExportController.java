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

package org.gephi.desktop.io.export;

import java.io.IOException;
import java.util.Collection;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExportControllerUI.class)
public class DesktopExportController implements ExportControllerUI {

    private final LongTaskExecutor executor;
    private final LongTaskErrorHandler errorHandler;
    private final ExportController controller;

    public DesktopExportController() {
        controller = Lookup.getDefault().lookup(ExportController.class);
        errorHandler = new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                Exceptions.printStackTrace(t);
            }
        };
        executor = new LongTaskExecutor(true, "Exporter", 10);
    }

    @Override
    public void exportFiles(FileObject folder, Exporter exporter, final String extension) {
        checkExporter(exporter);

        MultipleWorkspacesExporter task = new MultipleWorkspacesExporter(exporter, folder, extension);
        executor.execute(task, task);
    }

    @Override
    public void exportFile(final FileObject fileObject, final Exporter exporter) {
        checkExporter(exporter);

        //Export Task
        LongTask task = null;
        if (exporter instanceof LongTask) {
            task = (LongTask) exporter;
        }

        String taskmsg = NbBundle.getMessage(DesktopExportController.class, "DesktopExportController.exportTaskName",
            fileObject.getNameExt());
        executor.execute(task, new Runnable() {

            @Override
            public void run() {
                try {
                    controller.exportFile(FileUtil.toFile(fileObject), exporter);
                    StatusDisplayer.getDefault().setStatusText(NbBundle
                        .getMessage(DesktopExportController.class, "DesktopExportController.status.exportSuccess",
                            fileObject.getNameExt()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, taskmsg, errorHandler);
    }

    @Override
    public ExportController getExportController() {
        return controller;
    }

    private void checkExporter(Exporter exporter) {
        if (exporter == null) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_no_matching_file_exporter"));
        }
    }

    private class MultipleWorkspacesExporter implements Runnable, LongTask {

        private final Exporter exporter;
        private final FileObject folder;
        private final String extension;
        private boolean cancel = false;
        private ProgressTicket progressTicket;

        public MultipleWorkspacesExporter(Exporter exporter, FileObject folder, String extension) {
            this.exporter = exporter;
            this.folder = folder;
            this.extension = extension.replace(".", "");
        }

        @Override
        public void run() {
            Project project = Lookup.getDefault().lookup(ProjectController.class).getCurrentProject();
            if (project != null) {
                Collection<Workspace> workspaceCollection = project.getWorkspaces();
                Progress.start(progressTicket, workspaceCollection.size());

                for(Workspace workspace :workspaceCollection) {
                    if (cancel) {
                        break;
                    }
                    try {
                        FileObject file;
                        String workspaceName = workspace.getName().replaceAll("[\\\\/:*?\"<>|]", "_");
                        if (folder.getFileObject(workspaceName, extension) == null) {
                            file = folder.createData(workspaceName, extension);
                        } else {
                            // Overwrite
                            file = folder.getFileObject(workspaceName, extension);
                        }
                        String taskmsg = NbBundle.getMessage(DesktopExportController.class, "DesktopExportController.exportTaskName",
                            file.getNameExt());
                        Progress.setDisplayName(progressTicket, taskmsg);

                        exporter.setWorkspace(workspace);
                        controller.exportFile(FileUtil.toFile(file), exporter);
                        Progress.progress(progressTicket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                Progress.finish(progressTicket);

                if(!cancel) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle
                        .getMessage(DesktopExportController.class, "DesktopExportController.status.exportAllSuccess",
                            workspaceCollection.size(), folder.getPath()));
                }
            }
        }

        @Override
        public boolean cancel() {
            cancel = true;
            return true;
        }

        @Override
        public void setProgressTicket(ProgressTicket progressTicket) {
            this.progressTicket = progressTicket;
        }
    }
}
