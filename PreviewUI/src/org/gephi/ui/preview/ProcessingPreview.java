package org.gephi.ui.preview;

import java.awt.Font;
import java.util.Iterator;
import org.gephi.preview.api.*;
import processing.core.*;
import processing.opengl.*;

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

    public void setGraph(Graph graph, PreviewController controller) {
        graphSet = false;

        this.graph = graph;

        // add itself to controller listeners to get font updates
        genPFonts(controller);

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
        if (graphSet)
            drawGraph(graph);
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

    public void genPFonts(PreviewController controller) {
        nodeLabelFont = createFont(controller.getNodeLabelFont());
    }

    private PFont createFont(Font font) {
        return createFont(font.getName(), font.getSize());
    }

    public void drawGraph(Graph graph) {
        // nodes are above edges and self-loops
        if (graph.showNodes()) {
            textFont(nodeLabelFont);
            textAlign(CENTER, CENTER);
            for (Iterator<Node> it = graph.getNodes(); it.hasNext(); )
                drawNode(it.next());
        }
    }

    public void drawNode(Node node) {
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
            if (node.showLabelBorders())
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
}
