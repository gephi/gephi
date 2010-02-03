/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview.api;

import java.beans.PropertyEditor;
import org.gephi.preview.api.supervisors.Supervisor;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Mathieu Bastian
 */
public final class SupervisorPropery {

    protected Property property;
    protected String category;

    SupervisorPropery(Property property, String category) {
        this.property = property;
        this.category = category;
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

    /**
     * Create a property.
     * @param supervisor The supervisor instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyDescription A description string for the property
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static SupervisorPropery createProperty(Supervisor supervisor, Class valueType, String propertyName, String propertyCategory, String propertyDescription) throws NoSuchMethodException {
        Property property = new PropertySupport.Reflection(
                supervisor, valueType, propertyName);

        property.setName(propertyName);
        property.setDisplayName(propertyDescription);
        property.setShortDescription(propertyDescription);

        return new SupervisorPropery(property, propertyCategory);
    }

    /**
     * Create a property, with a particular {@link PropertyEditor}. A particular
     * editor must be specified when the property type don't have a registered
     * editor class.
     * @param supervisor The supervisor instance
     * @param valueType The type of the property value, ex: <code>Double.class</code>
     * @param propertyName The display name of the property
     * @param propertyCategory A category string or <code>null</code> for using
     * default category
     * @param propertyDescription A description string for the property
     * @param editorClass A <code>PropertyEditor</code> class for the given type
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be found
     */
    public static SupervisorPropery createProperty(Supervisor supervisor, Class valueType, String propertyName, String propertyCategory, String propertyDescription, Class<? extends PropertyEditor> editorClass) throws NoSuchMethodException {
        PropertySupport.Reflection property = new PropertySupport.Reflection(
                supervisor, valueType, propertyName);

        property.setName(propertyName);
        property.setDisplayName(propertyDescription);
        property.setShortDescription(propertyDescription);
        property.setPropertyEditorClass(editorClass);

        return new SupervisorPropery(property, propertyCategory);
    }
}
