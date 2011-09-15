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
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import processing.core.PGraphics;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
@ServiceProvider(service = Renderer.class, position = 300)
public class NodeRenderer implements Renderer {

    //Default values
    protected float defaultBorderWidth = 1f;
    protected DependantColor defaultBorderColor = new DependantColor(Color.BLACK);
    protected float defaultOpacity = 100f;

    public void preProcess(PreviewModel previewModel) {
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (target instanceof ProcessingTarget) {
            renderProcessing(item, (ProcessingTarget) target, properties);
        } else if (target instanceof SVGTarget) {
            renderSVG(item, (SVGTarget) target, properties);
        } else if (target instanceof PDFTarget) {
            renderPDF(item, (PDFTarget) target, properties);
        }
    }

    public void renderProcessing(Item item, ProcessingTarget target, PreviewProperties properties) {
        //Params
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        int alpha = (int) ((properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f) * 255f);
        if (alpha > 255) {
            alpha = 255;
        }

        //Graphics
        PGraphics graphics = target.getGraphics();

//        x = x - size;
//        y = y - size;
        if (borderSize > 0) {
            graphics.stroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha);
            graphics.strokeWeight(borderSize);
        } else {
            graphics.noStroke();
        }
        graphics.fill(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        graphics.ellipse(x, y, size, size);
    }

    public void renderSVG(Item item, SVGTarget target, PreviewProperties properties) {
        Node node = (Node) item.getSource();
        //Params
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        size /= 2f;
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        float alpha = properties.getIntValue(PreviewProperty.NODE_OPACITY) / 100f;
        if (alpha > 1) {
            alpha = 1;
        }

        Element nodeElem = target.createElement("circle");
        nodeElem.setAttribute("class", node.getNodeData().getId());
        nodeElem.setAttribute("cx", x.toString());
        nodeElem.setAttribute("cy", y.toString());
        nodeElem.setAttribute("r", size.toString());
        nodeElem.setAttribute("fill", target.toHexString(color));
        nodeElem.setAttribute("fill-opacity", "" + alpha);
        if (borderSize > 0) {
            nodeElem.setAttribute("stroke", target.toHexString(borderColor));
            nodeElem.setAttribute("stroke-width", new Float(borderSize * target.getScaleRatio()).toString());
            nodeElem.setAttribute("stroke-opacity", "" + alpha);
        }
        target.getTopElement(SVGTarget.TOP_NODES).appendChild(nodeElem);
    }

    public void renderPDF(Item item, PDFTarget target, PreviewProperties properties) {
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        size /= 2f;
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        float alpha = properties.getIntValue(PreviewProperty.NODE_OPACITY) / 100f;

        PdfContentByte cb = target.getContentByte();
        cb.setRGBColorStroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());
        cb.setLineWidth(borderSize);
        cb.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
        if (alpha < 1f) {
            cb.saveState();
            PdfGState gState = new PdfGState();
            gState.setFillOpacity(alpha);
            gState.setStrokeOpacity(alpha);
            cb.setGState(gState);
        }
        cb.circle(x, -y, size);
        cb.fillStroke();
        if (alpha < 1f) {
            cb.restoreState();
        }
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_BORDER_WIDTH, Float.class,
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderWidth.displayName"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderWidth.description"),
                    PreviewProperty.CATEGORY_NODES).setValue(defaultBorderWidth),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_BORDER_COLOR, DependantColor.class,
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderColor.displayName"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderColor.description"),
                    PreviewProperty.CATEGORY_NODES).setValue(defaultBorderColor),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_OPACITY, Float.class,
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.opacity.displayName"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.opacity.description"),
                    PreviewProperty.CATEGORY_NODES).setValue(defaultOpacity)};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        if (item instanceof NodeItem) {
            return true;
        }
        return false;
    }
}
