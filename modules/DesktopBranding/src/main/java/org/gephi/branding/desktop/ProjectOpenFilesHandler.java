package org.gephi.branding.desktop;

import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Handles files from double click at opening time
 */
public class ProjectOpenFilesHandler implements OpenFilesHandler {

    private static final String GEPHI_EXTENSION = "gephi";

    @Override
    public void openFiles(OpenFilesEvent openFilesEvent) {
        Logger.getLogger(ProjectOpenFilesHandler.class.getName())
            .info("Handling " + openFilesEvent.getFiles().size() + " from opening");

        FileObject[] fileObjects = openFilesEvent.getFiles().stream().filter(File::exists).map(
            FileUtil::toFileObject).toArray(FileObject[]::new);

        Optional<FileObject>
            projectFile = Arrays.stream(fileObjects).filter(f -> f.hasExt(GEPHI_EXTENSION)).findFirst();

        // Open single project file and discard any other files
        if (projectFile.isPresent()) {
            Actions.forID("File", "org.gephi.desktop.project.actions.OpenFile").actionPerformed(
                new ActionEvent(FileUtil.toFile(projectFile.get()), 0, null));
        } else if (fileObjects.length > 0) {
            // Open files
            ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
            importController.importFiles(fileObjects);
        }
    }
}
