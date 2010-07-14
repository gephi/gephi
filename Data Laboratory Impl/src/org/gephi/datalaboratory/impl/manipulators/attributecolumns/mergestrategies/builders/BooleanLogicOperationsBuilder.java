/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.builders;

import org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.BooleanLogicOperations;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategyBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service=AttributeColumnsMergeStrategyBuilder.class)
public class BooleanLogicOperationsBuilder implements AttributeColumnsMergeStrategyBuilder{

    public AttributeColumnsMergeStrategy getAttributeColumnsMergeStrategy() {
        return new BooleanLogicOperations();
    }
}
