package org.gephi.visualization;

import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.text.TextModelImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VizReadWriteXML {
    protected VizConfig config;
    protected GraphLimits limits;
    //Variable
    protected float[] cameraPosition;
    protected float[] cameraTarget;
    protected TextModelImpl textModel;
    protected Color backgroundColor;
    protected float[] backgroundColorComponents = new float[4];
    protected boolean showEdges;
    protected boolean lightenNonSelectedAuto;
    protected boolean autoSelectNeighbor;
    protected boolean hideNonSelectedEdges;
    protected boolean uniColorSelected;
    protected boolean edgeHasUniColor;
    protected float[] edgeUniColor;
    protected boolean edgeSelectionColor;
    protected float[] edgeInSelectionColor;
    protected float[] edgeOutSelectionColor;
    protected float[] edgeBothSelectionColor;
    protected boolean adjustByText;
    protected float edgeScale;
    //Listener
    protected List<PropertyChangeListener> listeners = new ArrayList<>();
    private boolean defaultModel = false;

    public void fireProperyChange(String propertyName, Object oldvalue, Object newValue) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldvalue, newValue);
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
        fireProperyChange("showEdges", null, showEdges);
    }

    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        this.lightenNonSelectedAuto = lightenNonSelectedAuto;
        fireProperyChange("lightenNonSelectedAuto", null, lightenNonSelectedAuto);
    }

    public void setAutoSelectNeighbor(boolean autoSelectNeighbor) {
        this.autoSelectNeighbor = autoSelectNeighbor;
        fireProperyChange("autoSelectNeighbor", null, autoSelectNeighbor);
    }

    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        this.hideNonSelectedEdges = hideNonSelectedEdges;
        fireProperyChange("hideNonSelectedEdges", null, hideNonSelectedEdges);
    }

    public void setUniColorSelected(boolean uniColorSelected) {
        this.uniColorSelected = uniColorSelected;
        fireProperyChange("uniColorSelected", null, uniColorSelected);
    }

    public void setEdgeHasUniColor(boolean edgeHasUniColor) {
        this.edgeHasUniColor = edgeHasUniColor;
        fireProperyChange("edgeHasUniColor", null, edgeHasUniColor);
    }

    public void setAdjustByText(boolean adjustByText) {
        this.adjustByText = adjustByText;
        fireProperyChange("adjustByText", null, adjustByText);
    }

    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        this.edgeSelectionColor = edgeSelectionColor;
        fireProperyChange("edgeSelectionColor", null, edgeSelectionColor);
    }

    public void setEdgeInSelectionColor(float[] edgeInSelectionColor) {
        this.edgeInSelectionColor = edgeInSelectionColor;
        fireProperyChange("edgeInSelectionColor", null, edgeInSelectionColor);
    }

    public void setEdgeOutSelectionColor(float[] edgeOutSelectionColor) {
        this.edgeOutSelectionColor = edgeOutSelectionColor;
        fireProperyChange("edgeOutSelectionColor", null, edgeOutSelectionColor);
    }

    public void setEdgeBothSelectionColor(float[] edgeBothSelectionColor) {
        this.edgeBothSelectionColor = edgeBothSelectionColor;
        fireProperyChange("edgeBothSelectionColor", null, edgeBothSelectionColor);
    }

    public void setEdgeScale(float edgeScale) {
        this.edgeScale = edgeScale;
        fireProperyChange("edgeScale", null, edgeScale);
    }

    public void setEdgeUniColor(float[] edgeUniColor) {
        this.edgeUniColor = edgeUniColor;
        fireProperyChange("edgeUniColor", null, edgeUniColor);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.backgroundColorComponents = backgroundColor.getRGBComponents(this.backgroundColorComponents);
        fireProperyChange("backgroundColor", null, backgroundColor);
    }


    //XML
    public void readXML(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("textmodel".equalsIgnoreCase(name)) {
                        textModel.readXML(reader, workspace);
                    } else if ("cameraposition".equalsIgnoreCase(name)) {
                        cameraPosition[0] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                        cameraPosition[1] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                        cameraPosition[2] = Float.parseFloat(reader.getAttributeValue(null, "z"));
                    } else if ("cameratarget".equalsIgnoreCase(name)) {
                        cameraTarget[0] = Float.parseFloat(reader.getAttributeValue(null, "x"));
                        cameraTarget[1] = Float.parseFloat(reader.getAttributeValue(null, "y"));
                        cameraTarget[2] = Float.parseFloat(reader.getAttributeValue(null, "z"));

                    } else if ("showedges".equalsIgnoreCase(name)) {
                        setShowEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("lightennonselectedauto".equalsIgnoreCase(name)) {
                        setLightenNonSelectedAuto(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("autoselectneighbor".equalsIgnoreCase(name)) {
                        setAutoSelectNeighbor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("hidenonselectededges".equalsIgnoreCase(name)) {
                        setHideNonSelectedEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("unicolorselected".equalsIgnoreCase(name)) {
                        setUniColorSelected(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgehasunicolor".equalsIgnoreCase(name)) {
                        setEdgeHasUniColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("adjustbytext".equalsIgnoreCase(name)) {
                        setAdjustByText(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeSelectionColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));

                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        setBackgroundColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeunicolor".equalsIgnoreCase(name)) {
                        setEdgeUniColor(
                                ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeInSelectionColor(
                                ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeOutSelectionColor(
                                ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeBothSelectionColor(
                                ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));

                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        setEdgeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("vizmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        //Fast refreh
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        cameraPosition = Arrays.copyOf(drawable.getCameraLocation(), 3);
        cameraTarget = Arrays.copyOf(drawable.getCameraTarget(), 3);

        //TextModel
        textModel.writeXML(writer);

        //Camera
        writer.writeStartElement("cameraposition");
        writer.writeAttribute("x", Float.toString(cameraPosition[0]));
        writer.writeAttribute("y", Float.toString(cameraPosition[1]));
        writer.writeAttribute("z", Float.toString(cameraPosition[2]));
        writer.writeEndElement();
        writer.writeStartElement("cameratarget");
        writer.writeAttribute("x", Float.toString(cameraTarget[0]));
        writer.writeAttribute("y", Float.toString(cameraTarget[1]));
        writer.writeAttribute("z", Float.toString(cameraTarget[2]));
        writer.writeEndElement();

        writer.writeStartElement("showedges");
        writer.writeAttribute("value", String.valueOf(showEdges));
        writer.writeEndElement();

        writer.writeStartElement("lightennonselectedauto");
        writer.writeAttribute("value", String.valueOf(lightenNonSelectedAuto));
        writer.writeEndElement();

        writer.writeStartElement("autoselectneighbor");
        writer.writeAttribute("value", String.valueOf(autoSelectNeighbor));
        writer.writeEndElement();

        writer.writeStartElement("hidenonselectededges");
        writer.writeAttribute("value", String.valueOf(hideNonSelectedEdges));
        writer.writeEndElement();

        writer.writeStartElement("unicolorselected");
        writer.writeAttribute("value", String.valueOf(uniColorSelected));
        writer.writeEndElement();

        writer.writeStartElement("edgehasunicolor");
        writer.writeAttribute("value", String.valueOf(edgeHasUniColor));
        writer.writeEndElement();

        writer.writeStartElement("adjustbytext");
        writer.writeAttribute("value", String.valueOf(adjustByText));
        writer.writeEndElement();

        writer.writeStartElement("edgeSelectionColor");
        writer.writeAttribute("value", String.valueOf(edgeSelectionColor));
        writer.writeEndElement();

        //Colors
        writer.writeStartElement("backgroundcolor");
        writer.writeAttribute("value", ColorUtils.encode(backgroundColor));
        writer.writeEndElement();

        writer.writeStartElement("edgeunicolor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeUniColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgeInSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeInSelectionColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgeOutSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeOutSelectionColor)));
        writer.writeEndElement();

        writer.writeStartElement("edgeBothSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(edgeBothSelectionColor)));
        writer.writeEndElement();

        //Float
        writer.writeStartElement("edgeScale");
        writer.writeAttribute("value", String.valueOf(edgeScale));
        writer.writeEndElement();
    }
}
