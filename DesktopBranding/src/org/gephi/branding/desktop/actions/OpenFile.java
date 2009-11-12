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
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import org.gephi.io.importer.FileType;
import org.gephi.io.importer.ImportController;
import org.gephi.project.api.ProjectController;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

public class OpenFile extends SystemAction {

    private static final String LAST_PATH = "OpenFile_Last_Path";
    private static final String LAST_PATH_DEFAULT = "OpenFile_Last_Path_Default";

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenFile.class, "CTL_OpenFile");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectController.class).canOpenFile();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        //Get last directory
        String lastPathDefault = NbPreferences.forModule(OpenFile.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(OpenFile.class).get(LAST_PATH, lastPathDefault);

        //Init dialog
        final JFileChooser chooser = new JFileChooser(lastPath);
        DialogFileFilter graphFilter = new DialogFileFilter(NbBundle.getMessage(getClass(), "OpenFile_filechooser_graphfilter"));

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        for (FileType fileType : importController.getFileTypes()) {
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
            NbPreferences.forModule(OpenFile.class).put(LAST_PATH, file.getAbsolutePath());

            //Do
            importController.doImport(fileObject);
        }
    }
}