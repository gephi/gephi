package org.gephi.layout;

import java.io.StringWriter;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.utils.MockLayout;
import org.gephi.layout.utils.MockLayoutBuilder;
import org.gephi.layout.utils.Utils;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

public class PersistenceProviderTest {

    @Test
    public void testEmpty() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutDefaultProperties() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        layoutModel.saveProperties(layout);
        layoutModel.loadProperties(layout);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutChangedDoubleProperties() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        layout.setAngle(33.0);
        layoutModel.saveProperties(layout);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutChangedColumnProperties() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(layoutModel.getWorkspace());
        Column col = graphModel.getNodeTable().addColumn("foo", Integer.class);
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        layout.setColumn(col);
        layoutModel.saveProperties(layout);
        layoutModel.loadProperties(layout);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testSelectedLayout() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        layoutModel.setSelectedLayout(layout);

        // Make sure LayoutBuilder is found in Lookup
        MockServices.setServices(MockLayoutBuilder.class);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutPropertyWithoutRegisteredEditor() throws Exception {
        // Mode has no PropertyEditor registered, so Serialization.getValueAsText() returns null for it.
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        layout.setMode(MockLayout.Mode.FAST);
        layoutModel.saveProperties(layout);

        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("layoutmodel");
        layoutModel.writeXML(writer);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        stringWriter.close();
    }
}
