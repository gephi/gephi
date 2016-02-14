/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.preview.api;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.gephi.utils.Serialization;

/**
 * Container for {@link PreviewProperty} attached to a {@link PreviewModel}.
 * <p>
 * This class holds all preview properties defined in the model. Each property
 * has a unique name, a type and a value and can be configured by users.
 * <p>
 * Properties should be added using the <code>addProperty()</code> method before
 * calling <code>putValue()</code> to properly register properties.
 * <p>
 * Besides holding well-defined properties this class acts also as a <em>map</em>
 * and can store arbitrary (key,value) pairs. All (key,value) pairs are stored
 * when calling the <code>putValue()</code> method but only properties added with
 * the <code>addProperty()</code> method are returned when calling the
 * <code>getProperties()</code> methods. Therefore this class can both be used
 * for fixed properties and temporary variables.
 * <p>
 * To batch put a set of property values the best way is to create a <code>PreviewPreset</code>
 * and call the <code>applyPreset()</code> method.
 * 
 * @author Mathieu Bastian
 * @see PreviewPreset
 */
public class PreviewProperties {

    private final Map<String, Object> simpleValues;
    private final Map<String, PreviewProperty> properties;

    public PreviewProperties() {
        properties = new LinkedHashMap<>();//Use LinkedHashMap to retrieve properties in insertion order
        simpleValues = new HashMap<>();
    }

    /**
     * Add <code>property</code> to the properties.
     * <p>
     * The property should have a unique name and the method will throw an exception
     * if not.
     * @param property the property to add to the properties
     * @throws IllegalArgumentException if <code>property</code> already exists
     */
    public void addProperty(PreviewProperty property) {
        if (properties.containsKey(property.getName())) {
            throw new RuntimeException("The property " + property.getName() + " already exists. Each property name should be unique.");
        }
        for (String parent : property.dependencies) {
            PreviewProperty p = properties.get(parent);
            if (p != null && !p.getType().equals(Boolean.class)) {
                throw new IllegalArgumentException("The property " + property.getName() + " has dependencies to non-boolean property " + p.getName());
            }
        }
        properties.put(property.getName(), property);
    }

    public void removeProperty(PreviewProperty property) {
        properties.remove(property.getName());
    }

    /**
     * Returns <code>true</code> if a property <code>name</code> exists.
     * @param name the name of the property to lookup
     * @return <code>true</code> if the property exists, <code>false</code> otherwise
     */
    public boolean hasProperty(String name) {
        return simpleValues.containsKey(name) || properties.containsKey(name);
    }

    /**
     * Puts the property's value.
     * @param name the name of the property
     * @param value the value
     */
    public void putValue(String name, Object value) {
        PreviewProperty property = getProperty(name);
        if (property != null) {
            property.setValue(value);
        } else {
            simpleValues.put(name, value);
        }
    }
    
    /**
     * Removes a simple value if existing
     * @param name Simple value name
     */
    public void removeSimpleValue(String name){
        simpleValues.remove(name);
    }

    /**
     * Returns the property value as an int.
     * @param property the property's name
     * @return the property's value or <code>0</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Number</code>
     */
    public int getIntValue(String property) {
        return getNumberValue(property, new Integer(0)).intValue();
    }

    /**
     * Returns the property value as a float.
     * @param property the property's name
     * @return the property's value or <code>0</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Number</code>
     */
    public float getFloatValue(String property) {
        return getNumberValue(property, new Float(0f)).floatValue();
    }

    /**
     * Returns the property value as a double.
     * @param property the property's name
     * @return the property's value or <code>0.0</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Number</code>
     */
    public double getDoubleValue(String property) {
        return getNumberValue(property, new Double(0.)).doubleValue();
    }

    /**
     * Returns the property value as an string. If the value is not a <code>String</code>
     * it calls the <code>toString()</code> method.
     * @param property the property's name
     * @return the property's value or <code>""</code> if not found
     */
    public String getStringValue(String property) {
        return getValue(property, "").toString();
    }

    /**
     * Returns an the property value as a <code>Color</code>.
     * @param property the property's name
     * @return the property's value or <code>null</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Color</code>
     */
    public Color getColorValue(String property) {
        return getValue(property, null);
    }

    /**
     * Returns an the property value as a <code>Font</code>.
     * @param property the property's name
     * @return the property's value or <code>null</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Font</code>
     */
    public Font getFontValue(String property) {
        return getValue(property, null);
    }

    /**
     * Returns the property value as a boolean.
     * @param property the property's name
     * @return the property's value or <code>false</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Boolean</code>
     */
    public boolean getBooleanValue(String property) {
        return getValue(property, Boolean.FALSE);
    }

    /**
     * Returns the property value and cast it to the <code>T</code> type.
     * @param <T> the type to cast the property value to
     * @param property the property's name
     * @return the property's value or <code>null</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>T</code>
     */
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

    /**
     * Returns the property value and cast it to the <code>T</code> type.
     * @param <T> the type to cast the property value to
     * @param property the property's name
     * @param defaultValue the default value if not found
     * @return the property's value or <code>defaultValue</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>T</code>
     */
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

    /**
     * Returns the property value as a <code>Number</code>.
     * @param property the property's name
     * @param defaultValue the default value if not found
     * @return the property's value or <code>defaultValue</code> if not found
     * @throws ClassCastException if the property can't be cast to <code>Number</code>
     */
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
    
    /**
     * Return all simple values.
     * @return all simple values
     */
    public Set<Entry<String, Object>> getSimpleValues(){
        return simpleValues.entrySet();
    }    

    /**
     * Returns all properties.
     * @return all properties
     */
    public PreviewProperty[] getProperties() {
        PreviewProperty[] props = properties.values().toArray(new PreviewProperty[0]);
        //Reorder to put parents on top:
        Arrays.sort(props, new Comparator<PreviewProperty>() {

            @Override
            public int compare(PreviewProperty o1, PreviewProperty o2) {
                boolean hasParent1 = o1.dependencies.length > 0;
                boolean hasParent2 = o2.dependencies.length > 0;
                if (hasParent1 && !hasParent2) {
                    return 1;
                } else if (!hasParent1 && hasParent2) {
                    return -1;
                } else {
                    return 0;//Stable sort will not change original insertion order if no parents.
                }
            }
        });
        return props;
    }

    /**
     * Returns all properties with <code>category</code> as category. A property
     * can belong to only one category. Default categories names are defined in
     * {@link PreviewProperty}.
     * @param category the category properties belong to
     * @return all properties in <code>category</code>
     */
    public PreviewProperty[] getProperties(String category) {
        List<PreviewProperty> props = new ArrayList<>();
        for (PreviewProperty p : properties.values()) {
            if (p.getCategory().equals(category)) {
                props.add(p);
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    /**
     * Returns the property defined as <code>name</code>.
     * @param name the property's name
     * @return the property with this name or <code>null</code> if not found
     */
    public PreviewProperty getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Returns all properties with <code>source</code> as source. A property
     * can belong to only one source.
     * @param source the source properties belong to
     * @return all properties in <code>source</code>
     */
    public PreviewProperty[] getProperties(Object source) {
        List<PreviewProperty> props = new ArrayList<>();
        for (PreviewProperty p : properties.values()) {
            if (p.getSource().equals(source)) {
                props.add(p);
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    /**
     * Returns all properties which defined <code>property</code> as a dependency.
     * @param property the parent property
     * @return all properties with <code>property</code> as a parent property
     */
    public PreviewProperty[] getChildProperties(PreviewProperty property) {
        List<PreviewProperty> props = new ArrayList<>();
        for (PreviewProperty p : properties.values()) {
            for (String pn : p.dependencies) {
                if (pn.equals(property.getName())) {
                    props.add(p);
                }
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    /**
     * Returns all properties <code>property</code> defined as dependencies.
     * @param property the property to find parent properties from
     * @return all properties <code>property</code> depends on
     */
    public PreviewProperty[] getParentProperties(PreviewProperty property) {
        List<PreviewProperty> props = new ArrayList<>();
        for (PreviewProperty p : properties.values()) {
            for (String pn : property.dependencies) {
                if (pn.equals(p.getName())) {
                    props.add(p);
                }
            }
        }
        return props.toArray(new PreviewProperty[0]);
    }

    /**
     * Sets all preset's property values to this properties.
     * @param previewPreset the preset to get values from
     */
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
    
    /**
     * Converts any value to a serialized String.
     * Uses <code>PropertyEditor</code> for serialization except for values of <code>Font</code> class.
     * 
     * Note: Method moved to Utils module (org.gephi.utils.Serialization).
     * @param value Value to serialize as String
     * @return Result String or null if the value can't be serialized with a <code>PropertyEditor</code>
     */
    public static String getValueAsText(Object value) {
        return Serialization.getValueAsText(value);
    }

    /**
     * Deserializes a serialized String of the given class.
     * Uses <code>PropertyEditor</code> for serialization except for values of <code>Font</code> class.
     * 
     * Note: Method moved to Utils module (org.gephi.utils.Serialization).
     * @param valueStr String to deserialize
     * @param valueClass Class of the serialized value
     * @return Deserialized value or null if it can't be deserialized with a <code>PropertyEditor</code>
     */
    public static Object readValueFromText(String valueStr, Class valueClass) {
        return Serialization.readValueFromText(valueStr, valueClass);
    }
}
