package org.gephi.datalab.plugin.manipulators.columns.merge;

import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategyBuilder;
import org.openide.util.lookup.ServiceProvider;

/*
TODO:
Was part of https://github.com/gephi/gephi/issues/2586
but graphstore need a more deep refactorign to consider Kind as a "Column" to fit some
feature of Gephi.
So it's desactivated until the "Column" refactoring is done on graphstore.
*/

// @ServiceProvider(service = AttributeColumnsMergeStrategyBuilder.class)
public class GenerateAnEdgeKindBuilder implements AttributeColumnsMergeStrategyBuilder {

    @Override
    public AttributeColumnsMergeStrategy getAttributeColumnsMergeStrategy() {
        return new GenerateAnEdgeKind();
    }
}
