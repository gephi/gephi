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
package org.gephi.desktop.io.export;

import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.Exporter;


import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
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

            public void fatalError(Throwable t) {
                t.printStackTrace();
                String message = t.getCause().getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                //Logger.getLogger("").log(Level.WARNING, "", t.getCause());
            }
        };
        executor = new LongTaskExecutor(true, "Exporter", 10);
    }

    public void exportFile(final FileObject fileObject, final Exporter exporter) {
        if (exporter == null) {
            throw new RuntimeException(NbBundle.getMessage(getClass(), "error_no_matching_file_exporter"));
        }
        //Export Task
        LongTask task = null;
        if (exporter instanceof LongTask) {
            task = (LongTask) exporter;
        }

        executor.execute(task, new Runnable() {

            public void run() {
                try {
                    controller.exportFile(FileUtil.toFile(fileObject), exporter);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DesktopExportController.class, "DesktopExportController.status.exportSuccess", fileObject.getNameExt()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, "Export to " + fileObject.getNameExt(), errorHandler);
    }

    public ExportController getExportController() {
        return controller;
    }
}
