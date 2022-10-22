package org.gephi.project.io;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.io.utils.GephiFormat;
import org.gephi.project.io.utils.MockXMLPersistenceProvider;
import org.gephi.project.io.utils.Utils;
import org.gephi.project.impl.WorkspaceImpl;
import org.junit.Assert;
import org.junit.Test;

public class GephiWriterReaderTest {

    @Test
    public void testProject() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        ProjectImpl project = Utils.newProject();
        GephiWriter.writeProject(writer, project);

        StringReader stringReader = new StringReader(stringWriter.toString());
        XMLStreamReader reader = GephiFormat.newXMLReader(stringReader);
        ProjectImpl readProject = GephiReader.readProject(reader, null);

        //TODO Implement deepEquals in ProjectImpl
        Assert.assertNotNull(readProject);
        Assert.assertEquals(Utils.PROJECT_NAME, readProject.getName());
        Assert.assertEquals(project, readProject);
    }

    @Test
    public void testWorkspace() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        GephiWriter.writeWorkspace(writer, Utils.newWorkspace());

        StringReader stringReader = new StringReader(stringWriter.toString());
        XMLStreamReader reader = GephiFormat.newXMLReader(stringReader);
        WorkspaceImpl workspace = GephiReader.readWorkspace(reader, Utils.newProject());

        //TODO Implement deepEquals in ProjectImpl
        Assert.assertNotNull(workspace);
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
}
