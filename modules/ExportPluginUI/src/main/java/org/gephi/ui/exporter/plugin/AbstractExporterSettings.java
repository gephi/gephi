package org.gephi.ui.exporter.plugin;

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

    protected char get(String name, char defaultValue) {
        return (char) NbPreferences.forModule(AbstractExporterSettings.class).getInt(name, defaultValue);
    }

    protected void put(String name, char value) {
        NbPreferences.forModule(AbstractExporterSettings.class).putInt(name, value);
    }
}
