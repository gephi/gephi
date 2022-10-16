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

package org.gephi.desktop.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.project.ProjectControllerUIImpl;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.project.actions.OpenFile", category = "File")
@ActionRegistration(displayName = "#CTL_OpenFile", lazy = false)
@ActionReference(path = "Menu/File", position = 300)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 300),
    @ActionReference(path = "Shortcuts", name = "D-O")
})
public final class OpenFile extends AbstractAction {

    private static final String GEPHI_EXTENSION = "gephi";

    OpenFile() {
        super(NbBundle.getMessage(OpenFile.class, "CTL_OpenFile"));
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUIImpl.class).canOpenFile();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (isEnabled()) {
            if (ev.getSource() != null && ev.getSource() instanceof File) {
                FileObject fileObject = FileUtil.toFileObject((File) ev.getSource());
                if (fileObject.hasExt(GEPHI_EXTENSION)) {
                    Lookup.getDefault().lookup(ProjectControllerUIImpl.class).openProject(FileUtil.toFile(fileObject));
                } else {
                    ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                    if (importController.getImportController().isFileSupported(FileUtil.toFile(fileObject))) {
                        importController.importFile(fileObject);
                    } else {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                            NbBundle.getMessage(OpenFile.class, "OpenFile.fileNotSupported"),
                            NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                }
            } else if (ev.getSource() != null && ev.getSource() instanceof FileImporterBuilder[]) {
                Lookup.getDefault().lookup(ProjectControllerUIImpl.class)
                    .openFile((FileImporterBuilder[]) ev.getSource());
            } else {
                Lookup.getDefault().lookup(ProjectControllerUIImpl.class).openFile();
            }
        }
    }
}
