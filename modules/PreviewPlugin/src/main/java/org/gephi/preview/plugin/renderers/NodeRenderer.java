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
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.builders.NodeBuilder;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

/**
 * @author Yudi Xue, Mathieu Bastian
 */
@ServiceProvider(service = Renderer.class, position = 300)
public class NodeRenderer implements Renderer {

    //Default values
    protected float defaultBorderWidth = 1f;
    protected DependantColor defaultBorderColor = new DependantColor(Color.BLACK);
    protected float defaultOpacity = 100f;
    protected boolean defaultPerNodeOpacity = false;

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
    public void postProcess(PreviewModel previewModel, RenderTarget renderTarget, PreviewProperties properties) {
    }

    @Override
    public CanvasSize getCanvasSize(
        final Item item,
        final PreviewProperties properties) {
        final float x = item.getData(NodeItem.X);
        final float y = item.getData(NodeItem.Y);
        final float s = item.getData(NodeItem.SIZE);
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
        int alpha = properties.getBooleanValue(PreviewProperty.NODE_PER_NODE_OPACITY) ?
            color.getAlpha() :
            (int) ((properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f) * 255f);
        if (alpha < 0) {
            alpha = 0;
        }
        if (alpha > 255) {
            alpha = 255;
        }

        //Graphics
        Graphics2D graphics = target.getGraphics();

        //Border can't be larger than size
        borderSize = Math.min(borderSize, size / 2f);

        // Set size and pos
        size = size - borderSize;
        x = x - (size / 2f);
        y = y - (size / 2f);

        //Draw fill
        Ellipse2D.Float ellipse;
        if (alpha == 255) {
            // Allow the border and the fill to overlap a bit to avoid rendering artifacts
            ellipse = new Ellipse2D.Float(x + borderSize / 4f, y + borderSize / 4f, size - borderSize / 2f,
                size - borderSize / 2f);
        } else {
            // Special case making sure the border and the fill are not overlapping
            ellipse =
                new Ellipse2D.Float(x + borderSize / 2f, y + borderSize / 2f, size - borderSize, size - borderSize);
        }
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        graphics.fill(ellipse);

        if (borderSize > 0) {
            Ellipse2D.Float borderEllipse = new Ellipse2D.Float(x, y, size, size);
            graphics.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha));
            graphics.setStroke(new BasicStroke(borderSize));
            graphics.draw(borderEllipse);
        }
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
        float alpha = properties.getBooleanValue(PreviewProperty.NODE_PER_NODE_OPACITY) ?
            color.getAlpha() / 255f :
            properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f;
        if (alpha > 1) {
            alpha = 1;
        }

        // Border can't be larger than size
        borderSize = Math.min(borderSize, size / 2f);

        Element nodeElem = target.createElement("circle");
        Element nodeBorderElem = nodeElem;
        nodeElem.setAttribute("class", SVGUtils.idAsClassAttribute(node.getId()));
        nodeElem.setAttribute("cx", x.toString());
        nodeElem.setAttribute("cy", y.toString());
        nodeElem.setAttribute("fill", target.toHexString(color));
        nodeElem.setAttribute("fill-opacity", "" + alpha);

        if (borderSize > 0) {
            if (alpha < 1) {
                // Special case making sure the border and the fill are not overlapping
                nodeBorderElem = target.createElement("circle");
                nodeBorderElem.setAttribute("cx", x.toString());
                nodeBorderElem.setAttribute("cy", y.toString());
                nodeBorderElem.setAttribute("r", Float.toString((size / 2f) - borderSize / 2f));
                nodeBorderElem.setAttribute("fill", "none");

                nodeElem.setAttribute("r", Float.toString((size / 2f) - borderSize));
                target.getTopElement(SVGTarget.TOP_NODES).appendChild(nodeElem);
            } else {
                nodeElem.setAttribute("r", Float.toString((size - borderSize) / 2f));
            }
            nodeBorderElem.setAttribute("stroke", target.toHexString(borderColor));
            nodeBorderElem.setAttribute(
                "stroke-width",
                Float.toString(borderSize * target.getScaleRatio()));
            nodeBorderElem.setAttribute("stroke-opacity", "" + alpha);
        } else {
            nodeElem.setAttribute("r", Float.toString((size - borderSize) / 2f));
        }

        target.getTopElement(SVGTarget.TOP_NODES).appendChild(nodeBorderElem);
    }

    public void renderPDF(Item item, PDFTarget target, PreviewProperties properties) {
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        float alpha = properties.getBooleanValue(PreviewProperty.NODE_PER_NODE_OPACITY) ?
            color.getAlpha() / 255f :
            properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f;

        // Border can't be larger than size
        borderSize = Math.min(borderSize, size / 2f);

        PDPageContentStream cb = target.getContentByte();
        try {
            cb.setStrokingColor(borderColor);
            cb.setLineWidth(borderSize);
            cb.setNonStrokingColor(color);
            if (alpha < 1f) {
                PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                graphicsState.setStrokingAlphaConstant(alpha);
                graphicsState.setNonStrokingAlphaConstant(alpha);
                cb.saveGraphicsState();
                cb.setGraphicsStateParameters(graphicsState);
            }
            drawCircle(cb, x, -y, (size / 2f) - borderSize / 2f);
            if (borderSize > 0 && alpha == 1f) {
                cb.fillAndStroke();
            } else if (borderSize > 0 && alpha < 1f) {
                // Special case to make sure the border and the fill are not overlapping
                cb.stroke();
                drawCircle(cb, x, -y, (size / 2f) - borderSize);
                cb.fill();
            } else {
                cb.fill();
            }
            if (alpha < 1f) {
                cb.restoreGraphicsState();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

//        cb.setRGBColorStroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());
//        cb.setLineWidth(borderSize);
//        cb.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
//        if (alpha < 1f) {
//            cb.saveState();
//            PdfGState gState = new PdfGState();
//            gState.setFillOpacity(alpha);
//            gState.setStrokeOpacity(alpha);
//            cb.setGState(gState);
//        }
//
//        cb.circle(x, -y, (size / 2f) - borderSize / 2f);
//        if (borderSize > 0 && alpha == 1f) {
//            cb.fillStroke();
//        } else if (borderSize > 0 && alpha < 1f) {
//            // Special case to make sure the border and the fill are not overlapping
//            cb.stroke();
//            cb.circle(x, -y, (size / 2f) - borderSize);
//            cb.fill();
//        } else {
//            cb.fill();
//        }
//        if (alpha < 1f) {
//            cb.restoreState();
//        }
    }

    private void drawCircle(PDPageContentStream stream, final float x, final float y, final float r) throws IOException {
        float b = 0.5523f;
        stream.moveTo(x + r, y);
        stream.curveTo(x + r, y + r * b, x + r * b, y + r, x, y + r);
        stream.curveTo(x - r * b, y + r, x - r, y + r * b, x - r, y);
        stream.curveTo(x - r, y - r * b, x - r * b, y - r, x, y - r);
        stream.curveTo(x + r * b, y - r, x + r, y - r * b, x + r, y);
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[] {
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
                PreviewProperty.CATEGORY_NODES).setValue(defaultOpacity),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_PER_NODE_OPACITY, Boolean.class,
                NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.perNodeOpacity.displayName"),
                NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.perNodeOpacity.description"),
                PreviewProperty.CATEGORY_NODES).setValue(defaultPerNodeOpacity)
        };
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
