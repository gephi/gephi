package org.gephi.layout;

import org.gephi.layout.utils.MockLayout;
import org.gephi.layout.utils.MockLayoutBuilder;
import org.gephi.layout.utils.Utils;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Test;
import org.netbeans.junit.MockServices;

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

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutChangedProperties() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        layout.setAngle(33.0);
        layoutModel.saveProperties(layout);

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
}
