package org.gephi.ui.preview;

import java.util.Iterator;
import org.gephi.preview.api.*;
import processing.core.*;
import processing.opengl.*;

public class ProcessingPreview extends PApplet {

    private PVector m_ref, m_trans, m_lastMove;
    private float m_scaling;
    private PFont m_nodeLabelFont;
    private PFont m_unidirectionalEdgeLabelFont;
    private PFont m_unidirectionalEdgeMiniLabelFont;
    private PFont m_bidirectionalEdgeLabelFont;
    private PFont m_bidirectionalEdgeMiniLabelFont;
    private PFont m_edgeLabelFont;
    private PFont m_edgeMiniLabelFont;
    private boolean graphSet;
    private Graph m_graph;
    private Customizer m_customizer;

    private final static float MARGIN = 10f;

    public ProcessingPreview() {
        m_graph = null;
        graphSet = false;

        // move & zoom initializations
        m_trans = new PVector();
        m_ref = new PVector();
        m_lastMove = new PVector();
    }

    public void setGraph(Graph graph, Customizer customizer) {
        graphSet = false;

        m_graph = graph;
        m_customizer = customizer;

        // add itself to the customizer to get font updates
        //m_customizer.addCustomizedPreview(this);

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
            m_scaling = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

            // initializes move
            PVector center = new PVector(width / 2f, height / 2f);
            PVector semiBox = PVector.div(box, 2);
            PVector scaledCenter = PVector.add(topLeft, semiBox);
            m_trans.set(center);
            m_trans.sub(scaledCenter);
            m_lastMove.set(m_trans);
        }

        graphSet = true;
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
        PVector scaledCenter = PVector.mult(center, m_scaling);
        PVector scaledTrans = PVector.sub(center, scaledCenter);
        translate(scaledTrans.x, scaledTrans.y);
        scale(m_scaling);

        // user move
        translate(m_trans.x, m_trans.y);

        // draw graph
        if (graphSet)
            drawGraph(m_graph);
    }

    @Override
    public void mousePressed() {
        m_ref.set(mouseX, mouseY, 0);
    }

    @Override
    public void mouseDragged() {
        m_trans.set(mouseX, mouseY, 0);
        m_trans.sub(m_ref);
        m_trans.div(m_scaling); // ensure const. moving speed whatever the zoom is
        m_trans.add(m_lastMove);
        redraw();
    }

    @Override
    public void mouseReleased() {
        m_lastMove.set(m_trans);
    }

    @Override
    public void keyPressed() {
        switch (key) {
            case '+':
                m_scaling = m_scaling * 2f;
                break;
            case '-':
                m_scaling = m_scaling / 2f;
                break;
            case '0':
                m_scaling = 1;
                break;
        }

        redraw();
    }

    public void genNodeLabelPFont() {
        m_nodeLabelFont = createFont(
                m_customizer.getNodeLabelFont(),
                m_customizer.getNodeLabelFontSize());
    }
    
    public void genUnidirectionalEdgeLabelPFont() {
        m_unidirectionalEdgeLabelFont = createFont(
                m_customizer.getUnidirectionalEdgeLabelFont(),
                m_customizer.getUnidirectionalEdgeLabelFontSize());
    }

    public void genUnidirectionalEdgeMiniLabelPFont() {
        m_unidirectionalEdgeMiniLabelFont = createFont(
                m_customizer.getUnidirectionalEdgeMiniLabelFont(),
                m_customizer.getUnidirectionalEdgeMiniLabelFontSize());
    }
    
    public void genBidirectionalEdgeLabelPFont() {
        m_bidirectionalEdgeLabelFont = createFont(
                m_customizer.getBidirectionalEdgeLabelFont(),
                m_customizer.getBidirectionalEdgeLabelFontSize());
    }

    public void genBidirectionalEdgeMiniLabelPFont() {
        m_bidirectionalEdgeMiniLabelFont = createFont(
                m_customizer.getBidirectionalEdgeMiniLabelFont(),
                m_customizer.getBidirectionalEdgeMiniLabelFontSize());
    }

    public void drawGraph(Graph graph) {
        if (m_customizer.showEdges()) {
            // draw edges
            for (Iterator<UnidirectionalEdge> it = graph.getUnidirectionalEdges(); it.hasNext(); ) {
                m_edgeLabelFont = m_unidirectionalEdgeLabelFont;
                m_edgeMiniLabelFont = m_unidirectionalEdgeMiniLabelFont;
                drawEdge(it.next());
            }
            for (Iterator<BidirectionalEdge> it = graph.getBidirectionalEdges(); it.hasNext(); ) {
                m_edgeLabelFont = m_bidirectionalEdgeLabelFont;
                m_edgeMiniLabelFont = m_bidirectionalEdgeMiniLabelFont;
                drawEdge(it.next());
        }

            if (m_customizer.showSelfLoops()) {
                // draw self-loops
                for (Iterator<SelfLoop> it = graph.getSelfLoops(); it.hasNext(); )
                    drawSelfLoop(it.next());
            }
        }

        // nodes are above edges and self-loops
        if (m_customizer.showNodes()) {
            textFont(m_nodeLabelFont);
            textAlign(CENTER, CENTER);
            for (Iterator<Node> it = graph.getNodes(); it.hasNext(); )
                drawNode(it.next());
        }
    }

    public void drawNode(Node node) {
        // draw the node itself
        stroke(m_customizer.getNodeBorderColor().getRed(),
                m_customizer.getNodeBorderColor().getGreen(),
                m_customizer.getNodeBorderColor().getBlue());
        strokeWeight(m_customizer.getNodeBorderWidth());
        fill(node.getColor().getRed(),
                node.getColor().getGreen(),
                node.getColor().getBlue());
        ellipse(node.getPosition().x, node.getPosition().y,
                node.getDiameter(), node.getDiameter());

        // node label
        if (m_customizer.showNodeLabels()) {
            // draw a border
            if (m_customizer.showNodeLabelBorders())
                drawNodeLabelBorder(node.getLabelBorder());

            // print node's label
            drawNodeLabel(node.getLabel());
        }
    }

    public void drawNodeLabel(NodeLabel label) {
        fill(label.getColor().getRed(),
                label.getColor().getGreen(),
                label.getColor().getBlue());
        text(label.getValue(),
                label.getPosition().x,
                label.getPosition().y - (textAscent() + textDescent()) * 0.1f);
    }

    public void drawNodeLabelBorder(NodeLabelBorder border) {
        noStroke();
        fill(border.getColor().getRed(),
                border.getColor().getGreen(),
                border.getColor().getBlue());
        rect(border.getPosition().x, border.getPosition().y,
                textWidth(border.getLabel().getValue()), (textAscent() + textDescent()));
    }

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

    public void drawEdge(Edge edge) {
        strokeWeight(edge.getThickness());
        stroke(edge.getColor().getRed(),
                edge.getColor().getGreen(),
                edge.getColor().getBlue());
        noFill();
        
        if (edge.isCurved(m_customizer)) {
            // draw curved edge
            drawCurvedEdge(edge);
        }
        else {
            // draw straight edge
            drawStraightEdge(edge);

            // draw its arrows
            if (edge.showArrows(m_customizer)) {
                noStroke();
                for (Iterator<EdgeArrow> it = edge.getArrows(); it.hasNext(); )
                    drawEdgeArrow(it.next());
            }

            // draw its label
            if (edge.showLabel(m_customizer)) {
                textFont(m_edgeLabelFont);
                textAlign(CENTER, BASELINE);
                drawEdgeLabel(edge.getLabel());
            }

            // draw its mini-labels
            if (edge.showMiniLabels(m_customizer)) {
                textFont(m_edgeMiniLabelFont);
                for (Iterator<EdgeMiniLabel> it = edge.getMiniLabels(); it.hasNext(); )
                    drawEdgeMiniLabel(it.next());
            }
        }
    }

    public void drawStraightEdge(Edge edge) {
        PVector boundary1 = edge.getNode1().getPosition();
        PVector boundary2 = edge.getNode2().getPosition();

        // draw straight edge
        line(boundary1.x, boundary1.y, boundary2.x, boundary2.y);
    }

    public void drawCurvedEdge(Edge edge) {
        for (Iterator<CubicBezierCurve> it = edge.getCurves(); it.hasNext(); ) {
            CubicBezierCurve curve = it.next();

            // draw curve
            bezier(curve.getPt1().x, curve.getPt1().y,
                    curve.getPt2().x, curve.getPt2().y,
                    curve.getPt3().x, curve.getPt3().y,
                    curve.getPt4().x, curve.getPt4().y);
        }
    }

    public void drawEdgeArrow(EdgeArrow arrow) {
        fill(arrow.getColor().getRed(),
                arrow.getColor().getGreen(),
                arrow.getColor().getBlue());
        triangle(arrow.getPt1().x, arrow.getPt1().y,
                arrow.getPt2().x, arrow.getPt2().y,
                arrow.getPt3().x, arrow.getPt3().y);
    }

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
