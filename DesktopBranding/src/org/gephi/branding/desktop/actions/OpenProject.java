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
import org.gephi.project.api.ProjectController;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

public final class OpenProject extends SystemAction {

    private static final String LAST_PATH = "OpenProject_Last_Path";
    private static final String LAST_PATH_DEFAULT = "OpenProject_Last_Path_Default";

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectController.class).canOpenProject();
    }

    public void actionPerformed(ActionEvent e) {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (!pc.closeCurrentProject()) {
            return;
        }

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
                ew.printStackTrace();
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(OpenProject.class, "OpenProject.defaulterror"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        }
    }

    @Override
    protected String iconResource() {
        return "org/gephi/branding/desktop/actions/openProject.png";
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenProject.class, "CTL_OpenProject");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
