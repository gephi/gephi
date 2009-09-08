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
import org.gephi.visualization.VizController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
public class TextModel {

    protected ColorMode colorMode;
    protected SizeMode sizeMode;
    protected boolean selectedOnly;
    protected Font font;
    protected List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    protected float sizeFactor = 0.5f;//Between 0 and 1
    protected float[] nodeColor = {0f, 0f, 0f, 1f};
    protected float[] edgeColor = {0f, 0f, 0f, 1f};

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

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
        fireChangeEvent();
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

    public float getSizeFactor() {
        return sizeFactor;
    }

    public void setSizeFactor(float sizeFactor) {
        this.sizeFactor = sizeFactor;
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

    public void readXML(Element textModelElement) {

        //Font
        Element fontE = (Element) textModelElement.getElementsByTagName("font").item(0);
        String fontName = fontE.getAttribute("name");
        int fontSize = Integer.parseInt(fontE.getAttribute("size"));
        int fontStyle = Integer.parseInt(fontE.getAttribute("style"));
        font = new Font(fontName, fontStyle, fontSize);

        //Color
        Element nodeColorE = (Element) textModelElement.getElementsByTagName("nodecolor").item(0);
        nodeColor = new float[]{Float.parseFloat(nodeColorE.getAttribute("r")),
                    Float.parseFloat(nodeColorE.getAttribute("g")),
                    Float.parseFloat(nodeColorE.getAttribute("b")),
                    Float.parseFloat(nodeColorE.getAttribute("a"))};
        Element edgeColorE = (Element) textModelElement.getElementsByTagName("edgecolor").item(0);
        edgeColor = new float[]{Float.parseFloat(edgeColorE.getAttribute("r")),
                    Float.parseFloat(edgeColorE.getAttribute("g")),
                    Float.parseFloat(edgeColorE.getAttribute("b")),
                    Float.parseFloat(edgeColorE.getAttribute("a"))};

        //Size factor
        Element sizeFactorE = (Element) textModelElement.getElementsByTagName("sizefactor").item(0);
        sizeFactor = Float.parseFloat(sizeFactorE.getTextContent());

        //ColorMode
        Element colorModeE = (Element) textModelElement.getElementsByTagName("colormode").item(0);
        String colorModeClass = colorModeE.getAttribute("class");
        if (colorModeClass.equals("UniqueColorMode")) {
            colorMode = new UniqueColorMode(this);
        } else if (colorModeClass.equals("ObjectColorMode")) {
            colorMode = new ObjectColorMode();
        }

        //SizeMode
        Element sizeModeE = (Element) textModelElement.getElementsByTagName("colormode").item(0);
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
    }

    public Element writeXML(Document document) {

        Element textModelE = document.createElement("textmodel");

        //Font
        Element fontE = document.createElement("font");
        fontE.setAttribute("name", font.getName());
        fontE.setAttribute("size", Integer.toString(font.getSize()));
        fontE.setAttribute("style", Integer.toString(font.getStyle()));
        textModelE.appendChild(fontE);

        //Size factor
        Element sizeFactorE = document.createElement("sizefactor");
        sizeFactorE.setTextContent(String.valueOf(sizeFactor));
        textModelE.appendChild(sizeFactorE);

        //Colors
        Element nodeColorE = document.createElement("nodecolor");
        nodeColorE.setAttribute("r", Float.toString(nodeColor[0]));
        nodeColorE.setAttribute("g", Float.toString(nodeColor[1]));
        nodeColorE.setAttribute("b", Float.toString(nodeColor[2]));
        nodeColorE.setAttribute("a", Float.toString(nodeColor[3]));
        textModelE.appendChild(nodeColorE);
        Element edgeColorE = document.createElement("nodecolor");
        edgeColorE.setAttribute("r", Float.toString(edgeColor[0]));
        edgeColorE.setAttribute("g", Float.toString(edgeColor[1]));
        edgeColorE.setAttribute("b", Float.toString(edgeColor[2]));
        edgeColorE.setAttribute("a", Float.toString(edgeColor[3]));
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

        return textModelE;
    }
}
