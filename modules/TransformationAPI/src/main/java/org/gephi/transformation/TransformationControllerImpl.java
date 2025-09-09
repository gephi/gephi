package org.gephi.transformation;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.transformation.api.TransformationController;

import org.gephi.transformation.operation.mirror.MirrorXAxis;
import org.gephi.transformation.operation.mirror.MirrorYAxis;
import org.gephi.transformation.operation.rotation.Rotation;
import org.gephi.transformation.operation.scale.Scale;
import org.gephi.transformation.spi.TransformationOperation;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.util.Optional;


@ServiceProvider(service = TransformationController.class)
public class TransformationControllerImpl implements TransformationController {
    /*
    * Temporary implementation for the Controller that have predefined transformation
    * operations for the UI.
    * Next step will be to build all the function and helpers around to allow dynamic
    * creation of Controller based on transformation and a UI.
    *
    * This will be done later as it's slightly different from Layout or other element
    * as we could imagine a transformation to have multiple "ui" (Rotation via slider or button)
    * and we could also image extra transformation with their own UI (Cartography Projection e.g)
    * This means a proper thought and factoring that is out of scope for just making simple
    * access to basic transformation right now.
    * */
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

    private Graph getGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        GraphModel graphModel = graphController.getGraphModel();
        return graphModel.getGraphVisible();

    }

    @Override
    public void apply(TransformationOperation operation, Graph graph) {

        graph.writeLock();
        try {
            operation.transformation(graph);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            graph.writeUnlock();
        }

    }

    @Override
    public void mirrorX() {
       this.apply(xAxis,getGraph());
    }

    @Override
    public void mirrorY() {
        this.apply(yAxis,getGraph());
    }

    @Override
    public void rotateLeft1Deg() {
        this.apply(rotation_left_1deg,getGraph());
    }

    @Override
    public void rotateRight1Deg() {
        this.apply(rotation_right_1deg,getGraph());
    }

    @Override
    public void rotateLeft45Deg() {
        this.apply(rotation_left_45deg,getGraph());
    }

    @Override
    public void rotateRight45Deg() {
        this.apply(rotation_right_45deg,getGraph());
    }

    @Override
    public void extend() {
        this.apply(extend,getGraph());
    }

    @Override
    public void reduce() {
        this.apply(reduce,getGraph());
    }
}
