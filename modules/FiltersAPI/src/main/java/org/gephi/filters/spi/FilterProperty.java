/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.filters.spi;

import java.beans.PropertyEditor;
import org.gephi.filters.api.PropertyExecutor;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * Properties for filters. All editable properties of a filter must be used
 * through this class, especially setting value should be done by using
 * {@link #setValue(java.lang.Object) }.
 * <p>
 * The role of this class is to define filter's properties in order value changes
 * can be tracked by the system, UI can be generated and values correctly saved
 * in projects file.
 *
 * @author Mathieu Bastian
 */
public final class FilterProperty {

    protected PropertySupport.Reflection property;
    protected Filter filter;
    protected PropertyExecutor propertyExecutor;

    FilterProperty(Filter filter) {
        this.filter = filter;
        propertyExecutor = Lookup.getDefault().lookup(PropertyExecutor.class);
    }

    /**
     * Returns property's name
     * @return      property's name
     */
    public String getName() {
        return property.getDisplayName();
    }

    /**
     * Returns property's value, can be <code>null</code>
     * @return      property's value
     */
    public Object getValue() {
        try {
            return property.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Set property's value. The type of <code>value</code> must match with this
     * property value type.
     * @param value the value that is to be set
     */
    public void setValue(Object value) {
        if (propertyExecutor != null) {
            propertyExecutor.setValue(this, value, new PropertyExecutor.Callback() {

                public void setValue(Object value) {
                    try {
                        property.setValue(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            try {
                property.setValue(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the <code>PropertyEditor</code> associated to the property value.
     * @return      the property editor
     */
    public PropertyEditor getPropertyEditor() {
        return property.getPropertyEditor();
    }

    /**
     * Sets the property editor class. The class must implement
     * {@link PropertyEditor}.
     * @param clazz the property editor class
     */
    public void setPropertyEditorClass(Class<? extends PropertyEditor> clazz) {
        property.setPropertyEditorClass(clazz);
    }

    /**
     * Returns the property's value type.
     * @return      the value type
     */
    public Class getValueType() {
        return property.getValueType();
    }

    /**
     * Returns the filter instance this property is associated to.
     * @return      the filter this property belongs to
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Create a property.
     * @param filter The filter instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static FilterProperty createProperty(Filter filter, Class valueType, String propertyName, String getMethod, String setMethod) throws NoSuchMethodException {
        final FilterProperty filterProperty = new FilterProperty(filter);
        PropertySupport.Reflection property = new PropertySupport.Reflection(filter, valueType, getMethod, setMethod);
        property.setName(propertyName);
        filterProperty.property = property;

        return filterProperty;
    }

    /**
     * Create a property.
     * @param filter The filter instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param fieldName The Java field name of the property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static FilterProperty createProperty(Filter filter, Class valueType, String fieldName) throws NoSuchMethodException {
        if (valueType == Boolean.class) {
            valueType = boolean.class;
        }
        final FilterProperty filterProperty = new FilterProperty(filter);
        PropertySupport.Reflection property = new PropertySupport.Reflection(filter, valueType, fieldName);
        property.setName(fieldName);
        filterProperty.property = property;

        return filterProperty;
    }
}
