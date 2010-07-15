/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.builders;

import org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.JoinWithSeparator;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategyBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Builder for JoinWithSeparator AttributeColumnsMergeStrategyBuilder.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service=AttributeColumnsMergeStrategyBuilder.class)
public class JoinWithSeparatorBuilder implements AttributeColumnsMergeStrategyBuilder{

    public AttributeColumnsMergeStrategy getAttributeColumnsMergeStrategy() {
        return new JoinWithSeparator();
    }
}
