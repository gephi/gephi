package org.gephi.desktop.layout;

import org.gephi.layout.plugin.scale.Contract;
import org.gephi.layout.plugin.scale.ContractLayout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LayoutPresetPersistenceTest {

    private static final Contract BUILDER = new Contract();
    private LayoutPresetPersistence persistence;

    @Before
    public void setUp() {
        persistence = new LayoutPresetPersistence();
    }

    @After
    public void tearDown() {
        persistence.reset();
    }

    @Test
    public void testEmpty() {
        Assert.assertNull(persistence.getPresets(BUILDER.buildLayout()));
    }

    @Test
    public void testSave() {
        persistence.savePreset("preset1", BUILDER.buildLayout());
        Assert.assertEquals(1, persistence.getPresets(BUILDER.buildLayout()).size());
        Assert.assertTrue(persistence.hasPreset("preset1", BUILDER.buildLayout().getClass().getName()));
    }

    @Test
    public void testSaveMultiple() {
        persistence.savePreset("preset1", BUILDER.buildLayout());
        persistence.savePreset("preset2", BUILDER.buildLayout());
        Assert.assertEquals(2, persistence.getPresets(BUILDER.buildLayout()).size());
        Assert.assertTrue(persistence.hasPreset("preset2", BUILDER.buildLayout().getClass().getName()));
    }

    @Test
    public void testLoad() {
        ContractLayout layout = BUILDER.buildLayout();
        layout.setScale(42.0);
        persistence.savePreset("42", layout);

        layout.resetPropertiesValues();
        Preset preset = persistence.getPreset("42", layout);
        Assert.assertNotNull(preset);
        persistence.loadPreset(preset, layout);
        Assert.assertEquals(42.0, layout.getScale(), 0.0001);
    }
}