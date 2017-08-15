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
package org.gephi.branding.desktop;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.TransferHandler;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * See <a href="http://java.sun.com/docs/books/tutorial/uiswing/dnd/toplevel.html">Top-Level Drop Swing tutorial</a>
 *
 * @author Mathieu Bastian
 */
public class DragNDropFrameAdapter {

    private static final String GEPHI_EXTENSION = "gephi";

    public static void register() {
        JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
        frame.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                }
                //Due to bug 6759788 - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6759788
                //Impossible to get data here and look if compatible format
                return true;
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    List data = (List) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    File file = (File) data.get(0);
                    FileObject fileObject = FileUtil.toFileObject(file);
                    if(!file.exists()) {
                        return false;
                    }
                    if (fileObject.hasExt(GEPHI_EXTENSION)) {

                        ProjectControllerUI pc = Lookup.getDefault().lookup(ProjectControllerUI.class);
                        try {
                            pc.openProject(file);
                        } catch (Exception ew) {
                            Exceptions.printStackTrace(ew);
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(DragNDropFrameAdapter.class, "DragNDropFrameAdapter.openGephiError"), NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(msg);
                        }
                    } else {
                        ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                        if (importController.getImportController().isFileSupported(FileUtil.toFile(fileObject))) {
                            importController.importFile(fileObject);
                        } else {
                            return false;
                        }
                    }
                    return true;
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return false;
            }
        });
    }
}
