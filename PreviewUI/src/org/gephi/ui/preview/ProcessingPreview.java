package org.gephi.ui.preview;

import java.awt.Font;
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
    private PFont uniEdgeLabelFont;
    private PFont uniEdgeMiniLabelFont;
    private PFont biEdgeLabelFont;
    private PFont biEdgeMiniLabelFont;
    private PFont edgeLabelFont;
    private PFont edgeMiniLabelFont;
    private Graph graph = null;
    private final static float MARGIN = 10f;

    /**
     * Refreshes the preview using the current graph from the preview
     * controller.
     */
    public void refresh() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);

        // updates fonts
        nodeLabelFont = createFont(controller.getNodeSupervisor().getNodeLabelFont());
        uniEdgeLabelFont = createFont(controller.getUniEdgeSupervisor().getLabelFont());
        uniEdgeMiniLabelFont = createFont(controller.getUniEdgeSupervisor().getMiniLabelFont());
        biEdgeLabelFont = createFont(controller.getBiEdgeSupervisor().getLabelFont());
        biEdgeMiniLabelFont = createFont(controller.getBiEdgeSupervisor().getMiniLabelFont());

        // redraws the applet
        redraw();
    }

    @Override
    public void setup() {
        size(500, 500, JAVA2D);
        rectMode(CENTER);
        smooth();
        noLoop(); // the preview is drawn once and then redrawn when necessary
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
        if (null != graph) {
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
     * Defines the preview graph to draw.
     *
     * @param graph  the preview graph to draw
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
        initAppletLayout();
    }

    /**
     * Initializes the preview applet layout according to the graph's dimension.
     */
    private void initAppletLayout() {
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

    /**
     * Creates a Processing font from a classic font.
     *
     * @param font  a font to transform
     * @return a Processing font
     */
    private PFont createFont(Font font) {
        return createFont(font.getName(), font.getSize());
    }

    /**
     * Draws a graph on the preview.
     *
     * @param graph  the graph to draw
     */
    private void drawGraph(Graph graph) {
        PreviewUIController controller = PreviewUIController.findInstance();

        if (graph.showEdges()) {

            // draw edges
            for (UnidirectionalEdge ue : controller.getVisibleUnidirectionalEdges()) {
                edgeLabelFont = uniEdgeLabelFont;
                edgeMiniLabelFont = uniEdgeMiniLabelFont;
                drawEdge(ue);
            }
            for (BidirectionalEdge be : controller.getVisibleBidirectionalEdges()) {
                edgeLabelFont = biEdgeLabelFont;
                edgeMiniLabelFont = biEdgeMiniLabelFont;
                drawEdge(be);
            }

            if (graph.showSelfLoops()) {
                // draw self-loops
                for (SelfLoop sl : controller.getVisibleSelfLoops()) {
                    drawSelfLoop(sl);
                }
            }
        }

        // nodes are above edges and self-loops
        if (graph.showNodes()) {
            textFont(nodeLabelFont);
            textAlign(CENTER, CENTER);
            for (Node n : controller.getVisibleNodes()) {
                drawNode(n);
            }
        }
    }

    /**
     * Draws a node on the preview.
     *
     * @param node  the node to draw
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
        if (node.showLabel()) {
            // draw a border
            if (node.showLabelBorders()) {
                drawNodeLabelBorder(node.getLabelBorder());
            }

            // print the node's label
            drawNodeLabel(node.getLabel());
        }
    }

    /**
     * Draws a node label on the preview.
     *
     * @param label  the node label to draw
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
     * Draws a node label border on the preview.
     *
     * @param border  the node label border to draw
     */
    private void drawNodeLabelBorder(NodeLabelBorder border) {
        noStroke();
        fill(border.getColor().getRed(),
                border.getColor().getGreen(),
                border.getColor().getBlue());
        rect(border.getPosition().x, border.getPosition().y,
                textWidth(border.getLabel().getValue()), (textAscent() + textDescent()));
    }

    /**
     * Draws a self-loop on the preview.
     *
     * @param selfLoop  the self-loop to draw
     */
    public void drawSelfLoop(SelfLoop selfLoop) {
        CubicBezierCurve curve = selfLoop.getCurve();

        strokeWeight(selfLoop.getThickness());
        stroke(selfLoop.getColor().getRed(),
                selfLoop.getColor().getGreen(),
                selfLoop.getColor().getBlue());
        noFill();

        bezier(curve.getPt1().x, curve.getPt1().y,
                curve.getPt2().x, curve.getPt2().y,
                curve.getPt3().x, curve.getPt3().y,
                curve.getPt4().x, curve.getPt4().y);
    }

    /**
     * Draws an edge on the preview.
     *
     * @param edge  the edge to draw
     */
    public void drawEdge(Edge edge) {
        strokeWeight(edge.getThickness());
        stroke(edge.getColor().getRed(),
                edge.getColor().getGreen(),
                edge.getColor().getBlue());
        noFill();

        if (edge.isCurved()) {
            // draw curved edge
            drawCurvedEdge(edge);
        } else {
            // draw straight edge
            drawStraightEdge(edge);

            // draw its arrows
            if (edge.showArrows()) {
                noStroke();
                for (EdgeArrow a : edge.getArrows()) {
                    drawEdgeArrow(a);
                }
            }

            // draw its label
            if (edge.showLabel()) {
                textFont(edgeLabelFont);
                textAlign(CENTER, BASELINE);
                drawEdgeLabel(edge.getLabel());
            }

            // draw its mini-labels
            if (edge.showMiniLabels()) {
                textFont(edgeMiniLabelFont);
                for (EdgeMiniLabel ml : edge.getMiniLabels()) {
                    drawEdgeMiniLabel(ml);
                }
            }
        }
    }

    /**
     * Draws a straight edge on the preview.
     *
     * @param edge  the straight edge to draw
     */
    public void drawStraightEdge(Edge edge) {
        PVector boundary1 = edge.getNode1().getPosition();
        PVector boundary2 = edge.getNode2().getPosition();

        // draw straight edge
        line(boundary1.x, boundary1.y, boundary2.x, boundary2.y);
    }

    /**
     * Draws a curved edge on the preview.
     *
     * @param edge  the curved edge to draw
     */
    public void drawCurvedEdge(Edge edge) {
        for (CubicBezierCurve curve : edge.getCurves()) {
            // draw curve
            bezier(curve.getPt1().x, curve.getPt1().y,
                    curve.getPt2().x, curve.getPt2().y,
                    curve.getPt3().x, curve.getPt3().y,
                    curve.getPt4().x, curve.getPt4().y);
        }
    }

    /**
     * Draws an edge arrow on the preview.
     *
     * @param edge  the edge arrow edge to draw
     */
    public void drawEdgeArrow(EdgeArrow arrow) {
        fill(arrow.getColor().getRed(),
                arrow.getColor().getGreen(),
                arrow.getColor().getBlue());
        triangle(arrow.getPt1().x, arrow.getPt1().y,
                arrow.getPt2().x, arrow.getPt2().y,
                arrow.getPt3().x, arrow.getPt3().y);
    }

    /**
     * Draws an edge label on the preview.
     *
     * @param edge  the edge label edge to draw
     */
    public void drawEdgeLabel(EdgeLabel label) {
        pushMatrix();
        fill(label.getColor().getRed(),
                label.getColor().getGreen(),
                label.getColor().getBlue());
        translate(label.getPosition().x, label.getPosition().y);
        rotate(label.getAngle());
        text(label.getValue(), 0, 0);
        popMatrix();
    }

    /**
     * Draws an edge mini-label on the preview.
     *
     * @param edge  the edge mini-label edge to draw
     */
    public void drawEdgeMiniLabel(EdgeMiniLabel miniLabel) {
        pushMatrix();
        fill(miniLabel.getColor().getRed(),
                miniLabel.getColor().getGreen(),
                miniLabel.getColor().getBlue());
        textAlign(miniLabel.getHAlign().toProcessing(), BASELINE);
        translate(miniLabel.getPosition().x, miniLabel.getPosition().y);
        rotate(miniLabel.getAngle());
        text(miniLabel.getValue(), 0, 0);
        popMatrix();
    }
}
