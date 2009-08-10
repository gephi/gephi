/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.layout.api;

import java.lang.reflect.Field;
import java.util.MissingResourceException;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutProperty {

    protected String name;
    protected String description = "";
    protected Field field;

    public static LayoutProperty createProperty(Class layoutClass, String fieldName) {
        LayoutProperty lp = new LayoutProperty();

        try {
            //Field
            lp.field = layoutClass.getField(fieldName);

            //Name
            lp.name = fieldName;
            lp.name = NbBundle.getMessage(layoutClass, layoutClass.getName() + "_" + fieldName + "_name");
            lp.description = NbBundle.getMessage(layoutClass, layoutClass.getName() + "_" + fieldName + "_desc");
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (SecurityException ex) {
            return null;
        } catch (MissingResourceException ex) {
        }
        return lp;
    }

    public static Property createProperty(
        Object obj, Class valueType, String propertyName, String propertyDesc,
        String getMethod, String setMethod) throws NoSuchMethodException {
        Property property = new PropertySupport.Reflection(
            obj, valueType, getMethod, setMethod);

        property.setName(propertyName);
        property.setShortDescription(propertyDesc);

        return property;
    }
}
