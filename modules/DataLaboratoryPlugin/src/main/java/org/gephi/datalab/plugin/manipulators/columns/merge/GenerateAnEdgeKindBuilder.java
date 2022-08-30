package org.gephi.datalab.plugin.manipulators.columns.merge;

import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategyBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = AttributeColumnsMergeStrategyBuilder.class)
public class GenerateAnEdgeKindBuilder implements AttributeColumnsMergeStrategyBuilder {

    @Override
    public AttributeColumnsMergeStrategy getAttributeColumnsMergeStrategy() {
        return new GenerateAnEdgeKind();
    }
}
