package org.gephi.project.io;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.io.utils.MockXMLPersistenceProvider;
import org.gephi.project.io.utils.MockXMLPersistenceProviderFailRead;
import org.gephi.project.io.utils.MockXMLPersistenceProviderFailWrite;
import org.gephi.project.io.utils.Utils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

public class SaveAndLoadTaskTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testEmptyProject() throws Exception {
        ProjectImpl project = Utils.newProject();
        ProjectImpl readProject = saveAndLoad(project);
        Assert.assertNotNull(readProject);
        // TODO: DeepEquals
    }

    @Test
    public void testEmptyProjectFileOverwrite() throws Exception {
        ProjectImpl project = Utils.newProject();
        ProjectImpl readProject = saveAndLoadOverwrite(project);
        Assert.assertNotNull(readProject);
    }

    @Test
    public void testNotDeleteOnCancel() throws Exception {
        ProjectImpl project = Utils.newProject();
        File file = tempFolder.newFile("project.gephi");
        SaveTask saveTask = new SaveTask(project, file);
        saveTask.cancel();
        saveTask.run();
        Assert.assertTrue(file.exists());
    }

    @Test
    public void testEmptyWorkspace() throws Exception {
        WorkspaceImpl workspace = Utils.newWorkspace();
        ProjectImpl readProject = saveAndLoad(workspace.getProject());
        WorkspaceImpl readWorkspace = Utils.getCurrentWorkspace(readProject);
        Assert.assertNotNull(readWorkspace);
        // TODO: DeepEquals
    }

    @Test
    public void testPersistenceProvider() throws Exception {
        MockServices.setServices(MockXMLPersistenceProvider.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        saveAndLoad(workspace.getProject());

        Assert.assertEquals(MockXMLPersistenceProvider.TXT,
            Lookup.getDefault().lookup(MockXMLPersistenceProvider.class).getReadText());
    }

    @Test
    public void testPersistenceProviderFailWrite() throws Exception {
        MockServices.setServices(MockXMLPersistenceProviderFailWrite.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        saveAndLoad(workspace.getProject());
    }

    @Test
    public void testPersistenceProviderFailRead() throws Exception {
        MockServices.setServices(MockXMLPersistenceProviderFailRead.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        saveAndLoad(workspace.getProject());
    }

    private ProjectImpl saveAndLoad(ProjectImpl project) throws IOException {
        final File tempFile = new File(tempFolder.getRoot(), "tmp.gephi");

        return saveAndLoad(project, tempFile);
    }

    private ProjectImpl saveAndLoadOverwrite(ProjectImpl project) throws IOException {
        final File tempFile = tempFolder.newFile("tmp.gephi");

        return saveAndLoad(project, tempFile);
    }

    private ProjectImpl saveAndLoad(ProjectImpl project, File file) {
        int countWorkspaces = project.getWorkspaces().size();
        WorkspaceImpl w = project.newWorkspace();
        w.getWorkspaceMetadata().setTitle("Test");
        int workspaceId = w.getId();

        project.getProjectMetadata().setTitle("Test");
        SaveTask saveTask = new SaveTask(project, file);
        saveTask.run();
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.length() > 0);
        Assert.assertEquals(1, Objects.requireNonNull(file.getParentFile().list()).length);

        LoadTask loadTask = new LoadTask(file);
        ProjectImpl readProject = loadTask.execute(null);
        Assert.assertNotNull(readProject);
        Assert.assertEquals("Test", readProject.getProjectMetadata().getTitle());
        Assert.assertEquals(countWorkspaces + 1, readProject.getWorkspaces().size());
        Assert.assertEquals("Test", readProject.getWorkspace(workspaceId).getWorkspaceMetadata().getTitle());

        return readProject;
    }
}
