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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
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
import org.gephi.preview.plugin.builders.NodeLabelBuilder;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.visualization.api.VisualizationModel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
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
    public static final String FONT_SIZE_FLOAT = "node.label.fontSizeFloat";
    public static final String HIDDEN = "node.label.hidden";
    //Default values
    protected final boolean defaultShowLabels = false;
    protected final boolean defaultCustomFont = false;
    protected final Font defaultFont = new Font("Arial", Font.PLAIN, 12);
    protected final boolean defaultShorten = false;
    protected final DependantOriginalColor defaultColor =
        new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL);
    protected final int defaultMaxChar = 30;
    protected final boolean defaultProportinalSize = true;
    protected final float defaultOutlineSize = 4;
    protected final DependantColor defaultOutlineColor = new DependantColor(Color.WHITE);
    protected final int defaultOutlineOpacity = 40;
    protected final boolean defaultShowBox = false;
    protected final DependantColor defaultBoxColor = new DependantColor(DependantColor.Mode.PARENT);
    protected final int defaultBoxOpacity = 100;
    protected final boolean defaultAvoidOverlap = true;
    protected final int defaultOverlapGridSize = 10;
    //Font cache
    protected final Map<Integer, Font> fontCache = new HashMap<>();
    protected final FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);

    @Override
    public void preProcess(PreviewModel previewModel) {
        final Item[] nodeLabelsItems = previewModel.getItems(Item.NODE_LABEL);

        PreviewProperties properties = previewModel.getProperties();
        if (properties.getBooleanValue(PreviewProperty.NODE_LABEL_SHORTEN)) {
            //Shorten labels
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
        for (Item item : nodeLabelsItems) {
            Node node = (Node) item.getSource();
            Item nodeItem = previewModel.getItem(Item.NODE, node);
            item.setData(NODE_COLOR, nodeItem.getData(NodeItem.COLOR));
            item.setData(NODE_SIZE, SizeUtils.getNodeSize(nodeItem, properties) / 2f);
            item.setData(NODE_X, nodeItem.getData(NodeItem.X));
            item.setData(NODE_Y, nodeItem.getData(NodeItem.Y));

            // Initialize label as visible (not hidden by overlap avoidance)
            item.setData(HIDDEN, false);
        }

        // Get Viz model
        final VisualizationModel vizModel = previewModel.getWorkspace().getLookup().lookup(VisualizationModel.class);

        // Get font
        Font font = properties.getFontValue(PreviewProperty.NODE_LABEL_FONT);
        if (!properties.getBooleanValue(PreviewProperty.NODE_LABEL_CUSTOM_FONT)) {
            // Use font from visualization model for consistent font family and style with the graph view
            if (vizModel != null && vizModel.getNodeLabelFont() != null) {
                font = vizModel.getNodeLabelFont();
            }
        }

        // TODO: Access those values directly from GraphRenderingOptions
        float fitNodeLabelsToNodeSizeFactor = 0.05f;

        //Calculate font size and cache fonts
        final float baseFontSize = font.getSize() * properties.getFloatValue(PreviewProperty.NODE_LABEL_SCALE);
        for (Item item : nodeLabelsItems) {
            float nodeSize = item.getData(NODE_SIZE);
            float fontSize = baseFontSize;
            if (properties.getBooleanValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE)) {
                fontSize *= nodeSize * fitNodeLabelsToNodeSizeFactor;
            } else {
                // Add tiny bias (<1%) based on node size to prioritize labels of larger nodes in overlap detection
                fontSize *= (1.0f + nodeSize * 0.00001f);
            }
            if (item.getData(NodeLabelItem.SIZE) != null) {
                Float labelSize = item.getData(NodeLabelItem.SIZE);
                fontSize *= (float) Math.sqrt(labelSize);
            }
            item.setData(FONT_SIZE_FLOAT, fontSize);
            Font labelFont = font.deriveFont(fontSize);
            fontCache.put(labelFont.getSize(), labelFont);
            item.setData(FONT_SIZE, labelFont.getSize());
        }

        //Grid-based label overlap avoidance (mirrors NodeLabelUpdater algorithm)
        if (properties.getBooleanValue(PreviewProperty.NODE_LABEL_AVOID_OVERLAP) && nodeLabelsItems.length > 1) {
            int gridSize = properties.getIntValue(PreviewProperty.NODE_LABEL_OVERLAP_GRID_SIZE);

            //Compute graph bounds from node positions
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (Item item : nodeLabelsItems) {
                Float x = item.getData(NODE_X);
                Float y = item.getData(NODE_Y);
                if (x != null && y != null) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }

            float gridMinX = minX - gridSize;
            float gridMinY = minY - gridSize;
            float gridWidth = maxX + gridSize - gridMinX;
            float gridHeight = maxY + gridSize - gridMinY;

            if (gridWidth > 0 && gridHeight > 0) {
                int gridCols = (int) Math.ceil(gridWidth / gridSize);
                int gridRows = (int) Math.ceil(gridHeight / gridSize);
                Map<Integer, Boolean> gridOccupancy = new HashMap<>(); // cell index → occupied

                // Sort by descending float font size so larger (higher-priority) labels are placed first.
                // This uses the pre-rounding float value, preserving distinctions that would be lost
                // after deriveFont rounds to an integer size.
                Item[] sortedItems = Arrays.copyOf(nodeLabelsItems, nodeLabelsItems.length);
                Arrays.sort(sortedItems, (a, b) -> {
                    Float fsA = a.getData(FONT_SIZE_FLOAT);
                    Float fsB = b.getData(FONT_SIZE_FLOAT);
                    if (fsA == null) {
                        fsA = 0f;
                    }
                    if (fsB == null) {
                        fsB = 0f;
                    }
                    return Float.compare(fsB, fsA);
                });

                for (Item item : sortedItems) {
                    String label = item.getData(NodeLabelItem.LABEL);
                    if (label == null || label.trim().isEmpty()) {
                        continue;
                    }

                    Float x = item.getData(NODE_X);
                    Float y = item.getData(NODE_Y);
                    Integer fontSize = item.getData(FONT_SIZE);
                    if (x == null || y == null || fontSize == null) {
                        continue;
                    }

                    Font itemFont = fontCache.get(fontSize);
                    if (itemFont == null) {
                        continue;
                    }

                    Rectangle2D bounds = itemFont.getStringBounds(label, frc);
                    float width = (float) bounds.getWidth();
                    float height = (float) bounds.getHeight();
                    if (width <= 0 || height <= 0) {
                        continue;
                    }

                    float labelMinX = x - width / 2f;
                    float labelMaxX = x + width / 2f;
                    float labelMinY = y - height / 2f;
                    float labelMaxY = y + height / 2f;

                    int minCol = Math.max(0, (int) ((labelMinX - gridMinX) / gridSize));
                    int maxCol = Math.min(gridCols - 1, (int) ((labelMaxX - gridMinX) / gridSize));
                    int minRow = Math.max(0, (int) ((labelMinY - gridMinY) / gridSize));
                    int maxRow = Math.min(gridRows - 1, (int) ((labelMaxY - gridMinY) / gridSize));

                    // Since items are processed largest-first, any occupied cell means a higher-priority
                    // label has already claimed it — hide the current one unconditionally.
                    boolean shouldRender = true;
                    outer:
                    for (int row = minRow; row <= maxRow; row++) {
                        for (int col = minCol; col <= maxCol; col++) {
                            if (gridOccupancy.containsKey(row * gridCols + col)) {
                                shouldRender = false;
                                break outer;
                            }
                        }
                    }

                    if (shouldRender) {
                        for (int row = minRow; row <= maxRow; row++) {
                            for (int col = minCol; col <= maxCol; col++) {
                                gridOccupancy.put(row * gridCols + col, Boolean.TRUE);
                            }
                        }
                    } else {
                        item.setData(HIDDEN, true);
                    }
                }
            }
        }
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        //Skip labels hidden by overlap avoidance
        if (Boolean.TRUE.equals((Boolean) item.getData(HIDDEN))) {
            return;
        }

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

        //Skip if empty
        if (label == null || label.trim().isEmpty()) {
            return;
        }

        //Outline
        DependantColor outlineDependantColor = properties.getValue(PreviewProperty.NODE_LABEL_OUTLINE_COLOR);
        float outlineSize = properties.getFloatValue(PreviewProperty.NODE_LABEL_OUTLINE_SIZE);
        outlineSize = outlineSize * (fontSize / 32f);
        int outlineAlpha = (int) ((properties.getFloatValue(PreviewProperty.NODE_LABEL_OUTLINE_OPACITY) / 100f) * 255f);
        Color outlineColor = outlineDependantColor.getColor(nodeColor);
        outlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), outlineAlpha);

        //Box
        Boolean showBox = properties.getValue(PreviewProperty.NODE_LABEL_SHOW_BOX);
        float borderWidth = SizeUtils.getBorderWidth(properties, item.getData(NODE_SIZE));
        DependantColor boxDependantColor = properties.getValue(PreviewProperty.NODE_LABEL_BOX_COLOR);
        Color boxColor = boxDependantColor.getColor(nodeColor);
        int boxAlpha = (int) ((properties.getFloatValue(PreviewProperty.NODE_LABEL_BOX_OPACITY) / 100f) * 255f);
        boxColor = new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), boxAlpha);

        if (target instanceof G2DTarget) {
            renderG2D((G2DTarget) target, label, x, y, fontSize, color, outlineSize, outlineColor, showBox,
                boxColor, borderWidth);
        } else if (target instanceof SVGTarget) {
            renderSVG((SVGTarget) target, node, label, x, y, fontSize, color, outlineSize, outlineColor, showBox,
                boxColor, borderWidth);
        } else if (target instanceof PDFTarget) {
            renderPDF((PDFTarget) target, node, label, x, y, fontSize, color, outlineSize, outlineColor, showBox,
                boxColor, borderWidth);
        }
    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget renderTarget, PreviewProperties properties) {
    }

    @Override
    public CanvasSize getCanvasSize(
        final Item item,
        final PreviewProperties properties) {
        float x = item.getData(NODE_X);
        float y = item.getData(NODE_Y);
        Integer fontSize = item.getData(FONT_SIZE);

        Font font = fontCache.get(fontSize);
        String label = item.getData(NodeLabelItem.LABEL);
        float textWidth = (float) font.getStringBounds(label, frc).getWidth();
        float textHeight = (float) font.getStringBounds(label, frc).getHeight();

        return new CanvasSize(x - textWidth / 2f, y - textHeight / 2f, textWidth, textHeight);
    }

    public void renderG2D(G2DTarget target, String label, float x, float y, int fontSize, Color color,
                          float outlineSize, Color outlineColor, boolean showBox, Color boxColor, float boxStrokeSize) {
        Graphics2D graphics = target.getGraphics();

        Font font = fontCache.get(fontSize);
        graphics.setFont(font);

        FontMetrics fm = graphics.getFontMetrics();
        float posX = x - fm.stringWidth(label) / 2f;
        // Center text vertically: baseline = centerY + (ascent - descent) / 2
        // This matches the TextRenderer approach for consistent positioning
        float posY = y + (fm.getAscent() - fm.getDescent()) / 2f;

        Shape outlineGlyph = null;

        //Box
        if (showBox) {
            graphics.setStroke(new BasicStroke(boxStrokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            graphics.setColor(boxColor);
            Rectangle2D.Float rect = new Rectangle2D.Float();
            rect.setFrame(posX - (outlineSize + boxStrokeSize) / 2f,
                y - (fm.getAscent() + fm.getDescent()) / 2f - (outlineSize + boxStrokeSize) / 2f,
                fm.stringWidth(label) + outlineSize + boxStrokeSize,
                fm.getAscent() + fm.getDescent() + outlineSize + boxStrokeSize);
            graphics.draw(rect);
        }

        //Outline
        if (outlineSize > 0) {
            FontRenderContext frc = graphics.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, label);
            outlineGlyph = gv.getOutline(posX, posY);
            graphics.setColor(outlineColor);
            graphics.setStroke(new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(outlineGlyph);
        }

        graphics.setColor(color);
        if (null == outlineGlyph) {
            graphics.drawString(label, posX, posY);
        } else {
            graphics.fill(outlineGlyph);
        }
    }

    public void renderSVG(SVGTarget target, Node node, String label, float x, float y, int fontSize, Color color,
                          float outlineSize, Color outlineColor, boolean showBox, Color boxColor, float boxStrokeSize) {
        Text labelText = target.createTextNode(label);
        Font font = fontCache.get(fontSize);

        // Calculate proper baseline Y position using font metrics
        // This matches G2D and TextRenderer approaches for consistency

        Rectangle2D bounds = font.getStringBounds(label, frc);
        float ascent = (float) -bounds.getY();
        float descent = (float) (bounds.getHeight() + bounds.getY());
        float baselineY = y + (ascent - descent) / 2f;

        if (outlineSize > 0) {
            Text labelTextOutline = target.createTextNode(label);
            Element outlineElem = target.createElement("text");
            outlineElem.setAttribute("class", SVGUtils.idAsClassAttribute(node.getId()));
            outlineElem.setAttribute("x", String.valueOf(x));
            outlineElem.setAttribute("y", String.valueOf(baselineY));
            outlineElem.setAttribute("style", "text-anchor: middle;");
            outlineElem.setAttribute("font-family", font.getFamily());
            outlineElem.setAttribute("font-size", String.valueOf(fontSize));
            if (font.isBold()) {
                outlineElem.setAttribute("font-weight", "bold");
            }
            if (font.isItalic()) {
                outlineElem.setAttribute("font-style", "italic");
            }
            outlineElem.setAttribute("fill", "none");
            outlineElem.setAttribute("stroke", target.toHexString(outlineColor));
            outlineElem.setAttribute("stroke-width", Float.toString(outlineSize * target.getScaleRatio()));
            outlineElem.setAttribute("stroke-linecap", "round");
            outlineElem.setAttribute("stroke-linejoin", "round");
            outlineElem.setAttribute("stroke-opacity", String.valueOf(outlineColor.getAlpha() / 255f));
            outlineElem.appendChild(labelTextOutline);
            target.getTopElement(SVGTarget.TOP_NODE_LABELS_OUTLINE).appendChild(outlineElem);
        }

        Element labelElem = target.createElement("text");
        labelElem.setAttribute("class", SVGUtils.idAsClassAttribute(node.getId()));
        labelElem.setAttribute("x", String.valueOf(x));
        labelElem.setAttribute("y", String.valueOf(baselineY));
        labelElem.setAttribute("style", "text-anchor: middle;");
        labelElem.setAttribute("fill", target.toHexString(color));
        labelElem.setAttribute("fill-opacity", String.valueOf(color.getAlpha() / 255f));
        labelElem.setAttribute("font-family", font.getFamily());
        labelElem.setAttribute("font-size", String.valueOf(fontSize));
        if (font.isBold()) {
            labelElem.setAttribute("font-weight", "bold");
        }
        if (font.isItalic()) {
            labelElem.setAttribute("font-style", "italic");
        }
        labelElem.appendChild(labelText);
        target.getTopElement(SVGTarget.TOP_NODE_LABELS).appendChild(labelElem);

        //Box
        if (showBox) {
            // Calculate box dimensions using font metrics to match G2D rendering
            // Using ascent + descent ensures consistent box height across renderers
            float textWidth = (float) bounds.getWidth();
            float textHeight = ascent + descent;

            Element boxElem = target.createElement("rect");
            float strokeWidth = boxStrokeSize * target.getScaleRatio();
            float padding = strokeWidth + outlineSize * target.getScaleRatio();
            boxElem.setAttribute("x", Float.toString(x - textWidth / 2f - padding / 2f));
            boxElem.setAttribute("y", Float.toString(y - textHeight / 2f - padding / 2f));
            boxElem.setAttribute("width", Float.toString(textWidth + padding));
            boxElem.setAttribute("height", Float.toString(textHeight + padding));
            boxElem.setAttribute("fill", "none");
            boxElem.setAttribute("stroke", target.toHexString(boxColor));
            boxElem.setAttribute("stroke-opacity", String.valueOf(boxColor.getAlpha() / 255f));
            boxElem.setAttribute("stroke-width", Float.toString(strokeWidth));
            boxElem.setAttribute("opacity", String.valueOf(boxColor.getAlpha() / 255f));
            target.getTopElement(SVGTarget.TOP_NODE_LABELS).insertBefore(boxElem, labelElem);
        }
    }

    public void renderPDF(PDFTarget target, Node node, String label, float x, float y, int fontSize, Color color,
                          float outlineSize, Color outlineColor, boolean showBox, Color boxColor, float boxStrokeSize) {
        PDPageContentStream contentStream = target.getContentStream();
        Font font = fontCache.get(fontSize);
        PDFont pdFont = target.getPDFont(font);

        try {
            float textHeight = PDFUtils.getTextHeight(pdFont, fontSize);
            float textMaxHeight = PDFUtils.getMaxTextHeight(pdFont, fontSize);
            float textWidth = PDFUtils.getTextWidth(pdFont, fontSize, label);

            if (showBox) {
                contentStream.setStrokingColor(boxColor);
                contentStream.setRenderingMode(RenderingMode.STROKE);
                contentStream.setLineJoinStyle(0); //miter
                contentStream.setLineWidth(boxStrokeSize);
                if (boxColor.getAlpha() < 255) {
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setStrokingAlphaConstant(boxColor.getAlpha() / 255f);
                    contentStream.saveGraphicsState();
                    contentStream.setGraphicsStateParameters(graphicsState);
                }

                contentStream.addRect(x - (textWidth + outlineSize + boxStrokeSize) / 2f,
                    -y - (textMaxHeight + outlineSize + boxStrokeSize) / 2f,
                    textWidth + outlineSize + boxStrokeSize,
                    textMaxHeight + outlineSize + boxStrokeSize);

                contentStream.stroke();
                if (boxColor.getAlpha() < 255) {
                    contentStream.restoreGraphicsState();
                }
            }

            if (outlineSize > 0) {
                contentStream.setRenderingMode(RenderingMode.STROKE);
                contentStream.setStrokingColor(outlineColor);
                contentStream.setLineWidth(outlineSize);
                contentStream.setLineJoinStyle(1); //round
                contentStream.setLineCapStyle(1); //round
                if (outlineColor.getAlpha() < 255) {
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setStrokingAlphaConstant(outlineColor.getAlpha() / 255f);
                    contentStream.saveGraphicsState();
                    contentStream.setGraphicsStateParameters(graphicsState);
                }
                contentStream.beginText();
                contentStream.setFont(pdFont, fontSize);
                contentStream.newLineAtOffset(x - (textWidth / 2f), -y - (textHeight / 2f));
                contentStream.showText(label);
                contentStream.endText();
                if (outlineColor.getAlpha() < 255) {
                    contentStream.restoreGraphicsState();
                }
            }

            if (color.getAlpha() < 255) {
                PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                graphicsState.setNonStrokingAlphaConstant(color.getAlpha() / 255f);
                contentStream.saveGraphicsState();
                contentStream.setGraphicsStateParameters(graphicsState);
            }
            contentStream.beginText();
            contentStream.setFont(pdFont, fontSize);
            contentStream.setNonStrokingColor(color);
            contentStream.setRenderingMode(RenderingMode.FILL);
            contentStream.newLineAtOffset(x - (textWidth / 2f), -y - (textHeight / 2f));
            contentStream.showText(label);
            contentStream.endText();
            if (color.getAlpha() < 255) {
                contentStream.restoreGraphicsState();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[] {
            PreviewProperty.createProperty(this, PreviewProperty.SHOW_NODE_LABELS, Boolean.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.display.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.display.description"),
                PreviewProperty.CATEGORY_NODE_LABELS).setValue(defaultShowLabels),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_CUSTOM_FONT, Boolean.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.customFont.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.customFont.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultCustomFont),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_FONT, Font.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.font.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.font.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS,
                PreviewProperty.NODE_LABEL_CUSTOM_FONT).setValue(defaultFont),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.proportionalSize.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.proportionalSize.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(
                defaultProportinalSize),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_COLOR, DependantOriginalColor.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.color.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.color.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultColor),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_SHORTEN, Boolean.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.shorten.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.shorten.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultShorten),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_AVOID_OVERLAP, Boolean.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.avoidOverlap.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.avoidOverlap.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultAvoidOverlap),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OVERLAP_GRID_SIZE, Integer.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.overlapGridSize.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.overlapGridSize.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS,
                PreviewProperty.NODE_LABEL_AVOID_OVERLAP).setMinMax(1, null).setValue(defaultOverlapGridSize),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_MAX_CHAR, Integer.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.maxchar.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.maxchar.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultMaxChar),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OUTLINE_SIZE, Float.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineSize.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineSize.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setMinMax(0f, null).setValue(
                defaultOutlineSize),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OUTLINE_COLOR, DependantColor.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineColor.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineColor.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultOutlineColor),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_OUTLINE_OPACITY, Float.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineOpacity.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.outlineOpacity.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setMinMax(0f, 100f).setValue(
                defaultOutlineOpacity),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_SHOW_BOX, Boolean.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.box.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.box.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.SHOW_NODE_LABELS).setValue(defaultShowBox),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_BOX_COLOR, DependantColor.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.box.color.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.box.color.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.NODE_LABEL_SHOW_BOX,
                PreviewProperty.SHOW_NODE_LABELS).setValue(defaultBoxColor),
            PreviewProperty.createProperty(this, PreviewProperty.NODE_LABEL_BOX_OPACITY, Float.class,
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.box.opacity.displayName"),
                NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.property.box.opacity.description"),
                PreviewProperty.CATEGORY_NODE_LABELS, PreviewProperty.NODE_LABEL_SHOW_BOX,
                PreviewProperty.SHOW_NODE_LABELS).setMinMax(0f, 100f).setValue(defaultBoxOpacity)};
    }

    private boolean showNodeLabels(PreviewProperties properties) {
        return properties.getBooleanValue(PreviewProperty.SHOW_NODE_LABELS)
            && !properties.getBooleanValue(PreviewProperty.MOVING);
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item instanceof NodeLabelItem && showNodeLabels(properties);
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return (itemBuilder instanceof NodeLabelBuilder || itemBuilder instanceof NodeBuilder) &&
            showNodeLabels(properties);//Needs some properties of nodes
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NodeLabelRenderer.class, "NodeLabelRenderer.name");
    }
}
