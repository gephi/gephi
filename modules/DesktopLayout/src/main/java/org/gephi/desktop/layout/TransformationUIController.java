package org.gephi.desktop.layout;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.plugin.mirror.Mirror;
import org.gephi.layout.plugin.mirror.MirrorLayout;
import org.gephi.layout.plugin.rotate.Rotate;
import org.gephi.layout.plugin.rotate.RotateLayout;
import org.gephi.layout.plugin.scale.AbstractScaleLayout;
import org.gephi.layout.plugin.scale.Expand;
import org.openide.util.Lookup;


public class TransformationUIController {

    private final MirrorLayout mirrorLayout;
    private final RotateLayout rotateLayout;
    private final AbstractScaleLayout scaleLayout;

    private final LayoutController layoutController;

    public TransformationUIController() {

        mirrorLayout = Lookup.getDefault().lookup(Mirror.class).buildLayout();
        rotateLayout = Lookup.getDefault().lookup(Rotate.class).buildLayout();
        scaleLayout = Lookup.getDefault().lookup(Expand.class).buildLayout();

        layoutController = Lookup.getDefault().lookup(LayoutController.class);

    }

    private GraphModel getGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        return graphController.getGraphModel();

    }

    // Current implementation is not ok. Should use a controller to run the layout but
    // right now there is only one instance of the LayoutController that will modify also the
    // Layout part if we use it.
    // Need to find a way to create a new instance of the Layout controller

    public void mirrorXAxis() {

        mirrorLayout.setyAxis(false);
        mirrorLayout.setxAxis(true);
        layoutController.executeLayout(mirrorLayout);
    }


    public void mirrorYAxis() {

        mirrorLayout.setyAxis(true);
        mirrorLayout.setxAxis(false);
        layoutController.executeLayout(mirrorLayout);
    }


    public void rotateRight1Deg() {

        rotateLayout.setAngle(1.f);
        layoutController.executeLayout(rotateLayout);
    }


    public void rotateRight45Deg() {
        rotateLayout.setAngle(45.f);
        layoutController.executeLayout(rotateLayout);
    }


    public void rotateLeft1Deg() {
        rotateLayout.setAngle(-1.f);
        layoutController.executeLayout(rotateLayout);
    }


    public void rotateLeft45Deg() {
        rotateLayout.setAngle(-45.f);
        layoutController.executeLayout(rotateLayout);
    }


    public void expand() {
        scaleLayout.setScale(1.1f);
        layoutController.executeLayout(scaleLayout);
    }


    public void reduce() {
        scaleLayout.setScale(0.9f);
        layoutController.executeLayout(scaleLayout);
    }
}
