/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin;

import javax.swing.Icon;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;

/**
 *
 * @author mbastian
 */
public abstract class AbstractFilterBuilder implements FilterBuilder {

    protected final Category category;
    protected final String name;
    protected final String descrption;
    protected final Icon icon;

    public AbstractFilterBuilder(Category category, String name, String descrption, Icon icon) {
        this.category = category;
        this.name = name;
        this.descrption = descrption;
        this.icon = icon;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getDescription() {
        return descrption;
    }

    public void destroy(Filter filter) {
    }
}
