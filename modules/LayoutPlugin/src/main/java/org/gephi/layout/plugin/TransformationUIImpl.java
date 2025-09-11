package org.gephi.layout.plugin;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.LayoutControllerImpl;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.plugin.mirror.Mirror;
import org.gephi.layout.plugin.mirror.MirrorLayout;
import org.gephi.layout.plugin.rotate.Rotate;
import org.gephi.layout.plugin.rotate.RotateLayout;
import org.gephi.layout.plugin.scale.AbstractScaleLayout;
import org.gephi.layout.plugin.scale.Expand;
import org.gephi.layout.spi.TransformationUI;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationUI.class)
public class TransformationUIImpl extends LayoutControllerImpl implements TransformationUI {

    private final MirrorLayout mirrorLayout ;
    private final RotateLayout rotateLayout;
    private final AbstractScaleLayout scaleLayout;


    public TransformationUIImpl() {

        mirrorLayout = Lookup.getDefault().lookup(Mirror.class).buildLayout();
        rotateLayout = Lookup.getDefault().lookup(Rotate.class).buildLayout();
        scaleLayout = Lookup.getDefault().lookup(Expand.class).buildLayout();

    }
    private GraphModel getGraph(){
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        return graphController.getGraphModel();

    }
    // Current implementation is not ok. Should use a controller to run the layout but
    // right now there is only one instance of the LayoutController that will modify also the
    // Layout part if we use it.
    // Need to find a way to create a new instance of the Layout controller
    @Override
    public void mirrorXAxis() {
        mirrorLayout.setGraphModel(getGraph());
        mirrorLayout.setyAxis(false);
        mirrorLayout.setxAxis(true);
        mirrorLayout.goAlgo();
    }

    @Override
    public void mirrorYAxis() {
        mirrorLayout.setGraphModel(getGraph());
        mirrorLayout.setyAxis(true);
        mirrorLayout.setxAxis(false);
        mirrorLayout.goAlgo();
    }

    @Override
    public void rotateRight1Deg() {
        rotateLayout.setGraphModel(getGraph());
        rotateLayout.setAngle(1.f);
        rotateLayout.goAlgo();
    }

    @Override
    public void rotateRight45Deg() {
        rotateLayout.setGraphModel(getGraph());
        rotateLayout.setAngle(45.f);
        rotateLayout.goAlgo();
    }

    @Override
    public void rotateLeft1Deg() {
        rotateLayout.setGraphModel(getGraph());
        rotateLayout.setAngle(-1.f);
        rotateLayout.goAlgo();
    }

    @Override
    public void rotateLeft45Deg() {
        rotateLayout.setGraphModel(getGraph());
        rotateLayout.setAngle(-45.f);
        rotateLayout.goAlgo();
    }

    @Override
    public void expand() {
        scaleLayout.setGraphModel(getGraph());
        scaleLayout.setScale(1.1f);
        scaleLayout.goAlgo();
    }

    @Override
    public void reduce() {
        scaleLayout.setGraphModel(getGraph());
        scaleLayout.setScale(0.9f);
        scaleLayout.goAlgo();
    }
}
