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
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.gephi.graph.api.Edge;
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
import org.gephi.preview.types.EdgeColor;
import org.gephi.utils.NumberUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

/**
 * @author Yudi Xue, Mathieu Bastian, Mathieu Jacomy
 */
@ServiceProvider(service = Renderer.class, position = 100)
public class EdgeRenderer implements Renderer {

    //Custom properties
    public static final String EDGE_MIN_WEIGHT = "edge.min-weight";
    public static final String EDGE_MAX_WEIGHT = "edge.max-weight";
    /**
     * @deprecated We now use circle arcs to draw curved edges. See ARC_CURVENESS instead.
     */
    @Deprecated
    public static final String BEZIER_CURVENESS = "edge.bezier-curveness";
    public static final String ARC_CURVENESS = "edge.arc-curveness";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String TARGET_RADIUS = "edge.target.radius";
    public static final String SOURCE_RADIUS = "edge.source.radius";
    private static final StraightEdgeRenderer STRAIGHT_RENDERER
        = new StraightEdgeRenderer();
    private static final CurvedEdgeRenderer CURVED_RENDERER
        = new CurvedEdgeRenderer();
    private static final SelfLoopEdgeRenderer SELF_LOOP_RENDERER
        = new SelfLoopEdgeRenderer();
    //Default values
    protected boolean defaultShowEdges = true;
    protected float defaultThickness = 1;
    protected boolean defaultRescaleWeight = true;
    protected float defaultRescaleWeightMin = 0.1f;
    protected float defaultRescaleWeightMax = 1.0f;
    protected EdgeColor defaultColor = new EdgeColor(EdgeColor.Mode.MIXED);
    protected boolean defaultEdgeCurved = true;
    protected static float defaultArcCurviness = 1.2f;
    protected int defaultOpacity = 100;
    protected float defaultRadius = 0f;

    public static Color getColor(
        final Item item,
        final PreviewProperties properties) {
        final Item sourceItem = item.getData(SOURCE);
        final Item targetItem = item.getData(TARGET);
        final EdgeColor edgeColor
            = properties.getValue(PreviewProperty.EDGE_COLOR);
        final Color color = edgeColor.getColor(
            item.getData(EdgeItem.COLOR),
            sourceItem.getData(NodeItem.COLOR),
            targetItem.getData(NodeItem.COLOR));
        float opacity = properties.getIntValue(PreviewProperty.EDGE_OPACITY) / 100F;

        return new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            (int) (color.getAlpha() * opacity));
    }

    private static boolean isSelfLoopEdge(final Item item) {
        final Item sourceItem = item.getData(SOURCE);
        final Item targetItem = item.getData(TARGET);
        return item instanceof EdgeItem && sourceItem == targetItem;
    }

    private static float getThickness(final Item item) {
        return ((Double) item.getData(EdgeItem.WEIGHT)).floatValue();
    }

    protected static void putNodesInEdgeItems(PreviewModel previewModel, Item[] edgeItems) {
        for (final Item item : edgeItems) {
            final Edge edge = (Edge) item.getSource();
            final Node source = edge.getSource();
            final Node target = edge.getTarget();
            final Item nodeSource = previewModel.getItem(Item.NODE, source);
            final Item nodeTarget = previewModel.getItem(Item.NODE, target);
            item.setData(SOURCE, nodeSource);
            item.setData(TARGET, nodeTarget);
        }
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
        final PreviewProperties properties = previewModel.getProperties();
        final Item[] edgeItems = previewModel.getItems(Item.EDGE);

        //Put nodes in edge item
        putNodesInEdgeItems(previewModel, edgeItems);

        //Calculate max and min weight
        double minWeight = Double.POSITIVE_INFINITY;
        double maxWeight = Double.NEGATIVE_INFINITY;

        for (Item edge : edgeItems) {
            minWeight = Math.min(
                minWeight,
                edge.getData(EdgeItem.WEIGHT));
            maxWeight = Math.max(
                maxWeight,
                edge.getData(EdgeItem.WEIGHT));
        }
        properties.putValue(EDGE_MIN_WEIGHT, minWeight);
        properties.putValue(EDGE_MAX_WEIGHT, maxWeight);

        //Put arc curveness in properties
        if (!properties.hasProperty(ARC_CURVENESS)) {
            properties.putValue(ARC_CURVENESS, defaultArcCurviness);
        }

        //Rescale weight if necessary - and avoid negative weights
        final boolean rescaleWeight = properties.getBooleanValue(
            PreviewProperty.EDGE_RESCALE_WEIGHT);

        if (rescaleWeight) {
            final double weightDiff = maxWeight - minWeight;
            double minRescaledWeight = properties.getFloatValue(PreviewProperty.EDGE_RESCALE_WEIGHT_MIN);
            double maxRescaledWeight = properties.getFloatValue(PreviewProperty.EDGE_RESCALE_WEIGHT_MAX);

            if (minRescaledWeight < 0) {
                minRescaledWeight = defaultRescaleWeightMin;
                properties.putValue(PreviewProperty.EDGE_RESCALE_WEIGHT_MIN, defaultRescaleWeightMin);
            }

            if (maxRescaledWeight < 0) {
                maxRescaledWeight = defaultRescaleWeightMax;
                properties.putValue(PreviewProperty.EDGE_RESCALE_WEIGHT_MAX, defaultRescaleWeightMax);
            }

            if (minRescaledWeight > maxRescaledWeight) {
                minRescaledWeight = maxRescaledWeight;
            }

            final double rescaledWeightsDiff = maxRescaledWeight - minRescaledWeight;

            if (!Double.isInfinite(minWeight)
                && !Double.isInfinite(maxWeight)
                && !NumberUtils.equalsEpsilon(maxWeight, minWeight)) {
                for (final Item item : edgeItems) {
                    double weight = item.getData(EdgeItem.WEIGHT);
                    weight = rescaledWeightsDiff * (weight - minWeight) / weightDiff + minRescaledWeight;
                    setEdgeWeight(weight, properties, item);
                }
            } else {
                for (final Item item : edgeItems) {
                    setEdgeWeight(1.0, properties, item);
                }
            }
        } else {
            for (final Item item : edgeItems) {
                double weight = item.getData(EdgeItem.WEIGHT);

                if (minWeight <= 0) {
                    //Avoid negative weight
                    weight += Math.abs(minWeight) + 1;
                }

                //Multiply by thickness
                setEdgeWeight(weight, properties, item);
            }
        }

        //Radius
        for (final Item item : edgeItems) {
            if (!(Boolean) item.getData(EdgeItem.SELF_LOOP)) {
                final float edgeRadius
                    = properties.getFloatValue(PreviewProperty.EDGE_RADIUS);

                boolean isDirected = item.getData(EdgeItem.DIRECTED);

                //Target
                final Item targetItem = item.getData(TARGET);
                final Double weight = item.getData(EdgeItem.WEIGHT);
                //Avoid negative arrow size:
                float arrowSize = properties.getFloatValue(
                    PreviewProperty.ARROW_SIZE);
                if (arrowSize < 0F) {
                    arrowSize = 0F;
                }

                final float arrowRadiusSize = isDirected ? arrowSize * weight.floatValue() : 0f;

                final float targetRadius = -(edgeRadius
                    + (Float) targetItem.getData(NodeItem.SIZE) / 2f
                    + arrowRadiusSize);
                item.setData(TARGET_RADIUS, targetRadius);

                //Source
                final Item sourceItem = item.getData(SOURCE);
                final float sourceRadius = -(edgeRadius
                    + (Float) sourceItem.getData(NodeItem.SIZE) / 2f);
                item.setData(SOURCE_RADIUS, sourceRadius);
            }
        }
    }

    private void setEdgeWeight(double weight, final PreviewProperties properties, final Item item) {
        //Multiply by thickness
        weight *= properties.getFloatValue(PreviewProperty.EDGE_THICKNESS);
        item.setData(EdgeItem.WEIGHT, weight);
    }

    @Override
    public void render(
        Item item,
        RenderTarget target,
        PreviewProperties properties) {
        if (isSelfLoopEdge(item)) {
            SELF_LOOP_RENDERER.render(item, target, properties);
        } else if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
            CURVED_RENDERER.render(item, target, properties);
        } else {
            STRAIGHT_RENDERER.render(item, target, properties);
        }
    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget renderTarget, PreviewProperties properties) {
    }

    @Override
    public CanvasSize getCanvasSize(Item item, PreviewProperties properties) {
        if (isSelfLoopEdge(item)) {
            return SELF_LOOP_RENDERER.getCanvasSize(item, properties);
        } else if (properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
            return CURVED_RENDERER.getCanvasSize(item, properties);
        } else {
            return STRAIGHT_RENDERER.getCanvasSize(item, properties);
        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[] {
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
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT_MIN, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.min.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.min.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.EDGE_RESCALE_WEIGHT).setValue(defaultRescaleWeightMin),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT_MAX, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.max.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.max.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.EDGE_RESCALE_WEIGHT).setValue(defaultRescaleWeightMax),
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

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        if (item instanceof EdgeItem) {
            return showEdges(properties);
        }
        return false;
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof EdgeBuilder
            || itemBuilder instanceof NodeBuilder)
            && showEdges(properties);//Needs some properties of nodes
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.name");
    }

    private boolean showEdges(PreviewProperties properties) {
        return properties.getBooleanValue(PreviewProperty.SHOW_EDGES)
            && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    private static class StraightEdgeRenderer {

        public void render(
            final Item item,
            final RenderTarget target,
            final PreviewProperties properties) {
            final Helper h = new Helper(item);
            final Color color = getColor(item, properties);

            if (target instanceof G2DTarget) {
                final Graphics2D graphics = ((G2DTarget) target).getGraphics();
                graphics.setStroke(new BasicStroke(
                    getThickness(item),
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER));
                graphics.setColor(color);
                final Line2D.Float line
                    = new Line2D.Float(h.x1, h.y1, h.x2, h.y2);
                graphics.draw(line);
            } else if (target instanceof SVGTarget) {
                final SVGTarget svgTarget = (SVGTarget) target;
                final Element edgeElem = svgTarget.createElement("path");
                edgeElem.setAttribute("class", String.format(
                    "%s %s",
                    SVGUtils.idAsClassAttribute(((Node) h.sourceItem.getSource()).getId()),
                    SVGUtils.idAsClassAttribute(((Node) h.targetItem.getSource()).getId())
                ));
                edgeElem.setAttribute("d", String.format(
                    Locale.ENGLISH,
                    "M %f,%f L %f,%f",
                    h.x1, h.y1, h.x2, h.y2));
                edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
                edgeElem.setAttribute(
                    "stroke-width",
                    Float.toString(getThickness(item)
                        * svgTarget.getScaleRatio()));
                edgeElem.setAttribute(
                    "stroke-opacity",
                    (color.getAlpha() / 255f) + "");
                edgeElem.setAttribute("fill", "none");
                svgTarget.getTopElement(SVGTarget.TOP_EDGES)
                    .appendChild(edgeElem);
            } else if (target instanceof PDFTarget) {
                final PDFTarget pdfTarget = (PDFTarget) target;
                final PDPageContentStream cb = pdfTarget.getContentStream();
                try {
                    cb.moveTo(h.x1, -h.y1);
                    cb.lineTo(h.x2, -h.y2);
                    cb.setStrokingColor(color);
                    cb.setLineWidth(getThickness(item));
                    if (color.getAlpha() < 255) {
                        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                        graphicsState.setStrokingAlphaConstant(color.getAlpha() / 255f);
                        cb.saveGraphicsState();
                        cb.setGraphicsStateParameters(graphicsState);
                    }
                    cb.stroke();
                    if (color.getAlpha() < 255) {
                        cb.restoreGraphicsState();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        public CanvasSize getCanvasSize(
            final Item item,
            final PreviewProperties properties) {
            final Item sourceItem = item.getData(SOURCE);
            final Item targetItem = item.getData(TARGET);
            final Float x1 = sourceItem.getData(NodeItem.X);
            final Float x2 = targetItem.getData(NodeItem.X);
            final Float y1 = sourceItem.getData(NodeItem.Y);
            final Float y2 = targetItem.getData(NodeItem.Y);
            final float minX = Math.min(x1, x2);
            final float minY = Math.min(y1, y2);
            final float maxX = Math.max(x1, x2);
            final float maxY = Math.max(y1, y2);
            return new CanvasSize(minX, minY, maxX - minX, maxY - minY);
        }

        private static class Helper {

            public final Item sourceItem;
            public final Item targetItem;
            public final Float x1;
            public final Float x2;
            public final Float y1;
            public final Float y2;

            public Helper(final Item item) {
                sourceItem = item.getData(SOURCE);
                targetItem = item.getData(TARGET);

                Float _x1 = sourceItem.getData(NodeItem.X);
                Float _x2 = targetItem.getData(NodeItem.X);
                Float _y1 = sourceItem.getData(NodeItem.Y);
                Float _y2 = targetItem.getData(NodeItem.Y);

                //Target radius - to start at the base of the arrow
                final Float targetRadius = item.getData(TARGET_RADIUS);
                //Avoid edge from passing the node's center:
                if (targetRadius != null && targetRadius < 0) {
                    Vector direction = new Vector(_x2, _y2);
                    direction.sub(new Vector(_x1, _y1));
                    direction.normalize();
                    direction.mult(targetRadius);
                    direction.add(new Vector(_x2, _y2));
                    _x2 = direction.x;
                    _y2 = direction.y;
                }

                //Source radius
                final Float sourceRadius = item.getData(SOURCE_RADIUS);
                //Avoid edge from passing the node's center:
                if (sourceRadius != null && sourceRadius < 0) {
                    Vector direction = new Vector(_x1, _y1);
                    direction.sub(new Vector(_x2, _y2));
                    direction.normalize();
                    direction.mult(sourceRadius);
                    direction.add(new Vector(_x1, _y1));
                    _x1 = direction.x;
                    _y1 = direction.y;
                }

                x1 = _x1;
                y1 = _y1;
                x2 = _x2;
                y2 = _y2;
            }
        }
    }

    private static class CurvedEdgeRenderer {

        public void render(
            final Item item,
            final RenderTarget target,
            final PreviewProperties properties) {
            final Helper h = new Helper(item, properties);
            final Color color = getColor(item, properties);

            if (target instanceof G2DTarget) {
                final Graphics2D graphics = ((G2DTarget) target).getGraphics();
                graphics.setStroke(new BasicStroke(
                    getThickness(item),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_MITER));
                graphics.setColor(color);
                // Arc
                graphics.draw(new Arc2D.Double(h.bbx, h.bby, h.bbw, h.bbh, h.astart, h.asweep, Arc2D.OPEN));
            } else if (target instanceof SVGTarget) {
                final SVGTarget svgTarget = (SVGTarget) target;
                final Element edgeElem = svgTarget.createElement("path");
                edgeElem.setAttribute("class", String.format(
                    "%s %s",
                    SVGUtils.idAsClassAttribute(((Node) h.sourceItem.getSource()).getId()),
                    SVGUtils.idAsClassAttribute(((Node) h.targetItem.getSource()).getId())
                ));
                // Elliptical arc
                String path = String.format(
                    Locale.ENGLISH,
                    "M %f,%f A %f,%f %d,%d %d,%f,%f",
                    h.x1, h.y1,
                    h.r, h.r, 0, 0, 1, h.x2, h.y2);
                edgeElem.setAttribute("d", path);
                edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
                edgeElem.setAttribute(
                    "stroke-width",
                    Float.toString(getThickness(item)
                        * svgTarget.getScaleRatio()));
                edgeElem.setAttribute(
                    "stroke-opacity",
                    (color.getAlpha() / 255f) + "");
                edgeElem.setAttribute("fill", "none");
                svgTarget.getTopElement(SVGTarget.TOP_EDGES)
                    .appendChild(edgeElem);
            } else if (target instanceof PDFTarget) {
                final PDFTarget pdfTarget = (PDFTarget) target;
                final PDPageContentStream cb = pdfTarget.getContentStream();
                try {
                    PDFUtils.drawArc(cb, (float)h.bbx, (float)-h.bby, (float)(h.bbx+h.bbw), (float)-(h.bby+h.bbh), (float)h.astart, (float)h.asweep);
                    cb.setStrokingColor(color);
                    cb.setLineWidth(getThickness(item));
                    cb.setLineJoinStyle(1); //round
                    cb.setLineCapStyle(1); //round
                    if (color.getAlpha() < 255) {
                        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                        graphicsState.setStrokingAlphaConstant(color.getAlpha() / 255f);
                        cb.saveGraphicsState();
                        cb.setGraphicsStateParameters(graphicsState);
                    }
                    cb.stroke();
                    if (color.getAlpha() < 255) {
                        cb.restoreGraphicsState();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        public CanvasSize getCanvasSize(
            final Item item,
            final PreviewProperties properties
        ) {
            final Helper h = new Helper(item, properties);
            final float minX
                = Math.min(h.x1, h.x2);
            final float minY
                = Math.min(h.y1, h.y2);
            final float maxX
                = Math.max(h.x1, h.x2);
            final float maxY
                = Math.max(h.y1, h.y2);
            return new CanvasSize(minX, minY, maxX - minX, maxY - minY);
        }

        private static class Helper {

            public final Item sourceItem;
            public final Item targetItem;
            public final Float x1;
            public final Float x2;
            public final Float y1;
            public final Float y2;
            public final double r;
            public final double bbx;
            public final double bby;
            public final double bbw;
            public final double bbh;
            public final double astart;
            public final double asweep;

            public Helper(
                final Item item,
                final PreviewProperties properties) {
                sourceItem = item.getData(SOURCE);
                targetItem = item.getData(TARGET);

                x1 = sourceItem.getData(NodeItem.X);
                x2 = targetItem.getData(NodeItem.X);
                y1 = sourceItem.getData(NodeItem.Y);
                y2 = targetItem.getData(NodeItem.Y);

                final Vector direction = new Vector(x2, y2);
                direction.sub(new Vector(x1, y1));

                final float length = direction.mag();

                direction.normalize();

                // Arc radius
                r = length / properties.getDoubleValue(ARC_CURVENESS);

                // Arc bounding box (for Graphics2D)
                // Formulas from https://math.stackexchange.com/questions/1781438/finding-the-center-of-a-circle-given-two-points-and-a-radius-algebraically
                double _xa = 0.5 * (x1 - x2);
                double _ya = 0.5 * (y1 - y2);
                double _x0 = x2 + _xa;
                double _y0 = y2 + _ya;
                double _a = Math.sqrt(Math.pow(_xa, 2) + Math.pow(_ya, 2));
                double _b = 0.;
                if (_a < r) {
                    // Note: geometrically, _a <= r is granted.
                    // But in practice, we can have _a very close to r
                    // and numerical approximations may produce _a > r.
                    // This just corresponds to _b=0, but it would give a NaN.
                    // This is why we have to do the check.
                    _b = Math.sqrt(Math.pow(r, 2) - Math.pow(_a, 2));
                }
                double xc = _x0 + (_b * _ya) / _a;
                double yc = _y0 - (_b * _xa / _a);
                double angle1 = Math.atan2(y1 - yc, x1 - xc);
                double angle2 = Math.atan2(y2 - yc, x2 - xc);

                while (angle2 < angle1) {
                    angle2 += 2 * Math.PI;
                }

                // Target radius - to start at the base of the arrow
                final Float targetRadius = item.getData(TARGET_RADIUS);
                // Note: calling this a "radius" may be confusing.
                // Clarification:
                // This is about offsetting the arc at the end, using the
                // node radius + the arrow size. It is a radius in the same
                // sense as "node radius". It's not the radius of the edge curve.
                // The same goes for sourceRadius below.

                // Offset due to the source node
                if (targetRadius != null && targetRadius < 0) {
                    Double targetOffset = this.computeTheThing(r, (double) targetRadius);
                    angle2 += targetOffset;
                }

                // Source radius
                final Float sourceRadius = item.getData(SOURCE_RADIUS);
                // Avoid edge from passing the node's center:
                if (sourceRadius != null && sourceRadius < 0) {
                    Double sourceOffset = this.computeTheThing(r, (double) sourceRadius);
                    angle1 -= sourceOffset;
                }

                bbx = xc - r;
                bby = yc - r;
                bbw = 2 * r;
                bbh = 2 * r;
                astart = -180 * (angle1) / Math.PI;
                if (0. < angle1 - angle2) {
                    // This case corresponds to a negative length of the edge.
                    // It may happen because the arrow or the nodes are too big and "swallow" the edge.
                    // In that case we do not trace the edge (null length).
                    asweep = 0.;
                } else {
                    asweep = (180 * (angle1 - angle2) / Math.PI + 720) % 360 - 360;
                }
            }

            private Double computeTheThing(Double radius_curvature_edge, Double truncature_length) {
                // There is an edge that is a circle arc.
                // We want to truncate that arc so that truncated part has a chord of a given length.
                // i.e. not the length along the arc, but as a straight segment (like the string of a bow)
                // We give back the result as an angle, as it's how it's useful to us.
                Double rt = truncature_length;
                Double r = radius_curvature_edge;
                double x;
                if (r >= rt) {
                    x = Math.sqrt(Math.pow(r, 2) - Math.pow(rt / 2, 2));
                } else {
                    // There is no solution to the problem
                    // (this edge case is dealt with somewhere else)
                    return 0.;
                }
                return 2 * Math.atan2(rt / 2, x);
            }

            private Vector computeCtrlPoint(
                final Float x,
                final Float y,
                final Vector direction,
                final float factor,
                final Vector normalVector) {
                final Vector v = new Vector(direction.x, direction.y);
                v.mult(factor);
                v.add(new Vector(x, y));
                v.add(normalVector);
                return v;
            }
        }
    }

    private static class SelfLoopEdgeRenderer {

        public static final String ID = "SelfLoopEdge";

        public void render(
            final Item item,
            final RenderTarget target,
            final PreviewProperties properties) {
            final Helper h = new Helper(item);
            final Color color = getColor(item, properties);

            if (target instanceof G2DTarget) {
                final Graphics2D graphics = ((G2DTarget) target).getGraphics();
                graphics.setStroke(new BasicStroke(
                    getThickness(item),
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER));
                graphics.setColor(color);
                final GeneralPath gp
                    = new GeneralPath(GeneralPath.WIND_NON_ZERO);
                gp.moveTo(h.x, h.y);
                gp.curveTo(h.v1.x, h.v1.y, h.v2.x, h.v2.y, h.x, h.y);
                graphics.draw(gp);
            } else if (target instanceof SVGTarget) {
                final SVGTarget svgTarget = (SVGTarget) target;

                final Element selfLoopElem = svgTarget.createElement("path");
                selfLoopElem.setAttribute("d", String.format(
                    Locale.ENGLISH,
                    "M %f,%f C %f,%f %f,%f %f,%f",
                    h.x, h.y, h.v1.x, h.v1.y, h.v2.x, h.v2.y, h.x, h.y));
                selfLoopElem.setAttribute("class", SVGUtils.idAsClassAttribute(h.node.getId()));
                selfLoopElem.setAttribute(
                    "stroke",
                    svgTarget.toHexString(color));
                selfLoopElem.setAttribute(
                    "stroke-opacity",
                    (color.getAlpha() / 255f) + "");
                selfLoopElem.setAttribute("stroke-width", Float.toString(
                    getThickness(item) * svgTarget.getScaleRatio()));
                selfLoopElem.setAttribute("fill", "none");
                svgTarget.getTopElement(SVGTarget.TOP_EDGES)
                    .appendChild(selfLoopElem);
            } else if (target instanceof PDFTarget) {
                final PDFTarget pdfTarget = (PDFTarget) target;
                final PDPageContentStream cb = pdfTarget.getContentStream();
                try {
                    cb.moveTo(h.x, -h.y);
                    cb.curveTo(h.v1.x, -h.v1.y, h.v2.x, -h.v2.y, h.x, -h.y);
                    cb.setStrokingColor(color);
                    cb.setLineWidth(getThickness(item));
                    if (color.getAlpha() < 255) {
                        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                        graphicsState.setStrokingAlphaConstant(color.getAlpha() / 255f);
                        cb.saveGraphicsState();
                        cb.setGraphicsStateParameters(graphicsState);
                    }
                    cb.stroke();
                    if (color.getAlpha() < 255) {
                        cb.restoreGraphicsState();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        public CanvasSize getCanvasSize(
            final Item item,
            final PreviewProperties properties) {
            final Helper h = new Helper(item);
            final float minX = Math.min(Math.min(h.x, h.v1.x), h.v2.x);
            final float minY = Math.min(Math.min(h.y, h.v1.y), h.v2.y);
            final float maxX = Math.max(Math.max(h.x, h.v1.x), h.v2.x);
            final float maxY = Math.max(Math.max(h.y, h.v1.y), h.v2.y);
            return new CanvasSize(minX, minY, maxX - minX, maxY - minY);
        }

        private static class Helper {

            public final Float x;
            public final Float y;
            public final Node node;
            public final Vector v1;
            public final Vector v2;

            public Helper(final Item item) {
                node = ((Edge) item.getSource()).getSource();

                Item nodeSource = item.getData(SOURCE);
                x = nodeSource.getData(NodeItem.X);
                y = nodeSource.getData(NodeItem.Y);
                Float size = nodeSource.getData(NodeItem.SIZE);

                v1 = new Vector(x, y);
                v1.add(size, -size);

                v2 = new Vector(x, y);
                v2.add(size, size);
            }
        }
    }
}
