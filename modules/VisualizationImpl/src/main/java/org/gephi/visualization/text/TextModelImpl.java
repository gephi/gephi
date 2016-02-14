/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.text;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class TextModelImpl {

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
    protected List<ChangeListener> listeners = new ArrayList<>();
    protected Column[] nodeTextColumns = new Column[0];
    protected Column[] edgeTextColumns = new Column[0];

    public TextModelImpl() {
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
        colorMode = VizController.getInstance().getTextManager().getColorModes()[2];
        sizeMode = VizController.getInstance().getTextManager().getSizeModes()[1];
    }

    //Event
    public void addChangeListener(ChangeListener changeListener) {
        List<ChangeListener> list = listeners;
        if (list != null) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        List<ChangeListener> list = listeners;
        if (list != null) {
            listeners.remove(changeListener);
        }
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        List<ChangeListener> list = listeners;
        if (list != null) {
            for (ChangeListener l : list) {
                l.stateChanged(evt);
            }
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

    public Column[] getEdgeTextColumns() {
        return edgeTextColumns;
    }

    public void setTextColumns(Column[] nodeTextColumns, Column[] edgeTextColumns) {
        this.nodeTextColumns = nodeTextColumns;
        this.edgeTextColumns = edgeTextColumns;
        fireChangeEvent();
    }

    public Column[] getNodeTextColumns() {
        return nodeTextColumns;
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController != null ? graphController.getGraphModel(workspace) : null;
        List<Column> nodeCols = new ArrayList<>();
        List<Column> edgeCols = new ArrayList<>();

        boolean nodeColumn = false;
        boolean edgeColumn = false;
        boolean nodeSizeFac = false;
        boolean edgeSizeFac = false;
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("shownodelabels".equalsIgnoreCase(name)) {
                        showNodeLabels = Boolean.parseBoolean(reader.getAttributeValue(null, "enable"));
                    } else if ("showedgelabels".equalsIgnoreCase(name)) {
                        showEdgeLabels = Boolean.parseBoolean(reader.getAttributeValue(null, "enable"));
                    } else if ("selectedOnly".equalsIgnoreCase(name)) {
                        selectedOnly = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("nodefont".equalsIgnoreCase(name)) {
                        String nodeFontName = reader.getAttributeValue(null, "name");
                        int nodeFontSize = Integer.parseInt(reader.getAttributeValue(null, "size"));
                        int nodeFontStyle = Integer.parseInt(reader.getAttributeValue(null, "style"));
                        nodeFont = new Font(nodeFontName, nodeFontStyle, nodeFontSize);
                    } else if ("edgefont".equalsIgnoreCase(name)) {
                        String edgeFontName = reader.getAttributeValue(null, "name");
                        int edgeFontSize = Integer.parseInt(reader.getAttributeValue(null, "size"));
                        int edgeFontStyle = Integer.parseInt(reader.getAttributeValue(null, "style"));
                        edgeFont = new Font(edgeFontName, edgeFontStyle, edgeFontSize);
                    } else if ("nodecolor".equalsIgnoreCase(name)) {
                        nodeColor = ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null);
                    } else if ("edgecolor".equalsIgnoreCase(name)) {
                        edgeColor = ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null);
                    } else if ("nodesizefactor".equalsIgnoreCase(name)) {
                        nodeSizeFac = true;
                    } else if ("edgesizefactor".equalsIgnoreCase(name)) {
                        edgeSizeFac = true;
                    } else if ("colormode".equalsIgnoreCase(name)) {
                        String colorModeClass = reader.getAttributeValue(null, "class");
                        if (colorModeClass.equals("TextColorMode")) {
                            colorMode = VizController.getInstance().getTextManager().getColorModes()[2];
                        } else if (colorModeClass.equals("ObjectColorMode")) {
                            colorMode = VizController.getInstance().getTextManager().getColorModes()[1];
                        } else {
                            colorMode = VizController.getInstance().getTextManager().getColorModes()[0];
                        }
                    } else if ("sizemode".equalsIgnoreCase(name)) {
                        String sizeModeClass = reader.getAttributeValue(null, "class");
                        if (sizeModeClass.equals("FixedSizeMode")) {
                            sizeMode = VizController.getInstance().getTextManager().getSizeModes()[0];
                        } else if (sizeModeClass.equals("ProportionalSizeMode")) {
                            sizeMode = VizController.getInstance().getTextManager().getSizeModes()[2];
                        } else if (sizeModeClass.equals("ScaledSizeMode")) {
                            sizeMode = VizController.getInstance().getTextManager().getSizeModes()[1];
                        }
                    } else if ("nodecolumns".equalsIgnoreCase(name)) {
                        nodeColumn = true;
                    } else if ("edgecolumns".equalsIgnoreCase(name)) {
                        edgeColumn = true;
                    } else if ("column".equalsIgnoreCase(name)) {
                        String id = reader.getAttributeValue(null, "id");
                        if (nodeColumn && graphModel != null) {
                            Column col = graphModel.getNodeTable().getColumn(id);
                            if (col != null) {
                                nodeCols.add(col);
                            }
                        } else if (edgeColumn && graphModel != null) {
                            Column col = graphModel.getEdgeTable().getColumn(id);
                            if (col != null) {
                                edgeCols.add(col);
                            }
                        }
                    }

                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace() && nodeSizeFac) {
                        nodeSizeFactor = Float.parseFloat(reader.getText());
                    } else if (!reader.isWhiteSpace() && edgeSizeFac) {
                        edgeSizeFactor = Float.parseFloat(reader.getText());
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    nodeSizeFac = false;
                    edgeSizeFac = false;
                    if ("nodecolumns".equalsIgnoreCase(reader.getLocalName())) {
                        nodeColumn = false;
                    } else if ("edgecolumns".equalsIgnoreCase(reader.getLocalName())) {
                        edgeColumn = false;
                    } else if ("textmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }

                    break;
            }
        }

        nodeTextColumns = nodeCols.toArray(new Column[0]);
        edgeTextColumns = edgeCols.toArray(new Column[0]);

    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {

        writer.writeStartElement("textmodel");

        //Show
        writer.writeStartElement("shownodelabels");
        writer.writeAttribute("enable", String.valueOf(showNodeLabels));
        writer.writeEndElement();
        writer.writeStartElement("showedgelabels");
        writer.writeAttribute("enable", String.valueOf(showEdgeLabels));
        writer.writeEndElement();

        //Selectedonly
        writer.writeStartElement("selectedOnly");
        writer.writeAttribute("value", String.valueOf(selectedOnly));
        writer.writeEndElement();

        //Font
        writer.writeStartElement("nodefont");
        writer.writeAttribute("name", nodeFont.getName());
        writer.writeAttribute("size", Integer.toString(nodeFont.getSize()));
        writer.writeAttribute("style", Integer.toString(nodeFont.getStyle()));
        writer.writeEndElement();

        writer.writeStartElement("edgefont");
        writer.writeAttribute("name", edgeFont.getName());
        writer.writeAttribute("size", Integer.toString(edgeFont.getSize()));
        writer.writeAttribute("style", Integer.toString(edgeFont.getStyle()));
        writer.writeEndElement();

        //Size factor
        writer.writeStartElement("nodesizefactor");
        writer.writeCharacters(String.valueOf(nodeSizeFactor));
        writer.writeEndElement();

        writer.writeStartElement("edgesizefactor");
        writer.writeCharacters(String.valueOf(edgeSizeFactor));
        writer.writeEndElement();

        //Colors
        writer.writeStartElement("nodecolor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(nodeColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgecolor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeColor)));
        writer.writeEndElement();

        //Colormode
        writer.writeStartElement("colormode");
        if (colorMode instanceof UniqueColorMode) {
            writer.writeAttribute("class", "UniqueColorMode");
        } else if (colorMode instanceof ObjectColorMode) {
            writer.writeAttribute("class", "ObjectColorMode");
        } else if (colorMode instanceof TextColorMode) {
            writer.writeAttribute("class", "TextColorMode");
        }
        writer.writeEndElement();

        //SizeMode
        writer.writeStartElement("sizemode");
        if (sizeMode instanceof FixedSizeMode) {
            writer.writeAttribute("class", "FixedSizeMode");
        } else if (sizeMode instanceof ProportionalSizeMode) {
            writer.writeAttribute("class", "ProportionalSizeMode");
        } else if (sizeMode instanceof ScaledSizeMode) {
            writer.writeAttribute("class", "ScaledSizeMode");
        }
        writer.writeEndElement();

        //NodeColumns
        writer.writeStartElement("nodecolumns");
        for (Column c : nodeTextColumns) {
            writer.writeStartElement("column");
            writer.writeAttribute("id", c.getId());
            writer.writeEndElement();
        }
        writer.writeEndElement();

        //EdgeColumns
        writer.writeStartElement("edgecolumns");
        for (Column c : edgeTextColumns) {
            writer.writeStartElement("column");
            writer.writeAttribute("id", c.getId());
            writer.writeEndElement();
        }
        writer.writeEndElement();

        writer.writeEndElement();
    }
}
