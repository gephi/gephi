package org.gephi.ui.exporter.preview;

import org.openide.util.NbPreferences;

public abstract class AbstractExporterSettings {

    protected boolean get(String name, boolean defaultValue) {
        return NbPreferences.forModule(AbstractExporterSettings.class).getBoolean(name, defaultValue);
    }

    protected void put(String name, boolean value) {
        NbPreferences.forModule(AbstractExporterSettings.class).putBoolean(name, value);
    }

    protected int get(String name, int defaultValue) {
        return NbPreferences.forModule(AbstractExporterSettings.class).getInt(name, defaultValue);
    }

    protected void put(String name, int value) {
        NbPreferences.forModule(AbstractExporterSettings.class).putInt(name, value);
    }

    protected float get(String name, float defaultValue) {
        return NbPreferences.forModule(AbstractExporterSettings.class).getFloat(name, defaultValue);
    }

    protected void put(String name, float value) {
        NbPreferences.forModule(AbstractExporterSettings.class).putFloat(name, value);
    }
}

