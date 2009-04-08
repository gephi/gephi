/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

public final class OpenProject implements ActionListener {

    public void actionPerformed(ActionEvent e) {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();

        //Open Dialog
        DialogFileFilter filter = new DialogFileFilter(NbBundle.getMessage(OpenProject.class, "OpenProject_filechooser_filter"));
        filter.addExtension(".gephi");

        final JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(filter);

        int returnFile = chooser.showOpenDialog(null);

        if (returnFile == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);

            try {
                
                DataObject doe = DataObject.find(fileObject);
                pc.loadProject(doe);

            } catch (Exception ew) {
                Exceptions.printStackTrace(ew);
            }
        }
    }
}
