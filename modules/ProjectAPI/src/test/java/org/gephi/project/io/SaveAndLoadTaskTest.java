package org.gephi.project.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.io.utils.MockBytesPersistenceProviderFailWrite;
import org.gephi.project.io.utils.MockXMLPersistenceProvider;
import org.gephi.project.io.utils.MockXMLPersistenceProviderFailRead;
import org.gephi.project.io.utils.MockXMLPersistenceProviderFailWrite;
import org.gephi.project.io.utils.Utils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

public class SaveAndLoadTaskTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void resetServices() {
        // Persistence providers are registered globally, don't leak them into the next test
        MockServices.setServices();
    }

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
    public void testFirstSaveLeavesNoTempFile() throws Exception {
        MockServices.setServices(MockXMLPersistenceProvider.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        File file = new File(tempFolder.getRoot(), "first.gephi");
        Assert.assertFalse(file.exists());

        Assert.assertNotNull(saveAndLoad(workspace.getProject(), file));
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.length() > 0);
        assertNoTempFile();
    }

    /**
     * Regression test: a first-time save that fails half-way through must not leave an empty (or partial) file at the
     * destination path, as the user would then be unable to reopen it.
     */
    @Test
    public void testFailedFirstSaveDoesNotCreateFile() {
        MockServices.setServices(MockBytesPersistenceProviderFailWrite.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        File file = new File(tempFolder.getRoot(), "failed.gephi");
        Assert.assertFalse(file.exists());

        assertSaveFails(workspace.getProject(), file);

        Assert.assertFalse("A failed first save must not leave a file behind", file.exists());
        assertNoTempFile();
    }

    @Test
    public void testCancelFirstSaveDoesNotCreateFile() {
        WorkspaceImpl workspace = Utils.newWorkspace();
        File file = new File(tempFolder.getRoot(), "cancelled.gephi");

        SaveTask saveTask = new SaveTask(workspace.getProject(), file);
        saveTask.cancel();
        Assert.assertFalse(saveTask.run());

        Assert.assertFalse("A cancelled first save must not leave a file behind", file.exists());
        assertNoTempFile();
    }

    @Test
    public void testFailedResaveKeepsExistingFileIntact() throws Exception {
        MockServices.setServices(MockXMLPersistenceProvider.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        ProjectImpl project = workspace.getProject();
        File file = new File(tempFolder.getRoot(), "resave.gephi");
        Assert.assertTrue(new SaveTask(project, file).run());
        byte[] original = Files.readAllBytes(file.toPath());
        Assert.assertTrue(original.length > 0);

        MockServices.setServices(MockBytesPersistenceProviderFailWrite.class);
        assertSaveFails(project, file);

        Assert.assertArrayEquals("A failed resave must leave the previous file untouched", original,
            Files.readAllBytes(file.toPath()));
        assertNoTempFile();

        MockServices.setServices(MockXMLPersistenceProvider.class);
        Assert.assertNotNull(new LoadTask(file).execute(null));
    }

    /**
     * A destination that is already 0-byte long (e.g. left over by a previously interrupted save) used to be written
     * to directly, so a failing save would keep it corrupt instead of leaving it alone.
     */
    @Test
    public void testFailedResaveOverEmptyFileDoesNotWriteToIt() throws Exception {
        MockServices.setServices(MockBytesPersistenceProviderFailWrite.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        File file = tempFolder.newFile("empty.gephi");
        Assert.assertEquals(0, file.length());

        assertSaveFails(workspace.getProject(), file);

        Assert.assertTrue(file.exists());
        Assert.assertEquals("A failed save must not write to the destination file", 0, file.length());
        assertNoTempFile();
    }

    @Test
    public void testCancelResaveKeepsExistingFileIntact() throws Exception {
        MockServices.setServices(MockXMLPersistenceProvider.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        ProjectImpl project = workspace.getProject();
        File file = new File(tempFolder.getRoot(), "resave.gephi");
        Assert.assertTrue(new SaveTask(project, file).run());
        byte[] original = Files.readAllBytes(file.toPath());

        SaveTask saveTask = new SaveTask(project, file);
        saveTask.cancel();
        Assert.assertFalse(saveTask.run());

        Assert.assertArrayEquals("A cancelled resave must leave the previous file untouched", original,
            Files.readAllBytes(file.toPath()));
        assertNoTempFile();
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

    @Test
    public void testDuplicateTaskCancelReturnsNull() {
        MockServices.setServices(MockXMLPersistenceProvider.class);

        WorkspaceImpl workspace = Utils.newWorkspace();
        DuplicateTask task = new DuplicateTask(workspace);
        task.cancel();
        WorkspaceImpl result = task.run();
        Assert.assertNull(result);
    }

    private void assertSaveFails(ProjectImpl project, File file) {
        try {
            new SaveTask(project, file).run();
            Assert.fail("Expected the save to fail");
        } catch (GephiFormatException expected) {
        }
    }

    private void assertNoTempFile() {
        String[] files = Objects.requireNonNull(tempFolder.getRoot().list());
        for (String name : files) {
            Assert.assertFalse("Leftover temporary file " + name + " in " + Arrays.toString(files),
                name.contains("_temp"));
        }
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
