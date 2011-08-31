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

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewProperties {

    private final Map<String, Object> simpleValues;
    private final Map<String, PreviewProperty> properties;

    public PreviewProperties() {
        properties = new HashMap<String, PreviewProperty>();
        simpleValues = new HashMap<String, Object>();
    }

    public void addProperty(PreviewProperty property) {
        if (properties.containsKey(property.getName())) {
            throw new RuntimeException("The property " + property.getName() + " already exists. Each property name should be unique.");
        }
        for (String parent : property.dependencies) {
            PreviewProperty p = properties.get(parent);
            if (p != null && !p.getType().equals(Boolean.class)) {
                throw new RuntimeException("The property " + property.getName() + " has dependencies to non-boolean property " + p.getName());
            }
        }
        properties.put(property.getName(), property);
    }

    public void removeProperty(PreviewProperty property) {
        properties.remove(property.getName());
    }

    public boolean hasProperty(String name) {
        return simpleValues.containsKey(name) || properties.containsKey(name);
    }

    public void putValue(String name, Object value) {
        PreviewProperty property = getProperty(name);
        if (property != null) {
            property.setValue(value);
        } else {
            simpleValues.put(name, value);
        }
    }

    public int getIntValue(String property) {
        return getNumberValue(property, new Integer(0)).intValue();
    }

    public float getFloatValue(String property) {
        return getNumberValue(property, new Float(0f)).floatValue();
    }

    public double getDoubleValue(String property) {
        return getNumberValue(property, new Double(0.)).doubleValue();
    }

    public String getStringValue(String property) {
        return getValue(property, "");
    }

    public Color getColorValue(String property) {
        return getValue(property, Color.BLACK);
    }

    public Font getFontValue(String property) {
        return getValue(property, null);
    }

    public boolean getBooleanValue(String property) {
        return getValue(property, Boolean.FALSE);
    }

    public <T> T getValue(String property) {
        PreviewProperty p = getProperty(property);
        if (p != null && p.getValue() != null) {
            T value = (T) p.getValue();
            return value;
        } else if (simpleValues.containsKey(property)) {
            return (T) simpleValues.get(property);
        }
        return null;
    }

    public <T> T getValue(String property, T defaultValue) {
        PreviewProperty p = getProperty(property);
        if (p != null && p.getValue() != null) {
            T value = (T) p.getValue();
            return value;
        } else if (simpleValues.containsKey(property)) {
            return (T) simpleValues.get(property);
        }
        return defaultValue;
    }

    public Number getNumberValue(String property, Number defaultValue) {
        PreviewProperty p = getProperty(property);
        if (p != null && p.getValue() != null && p.getValue() instanceof Number) {
            Number value = (Number) p.getValue();
            return value;
        } else if (simpleValues.containsKey(property) && simpleValues.get(property) instanceof Number) {
            return (Number) simpleValues.get(property);
        }
        return defaultValue;
    }

    public PreviewProperty[] getProperties() {
        PreviewProperty[] props = properties.values().toArray(new PreviewProperty[0]);
        Arrays.sort(props, new Comparator<PreviewProperty>() {

            @Override
            public int compare(PreviewProperty o1, PreviewProperty o2) {
                boolean hasParent1 = o1.dependencies.length >0;
                boolean hasParent2 = o2.dependencies.length >0;
                if(hasParent1 && !hasParent2) {
                    return 1;
                } else if(!hasParent1 && hasParent2) {
                    return -1;
                } else {
                    return o1.displayName.compareTo(o2.displayName);
                }
            }
        });
        return props;
    }

    public PreviewProperty[] getProperties(String category) {
        List<PreviewProperty> props = new ArrayList<PreviewProperty>();
        for (PreviewProperty p : properties.values()) {
            if (p.getCategory().equals(category)) {
                props.add(p);
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    public PreviewProperty getProperty(String name) {
        return properties.get(name);
    }

    public PreviewProperty[] getProperties(Object source) {
        List<PreviewProperty> props = new ArrayList<PreviewProperty>();
        for (PreviewProperty p : properties.values()) {
            if (p.getSource().equals(source)) {
                props.add(p);
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    public PreviewProperty[] getChildProperties(PreviewProperty property) {
        List<PreviewProperty> props = new ArrayList<PreviewProperty>();
        for (PreviewProperty p : properties.values()) {
            for (String pn : p.dependencies) {
                if (pn.equals(property.getName())) {
                    props.add(p);
                }
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    public PreviewProperty[] getParentProperties(PreviewProperty property) {
        List<PreviewProperty> props = new ArrayList<PreviewProperty>();
        for (PreviewProperty p : properties.values()) {
            for (String pn : property.dependencies) {
                if (pn.equals(p.getName())) {
                    props.add(p);
                }
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    public void applyPreset(PreviewPreset previewPreset) {
        for (Entry<String, Object> entry : previewPreset.getProperties().entrySet()) {
            PreviewProperty prop = getProperty(entry.getKey());
            if (prop != null) {
                prop.setValue(entry.getValue());
            } else {
                simpleValues.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
