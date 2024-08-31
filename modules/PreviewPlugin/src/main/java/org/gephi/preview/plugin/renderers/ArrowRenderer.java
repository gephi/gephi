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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Locale;
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
import org.gephi.preview.api.Vector;
import org.gephi.preview.plugin.builders.EdgeBuilder;
import org.gephi.preview.plugin.builders.NodeBuilder;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

/**
 * @author Yudi Xue, Mathieu Bastian, Mathieu Jacomy
 */
@ServiceProvider(service = Renderer.class, position = 200)
public class ArrowRenderer implements Renderer {

    //Const
    protected final float BASE_RATIO = 0.5f;
    public static final String ARC_CURVENESS = "edge.arc-curveness";
    public static final String TARGET_RADIUS = "edge.target.radius";
    //Default values
    protected float defaultArrowSize = 3f;

    @Override
    public void preProcess(PreviewModel previewModel) {
        final PreviewProperties properties = previewModel.getProperties();
        //Put arc curveness in properties
        if (!properties.hasProperty(ARC_CURVENESS)) {
            properties.putValue(ARC_CURVENESS, EdgeRenderer.defaultArcCurviness);
        }
    }

    @Override
    public void render(
        final Item item,
        final RenderTarget target,
        final PreviewProperties properties) {
        final Helper h = new Helper(item, properties);
        final Color color = EdgeRenderer.getColor(item, properties);

        if (target instanceof G2DTarget) {
            Graphics2D graphics = ((G2DTarget) target).getGraphics();
            graphics.setColor(color);
            final GeneralPath gpath = new GeneralPath();
            gpath.moveTo(h.p1.x, h.p1.y);
            gpath.lineTo(h.p2.x, h.p2.y);
            gpath.lineTo(h.p3.x, h.p3.y);
            gpath.closePath();
            graphics.fill(gpath);
        } else if (target instanceof SVGTarget) {
            final SVGTarget svgTarget = (SVGTarget) target;
            final Element arrowElem = svgTarget.createElement("polyline");
            arrowElem.setAttribute("points", String.format(
                Locale.ENGLISH,
                "%f,%f %f,%f %f,%f",
                h.p1.x, h.p1.y, h.p2.x, h.p2.y, h.p3.x, h.p3.y));
            arrowElem.setAttribute("class", String.format(
                "%s %s",
                SVGUtils.idAsClassAttribute(((Node) h.sourceItem.getSource()).getId()),
                SVGUtils.idAsClassAttribute(((Node) h.targetItem.getSource()).getId())
            ));
            arrowElem.setAttribute("fill", svgTarget.toHexString(color));
            arrowElem.setAttribute("fill-opacity", (color.getAlpha() / 255f) + "");
            arrowElem.setAttribute("stroke", "none");
            svgTarget.getTopElement(SVGTarget.TOP_ARROWS).appendChild(arrowElem);
        } else if (target instanceof PDFTarget) {
            final PDFTarget pdfTarget = (PDFTarget) target;
            final PDPageContentStream cb = pdfTarget.getContentStream();

            try {
                cb.moveTo(h.p1.x, -h.p1.y);
                cb.lineTo(h.p2.x, -h.p2.y);
                cb.lineTo(h.p3.x, -h.p3.y);
                cb.closePath();
                cb.setNonStrokingColor(color);
                if (color.getAlpha() < 255) {
                    float alpha = color.getAlpha() / 255f;
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setNonStrokingAlphaConstant(alpha);
                    cb.saveGraphicsState();
                    cb.setGraphicsStateParameters(graphicsState);
                }
                cb.fill();
                if (color.getAlpha() < 255) {
                    cb.restoreGraphicsState();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget renderTarget, PreviewProperties properties) {
    }

    @Override
    public CanvasSize getCanvasSize(
        final Item item,
        final PreviewProperties properties) {
        final Helper h = new Helper(item, properties);
        final float minX = Math.min(Math.min(h.p1.x, h.p2.x), h.p3.x);
        final float minY = Math.min(Math.min(h.p1.y, h.p2.y), h.p3.y);
        final float maxX = Math.max(Math.max(h.p1.x, h.p2.x), h.p3.x);
        final float maxY = Math.max(Math.max(h.p1.y, h.p2.y), h.p3.y);
        return properties.getBooleanValue(PreviewProperty.EDGE_CURVED)
            ? new CanvasSize()
            : new CanvasSize(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[] {
            PreviewProperty.createProperty(this, PreviewProperty.ARROW_SIZE, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "ArrowRenderer.property.size.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "ArrowRenderer.property.size.description"),
                PreviewProperty.CATEGORY_EDGE_ARROWS, PreviewProperty.SHOW_EDGES).setValue(defaultArrowSize)};
    }

    private boolean showArrows(PreviewProperties properties) {
        return properties.getBooleanValue(PreviewProperty.SHOW_EDGES)
            && properties.getBooleanValue(PreviewProperty.DIRECTED)
            && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof EdgeItem
            && showArrows(properties)
            && (Boolean) item.getData(EdgeItem.DIRECTED)
            && !(Boolean) item.getData(EdgeItem.SELF_LOOP);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof EdgeBuilder
            || itemBuilder instanceof NodeBuilder)
            && showArrows(properties);//Needs some properties of nodes
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ArrowRenderer.class, "ArrowRenderer.name");
    }

    private class Helper {

        public final Item sourceItem;
        public final Item targetItem;
        public final Vector p1;
        public final Vector p2;
        public final Vector p3;

        public Helper(
            final Item item,
            final PreviewProperties properties) {
            sourceItem = item.getData(EdgeRenderer.SOURCE);
            targetItem = item.getData(EdgeRenderer.TARGET);

            final Float x1 = sourceItem.getData(NodeItem.X);
            final Float x2 = targetItem.getData(NodeItem.X);
            final Float y1 = sourceItem.getData(NodeItem.Y);
            final Float y2 = targetItem.getData(NodeItem.Y);

            final Double weight = item.getData(EdgeItem.WEIGHT);
            final float size = properties.getFloatValue(PreviewProperty.ARROW_SIZE)
                * weight.floatValue();
            float radius = -(properties.getFloatValue(PreviewProperty.EDGE_RADIUS)
                + (Float) targetItem.getData(NodeItem.SIZE) / 2f);

            //Avoid arrow from passing the node's center:
            if (radius > 0) {
                radius = 0;
            }

            Vector direction = new Vector(x2, y2);
            direction.sub(new Vector(x1, y1));
            final float length = direction.mag();
            direction.normalize();

            if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
                // Change the direction to account for the curvature
                // The direction won't be changed if no edge is drawn.
                double newAngle = Math.atan2(direction.y, direction.x);
                double curvature = properties.getDoubleValue(ARC_CURVENESS);
                double r = length / curvature;
                final Float targetRadius = item.getData(TARGET_RADIUS);
                double rt = Math.max(0.,-targetRadius);

                if (r >= rt / 2) {
                    double h = Math.sqrt(Math.pow(r, 2) - Math.pow(length / 2, 2));
                    newAngle += Math.PI / 2 - Math.atan2(h, length / 2);
                    double h2 = Math.sqrt(Math.pow(r, 2) - Math.pow(rt / 2, 2));
                    newAngle -= Math.PI / 2 - Math.atan2(h2, rt / 2);
                    direction = new Vector((float) Math.cos(newAngle), (float) Math.sin(newAngle));
                }
            }
            p1 = new Vector(direction.x, direction.y);
            p1.mult(radius);
            p1.add(new Vector(x2, y2));

            final Vector p1r = new Vector(direction.x, direction.y);
            p1r.mult(radius - size);
            p1r.add(new Vector(x2, y2));

            p2 = new Vector(-direction.y, direction.x);
            p2.mult(size * BASE_RATIO);
            p2.add(p1r);

            p3 = new Vector(direction.y, -direction.x);
            p3.mult(size * BASE_RATIO);
            p3.add(p1r);
        }
    }
}
