/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.preview.api.PreviewPreset;
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
public class PresetUtils {

    private List<PreviewPreset> presets;

    public void savePreset(PreviewPreset preset) {
        int exist = -1;
        for (int i = 0; i < presets.size(); i++) {
            PreviewPreset p = presets.get(i);
            if (p.getName().equals(preset.getName())) {
                exist = i;
                break;
            }
        }
        if (exist == -1) {
            addPreset(preset);
        } else {
            presets.set(exist, preset);
        }

        try {
            //Create file if dont exist
            FileObject folder = FileUtil.getConfigFile("previewpresets");
            if (folder == null) {
                folder = FileUtil.getConfigRoot().createFolder("previewpresets");
            }
            FileObject presetFile = folder.getFileObject(preset.getName(), "xml");
            if (presetFile == null) {
                presetFile = folder.createData(preset.getName(), "xml");
            }

            //Create doc
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            //Write doc
            writeXML(document, preset);

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

    public PreviewPreset[] getPresets() {
        if (presets == null) {
            presets = new ArrayList<PreviewPreset>();
            loadPresets();
        }
        return presets.toArray(new PreviewPreset[0]);
    }

    private void loadPresets() {
        FileObject folder = FileUtil.getConfigFile("previewpresets");
        if (folder != null) {
            for (FileObject child : folder.getChildren()) {
                if (child.isValid() && child.hasExt("xml")) {
                    try {
                        InputStream stream = child.getInputStream();
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(stream);
                        PreviewPreset preset = readXML(document);
                        addPreset(preset);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void writeXML(Document doc, PreviewPreset preset) {
        Element presetE = doc.createElement("previewpreset");
        presetE.setAttribute("name", preset.getName());
        presetE.setAttribute("version", "0.7");

        for (Entry<String, String> entry : preset.getProperties().entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();

            Element propertyE = doc.createElement("previewproperty");
            propertyE.setAttribute("name", propertyName);
            propertyE.setTextContent(propertyValue);
            presetE.appendChild(propertyE);
        }
        doc.appendChild(presetE);
    }

    private PreviewPreset readXML(Document document) {
        Element presetE = document.getDocumentElement();
        Map<String, String> propertiesMap = new HashMap<String, String>();

        NodeList propertyList = presetE.getElementsByTagName("previewproperty");
        for (int i = 0; i < propertyList.getLength(); i++) {
            Node n = propertyList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element propertyE = (Element) n;
                String name = propertyE.getAttribute("name");
                String value = propertyE.getTextContent();
                if (!value.isEmpty()) {
                    propertiesMap.put(name, value);
                }
            }
        }
        PreviewPreset preset = new PreviewPreset(presetE.getAttribute("name"), propertiesMap);
        return preset;
    }

    private void addPreset(PreviewPreset preset) {
        presets.add(preset);
    }
}
