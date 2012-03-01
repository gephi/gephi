/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin;

import java.util.ArrayList;
import java.util.List;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.openide.util.Exceptions;

/**
 *
 * @author mbastian
 */
public abstract class AbstractFilter implements Filter {

    protected final String name;
    protected final List<FilterProperty> properties;

    public AbstractFilter(String name) {
        this.name = name;
        this.properties = new ArrayList<FilterProperty>();
    }

    public String getName() {
        return name;
    }

    public FilterProperty[] getProperties() {
        return properties.toArray(new FilterProperty[0]);
    }

    public void addProperty(Class clazz, String name) {
        try {
            properties.add(FilterProperty.createProperty(this, clazz, name));
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
