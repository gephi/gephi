package org.gephi.viz.engine;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphModel;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
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

    protected VizEngineModel(GraphModel graphModel, GraphRenderingOptions renderingOptions) {
        this.graphModel = graphModel;
        this.graphIndex = new GraphIndexImpl(graphModel);
        this.graphSelection = new GraphSelectionImpl();
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

    public static VizEngineModel createEmptyModel() {
        Configuration config = Configuration.builder().enableSpatialIndex(true).build();
        GraphModel emptyModel = GraphModel.Factory.newInstance(config);
        return new VizEngineModel(emptyModel, new GraphRenderingOptionsImpl());
    }
}
