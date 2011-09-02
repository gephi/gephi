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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantOriginalColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

/**
 *
 * @author Yudi Xue, Mathieu Bastian
 */
@ServiceProvider(service = Renderer.class, position = 400)
public class NodeLabelRenderer implements Renderer {
    //Custom properties

    public static final String NODE_COLOR = "node.label.nodeColor";
    public static final String NODE_SIZE = "node.label.nodeSize";
    public static final String NODE_X = "node.x";
    public static final String NODE_Y = "node.y";
    public static final String FONT_SIZE = "node.label.fontSize";
    //Default values
    private final boolean defaultShowLabels = true;
    private final Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private final boolean defaultShorten = false;
    private final DependantOriginalColor defaultColor = new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL);
    private final int defaultMaxChar = 30;
    private final boolean defaultProportinalSize = true;
    private final float defaultOutlineSize = 4;
    private final Color defaultOutlineColor = Color.WHITE;
    private final float defaultOutlineTransparency = 0.6f;
    //Font cache
    private Map<Integer, Font> fontCache;

    public void preProcess(PreviewModel previewModel) {
        PreviewProperties properties = previewModel.getProperties();
        if (properties.getBooleanValue(PreviewProperty.NODE_LABEL_SHORTEN)) {
            //Shorten labels
            Item[] nodeLabelsItems = previewModel.getItems(Item.NODE_LABEL);

            int maxChars = properties.getIntValue(PreviewProperty.NODE_LABEL_MAX_CHAR);
            for (Item item : nodeLabelsItems) {
                String label = item.getData(NodeLabelItem.LABEL);
                if (label.length() >= maxChars + 3) {
                    label = label.substring(0, maxChars) + "...";
                    item.setData(NodeLabelItem.LABEL, label);
                }
            }
        }

        //Put parent color, size and position
        for (Item item : previewModel.getItems(Item.NODE_LABEL)) {
            Node node = (Node) item.getSource();
            Item nodeItem = previewModel.getItem(Item.NODE, node);
            item.setData(NODE_COLOR, nodeItem.getData(NodeItem.COLOR));
            item.setData(NODE_SIZE, nodeItem.getData(NodeItem.SIZE));
            item.setData(NODE_X, nodeItem.getData(NodeItem.X));
            item.setData(NODE_Y, nodeItem.getData(NodeItem.Y));
        }

        //Calculate font size and cache fonts
        fontCache = new HashMap<Integer, Font>();
        Font font = properties.getFontValue(PreviewProperty.NODE_LABEL_FONT);
        for (Item item : previewModel.getItems(Item.NODE_LABEL)) {
            Float nodeSize = item.getData(NODE_SIZE);
            Float fontSize = 1f;
            if (item.getData(NodeLabelItem.SIZE) != null) {
                fontSize = item.getData(NodeLabelItem.SIZE);
            }
            if (properties.getBooleanValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE)) {
                fontSize *= nodeSize / 10f;
            }
            fontSize *= font.getSize();
            Font labelFont = font.deriveFont((float) fontSize);
            fontCache.put(labelFont.getSize(), labelFont);
            item.setData(FONT_SIZE, labelFont.getSize());
        }
    }

    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        Node node = (Node) item.getSource();
        //Label
        Color nodeColor = item.getData(NODE_COLOR);
        Color color = item.getData(NodeLabelItem.COLOR);
        DependantOriginalColor propColor = properties.getValue(PreviewProperty.NODE_LABEL_COLOR);
        color = propColor.getColor(nodeColor, color);
        String label = item.getData(NodeLabelItem.LABEL);
        Integer fontSize = item.getData(FONT_SIZE);
        Float x = item.getData(NODE_X);
        Float y = item.getData(NODE_Y);

        //Outline
        Color outlineColor = properties.getColorValue(PreviewProperty.NODE_LABEL_OUTLINE_COLOR);
        Float outlineSize = properties.getFloatValue(PreviewProperty.NODE_LABEL_OUTLINE_SIZE);
        outlineSize = outlineSize * (fontSize / 32f);
        Float outlineTransparency = properties.getFloatValue(PreviewProperty.NODE_LABEL_OUTLINE_TRANSPARENCY);

        if (target instanceof ProcessingTarget) {
            renderProcessing((ProcessingTarget) target, label, x, y, fontSize, color, outlineSize, outlineColor, outlineTransparency);
        }
    }

    public void renderProcessing(ProcessingTarget target, String label, float x, float y, int fontSize, Color color, float outlineSize, Color outlineColor, float outlineTransparency) {
        PGraphics graphics = target.getGraphics();
        Graphics2D g2 = ((PGraphicsJava2D) graphics).g2;
        graphics.textAlign(PGraphics.CENTER, PGraphics.CENTER);

        Font font = fontCache.get(fontSize);
        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();
        float posX = x - fm.stringWidth(label) / 2f;
        float posY = y + fm.getAscent() / 2f;

        if (outlineSize > 0) {
            FontRenderContext frc = g2.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, label);
            Shape glyph = gv.getOutline(posX, posY);
            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(glyph);
        }

        g2.setColor(color);
        g2.drawString(label, posX, posY);
    }

    public void renderSVG(SVGTarget target, Node node, String label, float x, float y, int fontSize, Color color, float outlineSize, Color outlineColor, float outlineTransparency) {
        Text labelText = target.createTextNode(label);
        Font font = fontCache.get(fontSize);

        Element labelElem = target.createElement("text");
        labelElem.setAttribute("class", node.getNodeData().getId());
        labelElem.setAttribute("x", x + "");
        labelElem.setAttribute("y", y + "");
        labelElem.setAttribute("style", "text-anchor: middle");
        labelElem.setAttribute("fill", target.toHexString(color));
        labelElem.setAttribute("font-family", font.getFamily());
        labelElem.setAttribute("font-size", fontSize + "");
        labelElem.appendChild(labelText);
        target.getTopElement(SVGTarget.TOP_NODE_LABELS);
    }

    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
                    PreviewProperty.createProperty(this, PreviewProperty.SHOW_NODE_LABELS, Boolean.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.display.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.display.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category")).setValue(defaultShowLabels),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_FONT, Font.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.font.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.font.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultFont),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.proportionalSize.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.proportionalSize.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultProportinalSize),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_COLOR, DependantOriginalColor.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.color.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.color.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultColor),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_SHORTEN, Boolean.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.shorten.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.shorten.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultShorten),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_MAX_CHAR, Integer.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.maxchar.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.maxchar.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultMaxChar),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OUTLINE_SIZE, Float.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineSize.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineSize.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultOutlineSize),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OUTLINE_COLOR, Color.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineColor.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineColor.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultOutlineColor),
                    PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OUTLINE_TRANSPARENCY, Float.class,
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineTransparency.displayName"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineTransparency.description"),
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultOutlineTransparency),};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof NodeLabelItem && properties.getBooleanValue(PreviewProperty.SHOW_NODE_LABELS);
    }
}
