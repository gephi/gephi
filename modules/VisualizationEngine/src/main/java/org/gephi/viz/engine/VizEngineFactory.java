package org.gephi.viz.engine;

import java.util.List;
import org.gephi.graph.api.GraphModel;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.spi.VizEngineConfigurator;

/**
 *
 * @author Eduardo Ramos
 */
public class VizEngineFactory {

    public static <R extends RenderingTarget, I> VizEngine<R, I> newEngine(R renderingTarget,
                                                                           List<? extends VizEngineConfigurator<R, I>> configurators) {
        return newEngine(renderingTarget, null, configurators);
    }

    public static <R extends RenderingTarget, I> VizEngine<R, I> newEngine(R renderingTarget, GraphModel graphModel,
                                                                           List<? extends VizEngineConfigurator<R, I>> configurators) {
        final VizEngine<R, I> engine = new VizEngine<>(renderingTarget);
        if (graphModel != null) {
            engine.setGraphModel(graphModel, null);
        }

        //Configure
        if (configurators != null) {
            for (VizEngineConfigurator<R, I> configurator : configurators) {
                if (configurator != null) {
                    configurator.configure(engine);
                }
            }
        }

        return engine;
    }
}
