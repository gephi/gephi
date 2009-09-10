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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.gephi.desktop.io.export.api.ExporterClassUI;
import org.gephi.io.exporter.ExportController;
import org.gephi.io.exporter.FileType;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class FileExporterUI implements ExporterClassUI {

    public String getName() {
        return "File...";
    }

    public boolean isEnable() {
        return true;
    }

    public void action() {
        final String LAST_PATH = "FileExporter_Last_Path";
        final String LAST_PATH_DEFAULT = "FileExporter_Last_Path_Default";

        //Get last directory
        String lastPathDefault = NbPreferences.forModule(FileExporterUI.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(FileExporterUI.class).get(LAST_PATH, lastPathDefault);

        //File Chooser filters
        final JFileChooser chooser = new JFileChooser(lastPath);
        ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        if (exportController == null) {
            return;
        }
        for (FileType fileType : exportController.getFileTypes()) {
            DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
            dialogFileFilter.addExtensions(fileType.getExtensions());
            chooser.addChoosableFileFilter(dialogFileFilter);
        }

        //Show
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            //Save last path
            NbPreferences.forModule(FileExporterUI.class).put(LAST_PATH, file.getAbsolutePath());
        }
    }
}
