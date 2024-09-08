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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.api.Vector;
import org.gephi.preview.plugin.builders.EdgeBuilder;
import org.gephi.preview.plugin.builders.EdgeLabelBuilder;
import org.gephi.preview.plugin.builders.NodeBuilder;
import org.gephi.preview.plugin.items.EdgeLabelItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
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
    protected final DependantOriginalColor defaultColor =
        new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL);
    protected final int defaultMaxChar = 30;
    protected final float defaultOutlineSize = 2;
    protected final DependantColor defaultOutlineColor = new DependantColor(Color.WHITE);
    protected final float defaultOutlineOpacity = 40;
    //Font cache
    protected Font font;

    @Override
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

        //Put nodes in edge item (in case it was not done yet by EdgeRenderer)
        EdgeRenderer.putNodesInEdgeItems(previewModel, previewModel.getItems(Item.EDGE));

        //Put parent color, and calculate position
        for (Item item : previewModel.getItems(Item.EDGE_LABEL)) {
            Edge edge = (Edge) item.getSource();
            Item edgeItem = previewModel.getItem(Item.EDGE, edge);

            NodeItem sourceItem = edgeItem.getData(EdgeRenderer.SOURCE);
            NodeItem targetItem = edgeItem.getData(EdgeRenderer.TARGET);

            item.setData(EDGE_COLOR, EdgeRenderer.getColor(edgeItem, properties));
            if (edge.isSelfLoop()) {
                //Middle
                Float x = sourceItem.getData(NodeItem.X);
                Float y = sourceItem.getData(NodeItem.Y);
                Float size = sourceItem.getData(NodeItem.SIZE);

                Vector v1 = new Vector(x, y);
                v1.add(size, -size);

                Vector v2 = new Vector(x, y);
                v2.add(size, size);

                Vector middle = bezierPoint(x, y, v1.x, v1.y, v2.x, v2.y, x, y, 0.5f);
                item.setData(LABEL_X, middle.x);
                item.setData(LABEL_Y, middle.y);

            } else if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
                //Middle of the curve
                Float x1 = sourceItem.getData(NodeItem.X);
                Float x2 = targetItem.getData(NodeItem.X);
                Float y1 = sourceItem.getData(NodeItem.Y);
                Float y2 = targetItem.getData(NodeItem.Y);

                //Curved edges
                final Vector direction = new Vector(x2, y2);
                direction.sub(new Vector(x1, y1));
                final float length = direction.mag();
                // Arc radius
                double r = length / properties.getDoubleValue(EdgeRenderer.ARC_CURVENESS);
                // Arc bounding box
                Double _xa = 0.5*(x1-x2);
                Double _ya = 0.5*(y1-y2);
                Double _x0 = x2+_xa;
                Double _y0 = y2+_ya;
                Double _a = Math.sqrt(Math.pow(_xa, 2) + Math.pow(_ya, 2));
                Double _b = 0.;
                if (_a < r) {
                    _b = Math.sqrt(Math.pow(r, 2) - Math.pow(_a, 2));
                }
                Double xc = _x0 + (_b * _ya) / _a;
                Double yc = _y0 - (_b * _xa / _a);
                Double angle1 = Math.atan2(y1-yc, x1-xc);
                Double angle2 = Math.atan2(y2-yc, x2-xc);
                while (angle2<angle1) {
                    angle2 += 2*Math.PI;
                }
                double arcAngle = Math.abs(angle2 - angle1);
                while (arcAngle >= Math.PI) {
                    arcAngle -= Math.PI;
                }
                // Target radius - to start at the base of the arrow
                final Float targetRadius = edgeItem.getData(EdgeRenderer.TARGET_RADIUS);
                // Offset due to the source node
                if (targetRadius != null && targetRadius < 0) {
                    Double targetOffset = computeTruncateAngle(r, (double) targetRadius, (double) arcAngle);
                    angle2 += targetOffset;
                }
                // Source radius
                final Float sourceRadius = edgeItem.getData(EdgeRenderer.SOURCE_RADIUS);
                // Avoid edge from passing the node's center:
                if (sourceRadius != null && sourceRadius < 0) {
                    Double sourceOffset = computeTruncateAngle(r, (double) targetRadius, (double) arcAngle);
                    angle1 -= sourceOffset;
                }
                // Label coordinates
                final Double lAngle = (angle1+angle2)/2;
                final Float x = length != 0 ? (float)(xc + r*Math.cos(lAngle)) : x1;
                final Float y = length != 0 ? (float)(yc + r*Math.sin(lAngle)) : y1;
                item.setData(LABEL_X, x);
                item.setData(LABEL_Y, y);

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

    @Override
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

        //Skip if empty
        if (label == null || label.trim().isEmpty()) {
            return;
        }

        //Outline
        DependantColor outlineDependantColor = properties.getValue(PreviewProperty.EDGE_LABEL_OUTLINE_COLOR);
        Float outlineSize = properties.getFloatValue(PreviewProperty.EDGE_LABEL_OUTLINE_SIZE);
        outlineSize = outlineSize * (font.getSize() / 32f);
        int outlineAlpha = (int) ((properties.getFloatValue(PreviewProperty.EDGE_LABEL_OUTLINE_OPACITY) / 100f) * 255f);
        if (outlineAlpha < 0) {
            outlineAlpha = 0;
        }
        if (outlineAlpha > 255) {
            outlineAlpha = 255;
        }
        Color outlineColor = outlineDependantColor.getColor(edgeColor);
        outlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), outlineAlpha);

        if (target instanceof G2DTarget) {
            renderG2D((G2DTarget) target, label, x, y, color, outlineSize, outlineColor);
        } else if (target instanceof SVGTarget) {
            renderSVG((SVGTarget) target, edge, label, x, y, color, outlineSize, outlineColor);
        } else if (target instanceof PDFTarget) {
            renderPDF(((PDFTarget) target), label, x, y, color, outlineSize, outlineColor);
        }
    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget renderTarget, PreviewProperties properties) {
    }

    @Override
    public CanvasSize getCanvasSize(
        final Item item,
        final PreviewProperties properties) {
        //FIXME Compute the label canvas
        return new CanvasSize();
    }

    public void renderG2D(G2DTarget target, String label, float x, float y, Color color, float outlineSize,
                          Color outlineColor) {
        Graphics2D graphics = target.getGraphics();

        graphics.setFont(font);

        FontMetrics fm = graphics.getFontMetrics();
        float posX = x - fm.stringWidth(label) / 2f;
        float posY = y + fm.getAscent() / 2f;

        Shape outlineGlyph = null;

        if (outlineSize > 0) {
            FontRenderContext frc = graphics.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, label);
            outlineGlyph = gv.getOutline(posX, posY);
            graphics.setColor(outlineColor);
            graphics.setStroke(new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(outlineGlyph);
        }

        graphics.setColor(color);
        if (null == outlineGlyph) {
            graphics.drawString(label, posX, posY);
        } else {
            graphics.fill(outlineGlyph);
        }
    }

    private Double computeTruncateAngle(Double radius_curvature_edge, Double truncature_length, Double arc_angle) {
        // The edge is an arc of a circle.
        // We want to truncate that arc so that truncated part has a chord of a given length.
        // i.e. not the length along the arc, but as a straight segment (like the string of a bow)
        // We give back the result as an angle, as it's how it's useful to us.
        Double rt = truncature_length;
        Double r = radius_curvature_edge;
        Double s = r * arc_angle;
        if (s <= -rt) {
            // Can't truncate more than the arc length
            // Return 0 so the node's center is used to determine
            // where to draw the label
            return 0.;
        }
        // If you take a sector from a circle with radius r, and chord length |rt|,
        // x is the length bisecting the two radii.
        double x = Math.sqrt(Math.pow(r, 2) - Math.pow(rt / 2, 2));
        return 2 * Math.atan2(rt / 2, x);
    }

    public void renderSVG(SVGTarget target, Edge edge, String label, float x, float y, Color color, float outlineSize,
                          Color outlineColor) {
        Text labelText = target.createTextNode(label);

        if (outlineSize > 0) {
            Text labelTextOutline = target.createTextNode(label);
            Element outlineElem = target.createElement("text");
            outlineElem.setAttribute("class", SVGUtils.idAsClassAttribute(edge.getId()));
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
        labelElem.setAttribute("class", SVGUtils.idAsClassAttribute(edge.getId()));
        labelElem.setAttribute("x", x + "");
        labelElem.setAttribute("y", y + "");
        labelElem.setAttribute("style", "text-anchor: middle; dominant-baseline: central;");
        labelElem.setAttribute("fill", target.toHexString(color));
        labelElem.setAttribute("font-family", font.getFamily());
        labelElem.setAttribute("font-size", font.getSize() + "");
        labelElem.appendChild(labelText);
        target.getTopElement(SVGTarget.TOP_EDGE_LABELS).appendChild(labelElem);
    }

    public void renderPDF(PDFTarget target, String label, float x, float y, Color color, float outlineSize,
                          Color outlineColor) {
        PDPageContentStream contentStream = target.getContentStream();
        PDFont pdFont = target.getPDFont(font);
        int fontSize = font.getSize();

        try {
            float textHeight = PDFUtils.getTextHeight(pdFont, fontSize);
            float textWidth = PDFUtils.getTextWidth(pdFont, fontSize, label);

            if (outlineSize > 0) {
                contentStream.setRenderingMode(RenderingMode.STROKE);
                contentStream.setStrokingColor(outlineColor);
                contentStream.setLineWidth(outlineSize);
                contentStream.setLineJoinStyle(1); //round
                contentStream.setLineCapStyle(1); //round
                if (outlineColor.getAlpha() < 255) {
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setStrokingAlphaConstant(outlineColor.getAlpha() / 255f);
                    contentStream.saveGraphicsState();
                    contentStream.setGraphicsStateParameters(graphicsState);
                }
                contentStream.beginText();
                contentStream.setFont(pdFont, fontSize);
                contentStream.newLineAtOffset(x - (textWidth / 2f), -y - (textHeight / 2f));
                contentStream.showText(label);
                contentStream.endText();
                if (outlineColor.getAlpha() < 255) {
                    contentStream.restoreGraphicsState();
                }
            }

            if (color.getAlpha() < 255) {
                PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                graphicsState.setNonStrokingAlphaConstant(color.getAlpha() / 255f);
                contentStream.saveGraphicsState();
                contentStream.setGraphicsStateParameters(graphicsState);
            }
            contentStream.beginText();
            contentStream.setFont(pdFont, fontSize);
            contentStream.setNonStrokingColor(color);
            contentStream.setRenderingMode(RenderingMode.FILL);
            contentStream.newLineAtOffset(x - (textWidth / 2f), -y - (textHeight / 2f));
            contentStream.showText(label);
            contentStream.endText();
            if (color.getAlpha() < 255) {
                contentStream.restoreGraphicsState();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[] {
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
                PreviewProperty.CATEGORY_EDGE_LABELS, PreviewProperty.SHOW_EDGE_LABELS).setValue(
                defaultOutlineOpacity),};
    }

    private boolean showEdgeLabels(PreviewProperties properties) {
        return properties.getBooleanValue(PreviewProperty.SHOW_EDGE_LABELS)
            && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof EdgeLabelItem && showEdgeLabels(properties);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof EdgeLabelBuilder || itemBuilder instanceof NodeBuilder ||
            itemBuilder instanceof EdgeBuilder) && showEdgeLabels(properties);//Needs some properties of nodes and edges
    }

    protected Vector bezierPoint(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                 float c) {
        Vector ab = linearInterpolation(x1, y1, x2, y2, c);
        Vector bc = linearInterpolation(x2, y2, x3, y3, c);
        Vector cd = linearInterpolation(x3, y3, x4, y4, c);
        Vector abbc = linearInterpolation(ab.x, ab.y, bc.x, bc.y, c);
        Vector bccd = linearInterpolation(bc.x, bc.y, cd.x, cd.y, c);
        return linearInterpolation(abbc.x, abbc.y, bccd.x, bccd.y, c);
    }

    protected Vector linearInterpolation(float x1, float y1, float x2, float y2, float c) {
        Vector r = new Vector(x1 + (x2 - x1) * c, y1 + (y2 - y1) * c);
        return r;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EdgeLabelRenderer.class, "EdgeLabelRenderer.name");
    }
}
