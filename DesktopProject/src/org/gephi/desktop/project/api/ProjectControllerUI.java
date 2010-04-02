/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.project.api;

import org.openide.loaders.DataObject;

/**
 *
 * @author Mathieu Bastian
 */
public interface ProjectControllerUI {

    public void saveProject();

    public void saveAsProject();

    public void openProject();

    public void openProject(DataObject dataObject);

    public void renameProject(final String name);

    public void projectProperties();

    public void openFile();

    public void newWorkspace();

    public void newProject();

    public void deleteWorkspace();

    public void closeProject();

    public void cleanWorkspace();

    public boolean canNewProject();

    public boolean canOpenProject();

    public boolean canCloseProject();

    public boolean canOpenFile();

    public boolean canSave();

    public boolean canSaveAs();

    public boolean canNewWorkspace();

    public boolean canDeleteWorkspace();

    public boolean canCleanWorkspace();

    public boolean canProjectProperties();
}
