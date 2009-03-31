/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.gephi.project.api.ProjectController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

public final class OpenProject implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        File file = new File("H:\\Travaux\\Gephi\\GephiProjects\\test2.gephi");
        file = FileUtil.normalizeFile(file);
        FileObject fileObject = FileUtil.toFileObject(file);

        try {

            DataObject doe = DataObject.find(fileObject);
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.loadProject(doe);

        } catch (Exception ew) {
            ew.printStackTrace();
        }
    }
}
