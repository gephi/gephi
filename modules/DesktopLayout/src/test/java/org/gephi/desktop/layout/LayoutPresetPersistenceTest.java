package org.gephi.desktop.layout;

import org.gephi.layout.plugin.forceAtlas.ForceAtlas;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.layout.plugin.scale.Contract;
import org.junit.Assert;
import org.junit.Test;

public class LayoutPresetPersistenceTest {

    private static final Contract BUILDER = new Contract();

    @Test
    public void testEmpty() {
        LayoutPresetPersistence persistence = new LayoutPresetPersistence();
        Assert.assertNull(persistence.getPresets(BUILDER.buildLayout()));
    }

}