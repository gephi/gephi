/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.spi.AttributableFilter;

/**
 *
 * @author mbastian
 */
public abstract class AbstractAttributeFilter extends AbstractFilter implements AttributableFilter {

    protected AttributeColumn column;
    protected AbstractAttributeFilter.Type type;

    public AbstractAttributeFilter(String name, AttributeColumn column) {
        super(name + " (" + column.getTitle() + ")");
        this.column = column;
        this.type = AttributeUtils.getDefault().isNodeColumn(column) ? Type.NODE : Type.EDGE;

        //Add column property
        addProperty(AttributeColumn.class, "column");
    }

    public Type getType() {
        return type;
    }

    public AttributeColumn getColumn() {
        return column;
    }

    public void setColumn(AttributeColumn column) {
        this.column = column;
        this.type = AttributeUtils.getDefault().isNodeColumn(column) ? Type.NODE : Type.EDGE;
    }
}
