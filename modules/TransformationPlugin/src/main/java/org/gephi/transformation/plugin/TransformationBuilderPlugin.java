package org.gephi.transformation.plugin;

import org.gephi.transformation.spi.Transformation;
import org.gephi.transformation.spi.TransformationBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationBuilder.class)
public class TransformationBuilderPlugin implements TransformationBuilder {

    @Override
    public Transformation buildTransformation() {
        return new TransformationPlugin();
    }
}
