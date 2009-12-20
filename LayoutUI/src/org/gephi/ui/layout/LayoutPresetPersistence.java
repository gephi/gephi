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
package org.gephi.ui.layout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutPresetPersistence {

    private Map<String, List<Preset>> presets = new HashMap<String, List<Preset>>();

    public LayoutPresetPersistence() {
        loadPresets();
    }

    public void savePreset(String name, Layout layout) {
        Preset preset = new Preset(name, layout);
        addPreset(preset);

        try {
            //Create file if dont exist
            FileObject folder = FileUtil.getConfigFile("layoutpresets");
            if (folder == null) {
                folder = FileUtil.getConfigRoot().createFolder("layoutpresets");
            }
            FileObject presetFile = folder.getFileObject(name, "xml");
            if (presetFile == null) {
                presetFile = folder.createData(name, "xml");
            }

            //Create doc
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            //Write doc
            preset.writeXML(document);

            //Write XML file
            Source source = new DOMSource(document);
            Result result = new StreamResult(FileUtil.toFile(presetFile));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPreset(Preset preset, Layout layout) {
        for (LayoutProperty p : layout.getProperties()) {
            for (int i = 0; i < preset.propertyNames.size(); i++) {
                if (p.getProperty().getName().equals(preset.propertyNames.get(i))) {
                    try {
                        p.getProperty().setValue(preset.propertyValues.get(i));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public List<Preset> getPresets(Layout layout) {
        return presets.get(layout.getClass().getName());
    }

    private void loadPresets() {
        FileObject folder = FileUtil.getConfigFile("layoutpresets");
        if (folder != null) {
            for (FileObject child : folder.getChildren()) {
                if (child.isValid() && child.hasExt("xml")) {
                    try {
                        InputStream stream = child.getInputStream();
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(stream);
                        Preset preset = new Preset(document);
                        addPreset(preset);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void addPreset(Preset preset) {
        List<Preset> layoutPresets = presets.get(preset.layoutClassName);
        if (layoutPresets == null) {
            layoutPresets = new ArrayList<Preset>();
            presets.put(preset.layoutClassName, layoutPresets);
        }
        layoutPresets.add(preset);
    }

    protected static class Preset {

        private List<String> propertyNames = new ArrayList<String>();
        private List<Object> propertyValues = new ArrayList<Object>();
        private String layoutClassName;
        private String name;

        private Preset(String name, Layout layout) {
            this.name = name;
            this.layoutClassName = layout.getClass().getName();
            for (LayoutProperty p : layout.getProperties()) {
                try {
                    Object value = p.getProperty().getValue();
                    propertyNames.add(p.getProperty().getName());
                    propertyValues.add(value);
                } catch (Exception e) {
                }
            }
        }

        private Preset(Document document) {
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
                Class c = Class.forName(classStr);
                if (c.equals(Boolean.class)) {
                    return new Boolean(str);
                } else if (c.equals(Integer.class)) {
                    return new Integer(str);
                } else if (c.equals(Float.class)) {
                    return new Float(str);
                } else if (c.equals(Double.class)) {
                    return new Double(str);
                } else if (c.equals(Long.class)) {
                    return new Long(str);
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
    }
}
