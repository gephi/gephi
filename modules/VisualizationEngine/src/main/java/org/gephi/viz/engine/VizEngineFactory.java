package org.gephi.viz.engine;

import org.gephi.graph.api.GraphModel;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.spi.VizEngineConfigurator;

import java.util.List;

/**
 *
 * @author Eduardo Ramos
 */
public class VizEngineFactory {

    public static <R extends RenderingTarget, I> VizEngine<R, I> newEngine(R renderingTarget, GraphModel graphModel, List<? extends VizEngineConfigurator<R, I>> configurators) {
        final VizEngine<R, I> engine = new VizEngine<>(graphModel, renderingTarget);

        //Configure
        if (configurators != null) {
            for (VizEngineConfigurator configurator : configurators) {
                if (configurator != null) {
                    configurator.configure(engine);
                }
            }
        }

        return engine;
    }
}
