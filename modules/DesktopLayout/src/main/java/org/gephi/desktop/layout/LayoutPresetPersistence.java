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
import java.util.Optional;
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
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;

/**
 * @author Mathieu Bastian
 */
public class LayoutPresetPersistence {

    private final Map<String, List<Preset>> presets = new HashMap<>();
    private final Map<String, String> defaultPresets = new HashMap<>();

    public LayoutPresetPersistence() {
        loadPresets();

        // Load defaults from preferences
        for(String layoutClassName : presets.keySet()) {
            String defaultPreset = NbPreferences.forModule(LayoutPresetPersistence.class).get("LayoutPresetPersistence_defaultPreset_"+layoutClassName, null);
            if (defaultPreset != null && hasPreset(defaultPreset, layoutClassName)) {
                defaultPresets.put(layoutClassName, defaultPreset);
                Logger.getLogger(LayoutPresetPersistence.class.getName()).log(Level.INFO, "Default preset for {0} loaded: {1}", new Object[]{layoutClassName, defaultPreset});
            }
        }
    }

    public boolean hasPreset(String name, String layoutClassName) {
        List<Preset> layoutPresets = presets.get(layoutClassName);
        return layoutPresets != null && layoutPresets.stream().anyMatch(p -> p.name.equals(name));
    }

    public Preset getPreset(String name, Layout layout) {
        List<Preset> layoutPresets = presets.get(layout.getClass().getName());
        if (layoutPresets == null) {
            return null;
        }
        Optional<Preset> preset = layoutPresets.stream()
            .filter(p -> p.name.equals(name))
            .findFirst();
        return preset.orElse(null);
    }

    public void setDefaultPresent(String name, Layout layout) {
        if (name == null) {
            defaultPresets.remove(layout.getClass().getName());
            NbPreferences.forModule(LayoutPresetPersistence.class).remove("LayoutPresetPersistence_defaultPreset_"+layout.getClass().getName());
        } else {
            defaultPresets.put(layout.getClass().getName(), name);
            NbPreferences.forModule(LayoutPresetPersistence.class).put("LayoutPresetPersistence_defaultPreset_"+layout.getClass().getName(), name);
        }
    }

    public boolean hasDefaultPreset(Layout layout) {
        return defaultPresets.containsKey(layout.getClass().getName());
    }

    public boolean isDefaultPreset(String name, Layout layout) {
        String defaultPreset = defaultPresets.get(layout.getClass().getName());
        return defaultPreset != null && defaultPreset.equals(name);
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

    public void deletePreset(Preset preset) {
        // Remove default preset if needed
        if (defaultPresets.containsKey(preset.layoutClassName) && defaultPresets.get(preset.layoutClassName).equals(preset.name)) {
            defaultPresets.remove(preset.layoutClassName);
        }
        List<Preset> layoutPresets = presets.get(preset.layoutClassName);
        layoutPresets.remove(preset);
        FileObject folder = FileUtil.getConfigFile("layoutpresets");
        if (folder != null) {
            FileObject file = folder.getFileObject(preset.name + ".xml");
            if (file != null) {
                try {
                    file.delete();
                } catch (IOException ex) {
                    Logger.getLogger("").log(Level.SEVERE, "Error while deleting preset file", ex);
                }
            }
        }
    }

    public Preset loadDefaultPreset(Layout layout) {
        String defaultPreset = defaultPresets.get(layout.getClass().getName());
        if (defaultPreset != null) {
            Preset preset = getPreset(defaultPreset, layout);
            if (preset != null) {
                return loadPreset(preset, layout);
            }
        } else {
            layout.resetPropertiesValues();
        }
        return null;
    }

    public Preset loadPreset(Preset preset, Layout layout) {
        for (LayoutProperty p : layout.getProperties()) {
            for (int i = 0; i < preset.propertyNames.size(); i++) {
                if (p.getCanonicalName().equalsIgnoreCase(preset.propertyNames.get(i))
                    || p.getProperty().getName().equalsIgnoreCase(preset.propertyNames
                    .get(i))) {//Also compare with property name to maintain compatibility with old presets
                    try {
                        p.getProperty().setValue(preset.propertyValues.get(i));
                    } catch (Exception e) {
                        Logger.getLogger("").log(Level.SEVERE, "Error while setting preset property", e);
                    }
                }
            }
        }
        return preset;
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
        List<Preset> layoutPresets = presets.computeIfAbsent(preset.layoutClassName, k -> new ArrayList<>());
        for (Preset p : layoutPresets) {
            if (p.equals(preset)) {
                return p;
            }
        }
        layoutPresets.add(preset);
        return preset;
    }

}
