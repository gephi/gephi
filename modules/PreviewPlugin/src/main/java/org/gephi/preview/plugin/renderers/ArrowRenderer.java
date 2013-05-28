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
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.Locale;
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.*;
import org.gephi.preview.plugin.builders.EdgeBuilder;
import org.gephi.preview.plugin.builders.NodeBuilder;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

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

    @Override
    public void preProcess(PreviewModel previewModel) {
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        float size = properties.getFloatValue(PreviewProperty.ARROW_SIZE);
        if (size > 0) {
            //Get nodes
            Item sourceItem = item.getData(EdgeRenderer.SOURCE);
            Item targetItem = item.getData(EdgeRenderer.TARGET);

            //Weight and color
            Double weight = item.getData(EdgeItem.WEIGHT);
            EdgeColor edgeColor = (EdgeColor) properties.getValue(PreviewProperty.EDGE_COLOR);
            Color color = edgeColor.getColor((Color) item.getData(EdgeItem.COLOR),
                    (Color) sourceItem.getData(NodeItem.COLOR),
                    (Color) targetItem.getData(NodeItem.COLOR));
            int alpha = (int) ((properties.getFloatValue(PreviewProperty.EDGE_OPACITY) / 100f) * 255f);
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

            //Size and radius
            float radius = properties.getFloatValue(PreviewProperty.EDGE_RADIUS);

            size *= weight;
            radius = -(radius + (Float) targetItem.getData(NodeItem.SIZE) / 2f + Math.max(0, properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH)));

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
        Vector direction = new Vector(x2, y2);
        direction.sub(new Vector(x1, y1));
        direction.normalize();

        Vector p1 = new Vector(direction.x, direction.y);
        p1.mult(radius);
        p1.add(new Vector(x2, y2));

        Vector p1r = new Vector(direction.x, direction.y);
        p1r.mult(radius - size);
        p1r.add(new Vector(x2, y2));

        Vector p2 = new Vector(-direction.y, direction.x);
        p2.mult(size * BASE_RATIO);
        p2.add(p1r);

        Vector p3 = new Vector(direction.y, -direction.x);
        p3.mult(size * BASE_RATIO);
        p3.add(p1r);

        if (target instanceof G2DTarget) {
            Graphics2D graphics = ((G2DTarget) target).getGraphics();
            graphics.setColor(color);
            GeneralPath gpath = new GeneralPath();
            gpath.moveTo(p1.x, p1.y);
            gpath.lineTo(p2.x, p2.y);
            gpath.lineTo(p3.x, p3.y);
            gpath.closePath();
            graphics.fill(gpath);
        } else if (target instanceof SVGTarget) {
            SVGTarget svgTarget = (SVGTarget) target;
            Element arrowElem = svgTarget.createElement("polyline");
            arrowElem.setAttribute("points", String.format(Locale.ENGLISH, "%f,%f %f,%f %f,%f",
                    p1.x, p1.y, p2.x, p2.y, p3.x, p3.y));
            arrowElem.setAttribute("class", edge.getSource().getId() + " " + edge.getTarget().getId());
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

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
            PreviewProperty.createProperty(this, PreviewProperty.ARROW_SIZE, Float.class,
            NbBundle.getMessage(EdgeRenderer.class, "ArrowRenderer.property.size.displayName"),
            NbBundle.getMessage(EdgeRenderer.class, "ArrowRenderer.property.size.description"),
            PreviewProperty.CATEGORY_EDGE_ARROWS, PreviewProperty.SHOW_EDGES).setValue(defaultArrowSize)};
    }

    private boolean showArrows(PreviewProperties properties) {
        return properties.getBooleanValue(PreviewProperty.SHOW_EDGES) && properties.getBooleanValue(PreviewProperty.DIRECTED) && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof EdgeItem && showArrows(properties) && (Boolean) item.getData(EdgeItem.DIRECTED) && !(Boolean) item.getData(EdgeItem.SELF_LOOP);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof EdgeBuilder || itemBuilder instanceof NodeBuilder) && showArrows(properties);//Needs some properties of nodes
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ArrowRenderer.class, "ArrowRenderer.name");
    }
}
