/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.spi.Category;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public abstract class AbstractAttributeFilterBuilder extends AbstractFilterBuilder {

    protected final AttributeColumn column;

    public AbstractAttributeFilterBuilder(AttributeColumn column, Category category, String description, Icon icon) {
        super(category, "<font color='#000000'>" + column.getTitle() + "</font> "
                + "<font color='#999999'><i>" + column.getType().toString() + " "
                + (AttributeUtils.getDefault().isNodeColumn(column)
                ? "(" + NbBundle.getMessage(AbstractAttributeFilterBuilder.class, "AbstractAttributeFilterBuilder.Node") + ")"
                : "(" + NbBundle.getMessage(AbstractAttributeFilterBuilder.class, "AbstractAttributeFilterBuilder.Edge") + ")")
                + "</i></font>",
                description, icon);
        this.column = column;
    }
}
