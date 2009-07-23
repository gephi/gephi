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
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.gephi.project.api.ProjectController;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class OpenProject implements ActionListener {

    private static final String LAST_PATH = "OpenProject_Last_Path";
    private static final String LAST_PATH_DEFAULT = "OpenProject_Last_Path_Default";

    public void actionPerformed(ActionEvent e) {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();

        //Open Dialog
        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(OpenProject.class, "OpenProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(OpenProject.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(OpenProject.class).get(LAST_PATH, lastPathDefault);

        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.addChoosableFileFilter(filter);

        int returnFile = chooser.showOpenDialog(null);

        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            //Save last path
            NbPreferences.forModule(OpenProject.class).put(LAST_PATH, file.getAbsolutePath());

            try {
                
                DataObject doe = DataObject.find(fileObject);
                pc.loadProject(doe);

            } catch (Exception ew) {
                Exceptions.printStackTrace(ew);
            }
        }
    }
}
