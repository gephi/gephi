package org.gephi.project.io;

import java.io.File;
import java.io.IOException;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.io.utils.MockXMLPersistenceProvider;
import org.gephi.project.io.utils.MockXMLPersistenceProviderFailRead;
import org.gephi.project.io.utils.MockXMLPersistenceProviderFailWrite;
import org.gephi.project.io.utils.Utils;
import org.gephi.workspace.impl.WorkspaceImpl;
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
        final File tempFile = tempFolder.newFile("tmp.gephi");
        SaveTask saveTask = new SaveTask(project, tempFile);
        saveTask.run();
        Assert.assertTrue(tempFile.exists());

        LoadTask loadTask = new LoadTask(tempFile);
        ProjectImpl readProject = loadTask.execute();
        Assert.assertNotNull(readProject);

        return readProject;
    }
}
