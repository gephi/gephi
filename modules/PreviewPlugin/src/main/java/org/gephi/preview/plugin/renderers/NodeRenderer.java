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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.*;
import org.gephi.preview.plugin.builders.NodeBuilder;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

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

    @Override
    public void preProcess(PreviewModel previewModel) {
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (target instanceof G2DTarget) {
            renderG2D(item, (G2DTarget) target, properties);
        } else if (target instanceof SVGTarget) {
            renderSVG(item, (SVGTarget) target, properties);
        } else if (target instanceof PDFTarget) {
            renderPDF(item, (PDFTarget) target, properties);
        }
    }

    @Override
    public CanvasSize getCanvasSize(
            final Item item,
            final PreviewProperties properties) {
        final float x = item.getData(NodeItem.X);
        final float y = item.getData(NodeItem.Y);
        final float s = (Float) item.getData(NodeItem.SIZE)
                + properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        final float r = s / 2F;
        final int intS = Math.round(s);
        return new CanvasSize(
                Math.round(x - r),
                Math.round(y - r),
                intS,
                intS);
    }

    public void renderG2D(Item item, G2DTarget target, PreviewProperties properties) {
        //Params
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        int alpha = (int) ((properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f) * 255f);
        if (alpha < 0) {
            alpha = 0;
        }
        if (alpha > 255) {
            alpha = 255;
        }

        //Graphics
        Graphics2D graphics = target.getGraphics();

        x = x - (size / 2f);
        y = y - (size / 2f);
        Ellipse2D.Float ellipse = new Ellipse2D.Float(x, y, size, size);
        if (borderSize > 0) {
            graphics.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha));
            graphics.setStroke(new BasicStroke(borderSize));
            graphics.draw(ellipse);
        }

        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        graphics.fill(ellipse);
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
        float alpha = properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f;
        if (alpha > 1) {
            alpha = 1;
        }

        Element nodeElem = target.createElement("circle");
        nodeElem.setAttribute("class", node.getId().toString());
        nodeElem.setAttribute("cx", x.toString());
        nodeElem.setAttribute("cy", y.toString());
        nodeElem.setAttribute("r", size.toString());
        nodeElem.setAttribute("fill", target.toHexString(color));
        nodeElem.setAttribute("fill-opacity", "" + alpha);
        if (borderSize > 0) {
            nodeElem.setAttribute("stroke", target.toHexString(borderColor));
            nodeElem.setAttribute(
                    "stroke-width",
                    Float.toString(borderSize * target.getScaleRatio()));
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
        float alpha = properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f;

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
        if (borderSize > 0) {
            cb.fillStroke();
        } else {
            cb.fill();
        }
        if (alpha < 1f) {
            cb.restoreState();
        }
    }

    @Override
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

    private boolean showNodes(PreviewProperties properties) {
        return properties.getFloatValue(PreviewProperty.NODE_OPACITY) > 0;
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof NodeItem && showNodes(properties);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return itemBuilder instanceof NodeBuilder && showNodes(properties);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.name");
    }
}
