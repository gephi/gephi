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
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.gephi.project.api.ProjectController;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Mathieu
 */
public class SaveAsProject extends SystemAction {

    private static final String LAST_PATH = "SaveAsProject_Last_Path";
    private static final String LAST_PATH_DEFAULT = "SaveAsProject_Last_Path_Default";

    @Override
    public String getName() {
        return NbBundle.getMessage(SaveProject.class, "CTL_SaveAsProject");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        try
        {
            saveAs();
        }
        catch(IOException e)
        {
            Logger.getLogger(SaveAsProject.class.getName()).throwing(getClass().getName(), "saveAs", e);
        }

    }

    private void saveAs() throws IOException
    {
        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(SaveAsProject.class, "SaveAsProject_filechooser_filter"));
        filter.addExtension(".gephi");

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(SaveAsProject.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(SaveAsProject.class).get(LAST_PATH, lastPathDefault);

        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.addChoosableFileFilter(filter);

        int returnFile = chooser.showSaveDialog(null);

        if (returnFile == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();

            //Save last path
            NbPreferences.forModule(SaveAsProject.class).put(LAST_PATH, file.getAbsolutePath());

            if (!file.getPath().endsWith(".gephi")) {
                file = new File(file.getPath() + ".gephi");
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    String failMsg = NbBundle.getMessage(
                            SaveAsProject.class,
                            "SaveAsProject_SaveFailed", new Object[]{file.getPath()});
                    JOptionPane.showMessageDialog(null, failMsg);
                    return;
                }

            } else {
                String overwriteMsg = NbBundle.getMessage(
                        SaveAsProject.class,
                        "SaveAsProject_Overwrite", new Object[]{file.getPath()});

                if (JOptionPane.showConfirmDialog(null, overwriteMsg) != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            try {

                DataObject dataObject = DataObject.find(fileObject);
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                pc.saveProject(dataObject);

            } catch(Exception e)
            {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
