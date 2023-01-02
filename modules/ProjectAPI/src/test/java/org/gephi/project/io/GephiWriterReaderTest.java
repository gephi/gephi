package org.gephi.project.io;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.impl.WorkspaceInformationImpl;
import org.gephi.project.io.utils.GephiFormat;
import org.gephi.project.io.utils.MockXMLPersistenceProvider;
import org.gephi.project.io.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

public class GephiWriterReaderTest {

    @Test
    public void testProject() throws Exception {
        ProjectImpl project = Utils.newProject();
        ProjectImpl readProject = writeAndReadProject(project);

        //TODO Implement deepEquals in ProjectImpl
        Assert.assertNotNull(readProject);
        Assert.assertEquals(Utils.PROJECT_NAME, readProject.getName());
        Assert.assertEquals(project, readProject);
    }

    @Test
    public void testProjectMetadata() throws Exception {
        ProjectImpl project = Utils.newProject();
        project.getProjectMetadata().setDescription("desc");
        project.getProjectMetadata().setTitle("title");
        project.getProjectMetadata().setKeywords("keywords");
        project.getProjectMetadata().setAuthor("author");
        ProjectImpl readProject = writeAndReadProject(project);

        //TODO Implement deepEquals in ProjectImpl
        Assert.assertNotNull(readProject);
        Assert.assertEquals(Utils.PROJECT_NAME, readProject.getName());
        Assert.assertEquals(project, readProject);
        Assert.assertEquals(project.getProjectMetadata(), readProject.getProjectMetadata());
    }

    @Test
    public void testWorkspace() throws Exception {
        WorkspaceImpl workspace = Utils.newWorkspace();
        workspace.getLookup().lookup(WorkspaceInformationImpl.class).setName("foo");
        Workspace read = writeAndReadWorkspace(workspace);

        //TODO Implement deepEquals in ProjectImpl
        Assert.assertNotNull(read);
        Assert.assertEquals(workspace.getName(), read.getName());
    }

    @Test
    public void testWorkspaceMetadata() throws Exception {
        Workspace workspace = Utils.newWorkspace();
        workspace.getWorkspaceMetadata().setDescription("foo");
        workspace.getWorkspaceMetadata().setTitle("bar");

        Workspace read = writeAndReadWorkspace(workspace);
        Assert.assertEquals(workspace.getWorkspaceMetadata(), read.getWorkspaceMetadata());
    }

    @Test
    public void testPersistenceProvider() throws Exception {
        MockXMLPersistenceProvider pp = new MockXMLPersistenceProvider();

        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        GephiWriter.writeWorkspaceChildren(writer, Utils.newWorkspace(), pp);

        StringReader stringReader = new StringReader(stringWriter.toString());
        XMLStreamReader reader = GephiFormat.newXMLReader(stringReader);
        GephiReader.readWorkspaceChildren(Utils.newWorkspace(), reader, pp);
        Assert.assertEquals(MockXMLPersistenceProvider.TXT, pp.getReadText());
    }

    // Utils

    private ProjectImpl writeAndReadProject(ProjectImpl project) throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        GephiWriter.writeProject(writer, project);

        StringReader stringReader = new StringReader(stringWriter.toString());
        XMLStreamReader reader = GephiFormat.newXMLReader(stringReader);
        return GephiReader.readProject(reader, null);
    }

    private Workspace writeAndReadWorkspace(Workspace source) throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        GephiWriter.writeWorkspace(writer, source);

        StringReader stringReader = new StringReader(stringWriter.toString());
        XMLStreamReader reader = GephiFormat.newXMLReader(stringReader);
        return GephiReader.readWorkspace(reader, Utils.newProject());
    }
}
