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
    private final Rotation rotation_right_1deg;
    private final Rotation rotation_left_1deg;
    private final Rotation rotation_right_45deg;
    private final Rotation rotation_left_45deg;
    private final Scale extend;
    private final Scale reduce;


    public TransformationControllerImpl() {

        this.xAxis = new MirrorXAxis();
        this.yAxis = new MirrorYAxis();
        this.rotation_right_1deg = new Rotation(1.f);
        this.rotation_left_1deg = new Rotation(-1.f);
        this.rotation_right_45deg = new Rotation(45.f);
        this.rotation_left_45deg = new Rotation(-45.f);
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
    public void rotate_left_1deg() {
        rotation_left_1deg.apply(get_graph());
    }

    @Override
    public void rotate_right_1deg() {
        rotation_right_1deg.apply(get_graph());
    }

    @Override
    public void rotate_left_45deg() {
        rotation_left_45deg.apply(get_graph());
    }

    @Override
    public void rotate_right_45deg() {
        rotation_right_45deg.apply(get_graph());
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
