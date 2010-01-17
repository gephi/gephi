/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewPreset implements Comparable<PreviewPreset> {

    protected final Map<String, String> properties;
    protected final String name;

    public PreviewPreset(String name) {
        properties = new HashMap<String, String>();
        this.name = name;
    }

    public PreviewPreset(String name, Map<String, String> propertiesMap) {
        properties = propertiesMap;
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(PreviewPreset o) {
        return o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj instanceof PreviewPreset) {
            PreviewPreset p = (PreviewPreset) obj;
            if (p.name.equals(name) && p.properties.equals(properties)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
