package org.gephi.transformation.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.transformation.api.TransformationController;
import org.gephi.transformation.plugin.operations.MirrorXAxis;
import org.gephi.transformation.plugin.operations.MirrorYAxis;
import org.gephi.transformation.plugin.operations.Rotation;
import org.gephi.transformation.plugin.operations.Scale;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = TransformationController.class)
public class TransformationControllerImpl implements TransformationController {

    private final MirrorXAxis xAxis;
    private final MirrorYAxis yAxis;
    private final Rotation rotation_right;
    private final Rotation rotation_left;
    private final Scale extend;
    private final Scale reduce;


    public TransformationControllerImpl() {

        this.xAxis = new MirrorXAxis();
        this.yAxis = new MirrorYAxis();
        this.rotation_right = new Rotation(1.f);
        this.rotation_left = new Rotation(-1.f);
        this.extend = new Scale(1.1f);
        this.reduce = new Scale(.9f);

    }

    private Graph get_graph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        GraphModel graphModel = graphController.getGraphModel();
        return graphModel.getGraphVisible();

    }

    @Override
    public void mirror_x() {
        xAxis.apply(get_graph());
    }

    @Override
    public void mirror_y() {
       yAxis.apply(get_graph());
    }

    @Override
    public void rotate_left() {
        rotation_left.apply(get_graph());
    }

    @Override
    public void rotate_right() {
        rotation_right.apply(get_graph());
    }

    @Override
    public void extend() {
        extend.apply(get_graph());
    }

    @Override
    public void reduce() {
        reduce.apply(get_graph());
    }
}
