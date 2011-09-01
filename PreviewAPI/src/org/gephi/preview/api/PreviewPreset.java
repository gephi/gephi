/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.preview.api;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.preview.presets.DefaultPreset;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewPreset implements Comparable<PreviewPreset> {
    
    protected final Map<String, Object> properties;
    protected final String name;
    
    public PreviewPreset(String name) {
        properties = new HashMap<String, Object>();
        this.name = name;
    }
    
    public PreviewPreset(String name, Map<String, Object> propertiesMap) {
        properties = propertiesMap;
        this.name = name;
    }
    
    public Map<String, Object> getProperties() {
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
    
    public static Map<String, String> serialize(PreviewPreset preset) {
        Map<String, String> result = new HashMap<String, String>();
        for (Entry<String, Object> entry : preset.properties.entrySet()) {
            String propertyName = entry.getKey();
            try {
                Object propertyValue = entry.getValue();
                if (propertyValue != null) {
                    PropertyEditor editor = PropertyEditorManager.findEditor(propertyValue.getClass());
                    if (editor != null) {
                        editor.setValue(propertyValue);
                        result.put(propertyName, editor.getAsText());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        return result;
    }
    
    public static PreviewPreset deserialize(String presetName, Map<String, String> propertiesString) {
        DefaultPreset defaultPreset = new DefaultPreset();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(defaultPreset.getProperties());
        
        for (Entry<String, String> entry : propertiesString.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValueString = entry.getValue();
            if (propertyValueString != null && !propertyValueString.isEmpty()) {
                Object defaultPropertyValue = properties.get(propertyName);
                if (defaultPropertyValue != null) {
                    PropertyEditor editor = PropertyEditorManager.findEditor(defaultPropertyValue.getClass());
                    
                    if (editor != null) {
                        editor.setAsText(propertyValueString);
                        if (editor.getValue() != null) {
                            try {
                                properties.put(propertyName, editor.getValue());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return defaultPreset;
    }
}
