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

import java.awt.Color;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
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
    private float defaultBorderWidth = 1;
    private DependantColor defaultBorderColor = new DependantColor();

    public void preProcess(PreviewModel previewModel) {
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (target instanceof ProcessingTarget) {
            renderProcessing(item, (ProcessingTarget) target, properties);
        } else if (target instanceof SVGTarget) {
            renderSVG(item, (SVGTarget) target, properties);
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

        //Graphics
        PGraphics graphics = target.getGraphics();

//        x = x - size;
//        y = y - size;
        graphics.stroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha());
        graphics.strokeWeight(borderSize);
        graphics.fill(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        graphics.ellipse(x, y, size, size);
    }

    public void renderSVG(Item item, SVGTarget target, PreviewProperties properties) {
        Node node = (Node) item.getSource();
        //Params
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);

        Element nodeElem = target.createElement("circle");
        nodeElem.setAttribute("class", node.getNodeData().getId());
        nodeElem.setAttribute("cx", x.toString());
        nodeElem.setAttribute("cy", y.toString());
        nodeElem.setAttribute("r", size.toString());
        nodeElem.setAttribute("fill", target.toHexString(color));
        nodeElem.setAttribute("stroke", target.toHexString(borderColor));
        nodeElem.setAttribute("stroke-width", new Float(borderSize * target.getScaleRatio()).toString());
        target.getTopElement(SVGTarget.TOP_NODES).appendChild(nodeElem);
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_BORDER_WIDTH, Float.class,
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderWidth.displayName"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderWidth.description"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.category")).setValue(defaultBorderWidth),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_BORDER_COLOR, DependantColor.class,
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderColor.displayName"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.borderColor.description"),
                    NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.category")).setValue(defaultBorderColor)};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        if (item instanceof NodeItem) {
            return true;
        }
        return false;
    }
}
