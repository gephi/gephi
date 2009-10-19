package org.gephi.ui.preview;

import java.awt.Font;
import java.util.Iterator;
import org.gephi.preview.api.*;
import org.openide.util.Lookup;
import processing.core.*;
import processing.opengl.*;

/**
 * Processing applet displaying the graph preview.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class ProcessingPreview extends PApplet {

	private PVector ref = new PVector();
	private PVector trans = new PVector();
	private PVector lastMove = new PVector();
	private float scaling;
	private PFont nodeLabelFont;
	private PFont unidirectionalEdgeLabelFont;
	private PFont unidirectionalEdgeMiniLabelFont;
	private PFont bidirectionalEdgeLabelFont;
	private PFont bidirectionalEdgeMiniLabelFont;
	private PFont edgeLabelFont;
	private PFont edgeMiniLabelFont;
	private boolean graphSet = false;
	private Graph graph = null;
	private final static float MARGIN = 10f;

	/**
	 * Refresh the preview using the current graph from the preview controller.
	 */
	public void refresh() {
		graphSet = false;

		// fetch the current graph
		PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
		graph = controller.getGraph();

		// update fonts
		nodeLabelFont = createFont(controller.getNodeLabelFont());

		// initial graph positioning
		{
			PVector topLeft = graph.getMinPos().get();
			topLeft.sub(MARGIN, MARGIN, 0);
			PVector bottomRight = graph.getMaxPos().get();
			bottomRight.add(MARGIN, MARGIN, 0);

			// initializes zoom
			PVector box = PVector.sub(bottomRight, topLeft);
			float ratioWidth = width / box.x;
			float ratioHeight = height / box.y;
			scaling = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

			// initializes move
			PVector center = new PVector(width / 2f, height / 2f);
			PVector semiBox = PVector.div(box, 2);
			PVector scaledCenter = PVector.add(topLeft, semiBox);
			trans.set(center);
			trans.sub(scaledCenter);
			lastMove.set(trans);
		}

		graphSet = true;

		// redraw the applet
		redraw();
	}

	@Override
	public void setup() {
		size(500, 500, JAVA2D);
		//size(500, 500, OPENGL);
		rectMode(CENTER);

		// the preview is drawn once and then redrawn when necessary
		noLoop();
	}

	@Override
	public void draw() {
		// blank the applet
		background(255);

		// user zoom
		PVector center = new PVector(width / 2f, height / 2f);
		PVector scaledCenter = PVector.mult(center, scaling);
		PVector scaledTrans = PVector.sub(center, scaledCenter);
		translate(scaledTrans.x, scaledTrans.y);
		scale(scaling);

		// user move
		translate(trans.x, trans.y);

		// draw graph
		if (graphSet) {
			drawGraph(graph);
		}
	}

	@Override
	public void mousePressed() {
		ref.set(mouseX, mouseY, 0);
	}

	@Override
	public void mouseDragged() {
		trans.set(mouseX, mouseY, 0);
		trans.sub(ref);
		trans.div(scaling); // ensure const. moving speed whatever the zoom is
		trans.add(lastMove);
		redraw();
	}

	@Override
	public void mouseReleased() {
		lastMove.set(trans);
	}

	@Override
	public void keyPressed() {
		switch (key) {
			case '+':
				scaling = scaling * 2f;
				break;
			case '-':
				scaling = scaling / 2f;
				break;
			case '0':
				scaling = 1;
				break;
		}

		redraw();
	}

	/**
	 * Create a Processing font from a classic font.
	 *
	 * @param font a font to transform
	 * @return a Processing font
	 */
	private PFont createFont(Font font) {
		return createFont(font.getName(), font.getSize());
	}

	/**
	 * Draw a graph on the preview.
	 *
	 * @param graph the graph to draw
	 */
	private void drawGraph(Graph graph) {
		// nodes are above edges and self-loops
		if (graph.showNodes()) {
			textFont(nodeLabelFont);
			textAlign(CENTER, CENTER);
			for (Iterator<Node> it = graph.getNodes(); it.hasNext();) {
				drawNode(it.next());
			}
		}
	}

	/**
	 * Draw a node on the preview.
	 *
	 * @param node the node to draw
	 */
	private void drawNode(Node node) {
		// draw the node itself
		stroke(node.getBorderColor().getRed(),
				node.getBorderColor().getGreen(),
				node.getBorderColor().getBlue());
		strokeWeight(node.getBorderWidth());
		fill(node.getColor().getRed(),
				node.getColor().getGreen(),
				node.getColor().getBlue());
		ellipse(node.getPosition().x, node.getPosition().y,
				node.getDiameter(), node.getDiameter());

		// node label
		if (node.showLabels()) {
			// draw a border
			if (node.showLabelBorders()) {
				drawNodeLabelBorder(node.getLabelBorder());
			}

			// print the node's label
			drawNodeLabel(node.getLabel());
		}
	}

	/**
	 * Draw a node label on the preview.
	 *
	 * @param label the node label to draw
	 */
	private void drawNodeLabel(NodeLabel label) {
		fill(label.getColor().getRed(),
				label.getColor().getGreen(),
				label.getColor().getBlue());
		text(label.getValue(),
				label.getPosition().x,
				label.getPosition().y - (textAscent() + textDescent()) * 0.1f);
	}

	/**
	 * Draw a node label border on the preview.
	 *
	 * @param border the node label border to draw
	 */
	private void drawNodeLabelBorder(NodeLabelBorder border) {
		noStroke();
		fill(border.getColor().getRed(),
				border.getColor().getGreen(),
				border.getColor().getBlue());
		rect(border.getPosition().x, border.getPosition().y,
				textWidth(border.getLabel().getValue()), (textAscent() + textDescent()));
	}
}
