package org.gephi.transformation.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;

import org.gephi.transformation.api.TransformationController;
import org.gephi.transformation.spi.Transformation;
import org.gephi.transformation.spi.TransformationBuilder;
import org.openide.util.Lookup;

import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service = TransformationController.class)})
public class TransformationControllerImpl implements TransformationController {
    private Transformation transformation;
    public TransformationControllerImpl() {
        TransformationBuilder transformationBuilder = Lookup.getDefault().lookup(TransformationBuilder.class);
        this.transformation = transformationBuilder.buildTransformation();
    }
    private Graph get_graph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        GraphModel graphModel = graphController.getGraphModel();
        return graphModel.getGraphVisible();

    }

    @Override
    public void mirror_x() {
        transformation.mirror_x(get_graph());
    }

    @Override
    public void mirror_y() {
        transformation.mirror_y(get_graph());
    }

    @Override
    public void rotate_left() {
        transformation.rotate_left(get_graph());
    }

    @Override
    public void rotate_right() {
        transformation.rotate_right(get_graph());
    }

    @Override
    public void extend() {
        transformation.extend(get_graph());
    }

    @Override
    public void reduce() {
        transformation.reduce(get_graph());
    }
}
