package org.gephi.visualization;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.status.GraphSelectionImpl;

public class SelectionModelImpl {

    // Model
    private final VizModel visualizationModel;
    // Settings
    private int mouseSelectionDiameter;
    private boolean mouseSelectionZoomProportional;
    //States
    private boolean rectangleSelection = false;
    private boolean selectionEnable = true;
    private boolean customSelection = false;
    private boolean singleNodeSelection = false;
    private boolean nodeSelection = false;

    public SelectionModelImpl(VizModel visualizationModel) {
        this.visualizationModel = visualizationModel;

        // Settings
        this.mouseSelectionDiameter = VizConfig.getDefaultMouseSelectionDiameter();
    }

    public GraphSelection toGraphSelection() {
        GraphSelectionImpl gs = new GraphSelectionImpl();
        if (selectionEnable) {
            if (rectangleSelection) {
                gs.setMode(GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION);
            } else if (customSelection) {
                gs.setMode(GraphSelection.GraphSelectionMode.CUSTOM_SELECTION);
            } else if (nodeSelection) {
                if (singleNodeSelection) {
                    gs.setMode(GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION);
                } else {
                    gs.setMode(GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION);
                }
            } else {
                gs.setMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
            }
        }
        gs.setMouseSelectionDiameter(mouseSelectionDiameter);
        gs.setMouseSelectionDiameterZoomProportional(mouseSelectionZoomProportional);
        return gs;
    }

    protected Optional<GraphSelection> currentEngineSelectionModel() {
        return visualizationModel.getEngine().map(VizEngine::getGraphSelection);
    }

    public Collection<Node> getSelectedNodes() {
        return currentEngineSelectionModel()
            .map(GraphSelection::getSelectedNodes)
            .orElse(Collections.emptyList());
    }

    public int getMouseSelectionDiameter() {
        return mouseSelectionDiameter;
    }

    protected void setMouseSelectionDiameter(int mouseSelectionDiameter) {
        this.mouseSelectionDiameter = mouseSelectionDiameter;
    }

    public boolean isMouseSelectionZoomProportional() {
        return mouseSelectionZoomProportional;
    }

    protected void setMouseSelectionZoomProportional(boolean mouseSelectionZoomProportional) {
        this.mouseSelectionZoomProportional = mouseSelectionZoomProportional;
    }

    protected void setSelectionEnable(boolean selectionEnable) {
        this.selectionEnable = selectionEnable;
    }

    protected void setRectangleSelection(boolean rectangleSelection) {
        this.rectangleSelection = rectangleSelection;
    }

    protected void setCustomSelection(boolean customSelection) {
        this.customSelection = customSelection;
    }

    public void setSingleNodeSelection(boolean singleNodeSelection) {
        this.singleNodeSelection = singleNodeSelection;
    }

    public void setNodeSelection(boolean nodeSelection) {
        this.nodeSelection = nodeSelection;
    }

    public boolean isRectangleSelection() {
        return selectionEnable && rectangleSelection;
    }

    public boolean isDirectMouseSelection() {
        return selectionEnable && !rectangleSelection;
    }

    public boolean isCustomSelection() {
        return selectionEnable && customSelection;
    }

    public boolean isSingleNodeSelection() {
        return singleNodeSelection;
    }

    public boolean isSelectionEnabled() {
        return selectionEnable;
    }

    public boolean isNodeSelection() {
        return nodeSelection;
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();
            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("mouseSelectionDiameter".equalsIgnoreCase(name)) {
                        mouseSelectionDiameter = Integer.parseInt(reader.getAttributeValue(null, "value"));
                    } else if ("mouseSelectionZoomProportional".equalsIgnoreCase(name)) {
                        mouseSelectionZoomProportional =
                            Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("rectangleSelection".equalsIgnoreCase(name)) {
                        rectangleSelection = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("selectionEnable".equalsIgnoreCase(name)) {
                        selectionEnable = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("nodeSelection".equalsIgnoreCase(name)) {
                        nodeSelection = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    } else if ("singleNodeSelection".equalsIgnoreCase(name)) {
                        singleNodeSelection = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("selectionModel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("mouseSelectionDiameter");
        writer.writeAttribute("value", String.valueOf(mouseSelectionDiameter));
        writer.writeEndElement();

        writer.writeStartElement("mouseSelectionZoomProportional");
        writer.writeAttribute("value", String.valueOf(mouseSelectionZoomProportional));
        writer.writeEndElement();

        writer.writeStartElement("rectangleSelection");
        writer.writeAttribute("value", String.valueOf(rectangleSelection));
        writer.writeEndElement();

        writer.writeStartElement("selectionEnable");
        writer.writeAttribute("value", String.valueOf(selectionEnable));
        writer.writeEndElement();

        writer.writeStartElement("nodeSelection");
        writer.writeAttribute("value", String.valueOf(nodeSelection));
        writer.writeEndElement();

        writer.writeStartElement("singleNodeSelection");
        writer.writeAttribute("value", String.valueOf(singleNodeSelection));
        writer.writeEndElement();
    }
}
