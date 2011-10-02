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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.gephi.preview.presets.DefaultPreset;

/**
 * Read only set of {@link PreviewProperty} values.
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

    /**
     * Returns a read-only map of all properties
     * @return a read-only map of all properties
     */
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

    @Override
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

    /**
     * Serialize preset property values to strings and return a map of
     * (key,value) for all properties.
     * @param preset the preset to serialize
     * @return a map of all properties with values serialized as strings
     */
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

    /**
     * Create a PreviewPreset from a list of (key,value) pairs as strings. Typically
     * property values are not string but serialized as strings and this method
     * is used to deserialize them
     * @param presetName        the name of the preset
     * @param propertiesString  the property keys and values as stirngs
     * @return the deserialized preset with the given preset name
     */
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
