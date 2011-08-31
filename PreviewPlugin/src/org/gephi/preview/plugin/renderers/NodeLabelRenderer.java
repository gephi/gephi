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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantOriginalColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
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
        Color nodeColor = item.getData(NODE_COLOR);
        Color color = item.getData(NodeLabelItem.COLOR);
        DependantOriginalColor propColor = properties.getValue(PreviewProperty.NODE_LABEL_COLOR);
        color = propColor.getColor(nodeColor, color);
        String label = item.getData(NodeLabelItem.LABEL);
        Integer fontSize = item.getData(FONT_SIZE);
        Float x = item.getData(NODE_X);
        Float y = item.getData(NODE_Y);

        if (target instanceof ProcessingTarget) {
            renderProcessing((ProcessingTarget) target, label, x, y, fontSize, color);
        }
    }

    public void renderProcessing(ProcessingTarget target, String label, float x, float y, int fontSize, Color color) {
        PGraphics graphics = target.getGraphics();
        Graphics2D g2 = ((PGraphicsJava2D) graphics).g2;
        graphics.textAlign(PGraphics.CENTER, PGraphics.CENTER);

        Font font = fontCache.get(fontSize);
        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();
        float posX = x - fm.stringWidth(label) / 2f;
        float posY = y + fm.getAscent() / 2f;

        g2.setColor(color);
        g2.drawString(label, posX, posY);
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
                    NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.category"), PreviewProperty.SHOW_NODE_LABELS).setValue(defaultMaxChar),};
    }

    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof NodeLabelItem && properties.getBooleanValue(PreviewProperty.SHOW_NODE_LABELS);
    }
}
