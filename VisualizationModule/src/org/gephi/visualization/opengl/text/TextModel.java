/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.opengl.text;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VizConfig;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class TextModel {

    protected ColorMode colorMode;
    protected SizeMode sizeMode;
    protected boolean selectedOnly;
    protected boolean showNodeLabels;
    protected boolean showEdgeLabels;
    protected Font nodeFont;
    protected Font edgeFont;
    protected float[] nodeColor = {0f, 0f, 0f, 1f};
    protected float[] edgeColor = {0f, 0f, 0f, 1f};
    protected float nodeSizeFactor = 0.5f;//Between 0 and 1
    protected float edgeSizeFactor = 0.5f;
    protected List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    protected AttributeColumn[] nodeTextColumns = new AttributeColumn[0];
    protected AttributeColumn[] edgeTextColumns = new AttributeColumn[0];

    public TextModel() {
        defaultValues();
    }

    private void defaultValues() {
        VizConfig vizConfig = VizController.getInstance().getVizConfig();
        showNodeLabels = vizConfig.isDefaultShowNodeLabels();
        showEdgeLabels = vizConfig.isDefaultShowEdgeLabels();
        nodeFont = vizConfig.getDefaultNodeLabelFont();
        edgeFont = vizConfig.getDefaultEdgeLabelFont();
        nodeColor = vizConfig.getDefaultNodeLabelColor().getRGBComponents(null);
        edgeColor = vizConfig.getDefaultEdgeLabelColor().getRGBComponents(null);
        selectedOnly = vizConfig.isDefaultShowLabelOnSelectedOnly();
        colorMode = VizController.getInstance().getTextManager().getColorModes()[0];
        sizeMode = VizController.getInstance().getTextManager().getSizeModes()[0];
    }

    //Event
    public void addChangeListener(ChangeListener changeListener) {
        listeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(evt);
        }
    }

    public void setListeners(List<ChangeListener> listeners) {
        this.listeners = listeners;
    }

    public List<ChangeListener> getListeners() {
        return listeners;
    }

    //Getter & Setters
    public boolean isShowEdgeLabels() {
        return showEdgeLabels;
    }

    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    public void setShowEdgeLabels(boolean showEdgeLabels) {
        this.showEdgeLabels = showEdgeLabels;
        fireChangeEvent();
    }

    public void setShowNodeLabels(boolean showNodeLabels) {
        this.showNodeLabels = showNodeLabels;
        fireChangeEvent();
    }

    public void setEdgeFont(Font edgeFont) {
        this.edgeFont = edgeFont;
        fireChangeEvent();
    }

    public void setEdgeSizeFactor(float edgeSizeFactor) {
        this.edgeSizeFactor = edgeSizeFactor;
        fireChangeEvent();
    }

    public void setNodeFont(Font nodeFont) {
        this.nodeFont = nodeFont;
        fireChangeEvent();
    }

    public void setNodeSizeFactor(float nodeSizeFactor) {
        this.nodeSizeFactor = nodeSizeFactor;
        fireChangeEvent();
    }

    public Font getEdgeFont() {
        return edgeFont;
    }

    public float getEdgeSizeFactor() {
        return edgeSizeFactor;
    }

    public Font getNodeFont() {
        return nodeFont;
    }

    public float getNodeSizeFactor() {
        return nodeSizeFactor;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
        fireChangeEvent();
    }

    public boolean isSelectedOnly() {
        return selectedOnly;
    }

    public void setSelectedOnly(boolean value) {
        this.selectedOnly = value;
        fireChangeEvent();
    }

    public SizeMode getSizeMode() {
        return sizeMode;
    }

    public void setSizeMode(SizeMode sizeMode) {
        this.sizeMode = sizeMode;
        fireChangeEvent();
    }

    public Color getNodeColor() {
        return new Color(nodeColor[0], nodeColor[1], nodeColor[2], nodeColor[3]);
    }

    public void setNodeColor(Color color) {
        this.nodeColor = color.getRGBComponents(null);
        fireChangeEvent();
    }

    public Color getEdgeColor() {
        return new Color(edgeColor[0], edgeColor[1], edgeColor[2], edgeColor[3]);
    }

    public void setEdgeColor(Color color) {
        this.edgeColor = color.getRGBComponents(null);
        fireChangeEvent();
    }

    public AttributeColumn[] getEdgeTextColumns() {
        return edgeTextColumns;
    }

    public void setTextColumns(AttributeColumn[] nodeTextColumns, AttributeColumn[] edgeTextColumns) {
        this.nodeTextColumns = nodeTextColumns;
        this.edgeTextColumns = edgeTextColumns;
        fireChangeEvent();
    }

    public AttributeColumn[] getNodeTextColumns() {
        return nodeTextColumns;
    }

    public void readXML(Element textModelElement) {

        //Show
        Element showNodeE = (Element) textModelElement.getElementsByTagName("shownodelabels").item(0);
        showNodeLabels = Boolean.parseBoolean(showNodeE.getAttribute("enable"));
        Element showEdgeE = (Element) textModelElement.getElementsByTagName("showedgelabels").item(0);
        showEdgeLabels = Boolean.parseBoolean(showEdgeE.getAttribute("enable"));

        //Selectedonly
        Element selectedOnlyE = (Element) textModelElement.getElementsByTagName("selectedOnly").item(0);
        selectedOnly = Boolean.parseBoolean(selectedOnlyE.getAttribute("value"));

        //Font
        Element nodeFontE = (Element) textModelElement.getElementsByTagName("nodefont").item(0);
        String nodeFontName = nodeFontE.getAttribute("name");
        int nodeFontSize = Integer.parseInt(nodeFontE.getAttribute("size"));
        int nodeFontStyle = Integer.parseInt(nodeFontE.getAttribute("style"));
        nodeFont = new Font(nodeFontName, nodeFontSize, nodeFontStyle);

        Element edgeFontE = (Element) textModelElement.getElementsByTagName("edgefont").item(0);
        String edgeFontName = edgeFontE.getAttribute("name");
        int edgeFontSize = Integer.parseInt(edgeFontE.getAttribute("size"));
        int edgeFontStyle = Integer.parseInt(edgeFontE.getAttribute("style"));
        edgeFont = new Font(edgeFontName, edgeFontSize, edgeFontStyle);

        //Color
        Element nodeColorE = (Element) textModelElement.getElementsByTagName("nodecolor").item(0);
        nodeColor = ColorUtils.decode(nodeColorE.getAttribute("value")).getRGBComponents(null);

        Element edgeColorE = (Element) textModelElement.getElementsByTagName("edgecolor").item(0);
        edgeColor = ColorUtils.decode(edgeColorE.getAttribute("value")).getRGBComponents(null);

        //Size factor
        Element nodeSizeFactorE = (Element) textModelElement.getElementsByTagName("nodesizefactor").item(0);
        nodeSizeFactor = Float.parseFloat(nodeSizeFactorE.getTextContent());

        Element edgeSizeFactorE = (Element) textModelElement.getElementsByTagName("edgesizefactor").item(0);
        edgeSizeFactor = Float.parseFloat(edgeSizeFactorE.getTextContent());

        //ColorMode
        Element colorModeE = (Element) textModelElement.getElementsByTagName("colormode").item(0);
        String colorModeClass = colorModeE.getAttribute("class");
        if (colorModeClass.equals("UniqueColorMode")) {
            colorMode = VizController.getInstance().getTextManager().getColorModes()[0];
        } else if (colorModeClass.equals("ObjectColorMode")) {
            colorMode = VizController.getInstance().getTextManager().getColorModes()[1];
        }

        //SizeMode
        Element sizeModeE = (Element) textModelElement.getElementsByTagName("sizemode").item(0);
        String sizeModeClass = sizeModeE.getAttribute("class");
        if (sizeModeClass.equals("FixedSizeMode")) {
//            sizeMode = new FixedSizeMode(this);
            sizeMode = VizController.getInstance().getTextManager().getSizeModes()[0];
        } else if (colorModeClass.equals("ProportionalSizeMode")) {
//            sizeMode = new ProportionalSizeMode(this);
            sizeMode = VizController.getInstance().getTextManager().getSizeModes()[2];
        } else if (colorModeClass.equals("ScaledSizeMode")) {
//            sizeMode = new ScaledSizeMode(this);
            sizeMode = VizController.getInstance().getTextManager().getSizeModes()[1];
        }

        //NodeColumns
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        if (attributeController != null && attributeController.getModel() != null) {
            AttributeModel attributeModel = attributeController.getModel();
            List<AttributeColumn> nodeCols = new ArrayList<AttributeColumn>();
            List<AttributeColumn> edgeCols = new ArrayList<AttributeColumn>();

            Element nodeColumnsE = (Element) textModelElement.getElementsByTagName("nodecolumns").item(0);
            NodeList nodeColumnList = nodeColumnsE.getElementsByTagName("column");
            for (int i = 0; i < nodeColumnList.getLength(); i++) {
                if (nodeColumnList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeColumnE = (Element) nodeColumnList.item(i);
                    String id = nodeColumnE.getAttribute("id");
                    AttributeColumn col = attributeModel.getNodeTable().getColumn(id);
                    if (col != null) {
                        nodeCols.add(col);
                    }
                }
            }

            Element edgeColumnsE = (Element) textModelElement.getElementsByTagName("edgecolumns").item(0);
            NodeList edgeColumnList = edgeColumnsE.getElementsByTagName("column");
            for (int i = 0; i < edgeColumnList.getLength(); i++) {
                if (edgeColumnList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element edgeColumnE = (Element) edgeColumnList.item(i);
                    String id = edgeColumnE.getAttribute("id");
                    AttributeColumn col = attributeModel.getEdgeTable().getColumn(id);
                    if (col != null) {
                        edgeCols.add(col);
                    }
                }
            }

            nodeTextColumns = nodeCols.toArray(new AttributeColumn[0]);
            edgeTextColumns = edgeCols.toArray(new AttributeColumn[0]);
        }
    }

    public Element writeXML(Document document) {

        Element textModelE = document.createElement("textmodel");

        //Show
        Element showNodeE = document.createElement("shownodelabels");
        showNodeE.setAttribute("enable", String.valueOf(showNodeLabels));
        textModelE.appendChild(showNodeE);
        Element showEdgeE = document.createElement("showedgelabels");
        showEdgeE.setAttribute("enable", String.valueOf(showEdgeLabels));
        textModelE.appendChild(showEdgeE);

        //Selectedonly
        Element selectedOnlyE = document.createElement("selectedOnly");
        selectedOnlyE.setAttribute("value", String.valueOf(selectedOnly));
        textModelE.appendChild(selectedOnlyE);

        //Font
        Element nodeFontE = document.createElement("nodefont");
        nodeFontE.setAttribute("name", nodeFont.getName());
        nodeFontE.setAttribute("size", Integer.toString(nodeFont.getSize()));
        nodeFontE.setAttribute("style", Integer.toString(nodeFont.getStyle()));
        textModelE.appendChild(nodeFontE);

        Element edgeFontE = document.createElement("edgefont");
        edgeFontE.setAttribute("name", edgeFont.getName());
        edgeFontE.setAttribute("size", Integer.toString(edgeFont.getSize()));
        edgeFontE.setAttribute("style", Integer.toString(edgeFont.getStyle()));
        textModelE.appendChild(edgeFontE);

        //Size factor
        Element nodeSizeFactorE = document.createElement("nodesizefactor");
        nodeSizeFactorE.setTextContent(String.valueOf(nodeSizeFactor));
        textModelE.appendChild(nodeSizeFactorE);

        Element edgeSizeFactorE = document.createElement("edgesizefactor");
        edgeSizeFactorE.setTextContent(String.valueOf(edgeSizeFactor));
        textModelE.appendChild(edgeSizeFactorE);

        //Colors
        Element nodeColorE = document.createElement("nodecolor");
        nodeColorE.setAttribute("value", ColorUtils.encode(ColorUtils.decode(nodeColor)));
        textModelE.appendChild(nodeColorE);

        Element edgeColorE = document.createElement("edgecolor");
        edgeColorE.setAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeColor)));
        textModelE.appendChild(edgeColorE);

        //Colormode
        Element colorModeE = document.createElement("colormode");
        if (colorMode instanceof UniqueColorMode) {
            colorModeE.setAttribute("class", "UniqueColorMode");
        } else if (colorMode instanceof ObjectColorMode) {
            colorModeE.setAttribute("class", "ObjectColorMode");
        }
        textModelE.appendChild(colorModeE);

        //SizeMode
        Element sizeModeE = document.createElement("sizemode");
        if (sizeMode instanceof FixedSizeMode) {
            sizeModeE.setAttribute("class", "FixedSizeMode");
        } else if (sizeMode instanceof ProportionalSizeMode) {
            sizeModeE.setAttribute("class", "ProportionalSizeMode");
        } else if (sizeMode instanceof ScaledSizeMode) {
            sizeModeE.setAttribute("class", "ScaledSizeMode");
        }
        textModelE.appendChild(sizeModeE);

        //NodeColumns
        Element nodeColumnsE = document.createElement("nodecolumns");
        for (AttributeColumn c : nodeTextColumns) {
            Element nodeColumnE = document.createElement("column");
            nodeColumnE.setAttribute("id", c.getId());
            nodeColumnsE.appendChild(nodeColumnE);
        }
        textModelE.appendChild(nodeColumnsE);

        //EdgeColumns
        Element edgeColumnsE = document.createElement("edgecolumns");
        for (AttributeColumn c : edgeTextColumns) {
            Element edgeColumnE = document.createElement("column");
            edgeColumnE.setAttribute("id", c.getId());
            edgeColumnsE.appendChild(edgeColumnE);
        }
        textModelE.appendChild(edgeColumnsE);

        return textModelE;
    }
}
