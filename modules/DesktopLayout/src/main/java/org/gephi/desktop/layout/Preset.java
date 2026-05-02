package org.gephi.desktop.layout;

import java.util.ArrayList;
import java.util.List;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Preset {

    protected final List<String> propertyNames = new ArrayList<>();
    protected final List<Object> propertyValues = new ArrayList<>();
    protected String layoutClassName;
    protected String name;

    Preset(String name, Layout layout) {
        this.name = name;
        this.layoutClassName = layout.getClass().getName();
        for (LayoutProperty p : layout.getProperties()) {
            try {
                Object value = p.getProperty().getValue();
                if (value != null) {
                    propertyNames.add(p.getCanonicalName());
                    propertyValues.add(value);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    Preset(Document document) {
        readXML(document);
    }

    public void readXML(Document document) {
        NodeList propertiesList = document.getDocumentElement().getElementsByTagName("properties");
        if (propertiesList.getLength() > 0) {
            for (int j = 0; j < propertiesList.getLength(); j++) {
                Node m = propertiesList.item(j);
                if (m.getNodeType() == Node.ELEMENT_NODE) {
                    Element propertiesE = (Element) m;
                    layoutClassName = propertiesE.getAttribute("layoutClassName");
                    name = propertiesE.getAttribute("name");
                    NodeList propertyList = propertiesE.getElementsByTagName("property");
                    for (int i = 0; i < propertyList.getLength(); i++) {
                        Node n = propertyList.item(i);
                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            Element propertyE = (Element) n;
                            String propStr = propertyE.getAttribute("property");
                            String classStr = propertyE.getAttribute("class");
                            String valStr = propertyE.getTextContent();
                            Object value = parse(classStr, valStr);
                            if (value != null) {
                                propertyNames.add(propStr);
                                propertyValues.add(value);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private Object parse(String classStr, String str) {
        try {
            Class<?> c = Class.forName(classStr);
            if (c.equals(Boolean.class)) {
                return Boolean.parseBoolean(str);
            } else if (c.equals(Integer.class)) {
                return Integer.parseInt(str);
            } else if (c.equals(Float.class)) {
                return Float.parseFloat(str);
            } else if (c.equals(Double.class)) {
                return Double.parseDouble(str);
            } else if (c.equals(Long.class)) {
                return Long.parseLong(str);
            } else if (c.equals(String.class)) {
                return str;
            }
        } catch (ClassNotFoundException ex) {
            return null;
        }
        return null;
    }

    public void writeXML(Document document) {
        Element rootE = document.createElement("layoutproperties");

        //Properties
        Element propertiesE = document.createElement("properties");
        propertiesE.setAttribute("layoutClassName", layoutClassName);
        propertiesE.setAttribute("name", name);
        propertiesE.setAttribute("version", "0.7");
        for (int i = 0; i < propertyNames.size(); i++) {
            Element propertyE = document.createElement("property");
            propertyE.setAttribute("property", propertyNames.get(i));
            propertyE.setAttribute("class", propertyValues.get(i).getClass().getName());
            propertyE.setTextContent(propertyValues.get(i).toString());
            propertiesE.appendChild(propertyE);
        }
        rootE.appendChild(propertiesE);
        document.appendChild(rootE);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Preset other = (Preset) obj;
        return (this.name == null) ? (other.name == null) : this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
