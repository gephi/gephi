package org.gephi.preview;

import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.controller.PreviewControllerImpl;
import org.openide.util.Lookup;
import processing.core.PVector;

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
	 * @param alpha      the self-loop's alpha color
	 * @param node       the self-loop's related node
	 */
	public SelfLoopImpl(GraphImpl parent, float thickness, int alpha, NodeImpl node) {
		super(parent, thickness, alpha);
		this.node = node;

		// generate the self-loop's curve
		genCurve();

		// register the self-loop to its supervisor
		PreviewControllerImpl controller = (PreviewControllerImpl) Lookup.getDefault().lookup(PreviewController.class);
		controller.getSelfLoopSupervisor().addSelfLoop(this);
	}

	/**
	 * Generates the self-loop's curve.
	 */
	private void genCurve() {
		PVector checkpoint1 = node.getPosition().get();
		checkpoint1.add(node.getDiameter(), -node.getDiameter(), 0);

		PVector checkpoint2 = node.getPosition().get();
		checkpoint2.add(node.getDiameter(), node.getDiameter(), 0);

		curve = new CubicBezierCurveImpl(
				node.getPosition(),
				checkpoint1,
				checkpoint2,
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
}
