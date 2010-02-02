package org.gephi.desktop.preview;

import java.awt.Font;
import java.util.HashMap;
import org.gephi.preview.api.*;
import org.openide.util.Lookup;
import processing.core.*;

/**
 * Processing applet displaying the graph preview.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class ProcessingPreview extends PApplet
        implements GraphRenderer {

    private PVector ref = new PVector();
    private PVector trans = new PVector();
    private PVector lastMove = new PVector();
    private float scaling;
    private PFont uniEdgeMiniLabelFont;
    private PFont biEdgeMiniLabelFont;
    private GraphSheet graphSheet = null;
    private final HashMap<Font, PFont> fontMap = new HashMap<Font, PFont>();
    private final static float MARGIN = 10f;

    /**
     * Refreshes the preview using the current graph from the preview
     * controller.
     */
    public void refresh() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel model = controller.getModel();

        // updates fonts
        fontMap.clear();
        uniEdgeMiniLabelFont = getPFont(model.getUniEdgeSupervisor().getMiniLabelFont());
        biEdgeMiniLabelFont = getPFont(model.getBiEdgeSupervisor().getMiniLabelFont());

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
        if (null != graphSheet) {
            renderGraph(graphSheet.getGraph());
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
    public void setGraphSheet(GraphSheet graphSheet) {
        this.graphSheet = graphSheet;
        initAppletLayout();
    }

    /**
     * Initializes the preview applet layout according to the graph's dimension.
     */
    private void initAppletLayout() {
        graphSheet.setMargin(MARGIN);

        // initializes zoom
        PVector box = new PVector(graphSheet.getWidth(), graphSheet.getHeight());
        float ratioWidth = width / box.x;
        float ratioHeight = height / box.y;
        scaling = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

        // initializes move
        PVector semiBox = PVector.div(box, 2);
        Point topLeftPosition = graphSheet.getTopLeftPosition();
        PVector topLeftVector = new PVector(topLeftPosition.getX(), topLeftPosition.getY());
        PVector center = new PVector(width / 2f, height / 2f);
        PVector scaledCenter = PVector.add(topLeftVector, semiBox);
        trans.set(center);
        trans.sub(scaledCenter);
        lastMove.set(trans);
    }

    public void renderGraph(Graph graph) {
        if (graph.showEdges()) {
            renderGraphEdges(graph);
        }

        if (graph.showNodes()) {
            renderGraphNodes(graph);
        }

        renderGraphLabelBorders(graph);

        renderGraphLabels(graph);
    }

    public void renderGraphEdges(Graph graph) {
        renderGraphUnidirectionalEdges(graph);
        renderGraphBidirectionalEdges(graph);
        renderGraphUndirectedEdges(graph);

        if (graph.showSelfLoops()) {
            renderGraphSelfLoops(graph);
        }
    }

    public void renderGraphSelfLoops(Graph graph) {
        for (SelfLoop sl : graph.getSelfLoops()) {
            renderSelfLoop(sl);
        }
    }

    public void renderGraphUnidirectionalEdges(Graph graph) {
        for (UnidirectionalEdge edge : graph.getUnidirectionalEdges()) {
            renderDirectedEdge(edge);
        }
    }

    public void renderGraphBidirectionalEdges(Graph graph) {
        for (BidirectionalEdge edge : graph.getBidirectionalEdges()) {
            renderDirectedEdge(edge);
        }
    }

    public void renderGraphUndirectedEdges(Graph graph) {
        for (UndirectedEdge e : graph.getUndirectedEdges()) {
            renderEdge(e);
        }
    }

    public void renderGraphNodes(Graph graph) {

        for (Node n : graph.getNodes()) {
            renderNode(n);
        }
    }

    public void renderGraphLabels(Graph graph) {
        textFont(uniEdgeMiniLabelFont);
        for (UnidirectionalEdge e : graph.getUnidirectionalEdges()) {
            if (!e.isCurved()) {
                if (e.showLabel() && e.hasLabel()) {
                    renderEdgeLabel(e.getLabel());
                }

                if (e.showMiniLabels()) {
                    renderEdgeMiniLabels(e);
                }
            }
        }

        textFont(biEdgeMiniLabelFont);
        for (BidirectionalEdge e : graph.getBidirectionalEdges()) {
            if (!e.isCurved()) {
                if (e.showLabel() && e.hasLabel()) {
                    renderEdgeLabel(e.getLabel());
                }

                if (e.showMiniLabels()) {
                    renderEdgeMiniLabels(e);
                }
            }
        }

        for (UndirectedEdge e : graph.getUndirectedEdges()) {
            if (e.showLabel() && !e.isCurved() && e.hasLabel()) {
                renderEdgeLabel(e.getLabel());
            }
        }

        for (Node n : graph.getNodes()) {
            if (n.showLabel() && n.hasLabel()) {
                renderNodeLabel(n.getLabel());
            }
        }
    }

    public void renderGraphLabelBorders(Graph graph) {
        for (Node n : graph.getNodes()) {
            if (n.showLabel() && n.hasLabel() && n.showLabelBorders()) {
                renderNodeLabelBorder(n.getLabelBorder());
            }
        }
    }

    public void renderNode(Node node) {
        stroke(node.getBorderColor().getRed(),
                node.getBorderColor().getGreen(),
                node.getBorderColor().getBlue());
        strokeWeight(node.getBorderWidth());
        fill(node.getColor().getRed(),
                node.getColor().getGreen(),
                node.getColor().getBlue());
        ellipse(node.getPosition().getX(), node.getPosition().getY(),
                node.getDiameter(), node.getDiameter());
    }

    public void renderNodeLabel(NodeLabel label) {
        textFont(getPFont(label.getFont()));
        textAlign(CENTER, CENTER);

        fill(label.getColor().getRed(),
                label.getColor().getGreen(),
                label.getColor().getBlue());
        text(label.getValue(),
                label.getPosition().getX(),
                label.getPosition().getY() - (textAscent() + textDescent()) * 0.1f);
    }

    public void renderNodeLabelBorder(NodeLabelBorder border) {
        textFont(getPFont(border.getLabel().getFont()));
        noStroke();
        fill(border.getColor().getRed(),
                border.getColor().getGreen(),
                border.getColor().getBlue());

        rect(border.getPosition().getX(), border.getPosition().getY(),
                textWidth(border.getLabel().getValue()), (textAscent() + textDescent()));
    }

    public void renderSelfLoop(SelfLoop selfLoop) {
        CubicBezierCurve curve = selfLoop.getCurve();

        strokeWeight(selfLoop.getThickness() * selfLoop.getScale());
        stroke(selfLoop.getColor().getRed(),
                selfLoop.getColor().getGreen(),
                selfLoop.getColor().getBlue());
        noFill();

        bezier(curve.getPt1().getX(), curve.getPt1().getY(),
                curve.getPt2().getX(), curve.getPt2().getY(),
                curve.getPt3().getX(), curve.getPt3().getY(),
                curve.getPt4().getX(), curve.getPt4().getY());
    }

    public void renderDirectedEdge(DirectedEdge edge) {
        renderEdge(edge);

        if (!edge.isCurved() && edge.showArrows()) {
            renderEdgeArrows(edge);
        }
    }

    public void renderEdge(Edge edge) {
        strokeWeight(edge.getThickness() * edge.getScale());
        stroke(edge.getColor().getRed(),
                edge.getColor().getGreen(),
                edge.getColor().getBlue());
        noFill();

        if (edge.isCurved()) {
            renderCurvedEdge(edge);
        } else {
            renderStraightEdge(edge);
        }
    }

    public void renderEdgeArrows(DirectedEdge edge) {
        noStroke();
        for (EdgeArrow a : edge.getArrows()) {
            renderEdgeArrow(a);
        }
    }

    public void renderEdgeMiniLabels(DirectedEdge edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            renderEdgeMiniLabel(ml);
        }
    }

    public void renderStraightEdge(Edge edge) {
        Point boundary1 = edge.getNode1().getPosition();
        Point boundary2 = edge.getNode2().getPosition();

        line(boundary1.getX(), boundary1.getY(), boundary2.getX(), boundary2.getY());
    }

    public void renderCurvedEdge(Edge edge) {
        for (CubicBezierCurve curve : edge.getCurves()) {
            bezier(curve.getPt1().getX(), curve.getPt1().getY(),
                    curve.getPt2().getX(), curve.getPt2().getY(),
                    curve.getPt3().getX(), curve.getPt3().getY(),
                    curve.getPt4().getX(), curve.getPt4().getY());
        }
    }

    public void renderEdgeArrow(EdgeArrow arrow) {
        fill(arrow.getColor().getRed(),
                arrow.getColor().getGreen(),
                arrow.getColor().getBlue());
        triangle(arrow.getPt1().getX(), arrow.getPt1().getY(),
                arrow.getPt2().getX(), arrow.getPt2().getY(),
                arrow.getPt3().getX(), arrow.getPt3().getY());
    }

    public void renderEdgeLabel(EdgeLabel label) {
        textFont(getPFont(label.getFont()));
        textAlign(CENTER, BASELINE);

        pushMatrix();
        fill(label.getColor().getRed(),
                label.getColor().getGreen(),
                label.getColor().getBlue());
        translate(label.getPosition().getX(), label.getPosition().getY());
        rotate(label.getAngle());
        text(label.getValue(), 0, 0);
        popMatrix();
    }

    public void renderEdgeMiniLabel(EdgeMiniLabel miniLabel) {
        pushMatrix();
        fill(miniLabel.getColor().getRed(),
                miniLabel.getColor().getGreen(),
                miniLabel.getColor().getBlue());
        textAlign(miniLabel.getHAlign().toProcessing(), BASELINE);
        translate(miniLabel.getPosition().getX(), miniLabel.getPosition().getY());
        rotate(miniLabel.getAngle());
        text(miniLabel.getValue(), 0, 0);
        popMatrix();
    }

    /**
     * Creates a Processing font from a classic font.
     *
     * @param font  a font to transform
     * @return      a Processing font
     */
    private PFont createFont(Font font) {
        return createFont(font.getName(), font.getSize());
    }

    /**
     * Returns the Processing font related to the given classic font.
     *
     * @param font  a classic font
     * @return      the related Processing font
     */
    private PFont getPFont(Font font) {
        if (fontMap.containsKey(font)) {
            return fontMap.get(font);
        }

        PFont pFont = createFont(font);
        fontMap.put(font, pFont);
        return pFont;
    }
}
