package org.gephi.viz.engine;

import org.gephi.graph.api.GraphModel;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.status.GraphSelectionImpl;
import org.gephi.viz.engine.structure.GraphIndexImpl;

public class VizEngineModel {

    // Graph Model
    private final GraphModel graphModel;

    //Graph Index
    private final GraphIndexImpl graphIndex;

    //Selection
    private final GraphSelectionImpl graphSelection;

    //Rendering Options
    private final GraphRenderingOptionsImpl renderingOptions;

    protected VizEngineModel(GraphModel graphModel, GraphRenderingOptions renderingOptions, GraphSelection graphSelection) {
        this.graphModel = graphModel;
        this.graphSelection = new GraphSelectionImpl(graphSelection);
        this.graphIndex = new GraphIndexImpl(graphModel, this.graphSelection);
        this.renderingOptions = new GraphRenderingOptionsImpl(renderingOptions);
    }

    public GraphIndexImpl getGraphIndex() {
        return graphIndex;
    }

    public GraphModel getGraphModel() {
        return graphModel;
    }

    public GraphRenderingOptionsImpl getRenderingOptions() {
        return renderingOptions;
    }

    public GraphSelectionImpl getGraphSelection() {
        return graphSelection;
    }
}
