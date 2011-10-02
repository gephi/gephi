/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.preview.plugin.renderers;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;

import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.EdgeLabelItem;

import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PVector;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
@ServiceProvider(service = Renderer.class, position = 500)
public class EdgeLabelRenderer implements Renderer {
    //Custom properties

    public static final String EDGE_COLOR = "edge.label.edgeColor";
    public static final String LABEL_X = "edge.label.x";
    public static final String LABEL_Y = "edge.label.y";
    //Default values
    protected final boolean defaultShowLabels = true;
    protected final Font defaultFont = new Font("Arial", Font.PLAIN, 10);
    protected final boolean defaultShorten = false;
    protected final DependantOriginalColor defaultColor = new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL);
    protected final int defaultMaxChar = 30;
    protected final float defaultOutlineSize = 2;
    protected final DependantColor defaultOutlineColor = new DependantColor(Color.WHITE);
    protected final float defaultOutlineOpacity = 40;
    //Font cache
    protected Font font;

    public void preProcess(PreviewModel previewModel) {
        PreviewProperties properties = previewModel.getProperties();
        if (properties.getBooleanValue(PreviewProperty.EDGE_LABEL_SHORTEN)) {
            //Shorten labels
            Item[] EdgeLabelsItems = previewModel.getItems(Item.EDGE_LABEL);

            int maxChars = properties.getIntValue(PreviewProperty.EDGE_LABEL_MAX_CHAR);
            for (Item item : EdgeLabelsItems) {
                String label = item.getData(EdgeLabelItem.LABEL);
                if (label.length() >= maxChars + 3) {
                    label = label.substring(0, maxChars) + "...";
                    item.setData(EdgeLabelItem.LABEL, label);
                }
            }
        }

        //Put parent color, and calculate position
        for (Item item : previewModel.getItems(Item.EDGE_LABEL)) {
            Edge edge = (Edge) item.getSource();
            Item edgeItem = previewModel.getItem(Item.EDGE, edge);
            
            EdgeColor edgeColor = (EdgeColor) properties.getValue(PreviewProperty.EDGE_COLOR);  
            NodeItem sourceItem = (NodeItem) edgeItem.getData(EdgeRenderer.SOURCE);
            NodeItem targetItem = (NodeItem) edgeItem.getData(EdgeRenderer.TARGET);
            Color color = edgeColor.getColor((Color) item.getData(EdgeItem.COLOR),
                (Color) sourceItem.getData(NodeItem.COLOR),
                (Color) targetItem.getData(NodeItem.COLOR));
            item.setData(EDGE_COLOR, color);
            if (edge.isSelfLoop()) {
                //Middle
                Float x = sourceItem.getData(NodeItem.X);
                Float y = sourceItem.getData(NodeItem.Y);
                Float size = sourceItem.getData(NodeItem.SIZE);

                PVector v1 = new PVector(x, y);
                v1.add(size, -size, 0);

                PVector v2 = new PVector(x, y);
                v2.add(size, size, 0);

                PVector middle = bezierPoint(x, y, v1.x, v1.y, v2.x, v2.y, x, y, 0.5f);
                item.setData(LABEL_X, middle.x);
                item.setData(LABEL_Y, middle.y);

            } else if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
                //Middle of the curve
                Float x1 = sourceItem.getData(NodeItem.X);
                Float x2 = targetItem.getData(NodeItem.X);
                Float y1 = sourceItem.getData(NodeItem.Y);
                Float y2 = targetItem.getData(NodeItem.Y);

                //Curved edgs
                PVector direction = new PVector(x2, y2);
                direction.sub(new PVector(x1, y1));
                float length = direction.mag();
                direction.normalize();

                float factor = properties.getFloatValue(EdgeRenderer.BEZIER_CURVENESS) * length;

                // normal vector to the edge
                PVector n = new PVector(direction.y, -direction.x);
                n.mult(factor);

                // first control point
                PVector v1 = new PVector(direction.x, direction.y);
                v1.mult(factor);
                v1.add(new PVector(x1, y1));
                v1.add(n);

                // second control point
                PVector v2 = new PVector(direction.x, direction.y);
                v2.mult(-factor);
                v2.add(new PVector(x2, y2));
                v2.add(n);

                PVector middle = bezierPoint(x1, y1, v1.x, v1.y, v2.x, v2.y, x2, y2, 0.5f);
                item.setData(LABEL_X, middle.x);
                item.setData(LABEL_Y, middle.y);
            } else {
                Float x = ((Float) sourceItem.getData(NodeItem.X) + (Float) targetItem.getData(NodeItem.X)) / 2f;
                Float y = ((Float) sourceItem.getData(NodeItem.Y) + (Float) targetItem.getData(NodeItem.Y)) / 2f;
                item.setData(LABEL_X, x);
                item.setData(LABEL_Y, y);
            }
        }

        //Property font
        font = properties.getFontValue(PreviewProperty.EDGE_LABEL_FONT);
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        Edge edge = (Edge) item.getSource();
        //Label
        Color edgeColor = item.getData(EDGE_COLOR);
        Color color = item.getData(EdgeLabelItem.COLOR);
        DependantOriginalColor propColor = properties.getValue(PreviewProperty.EDGE_LABEL_COLOR);
        color = propColor.getColor(edgeColor, color);
        String label = item.getData(EdgeLabelItem.LABEL);
        Float x = item.getData(LABEL_X);
        Float y = item.getData(LABEL_Y);

        //Outline
        DependantColor outlineDependantColor = properties.getValue(PreviewProperty.EDGE_LABEL_OUTLINE_COLOR);
        Float outlineSize = properties.getFloatValue(PreviewProperty.EDGE_LABEL_OUTLINE_SIZE);
        outlineSize = outlineSize * (font.getSize() / 32f);
        int outlineAlpha = (int) ((properties.getFloatValue(PreviewProperty.EDGE_LABEL_OUTLINE_OPACITY) / 100f) * 255f);
        if (outlineAlpha > 255) {
            outlineAlpha = 255;
        }
        Color outlineColor = outlineDependantColor.getColor(edgeColor);
        outlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), outlineAlpha);

        if (target instanceof ProcessingTarget) {
            renderProcessing((ProcessingTarget) target, label, x, y, color, outlineSize, outlineColor);
        } else if (target instanceof SVGTarget) {
            renderSVG((SVGTarget) target, edge, label, x, y, color, outlineSize, outlineColor);
        } else if (target instanceof PDFTarget) {
            renderPDF(((PDFTarget) target), label, x, y, color, outlineSize, outlineColor);
        }
    }

    public void renderProcessing(ProcessingTarget target, String label, float x, float y, Color color, float outlineSize, Color outlineColor) {
        PGraphics graphics = target.getGraphics();
        Graphics2D g2 = ((PGraphicsJava2D) graphics).g2;
        graphics.textAlign(PGraphics.CENTER, PGraphics.CENTER);

        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();
        float posX = x - fm.stringWidth(label) / 2f;
        float posY = y + fm.getAscent() / 2f;

        if (outlineSize > 0) {
            FontRenderContext frc = g2.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, label);
            Shape glyph = gv.getOutline(posX, posY);
            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(glyph);
        }

        g2.setColor(color);
        g2.drawString(label, posX, posY);
    }

    public void renderSVG(SVGTarget target, Edge edge, String label, float x, float y, Color color, float outlineSize, Color outlineColor) {
        Text labelText = target.createTextNode(label);

        if (outlineSize > 0) {
            Text labelTextOutline = target.createTextNode(label);
            Element outlineElem = target.createElement("text");
            outlineElem.setAttribute("class", edge.getEdgeData().getId());
            outlineElem.setAttribute("x", String.valueOf(x));
            outlineElem.setAttribute("y", String.valueOf(y));
            outlineElem.setAttribute("style", "text-anchor: middle; dominant-baseline: central;");
            outlineElem.setAttribute("fill", target.toHexString(color));
            outlineElem.setAttribute("font-family", font.getFamily());
            outlineElem.setAttribute("font-size", font.getSize() + "");
            outlineElem.setAttribute("stroke", target.toHexString(outlineColor));
            outlineElem.setAttribute("stroke-width", (outlineSize * target.getScaleRatio()) + "px");
            outlineElem.setAttribute("stroke-linecap", "round");
            outlineElem.setAttribute("stroke-linejoin", "round");
            outlineElem.setAttribute("stroke-opacity", String.valueOf(outlineColor.getAlpha() / 255f));
            outlineElem.appendChild(labelTextOutline);
            target.getTopElement(SVGTarget.TOP_NODE_LABELS_OUTLINE).appendChild(outlineElem);
        }

        Element labelElem = target.createElement("text");
        labelElem.setAttribute("class", edge.getEdgeData().getId());
        labelElem.setAttribute("x", x + "");
        labelElem.setAttribute("y", y + "");
        labelElem.setAttribute("style", "text-anchor: middle; dominant-baseline: central;");
        labelElem.setAttribute("fill", target.toHexString(color));
        labelElem.setAttribute("font-family", font.getFamily());
        labelElem.setAttribute("font-size", font.getSize() + "");
        labelElem.appendChild(labelText);
        target.getTopElement(SVGTarget.TOP_EDGE_LABELS).appendChild(labelElem);
    }

    public void renderPDF(PDFTarget target, String label, float x, float y, Color color, float outlineSize, Color outlineColor) {
        PdfContentByte cb = target.getContentByte();
        cb.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
        BaseFont bf = target.getBaseFont(font);
        float textHeight = getTextHeight(bf, font.getSize(), label);
        if (outlineSize > 0) {
            cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
            cb.setRGBColorStroke(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue());
            cb.setLineWidth(outlineSize);
            cb.setLineJoin(PdfContentByte.LINE_JOIN_ROUND);
            cb.setLineCap(PdfContentByte.LINE_CAP_ROUND);
            if (outlineColor.getAlpha() < 255) {
                cb.saveState();
                float alpha = outlineColor.getAlpha() / 255f;
                PdfGState gState = new PdfGState();
                gState.setStrokeOpacity(alpha);
                cb.setGState(gState);
            }
            cb.beginText();
            cb.setFontAndSize(bf, font.getSize());
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label, x, -y - (textHeight / 2f), 0f);
            cb.endText();
            if (outlineColor.getAlpha() < 255) {
                cb.restoreState();
            }
        }
        cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
        cb.beginText();
        cb.setFontAndSize(bf, font.getSize());
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label, x, -y - (textHeight / 2f), 0f);
        cb.endText();
    }

    private float getTextHeight(BaseFont baseFont, float fontSize, String text) {
        float ascend = baseFont.getAscentPoint(text, fontSize);
        float descend = baseFont.getDescentPoint(text, fontSize);
        return ascend + descend;
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.SHOW_EDGE_LABELS, Boolean.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.display.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.display.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS).setValue(defaultShowLabels),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_FONT, Font.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.font.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.font.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultFont),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_COLOR, DependantOriginalColor.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.color.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.color.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultColor),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_SHORTEN, Boolean.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.shorten.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.shorten.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultShorten),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_MAX_CHAR, Integer.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.maxchar.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.maxchar.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultMaxChar),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_OUTLINE_SIZE, Float.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineSize.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineSize.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultOutlineSize),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_OUTLINE_COLOR, DependantColor.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineColor.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineColor.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultOutlineColor),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_LABEL_OUTLINE_OPACITY, Float.class,
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineOpacity.displayName"),
                    NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.property.outlineOpacity.description"),
                    PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(defaultOutlineOpacity),};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof EdgeLabelItem && properties.getBooleanValue(PreviewProperty.SHOW_EDGE_LABELS)
                && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    protected PVector bezierPoint(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float c) {
        PVector ab = linearInterpolation(x1, y1, x2, y2, c);
        PVector bc = linearInterpolation(x2, y2, x3, y3, c);
        PVector cd = linearInterpolation(x3, y3, x4, y4, c);
        PVector abbc = linearInterpolation(ab.x, ab.y, bc.x, bc.y, c);
        PVector bccd = linearInterpolation(bc.x, bc.y, cd.x, cd.y, c);
        return linearInterpolation(abbc.x, abbc.y, bccd.x, bccd.y, c);
    }

    protected PVector linearInterpolation(float x1, float y1, float x2, float y2, float c) {
        PVector r = new PVector(x1 + (x2 - x1) * c, y1 + (y2 - y1) * c);
        return r;
    }
}
