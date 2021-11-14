package org.gephi.layout;

import org.gephi.layout.plugin.rotate.Rotate;
import org.gephi.layout.plugin.rotate.RotateLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.test.GephiFormat;
import org.junit.Test;

public class PersistenceProviderTest {

    @Test
    public void testEmpty() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutDefaultProperties() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        Layout layout = new Rotate().buildLayout();
        layoutModel.saveProperties(layout);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testLayoutChangedProperties() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        RotateLayout layout = new Rotate().buildLayout();
        layout.setAngle(33.0);
        layoutModel.saveProperties(layout);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }

    @Test
    public void testSelectedLayout() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        RotateLayout layout = new Rotate().buildLayout();
        layoutModel.setSelectedLayout(layout);

        GephiFormat.testXMLPersistenceProvider(new LayoutModelPersistenceProvider(), layoutModel.getWorkspace());
    }
}
