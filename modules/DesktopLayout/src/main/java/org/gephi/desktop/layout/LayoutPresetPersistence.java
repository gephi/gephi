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
package org.gephi.desktop.layout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private Map<String, List<Preset>> presets = new HashMap<>();

    public LayoutPresetPersistence() {
        loadPresets();
    }

    public void savePreset(String name, Layout layout) {
        Preset preset = addPreset(new Preset(name, layout));

        FileOutputStream fos = null;
        try {
            //Create file if dont exist
            FileObject folder = FileUtil.getConfigFile("layoutpresets");
            if (folder == null) {
                folder = FileUtil.getConfigRoot().createFolder("layoutpresets");
            }
            File presetFile = new File(FileUtil.toFile(folder), name + ".xml");

            //Create doc
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            //Write doc
            preset.writeXML(document);

            //Write XML file
            fos = new FileOutputStream(presetFile);
            Source source = new DOMSource(document);
            Result result = new StreamResult(fos);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
        } catch (Exception e) {
            Logger.getLogger("").log(Level.SEVERE, "Error while writing preset file", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public void loadPreset(Preset preset, Layout layout) {
        for (LayoutProperty p : layout.getProperties()) {
            for (int i = 0; i < preset.propertyNames.size(); i++) {
                if (p.getCanonicalName().equalsIgnoreCase(preset.propertyNames.get(i))
                        || p.getProperty().getName().equalsIgnoreCase(preset.propertyNames.get(i))) {//Also compare with property name to maintain compatibility with old presets
                    try {
                        p.getProperty().setValue(preset.propertyValues.get(i));
                    } catch (Exception e) {
                        Logger.getLogger("").log(Level.SEVERE, "Error while setting preset property", e);
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
                    } catch (Exception e) {
                        Logger.getLogger("").log(Level.SEVERE, "Error while reading preset file", e);
                    }
                }
            }
        }
    }

    private Preset addPreset(Preset preset) {
        List<Preset> layoutPresets = presets.get(preset.layoutClassName);
        if (layoutPresets == null) {
            layoutPresets = new ArrayList<>();
            presets.put(preset.layoutClassName, layoutPresets);
        }
        for (Preset p : layoutPresets) {
            if (p.equals(preset)) {
                return p;
            }
        }
        layoutPresets.add(preset);
        return preset;
    }

    protected static class Preset {

        private List<String> propertyNames = new ArrayList<>();
        private List<Object> propertyValues = new ArrayList<>();
        private String layoutClassName;
        private String name;

        private Preset(String name, Layout layout) {
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

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Preset other = (Preset) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}
