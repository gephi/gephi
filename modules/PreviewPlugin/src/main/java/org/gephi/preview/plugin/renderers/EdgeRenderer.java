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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
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
    // Same multiplier as the GLSL selfloop.vert shader constant
    public static final float STROKE_MULTIPLIER = 1.3f;
    // Stores the source node radius for self-loops (used for partial arc clipping)
    public static final String SELF_LOOP_NODE_RADIUS = "edge.selfloop.nodeRadius";
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
    protected boolean defaultUseWeight = true;
    protected boolean defaultRescaleWeight = true;
    protected float defaultRescaleWeightMin = 0.4f;
    protected float defaultRescaleWeightMax = 8f;
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

    public static float getThickness(final Item item) {
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
        final boolean useWeight = properties.getBooleanValue(
            PreviewProperty.EDGE_USE_WEIGHT);
        final boolean rescaleWeight = properties.getBooleanValue(
            PreviewProperty.EDGE_RESCALE_WEIGHT);

        // Get thickness
        double thickness = properties.getFloatValue(PreviewProperty.EDGE_THICKNESS);
        thickness *= properties.getFloatValue(PreviewProperty.EDGE_SCALE_FACTOR);

        if (useWeight && rescaleWeight) {
            final double weightDiff = maxWeight - minWeight;
            double minRescaledWeight = properties.getFloatValue(PreviewProperty.EDGE_RESCALE_WEIGHT_MIN);
            double maxRescaledWeight = properties.getFloatValue(PreviewProperty.EDGE_RESCALE_WEIGHT_MAX);

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
                    item.setData(EdgeItem.WEIGHT, weight * thickness);
                }
            } else {
                for (final Item item : edgeItems) {
                    item.setData(EdgeItem.WEIGHT, thickness);
                }
            }
        } else if (useWeight) {
            for (final Item item : edgeItems) {
                double weight = item.getData(EdgeItem.WEIGHT);

                if (minWeight <= 0) {
                    //Avoid negative weight
                    weight += Math.abs(minWeight) + 1;
                }

                //Multiply by thickness
                item.setData(EdgeItem.WEIGHT, weight * thickness);
            }
        } else {
            for (final Item item : edgeItems) {
                item.setData(EdgeItem.WEIGHT, thickness);
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
                final float arrowSize = properties.getFloatValue(PreviewProperty.ARROW_SIZE);
                final float arrowRadiusSize = isDirected ? arrowSize * weight.floatValue() : 0f;

                final float targetRadius = -(edgeRadius
                    + SizeUtils.getNodeSize(targetItem, properties) / 2f
                    + arrowRadiusSize);
                item.setData(TARGET_RADIUS, targetRadius);

                //Source
                final Item sourceItem = item.getData(SOURCE);
                final float sourceRadius = -(edgeRadius
                    + SizeUtils.getNodeSize(sourceItem, properties) / 2f);
                item.setData(SOURCE_RADIUS, sourceRadius);
            } else {
                // Self-loop: precompute loopRadius matching the GLSL selfloop.vert shader formula:
                //   loopRadius = scaledNodeSize * 0.5 + strokeWidth * 0.33
                //   strokeWidth = thickness * STROKE_MULTIPLIER
                final Item sourceItem = item.getData(SOURCE);
                final float nodeRadius = SizeUtils.getNodeSize(sourceItem, properties) / 2f;
                final float strokeWidth = getThickness(item) * STROKE_MULTIPLIER;
                final float loopRadius = nodeRadius * 0.5f + strokeWidth * 0.33f;
                item.setData(SOURCE_RADIUS, loopRadius);
                item.setData(SELF_LOOP_NODE_RADIUS, nodeRadius);
            }
        }
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
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setMinMax(0f, null).setValue(
                defaultThickness),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_USE_WEIGHT, Boolean.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.useWeight.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.useWeight.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultUseWeight),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES, PreviewProperty.EDGE_USE_WEIGHT).setValue(
                defaultRescaleWeight),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT_MIN, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.min.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.min.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES, PreviewProperty.EDGE_RESCALE_WEIGHT,
                PreviewProperty.EDGE_USE_WEIGHT).setMinMax(0f, null).setValue(defaultRescaleWeightMin),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT_MAX, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.max.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.max.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES, PreviewProperty.EDGE_RESCALE_WEIGHT,
                PreviewProperty.EDGE_USE_WEIGHT).setMinMax(0f, null).setValue(defaultRescaleWeightMax),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_COLOR, EdgeColor.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.color.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.color.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultColor),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_OPACITY, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.opacity.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.opacity.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setMinMax(0f, 100f).setValue(
                defaultOpacity),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_CURVED, Boolean.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.curvedEdges.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.curvedEdges.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setValue(defaultEdgeCurved),
            PreviewProperty.createProperty(this, PreviewProperty.EDGE_RADIUS, Float.class,
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.radius.displayName"),
                NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.radius.description"),
                PreviewProperty.CATEGORY_EDGES, PreviewProperty.SHOW_EDGES).setMinMax(0f, null).setValue(
                defaultRadius),};
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
                    // Guard: skip offset when nodes overlap to avoid NaN from normalize()
                    if (direction.mag() > 0) {
                        direction.normalize();
                        direction.mult(targetRadius);
                        direction.add(new Vector(_x2, _y2));
                        _x2 = direction.x;
                        _y2 = direction.y;
                    }
                }

                //Source radius
                final Float sourceRadius = item.getData(SOURCE_RADIUS);
                //Avoid edge from passing the node's center:
                if (sourceRadius != null && sourceRadius < 0) {
                    Vector direction = new Vector(_x1, _y1);
                    direction.sub(new Vector(_x2, _y2));
                    // Guard: skip offset when nodes overlap to avoid NaN from normalize()
                    if (direction.mag() > 0) {
                        direction.normalize();
                        direction.mult(sourceRadius);
                        direction.add(new Vector(_x1, _y1));
                        _x1 = direction.x;
                        _y1 = direction.y;
                    }
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

            // Do not draw the edge if negative length
            if (h.asweep == 0) {
                return;
            }

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
                    h.x1WithRadius, h.y1WithRadius,
                    h.r, h.r, 0, 0, 1, h.x2WithRadius, h.y2WithRadius);
                edgeElem.setAttribute("d", path);
                edgeElem.setAttribute("stroke", svgTarget.toHexString(color));
                edgeElem.setAttribute(
                    "stroke-width",
                    Float.toString(getThickness(item)
                        * svgTarget.getScaleRatio()));
                edgeElem.setAttribute("stroke-linecap", "round");
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
                    PDFUtils.drawArc(cb, (float) h.bbx, (float) -h.bby, (float) (h.bbx + h.bbw),
                        (float) -(h.bby + h.bbh), (float) h.astart, (float) h.asweep);
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
            if (h.asweep == 0) {
                // Edge not rendered (too short/swallowed by nodes); fall back to endpoint bbox
                final float minX = Math.min(h.x1, h.x2);
                final float minY = Math.min(h.y1, h.y2);
                return new CanvasSize(minX, minY, Math.abs(h.x2 - h.x1), Math.abs(h.y2 - h.y1));
            }
            // The arc can bow significantly beyond its endpoints (e.g. for near-vertical edges).
            // Use Arc2D.getBounds2D() which computes the exact tight bounding box of the arc,
            // accounting for any cardinal-angle extrema the sweep passes through.
            final Rectangle2D bounds = new Arc2D.Double(
                h.bbx, h.bby, h.bbw, h.bbh, h.astart, h.asweep, Arc2D.OPEN).getBounds2D();
            return new CanvasSize(
                (float) bounds.getX(), (float) bounds.getY(),
                (float) bounds.getWidth(), (float) bounds.getHeight());
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
            public final Float x1WithRadius;
            public final Float x2WithRadius;
            public final Float y1WithRadius;
            public final Float y2WithRadius;

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
                double arcAngle = Math.abs(angle2 - angle1);
                while (arcAngle >= Math.PI) {
                    arcAngle -= Math.PI;
                }

                // Target radius - to start at the base of the arrow
                final Float targetRadius = item.getData(TARGET_RADIUS);
                // Note: calling this a "radius" may be confusing.
                // Clarification:
                // This is about offsetting the arc at the end, using the
                // node radius + the arrow size. It is a radius in the same
                // sense as "node radius". It's not the radius of the edge curve.
                // The same goes for sourceRadius below.

                // Offset due to the target node radius
                if (targetRadius != null && targetRadius < 0) {
                    Double targetOffset = this.computeTruncateAngle(r, (double) targetRadius, (double) arcAngle);
                    angle2 += targetOffset;

                    x2WithRadius = (float) (r * Math.cos(angle2) + xc);
                    y2WithRadius = (float) (r * Math.sin(angle2) + yc);
                } else {
                    x2WithRadius = x2;
                    y2WithRadius = y2;
                }

                // Source radius
                final Float sourceRadius = item.getData(SOURCE_RADIUS);
                // Avoid edge from passing the node's center:
                if (sourceRadius != null && sourceRadius < 0) {
                    Double sourceOffset = this.computeTruncateAngle(r, (double) sourceRadius, (double) arcAngle);
                    angle1 -= sourceOffset;

                    x1WithRadius = (float) (r * Math.cos(angle1) + xc);
                    y1WithRadius = (float) (r * Math.sin(angle1) + yc);
                } else {
                    x1WithRadius = x1;
                    y1WithRadius = y1;
                }

                bbx = xc - r;
                bby = yc - r;
                bbw = 2 * r;
                bbh = 2 * r;
                astart = -180 * (angle1) / Math.PI;
                if (0. <= angle1 - angle2 || length == 0) {
                    // This case corresponds to a negative length of the edge.
                    // It may happen because the arrow or the nodes are too big and "swallow" the edge.
                    // In that case we do not trace the edge (null length).
                    // length is 0 when nodes occupy the same position.
                    asweep = 0.;
                } else {
                    asweep = (180 * (angle1 - angle2) / Math.PI + 720) % 360 - 360;
                }
            }

            private Double computeTruncateAngle(Double radius_curvature_edge, Double truncature_length,
                                                Double arc_angle) {
                // The edge is an arc of a circle.
                // We want to truncate that arc so that truncated part has a chord of a given length.
                // i.e. not the length along the arc, but as a straight segment (like the string of a bow)
                // We give back the result as an angle, as it's how it's useful to us.
                Double rt = truncature_length;
                Double r = radius_curvature_edge;
                Double s = r * arc_angle;
                if (s <= -rt) {
                    // Can't truncate more than the arc length
                    // Return a large value so later on we know the
                    // edge shouldn't get drawn.
                    return -arc_angle - 20;
                }
                // If you take a sector from a circle with radius r, and chord length |rt|,
                // x is the length bisecting the two radii.
                double x = Math.sqrt(Math.pow(r, 2) - Math.pow(rt / 2, 2));
                return 2 * Math.atan2(rt / 2, x);
            }
        }
    }

    private static class SelfLoopEdgeRenderer {

        // Bezier kappa constant for approximating a circle with 4 cubic segments
        public void render(
            final Item item,
            final RenderTarget target,
            final PreviewProperties properties) {
            final Helper h = new Helper(item);
            final Color color = getColor(item, properties);

            if (target instanceof G2DTarget) {
                final Graphics2D graphics = ((G2DTarget) target).getGraphics();
                graphics.setStroke(new BasicStroke(
                    h.strokeWidth,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
                graphics.setColor(color);
                if (h.fullCircle) {
                    graphics.draw(new Ellipse2D.Float(
                        h.cx - h.loopRadius, h.cy - h.loopRadius,
                        2 * h.loopRadius, 2 * h.loopRadius));
                } else {
                    // Partial arc: endpoints touch the node circle.
                    // CAP_ROUND extends the stroke by strokeWidth/2 beyond each endpoint,
                    // visually hiding the gap between the arc and the node.
                    // arcExtent is negative = CW on screen (outer arc, away from node).
                    graphics.draw(new Arc2D.Float(
                        h.cx - h.loopRadius, h.cy - h.loopRadius,
                        2 * h.loopRadius, 2 * h.loopRadius,
                        h.arcStart, h.arcExtent, Arc2D.OPEN));
                }
            } else if (target instanceof SVGTarget) {
                final SVGTarget svgTarget = (SVGTarget) target;
                final Element selfLoopElem;

                if (h.fullCircle) {
                    selfLoopElem = svgTarget.createElement("circle");
                    selfLoopElem.setAttribute("cx", String.format(Locale.ENGLISH, "%f", h.cx));
                    selfLoopElem.setAttribute("cy", String.format(Locale.ENGLISH, "%f", h.cy));
                    selfLoopElem.setAttribute("r", String.format(Locale.ENGLISH, "%f", h.loopRadius));
                } else {
                    // SVG arc: M startPoint A rx,ry 0 largeArcFlag,sweep endPoint
                    // sweep=1 = CW in SVG (y+ down) = CW on screen = outer arc direction.
                    // stroke-linecap="round" visually bridges the gap into the node,
                    // matching G2D's CAP_ROUND behaviour.
                    selfLoopElem = svgTarget.createElement("path");
                    selfLoopElem.setAttribute("d", String.format(Locale.ENGLISH,
                        "M %f,%f A %f,%f 0 %d,1 %f,%f",
                        h.svgSx, h.svgSy,
                        h.loopRadius, h.loopRadius,
                        h.svgLargeArcFlag,
                        h.svgEx, h.svgEy));
                    selfLoopElem.setAttribute("stroke-linecap", "round");
                }
                selfLoopElem.setAttribute("class", SVGUtils.idAsClassAttribute(h.node.getId()));
                selfLoopElem.setAttribute("stroke", svgTarget.toHexString(color));
                selfLoopElem.setAttribute("stroke-opacity", (color.getAlpha() / 255f) + "");
                selfLoopElem.setAttribute("stroke-width",
                    Float.toString(h.strokeWidth * svgTarget.getScaleRatio()));
                selfLoopElem.setAttribute("fill", "none");
                svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(selfLoopElem);
            } else if (target instanceof PDFTarget) {
                final PDFTarget pdfTarget = (PDFTarget) target;
                final PDPageContentStream cb = pdfTarget.getContentStream();
                try {
                    // PDF uses y+ up, so negate y relative to Preview/G2D coordinates.
                    final float pdfCx = h.cx;
                    final float pdfCy = -h.cy;
                    final float r = h.loopRadius;

                    if (h.fullCircle) {
                        // Full circle: 4-segment bezier approximation (kappa = 0.5523)
                        final float k = 0.5523f * r;
                        cb.moveTo(pdfCx + r, pdfCy);
                        cb.curveTo(pdfCx + r, pdfCy + k, pdfCx + k, pdfCy + r, pdfCx, pdfCy + r);
                        cb.curveTo(pdfCx - k, pdfCy + r, pdfCx - r, pdfCy + k, pdfCx - r, pdfCy);
                        cb.curveTo(pdfCx - r, pdfCy - k, pdfCx - k, pdfCy - r, pdfCx, pdfCy - r);
                        cb.curveTo(pdfCx + k, pdfCy - r, pdfCx + r, pdfCy - k, pdfCx + r, pdfCy);
                        cb.closePath();
                    } else {
                        // Partial arc: arc endpoints are already extended into the node (capExt
                        // baked into pdfArcAlpha/pdfArcBeta in Helper), so round caps are hidden.
                        cb.moveTo(pdfCx + r * (float) Math.cos(h.pdfArcAlpha),
                            pdfCy + r * (float) Math.sin(h.pdfArcAlpha));
                        appendCWArcPDF(cb, pdfCx, pdfCy, r, h.pdfArcAlpha, h.pdfArcBeta);
                    }

                    cb.setStrokingColor(color);
                    cb.setLineWidth(h.strokeWidth);
                    cb.setLineJoinStyle(1); // round
                    cb.setLineCapStyle(1);  // round
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

        /**
         * Appends a clockwise arc in PDF coordinate space (y+ up) using cubic bezier approximation.
         * Splits into ≤90° segments for accuracy.
         * CW direction = decreasing angle, so {@code startAngle > endAngle}.
         */
        private static void appendCWArcPDF(final PDPageContentStream cb,
                                           final float cx, final float cy, final float r,
                                           final float startAngle, final float endAngle)
            throws IOException {
            final float span = startAngle - endAngle; // positive for CW
            final int nSegments = Math.max(1, (int) Math.ceil(span / (Math.PI / 2)));
            final float segSpan = span / nSegments;

            float angle = startAngle;
            for (int i = 0; i < nSegments; i++) {
                final float next = angle - segSpan;
                final float k = (4f / 3f) * (float) Math.tan(segSpan / 4f);
                final float cosA = (float) Math.cos(angle), sinA = (float) Math.sin(angle);
                final float cosB = (float) Math.cos(next), sinB = (float) Math.sin(next);
                // CW tangent at angle a: (sin a, −cos a); at b: (sin b, −cos b)
                cb.curveTo(
                    cx + r * (cosA + k * sinA), cy + r * (sinA - k * cosA),
                    cx + r * (cosB - k * sinB), cy + r * (sinB + k * cosB),
                    cx + r * cosB, cy + r * sinB);
                angle = next;
            }
        }

        public CanvasSize getCanvasSize(
            final Item item,
            final PreviewProperties properties) {
            final Helper h = new Helper(item);
            final float halfStroke = h.strokeWidth / 2f;
            final float extent = h.loopRadius + halfStroke;
            return new CanvasSize(h.cx - extent, h.cy - extent, 2 * extent, 2 * extent);
        }

        private static class Helper {

            public final float x;
            public final float y;
            public final Node node;
            // Loop circle center in Preview/G2D coordinates (y+ down):
            // placed upper-right of node to match VisualizationEngine's selfloop shader
            public final float cx;
            public final float cy;
            public final float loopRadius;
            public final float strokeWidth;

            // true when the loop and node circles don't intersect: fall back to full circle
            public final boolean fullCircle;
            // G2D Arc2D parameters (valid when !fullCircle):
            //   arcExtent is negative = CW on screen = outer arc (away from node)
            public final float arcStart;
            public final float arcExtent;
            // SVG arc: start/end points on loop circle, and large-arc flag
            public final float svgSx, svgSy;
            public final float svgEx, svgEy;
            public final int svgLargeArcFlag; // 0 if arc < 180°, 1 if >= 180°
            // PDF arc angles in PDF y+ up convention; CW = decreasing, pdfArcAlpha > pdfArcBeta
            public final float pdfArcAlpha;
            public final float pdfArcBeta;

            public Helper(final Item item) {
                node = ((Edge) item.getSource()).getSource();

                final Item nodeSource = item.getData(SOURCE);
                x = nodeSource.getData(NodeItem.X);
                y = nodeSource.getData(NodeItem.Y);
                // loopRadius was precomputed in preProcess (shader formula):
                //   loopRadius = nodeRadius * 0.5 + strokeWidth * 0.33
                loopRadius = item.getData(SOURCE_RADIUS);
                strokeWidth = getThickness(item) * STROKE_MULTIPLIER;
                // Circle center: upper-right in screen space.
                // In Preview (y+ down), "up" = negative y direction.
                cx = x + loopRadius;
                cy = y - loopRadius;

                // ── Compute intersection of the loop circle with the node circle ──────────────
                //
                // A point on the loop circle at angle θ (G2D atan2, y+ down):
                //   P = (cx + R·cos θ,  cy + R·sin θ)
                //     = (nx + R·(1 + cos θ),  ny − R·(1 − sin θ))
                //
                // Substituting into X² + Y² = nodeRadius² and simplifying:
                //   cos θ − sin θ = C,   C = (nodeRadius² − 3·R²) / (2·R²)
                //
                // Identity:  cos θ − sin θ = √2·cos(θ + π/4)
                //   ⟹  θ = −π/4 ± arccos(C / √2)
                //
                // Outer arc (away from node): θ₂ → θ₁ clockwise on screen,
                // spanning 2·arccos(C/√2) degrees.
                final Float nodeRadiusData = item.getData(SELF_LOOP_NODE_RADIUS);
                final float nodeRadius = nodeRadiusData != null ? nodeRadiusData : loopRadius;
                final float R = loopRadius;

                final float C = (nodeRadius * nodeRadius - 3 * R * R) / (2 * R * R);
                final float cosArg = C / (float) Math.sqrt(2);

                if (Math.abs(cosArg) > 1f) {
                    // No intersection: fall back to full circle
                    fullCircle = true;
                    arcStart = arcExtent = 0;
                    svgSx = svgSy = svgEx = svgEy = 0;
                    svgLargeArcFlag = 0;
                    pdfArcAlpha = pdfArcBeta = 0;
                } else {
                    fullCircle = false;
                    final float alpha = (float) Math.acos(cosArg);
                    final float theta1 = (float) (-Math.PI / 4) + alpha; // arc end   (lower-right)
                    final float theta2 = (float) (-Math.PI / 4) - alpha; // arc start (upper-left)

                    // Extend arc endpoints slightly into the node so that the round stroke caps
                    // are fully hidden behind the node boundary in all renderers (G2D, SVG, PDF).
                    // arcsin(strokeWidth/2 / R) is the angle whose chord equals the stroke half-width.
                    final float capExt = (float) Math.asin(Math.min(1f, strokeWidth / (2 * R)));
                    final float extTheta1 = theta1 + capExt; // extend arc end further CW
                    final float extTheta2 = theta2 - capExt; // extend arc start further CCW

                    // G2D Arc2D: negate atan2 angle → Arc2D convention (y+ up math).
                    // Negative extent = CW on screen.
                    arcStart = -(float) Math.toDegrees(extTheta2);
                    arcExtent = -(float) Math.toDegrees(extTheta1 - extTheta2);

                    // SVG arc endpoints (using extended angles)
                    svgSx = cx + R * (float) Math.cos(extTheta2);
                    svgSy = cy + R * (float) Math.sin(extTheta2);
                    svgEx = cx + R * (float) Math.cos(extTheta1);
                    svgEy = cy + R * (float) Math.sin(extTheta1);
                    svgLargeArcFlag = (extTheta1 - extTheta2 >= Math.PI) ? 1 : 0;

                    // PDF: y-flip maps G2D atan2 angle θ → PDF angle −θ.
                    // CW in PDF = decreasing angle → pdfArcAlpha > pdfArcBeta.
                    pdfArcAlpha = -extTheta2; // start (larger angle)
                    pdfArcBeta = -extTheta1;  // end (smaller angle)
                }
            }
        }
    }
}
