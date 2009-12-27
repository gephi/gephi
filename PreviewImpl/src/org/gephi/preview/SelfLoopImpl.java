package org.gephi.preview;

import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.supervisors.SelfLoopSupervisorImpl;
import org.gephi.preview.util.Vector;
import org.openide.util.Lookup;

/**
 * Implementation of a preview self-loop.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SelfLoopImpl extends AbstractEdge implements SelfLoop {

    private final NodeImpl node;
    private CubicBezierCurveImpl curve;

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the self-loop
     * @param thickness  the self-loop's thickness
     * @param node       the self-loop's related node
     */
    public SelfLoopImpl(GraphImpl parent, float thickness, NodeImpl node) {
        super(parent, thickness);
        this.node = node;

        // generate the self-loop's curve
        genCurve();

        // register the self-loop to its supervisor
        getSelfLoopSupervisor().addSelfLoop(this);
    }

    /**
     * Generates the self-loop's curve.
     */
    private void genCurve() {
        Vector v1 = new Vector(node.getPosition());
        v1.add(node.getDiameter(), -node.getDiameter(), 0);

        Vector v2 = new Vector(node.getPosition());
        v2.add(node.getDiameter(), node.getDiameter(), 0);

        curve = new CubicBezierCurveImpl(
                node.getPosition(),
                new PointImpl(v1),
                new PointImpl(v2),
                node.getPosition());
    }

    /**
     * Returns the self-loop's related node.
     *
     * @return the self-loop's related node
     */
    public NodeImpl getNode() {
        return node;
    }

    /**
     * Alias of getNode().
     *
     * @return the self-loop's related node
     * @see getNode()
     */
    public NodeImpl getNode1() {
        return getNode();
    }

    /**
     * Alias of getNode().
     *
     * @return the self-loop's related node
     * @see getNode()
     */
    public NodeImpl getNode2() {
        return getNode();
    }

    /**
     * Returns the self-loop's curve.
     *
     * @return the self-loop's curve
     */
    public CubicBezierCurve getCurve() {
        return curve;
    }

    /**
     * Returns the self-loop supervisor.
     *
     * @return the controller's self-loop supervisor
     */
    public SelfLoopSupervisorImpl getSelfLoopSupervisor() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return (SelfLoopSupervisorImpl) controller.getSelfLoopSupervisor();
    }
}
