/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.preview.plugin.renderers;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Color;
import java.util.Locale;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.api.PDFTarget;
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
@ServiceProvider(service = Renderer.class, position = 200)
public class ArrowRenderer implements Renderer {

    //Const
    protected final float BASE_RATIO = 0.5f;
    //Default values
    protected float defaultArrowSize = 3f;

    public void preProcess(PreviewModel previewModel) {
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        float size = properties.getFloatValue(PreviewProperty.ARROW_SIZE);
        if (size > 0) {
            //Get nodes
            Item sourceItem = item.getData(EdgeRenderer.SOURCE);
            Item targetItem = item.getData(EdgeRenderer.TARGET);

            //Weight and color
            Float weight = item.getData(EdgeItem.WEIGHT);
            EdgeColor edgeColor = (EdgeColor) properties.getValue(PreviewProperty.EDGE_COLOR);
            Color color = edgeColor.getColor((Color) item.getData(EdgeItem.COLOR),
                    (Color) sourceItem.getData(NodeItem.COLOR),
                    (Color) targetItem.getData(NodeItem.COLOR));
            int alpha = (int) ((properties.getFloatValue(PreviewProperty.EDGE_OPACITY) / 100f) * 255f);
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

            //Size and radius
            float radius = properties.getFloatValue(PreviewProperty.EDGE_RADIUS);

            size *= weight;
            radius = -(radius + (Float) targetItem.getData(NodeItem.SIZE) / 2f + properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH));

            //Avoid arrow from passing the node's center:
            if (radius > 0) {
                radius = 0;
            }
            
            //3 points
            Float x1 = sourceItem.getData(NodeItem.X);
            Float x2 = targetItem.getData(NodeItem.X);
            Float y1 = sourceItem.getData(NodeItem.Y);
            Float y2 = targetItem.getData(NodeItem.Y);

            if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
            } else {
                renderStraight(target, item, x1, y1, x2, y2, radius, size, color);
            }
        }
    }

    public void renderStraight(RenderTarget target, Item item, float x1, float y1, float x2, float y2, float radius, float size, Color color) {
        Edge edge = (Edge) item.getSource();
        PVector direction = new PVector(x2, y2);
        direction.sub(new PVector(x1, y1));
        direction.normalize();

        PVector p1 = new PVector(direction.x, direction.y);
        p1.mult(radius);
        p1.add(new PVector(x2, y2));

        PVector p1r = new PVector(direction.x, direction.y);
        p1r.mult(radius - size);
        p1r.add(new PVector(x2, y2));

        PVector p2 = new PVector(-direction.y, direction.x);
        p2.mult(size * BASE_RATIO);
        p2.add(p1r);

        PVector p3 = new PVector(direction.y, -direction.x);
        p3.mult(size * BASE_RATIO);
        p3.add(p1r);

        if (target instanceof ProcessingTarget) {
            PGraphics graphics = ((ProcessingTarget) target).getGraphics();
            graphics.noStroke();
            graphics.fill(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            graphics.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
        } else if (target instanceof SVGTarget) {
            SVGTarget svgTarget = (SVGTarget) target;
            Element arrowElem = svgTarget.createElement("polyline");
            arrowElem.setAttribute("points", String.format(Locale.ENGLISH, "%f,%f %f,%f %f,%f",
                    p1.x, p1.y, p2.x, p2.y, p3.x, p3.y));
            arrowElem.setAttribute("class", edge.getSource().getNodeData().getId() + " " + edge.getTarget().getNodeData().getId());
            arrowElem.setAttribute("fill", svgTarget.toHexString(color));
            arrowElem.setAttribute("fill-opacity", (color.getAlpha() / 255f) + "");
            arrowElem.setAttribute("stroke", "none");
            svgTarget.getTopElement(SVGTarget.TOP_ARROWS).appendChild(arrowElem);
        } else if (target instanceof PDFTarget) {
            PDFTarget pdfTarget = (PDFTarget) target;
            PdfContentByte cb = pdfTarget.getContentByte();
            cb.moveTo(p1.x, -p1.y);
            cb.lineTo(p2.x, -p2.y);
            cb.lineTo(p3.x, -p3.y);
            cb.closePath();
            cb.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
            if (color.getAlpha() < 255) {
                cb.saveState();
                float alpha = color.getAlpha() / 255f;
                PdfGState gState = new PdfGState();
                gState.setFillOpacity(alpha);
                cb.setGState(gState);
            }
            cb.fill();
            if (color.getAlpha() < 255) {
                cb.restoreState();
            }
        }
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.ARROW_SIZE, Float.class,
                    NbBundle.getMessage(EdgeRenderer.class, "ArrowRenderer.property.size.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "ArrowRenderer.property.size.description"),
                    PreviewProperty.CATEGORY_EDGE_ARROWS, PreviewProperty.SHOW_EDGES).setValue(defaultArrowSize)};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof EdgeItem && properties.getBooleanValue(PreviewProperty.DIRECTED)
                && (Boolean) item.getData(EdgeItem.DIRECTED) && !(Boolean) item.getData(EdgeItem.SELF_LOOP)
                && !properties.getBooleanValue(PreviewProperty.MOVING);
    }
}
