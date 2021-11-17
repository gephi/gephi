package org.gephi.project.io;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.test.GephiFormat;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.junit.Assert;
import org.junit.Test;

public class WriterReaderTest {

    @Test
    public void testProject() throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        GephiWriter.writeProject(writer, Utils.newProject());

        StringReader stringReader = new StringReader(stringWriter.toString());
        XMLStreamReader reader = GephiFormat.newXMLReader(stringReader);
        ProjectImpl project = GephiReader.readProject(reader, null);

        //TODO Implement deepEquals in ProjectImpl
        Assert.assertNotNull(project);
        Assert.assertEquals(Utils.PROJECT_NAME, project.getName());
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
        Assert.assertEquals(Utils.WORKSPACE_NAME, workspace.getName());
    }
}
