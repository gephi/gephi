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
import java.util.Locale;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
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
    public static final String SOURCE = "edge.source";
    public static final String TARGET = "edge.target";
    public static final String TARGET_RADIUS = "edge.target.radius";
    //Default values
    private boolean defaultShowEdges = true;
    private float defaultThickness = 1;
    private boolean defaultRescaleWeight = true;
    private EdgeColor defaultColor = new EdgeColor(EdgeColor.Mode.MIXED);
    private boolean defaultEdgeCurved = true;
    private float defaultBezierCurviness = 0.2f;

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

        //Target radius
        for (Item item : edgeItems) {
            if ((Boolean) item.getData(EdgeItem.DIRECTED) && !(Boolean) item.getData(EdgeItem.SELF_LOOP)) {
                Item targetItem = (Item) item.getData(TARGET);
                Float weight = item.getData(EdgeItem.WEIGHT);
                float radius = properties.getFloatValue(PreviewProperty.ARROW_RADIUS);
                float size = properties.getFloatValue(PreviewProperty.ARROW_SIZE) * weight;
                radius = -(radius + (Float) targetItem.getData(NodeItem.SIZE) / 2f);
                item.setData(TARGET_RADIUS, radius - size);
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
            selfLoopElem.setAttribute("stroke-width", Float.toString(thickness * svgTarget.getScaleRatio()));
            selfLoopElem.setAttribute("fill", "none");
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(selfLoopElem);
        }
    }

    public void renderCurvedEdge(Item edgeItem, Item sourceItem, Item targetItem, float thickness, Color color, PreviewProperties properties, RenderTarget renderTarget) {
        Edge edge = (Edge) edgeItem;
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
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);
        }
    }

    public void renderStraightEdge(Item edgeItem, Item sourceItem, Item targetItem, float thickness, Color color, PreviewProperties properties, RenderTarget renderTarget) {
        Edge edge = (Edge) edgeItem;
        Float x1 = sourceItem.getData(NodeItem.X);
        Float x2 = targetItem.getData(NodeItem.X);
        Float y1 = sourceItem.getData(NodeItem.Y);
        Float y2 = targetItem.getData(NodeItem.Y);

        //Target radius - to start at the base of the arrow
        Float targetRadius = edgeItem.getData(TARGET_RADIUS);
        if (targetRadius != 0) {
            PVector direction = new PVector(x2, y2);
            direction.sub(new PVector(x1, y1));
            direction.normalize();
            direction = new PVector(direction.x, direction.y);
            direction.mult(targetRadius);
            direction.add(new PVector(x2, y2));
            x2 = direction.x;
            y2 = direction.y;
        }

        if (renderTarget instanceof ProcessingTarget) {
            PGraphics graphics = ((ProcessingTarget) renderTarget).getGraphics();
            graphics.strokeWeight(thickness);
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
            svgTarget.getTopElement(SVGTarget.TOP_EDGES).appendChild(edgeElem);
        }
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.SHOW_EDGES, Boolean.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.display.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.display.description"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.category")).setValue(defaultShowEdges),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_THICKNESS, Float.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.thickness.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.thickness.description"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.category"), PreviewProperty.SHOW_EDGES).setValue(defaultThickness),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.rescaleWeight.description"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.category"), PreviewProperty.SHOW_EDGES).setValue(defaultRescaleWeight),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_COLOR, EdgeColor.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.color.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.color.description"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.category"), PreviewProperty.SHOW_EDGES).setValue(defaultColor),
                    PreviewProperty.createProperty(this, PreviewProperty.EDGE_CURVED, Boolean.class,
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.curvedEdges.displayName"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.property.curvedEdges.description"),
                    NbBundle.getMessage(EdgeRenderer.class, "EdgeRenderer.category"), PreviewProperty.SHOW_EDGES).setValue(defaultEdgeCurved)};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        if (item instanceof EdgeItem && properties.getBooleanValue(PreviewProperty.SHOW_EDGES)
                && !properties.getBooleanValue(PreviewProperty.MOVING)) {
            return true;
        }
        return false;
    }
}
