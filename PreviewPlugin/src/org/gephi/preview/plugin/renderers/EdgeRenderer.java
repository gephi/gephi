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

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import java.util.Locale;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
@ServiceProvider(service = Renderer.class, position = 100)
public class EdgeRenderer implements Renderer {

    //Custom properties
    public static final String EDGE_MIN_WEIGHT = "edge.min-weight";
    public static final String EDGE_MAX_WEIGHT = "edge.max-weight";
    public static final String BEZIER_CURVENESS = "edge.bezier-curveness";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String TARGET_RADIUS = "edge.target.radius";
    public static final String SOURCE_RADIUS = "edge.source.radius";
    //Default values
    protected boolean defaultShowEdges = true;
    protected float defaultThickness = 1;
    protected boolean defaultRescaleWeight = true;
    protected EdgeColor defaultColor = new EdgeColor(EdgeColor.Mode.MIXED);
    protected boolean defaultEdgeCurved = true;
    protected float defaultBezierCurviness = 0.2f;
    protected int defaultOpacity = 100;
    protected float defaultRadius = 0f;

    public void preProcess(PreviewModel previewModel) {
        PreviewProperties properties = previewModel.getProperties();
        Item[] edgeItems = previewModel.getItems(Item.EDGE);

        //Put nodes in edge item
        for (Item item : edgeItems) {
            Edge edge = (Edge) item.getSource();
            Node source = edge.getSource().getNodeData().getRootNode();
            Node target = edge.getTarget().getNodeData().getRootNode();
            Item nodeSource = previewModel.getItem(Item.NODE, source);
            Item nodeTarget = previewModel.getItem(Item.NODE, target);
            item.setData(SOURCE, nodeSource);
            item.setData(TARGET, nodeTarget);
        }

        //Calculate max and min weight
        float minWeight = Float.POSITIVE_INFINITY;
        float maxWeight = Float.NEGATIVE_INFINITY;

        for (Item edge : edgeItems) {
            minWeight = Math.min(minWeight, (Float) edge.getData(EdgeItem.WEIGHT));
            maxWeight = Math.max(maxWeight, (Float) edge.getData(EdgeItem.WEIGHT));
        }
        properties.putValue(EDGE_MIN_WEIGHT, minWeight);
        properties.putValue(EDGE_MAX_WEIGHT, maxWeight);

        //Put bezier curveness in properties
        if (!properties.hasProperty(BEZIER_CURVENESS)) {
            properties.putValue(BEZIER_CURVENESS, defaultBezierCurviness);
        }

        //Rescale weight if necessary - and avoid negative weights
        boolean rescaleWeight = properties.getBooleanValue(PreviewProperty.EDGE_RESCALE_WEIGHT);
        for (Item item : edgeItems) {
            float weight = (Float) item.getData(EdgeItem.WEIGHT);

            //Rescale weight
            if (rescaleWeight) {
                if (!Double.isInfinite(minWeight) && !Double.isInfinite(maxWeight) && maxWeight != minWeight) {
                    float ratio = 1f / (maxWeight - minWeight);
                    weight = (weight - minWeight) * ratio;
                }
            } else if (minWeight <= 0) {
                //Avoid negative weight
                weight += Math.abs(minWeight) + 1;
            }
            //Multiply by thickness
            weight *= properties.getFloatValue(PreviewProperty.EDGE_THICKNESS);
            item.setData(EdgeItem.WEIGHT, weight);
        }

        //Radius
        for (Item item : edgeItems) {
            if (!(Boolean) item.getData(EdgeItem.SELF_LOOP)) {
                float edgeRadius = properties.getFloatValue(PreviewProperty.EDGE_RADIUS);
                float targetRadius = 0;
                if ((Boolean) item.getData(EdgeItem.DIRECTED) || edgeRadius > 0f) {
                    //Target
                    Item targetItem = (Item) item.getData(TARGET);
                    Float weight = item.getData(EdgeItem.WEIGHT);
                    //Avoid negative arrow size:
                    float arrowSize = properties.getFloatValue(PreviewProperty.ARROW_SIZE);
                    if (arrowSize < 0) {
                        arrowSize = 0;
                    }
                    float size = arrowSize * weight;
                    targetRadius = -(edgeRadius + (Float) targetItem.getData(NodeItem.SIZE) / 2f + properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH));
                    item.setData(TARGET_RADIUS, targetRadius - size);
                }
                if (edgeRadius > 0) {
                    //Source
                    Item sourceItem = (Item) item.getData(SOURCE);
                    float sourceRadius = -(edgeRadius + (Float) sourceItem.getData(NodeItem.SIZE) / 2f + properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH));
                    item.setData(SOURCE_RADIUS, sourceRadius);
                }
            }
        }
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        //Get nodes
        Item sourceItem = item.getData(SOURCE);
        Item targetItem = item.getData(TARGET);

        //Weight and color
        Float weight = item.getData(EdgeItem.WEIGHT);
        EdgeColor edgeColor = (EdgeColor) properties.getValue(PreviewProperty.EDGE_COLOR);
        Color color = edgeColor.getColor((Color) item.getData(EdgeItem.COLOR),
                (Color) sourceItem.getData(NodeItem.COLOR),
                (Color) targetItem.getData(NodeItem.COLOR));
        int alpha = (int) ((properties.getIntValue(PreviewProperty.EDGE_OPACITY) / 100f) * 255f);
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        if (sourceItem == targetItem) {
            renderSelfLoop(sourceItem, weight, color, properties, target);
        } else if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
            renderCurvedEdge(item, sourceItem, targetItem, weight, color, properties, target);
        } else {
            renderStraightEdge(item, sourceItem, targetItem, weight, color, properties, target);
        }
    }

    public void renderSelfLoop(Item nodeItem, float thickness, Color color, PreviewProperties properties, RenderTarget renderTarget) {
        Float x = nodeItem.getData(NodeItem.X);
        Float y = nodeItem.getData(NodeItem.Y);
        Float size = nodeItem.getData(NodeItem.SIZE);
        Node node = (Node) nodeItem.getSource();

        PVector v1 = new PVector(x, y);
        v1.add(size, -size, 0);

        PVector v2 = new PVector(x, y);
        v2.add(size, size, 0);

        if (renderTarget instanceof ProcessingTarget) {
            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();
            graphics.strokeWeight(thickness);
            graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            graphics.noFill();
            graphics.bezier(x, y, v1.x, v1.y, v1.x, v2.y, x, y);
        } else if (renderTarget instanceof SVGTarget) {
            SVGTarget svgTarget = (SVGTarget) renderTarget;

            Element selfLoopElem = svgTarget.createElement("path");
            selfLoopElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f C %f,%f %f,%f %f,%f",
                    x, y, v1.x, v1.y, v2.x, v2.y, x, y));
            selfLoopElem.setAttribute("class", node.getNodeData().getId());
            selfLoopElem.setAttribute("stroke", svgTarget.toHexString(color));
            selfLoopElem.setAttribute("stroke-opacity", (color.getAlpha() / 255f) + "");
            selfLoopElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
            selfLoopElem.setAttribute("fill", "none");
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(selfLoopElem);
        } else if (renderTarget instanceof PDFTarget) {
            PDFTarget pdfTarget = (PDFTarget) renderTarget;
            PdfContentByte cb = pdfTarget.getContentByte();
            cb.moveTo(x, -y);
            cb.curveTo(v1.x, -v1.y, v2.x, -v2.y, x, -y);
            cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
            cb.setLineWidth(thickness);
            if (color.getAlpha() < 255) {
                cb.saveState();
                float alpha = color.getAlpha() / 255f;
                PdfGState gState = new PdfGState();
                gState.setStrokeOpacity(alpha);
                cb.setGState(gState);
            }
            cb.stroke();
            if (color.getAlpha() < 255) {
                cb.restoreState();
            }
        }
    }

    public void renderCurvedEdge(Item edgeItem, Item sourceItem, Item targetItem, float thickness, Color color, PreviewProperties properties, RenderTarget renderTarget) {
        Edge edge = (Edge) edgeItem.getSource();
        Float x1 = sourceItem.getData(NodeItem.X);
        Float x2 = targetItem.getData(NodeItem.X);
        Float y1 = sourceItem.getData(NodeItem.Y);
        Float y2 = targetItem.getData(NodeItem.Y);

        //Curved edgs
        PVector direction = new PVector(x2, y2);
        direction.sub(new PVector(x1, y1));
        float length = direction.mag();
        direction.normalize();

        float factor = properties.getFloatValue(BEZIER_CURVENESS) * length;

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

        if (renderTarget instanceof ProcessingTarget) {
            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();
            graphics.strokeWeight(thickness);
            graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            graphics.noFill();
            graphics.bezier(x1, y1, v1.x, v1.y, v2.x, v2.y, x2, y2);
        } else if (renderTarget instanceof SVGTarget) {
            SVGTarget svgTarget = (SVGTarget) renderTarget;
            Element edgeElem = svgTarget.createElement("path");
            edgeElem.setAttribute("class", edge.getSource().getNodeData().getId() + " " + edge.getTarget().getNodeData().getId());
            edgeElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f C %f,%f %f,%f %f,%f",
                    x1, y1, v1.x, v1.y, v2.x, v2.y, x2, y2));
            edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
            edgeElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
            edgeElem.setAttribute("stroke-opacity", (color.getAlpha() / 255f) + "");
            edgeElem.setAttribute("fill", "none");
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);
        } else if (renderTarget instanceof PDFTarget) {
            PDFTarget pdfTarget = (PDFTarget) renderTarget;
            PdfContentByte cb = pdfTarget.getContentByte();
            cb.moveTo(x1, -y1);
            cb.curveTo(v1.x, -v1.y, v2.x, -v2.y, x2, -y2);
            cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
            cb.setLineWidth(thickness);
            if (color.getAlpha() < 255) {
                cb.saveState();
                float alpha = color.getAlpha() / 255f;
                PdfGState gState = new PdfGState();
                gState.setStrokeOpacity(alpha);
                cb.setGState(gState);
            }
            cb.stroke();
            if (color.getAlpha() < 255) {
                cb.restoreState();
            }
        }
    }

    public void renderStraightEdge(Item edgeItem, Item sourceItem, Item targetItem, float thickness, Color color, PreviewProperties properties, RenderTarget renderTarget) {
        Edge edge = (Edge) edgeItem.getSource();
        Float x1 = sourceItem.getData(NodeItem.X);
        Float x2 = targetItem.getData(NodeItem.X);
        Float y1 = sourceItem.getData(NodeItem.Y);
        Float y2 = targetItem.getData(NodeItem.Y);

        //Target radius - to start at the base of the arrow
        Float targetRadius = edgeItem.getData(TARGET_RADIUS);
        //Avoid edge from passing the node's center:
        if (targetRadius != null && targetRadius < 0) {
            PVector direction = new PVector(x2, y2);
            direction.sub(new PVector(x1, y1));
            direction.normalize();
            direction = new PVector(direction.x, direction.y);
            direction.mult(targetRadius);
            direction.add(new PVector(x2, y2));
            x2 = direction.x;
            y2 = direction.y;
        }
        //Source radius
        Float sourceRadius = edgeItem.getData(SOURCE_RADIUS);
        //Avoid edge from passing the node's center:
        if (sourceRadius != null && sourceRadius < 0) {
            PVector direction = new PVector(x1, y1);
            direction.sub(new PVector(x2, y2));
            direction.normalize();
            direction = new PVector(direction.x, direction.y);
            direction.mult(sourceRadius);
            direction.add(new PVector(x1, y1));
            x1 = direction.x;
            y1 = direction.y;
        }

        if (renderTarget instanceof ProcessingTarget) {
            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();
            graphics.strokeWeight(thickness);
            graphics.strokeCap(PGraphics.SQUARE);
            graphics.stroke(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            graphics.noFill();
            graphics.line(x1, y1, x2, y2);
        } else if (renderTarget instanceof SVGTarget) {
            SVGTarget svgTarget = (SVGTarget) renderTarget;
            Element edgeElem = svgTarget.createElement("path");
            edgeElem.setAttribute("class", edge.getSource().getNodeData().getId() + " " + edge.getTarget().getNodeData().getId());
            edgeElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f L %f,%f",
                    x1, y1, x2, y2));
            edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
            edgeElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
            edgeElem.setAttribute("stroke-opacity", (color.getAlpha() / 255f) + "");
            edgeElem.setAttribute("fill", "none");
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);
        } else if (renderTarget instanceof PDFTarget) {
            PDFTarget pdfTarget = (PDFTarget) renderTarget;
            PdfContentByte cb = pdfTarget.getContentByte();
            cb.moveTo(x1, -y1);
            cb.lineTo(x2, -y2);
            cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
            cb.setLineWidth(thickness);
            if (color.getAlpha() < 255) {
                cb.saveState();
                float alpha = color.getAlpha() / 255f;
                PdfGState gState = new PdfGState();
                gState.setStrokeOpacity(alpha);
                cb.setGState(gState);
            }
            cb.stroke();
            if (color.getAlpha() < 255) {
                cb.restoreState();
            }
        }
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.SHOW_EDGES, Boolean.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.display.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.display.description"),
                    PreviewProperty.CATEGORY_EDGES).setValue(defaultShowEdges),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_THICKNESS, Float.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.thickness.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.thickness.description"),
                    PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultThickness),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.description"),
                    PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultRescaleWeight),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_COLOR, EdgeColor.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.color.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.color.description"),
                    PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultColor),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_OPACITY, Float.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.opacity.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.opacity.description"),
                    PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultOpacity),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_CURVED, Boolean.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.curvedEdges.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.curvedEdges.description"),
                    PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultEdgeCurved),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_RADIUS, Float.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.radius.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.radius.description"),
                    PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultRadius),};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        if (item instanceof EdgeItem && properties.getBooleanValue(PreviewProperty.SHOW_EDGES)
                && !properties.getBooleanValue(PreviewProperty.MOVING)) {
            return true;
        }
        return false;
    }
}
