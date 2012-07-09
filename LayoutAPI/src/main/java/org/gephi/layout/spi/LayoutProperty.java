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
package org.gephi.layout.spi;

import java.beans.PropertyEditor;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;

/**
 * Properties for layout algorithms that are used by the UI to fill the property
 * sheet and thus allow user edit.
 *
 * @author Mathieu Bastian
 */
public final class LayoutProperty {

    protected Property property;
    protected String category;
    /**
     * Should be unique for a property and not localized.
     */
    protected String canonicalName;

    public LayoutProperty(Property property, String category, String canonicalName) {
        this.property = property;
        this.category = category;
        this.canonicalName = canonicalName;
    }

    /**
     * Return the underlying <code>Property</code>.
     * @return the instance of <code>Node.Property</code>
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Return the category of the property
     */
    public String getCategory() {
        return category;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Create a property.
     * The parameter <code>propertyName</code> will be used as the canonical name of the <code>LayoutProperty</code>.
     * @param layout The layout instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyDescription A description string for the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static LayoutProperty createProperty(Layout layout, Class valueType, String propertyName, String propertyCategory, String propertyDescription, String getMethod, String setMethod) throws NoSuchMethodException {
        Property property = new PropertySupport.Reflection(
                layout, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);

        return new LayoutProperty(property, propertyCategory, propertyName);
    }

    /**
     * Create a property, with a particular {@link PropertyEditor}. A particular
     * editor must be specified when the property type don't have a registered
     * editor class.
     * The parameter <code>propertyName</code> will be used as the canonical name of the <code>LayoutProperty</code>.
     * @param layout The layout instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyDescription A description string for the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @param editorClass A <code>PropertyEditor</code> class for the given type
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static LayoutProperty createProperty(Layout layout, Class valueType, String propertyName, String propertyCategory, String propertyDescription, String getMethod, String setMethod, Class<? extends PropertyEditor> editorClass) throws NoSuchMethodException {
        PropertySupport.Reflection property = new PropertySupport.Reflection(
                layout, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);
        property.setPropertyEditorClass(editorClass);

        return new LayoutProperty(property, propertyCategory, propertyName);
    }

    /**
     * Create a property.
     * The parameter <code>propertyName</code> will be used as the canonical name of the <code>LayoutProperty</code>.
     * @param layout The layout instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyCanonicalName Canonical name for the <code>LayoutProperty</code>. It should be unique and not localized
     * @param propertyDescription A description string for the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static LayoutProperty createProperty(Layout layout, Class valueType, String propertyName, String propertyCategory, String propertyCanonicalName, String propertyDescription, String getMethod, String setMethod) throws NoSuchMethodException {
        Property property = new PropertySupport.Reflection(
                layout, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);

        return new LayoutProperty(property, propertyCategory, propertyCanonicalName);
    }

    /**
     * Create a property, with a particular {@link PropertyEditor}. A particular
     * editor must be specified when the property type don't have a registered
     * editor class.
     * The parameter <code>propertyName</code> will be used as the canonical name of the <code>LayoutProperty</code>.
     * @param layout The layout instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyCanonicalName Canonical name for the <code>LayoutProperty</code>. It should be unique and not localized
     * @param propertyDescription A description string for the property
     * @param getMethod The name of the get method for this property, must exist
     * to make Java reflexion working.
     * @param setMethod The name of the set method for this property, must exist
     * to make Java reflexion working.
     * @param editorClass A <code>PropertyEditor</code> class for the given type
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static LayoutProperty createProperty(Layout layout, Class valueType, String propertyName, String propertyCategory, String propertyCanonicalName, String propertyDescription, String getMethod, String setMethod, Class<? extends PropertyEditor> editorClass) throws NoSuchMethodException {
        PropertySupport.Reflection property = new PropertySupport.Reflection(
                layout, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDescription);
        property.setPropertyEditorClass(editorClass);

        return new LayoutProperty(property, propertyCategory, propertyCanonicalName);
    }
}
