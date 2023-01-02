package org.gephi.layout;

import org.gephi.layout.utils.MockLayout;
import org.gephi.layout.utils.MockLayoutBuilder;
import org.gephi.layout.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

public class LayoutModelImplTest {

    @Test
    public void testLocalPropertyReset() throws Exception {
        LayoutModelImpl layoutModel = Utils.newLayoutModel();
        MockLayout layout = new MockLayoutBuilder().buildLayout();
        Assert.assertNotEquals(42.0, layout.getLocalProperty(), 0.0);
        layoutModel.setSelectedLayout(layout);

        Assert.assertEquals(42.0, layout.getLocalProperty(), 0.0);
    }
}
