/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian
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
package org.gephi.preview;

import java.beans.PropertyEditorManager;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.preview.api.*;
import org.gephi.preview.presets.DefaultPreset;
import org.gephi.preview.spi.MouseResponsiveRenderer;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.preview.types.editors.BasicDependantColorPropertyEditor;
import org.gephi.preview.types.editors.BasicDependantOriginalColorPropertyEditor;
import org.gephi.preview.types.editors.BasicEdgeColorPropertyEditor;
import org.gephi.project.api.Workspace;
import org.gephi.utils.Serialization;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewModelImpl implements PreviewModel {

    private final PreviewController previewController;
    private final Workspace workspace;
    //Items
    private final Map<String, List<Item>> typeMap;
    private final Map<Object, Object> sourceMap;
    //Renderers
    private ManagedRenderer[] managedRenderers;
    //Mouse listeners (of enabled renderers)
    private PreviewMouseListener[] enabledMouseListeners;
    //Properties
    private PreviewProperties properties;

    public PreviewModelImpl(Workspace workspace) {
        this(workspace, null);
    }

    public PreviewModelImpl(Workspace workspace, PreviewController previewController) {
        if (previewController != null) {
            this.previewController = previewController;
        } else {
            this.previewController = Lookup.getDefault().lookup(PreviewController.class);
        }
        typeMap = new HashMap<>();
        sourceMap = new HashMap<>();
        this.workspace = workspace;

        initBasicPropertyEditors();
        initManagedRenderers();
        prepareManagedListeners();
    }

    /**
     * Makes sure that, at least, basic property editors are available for serializing and deserializing
     */
    private void initBasicPropertyEditors() {
        if (PropertyEditorManager.findEditor(DependantColor.class) == null) {
            PropertyEditorManager.registerEditor(DependantColor.class, BasicDependantColorPropertyEditor.class);
        }
        if (PropertyEditorManager.findEditor(DependantOriginalColor.class) == null) {
            PropertyEditorManager.registerEditor(DependantOriginalColor.class, BasicDependantOriginalColorPropertyEditor.class);
        }
        if (PropertyEditorManager.findEditor(EdgeColor.class) == null) {
            PropertyEditorManager.registerEditor(EdgeColor.class, BasicEdgeColorPropertyEditor.class);
        }
    }

    /**
     * Makes sure that, if more than one plugin extends a default renderer, only the one with the lowest position is enabled initially.
     */
    private void initManagedRenderers() {
        Renderer[] registeredRenderers = previewController.getRegisteredRenderers();

        Set<String> replacedRenderers = new HashSet<>();

        managedRenderers = new ManagedRenderer[registeredRenderers.length];
        for (int i = 0; i < registeredRenderers.length; i++) {
            Renderer r = registeredRenderers[i];
            Class superClass = r.getClass().getSuperclass();
            if (superClass != null && superClass.getName().startsWith("org.gephi.preview.plugin.renderers.")) {
                managedRenderers[i] = new ManagedRenderer(r, !replacedRenderers.contains(superClass.getName()));
                replacedRenderers.add(superClass.getName());
            } else {
                managedRenderers[i] = new ManagedRenderer(r, true);
            }
        }
    }

    private void prepareManagedListeners() {
        ArrayList<PreviewMouseListener> listeners = new ArrayList<>();

        for (PreviewMouseListener listener : Lookup.getDefault().lookupAll(PreviewMouseListener.class)) {
            for (Renderer renderer : getManagedEnabledRenderers()) {
                if (renderer instanceof MouseResponsiveRenderer) {
                    if (((MouseResponsiveRenderer) renderer).needsPreviewMouseListener(listener) && !listeners.contains(listener)) {
                        listeners.add(listener);
                    }
                }
            }
        }

        Collections.reverse(listeners);//First listeners to receive events will be the ones coming from last called renderers.
        enabledMouseListeners = listeners.toArray(new PreviewMouseListener[0]);
    }

    private synchronized void initProperties() {
        if (properties == null) {
            properties = new PreviewProperties();

            //Properties from renderers
            for (Renderer renderer : getManagedEnabledRenderers()) {
                PreviewProperty[] props = renderer.getProperties();
                for (PreviewProperty p : props) {
                    properties.addProperty(p);
                }
            }

            //Default preset
            properties.applyPreset(new DefaultPreset());

            //Defaut values
            properties.putValue(PreviewProperty.VISIBILITY_RATIO, 1f);
        }
    }

    @Override
    public PreviewProperties getProperties() {
        initProperties();
        return properties;
    }

    @Override
    public Item[] getItems(String type) {
        List<Item> list = typeMap.get(type);
        if (list != null) {
            return list.toArray(new Item[0]);
        }
        return new Item[0];
    }

    @Override
    public Item getItem(String type, Object source) {
        Item[] items = getItems(source);
        for (Item item : items) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Item[] getItems(Object source) {
        Object value = sourceMap.get(source);
        if (value instanceof List) {
            return ((List<Item>) value).toArray(new Item[0]);
        } else if (value instanceof Item) {
            return new Item[]{(Item) value};
        }
        return new Item[0];
    }

    public String[] getItemTypes() {
        return typeMap.keySet().toArray(new String[0]);
    }

    public void loadItems(String type, Item[] items) {
        //Add to type map
        List<Item> typeList = typeMap.get(type);
        if (typeList == null) {
            typeList = new ArrayList<>(items.length);
            typeList.addAll(Arrays.asList(items));
            typeMap.put(type, typeList);

            //Add to source map
            for (Item item : items) {
                Object value = sourceMap.get(item.getSource());
                if (value == null) {
                    sourceMap.put(item.getSource(), item);
                } else if (value instanceof List) {
                    ((List) value).add(item);
                }
            }
        } else {
            //Possible items to merge
            for (Item item : items) {
                Object value = sourceMap.get(item.getSource());
                if (value == null) {
                    //No other object attached to this item
                    typeList.add(item);
                    sourceMap.put(item.getSource(), item);
                } else if (value instanceof Item && ((Item) value).getType().equals(item.getType())) {
                    //An object already exists with the same type and source, merge them
                    mergeItems(item, ((Item) value));
                } else if (value instanceof List) {
                    List<Item> list = (List<Item>) value;
                    for (Item itemSameSource : list) {
                        if (itemSameSource.getType().equals(item.getType())) {
                            //An object already exists with the same type and source, merge them
                            mergeItems(item, itemSameSource);
                            break;
                        }
                    }
                }
            }
        }
    }

    private Item mergeItems(Item item, Item toBeMerged) {
        for (String key : toBeMerged.getKeys()) {
            item.setData(key, toBeMerged.getData(key));
        }
        return item;
    }

    public void clear() {
        typeMap.clear();
        sourceMap.clear();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public CanvasSize getGraphicsCanvasSize() {
        float x1 = Float.MAX_VALUE;
        float y1 = Float.MAX_VALUE;
        float x2 = Float.MIN_VALUE;
        float y2 = Float.MIN_VALUE;
        for (Renderer r : getManagedEnabledRenderers()) {
            for (String type : getItemTypes()) {
                for (Item item : getItems(type)) {
                    if (r.isRendererForitem(item, getProperties())) {
                        CanvasSize cs = r.getCanvasSize(item, getProperties());
                        x1 = Math.min(x1, cs.getX());
                        y1 = Math.min(y1, cs.getY());
                        x2 = Math.max(x2, cs.getMaxX());
                        y2 = Math.max(y2, cs.getMaxY());
                    }
                }
            }
        }
        return new CanvasSize(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public ManagedRenderer[] getManagedRenderers() {
        return managedRenderers;
    }

    /**
     * Makes sure that managedRenderers contains every renderer existing implementations. If some renderers are not in the list, they are added in default implementation order at the end of the list
     * and not enabled.
     */
    private void completeManagedRenderersListIfNecessary() {
        if (managedRenderers != null) {
            Set<String> existing = new HashSet<>();
            for (ManagedRenderer mr : managedRenderers) {
                existing.add(mr.getRenderer().getClass().getName());
            }

            List<ManagedRenderer> completeManagedRenderersList = new ArrayList<>();
            completeManagedRenderersList.addAll(Arrays.asList(managedRenderers));

            for (Renderer renderer : previewController.getRegisteredRenderers()) {
                if (!existing.contains(renderer.getClass().getName())) {
                    completeManagedRenderersList.add(new ManagedRenderer(renderer, false));
                }
            }

            managedRenderers = completeManagedRenderersList.toArray(new ManagedRenderer[0]);
        }
    }

    /**
     * Removes unnecessary properties from not enabled renderers
     */
    private void reloadProperties() {
        if(properties == null){
            initProperties();
        }else{
            PreviewProperties newProperties = new PreviewProperties();//Ensure that the properties object doesn't change

            //Properties from renderers
            for (Renderer renderer : getManagedEnabledRenderers()) {
                PreviewProperty[] props = renderer.getProperties();
                for (PreviewProperty p : props) {
                    newProperties.addProperty(p);
                    if (properties.hasProperty(p.getName())) {
                        newProperties.putValue(p.getName(), properties.getValue(p.getName()));//Keep old values
                    }
                }
            }

            //Remove old properties (this keeps simple values)
            for (PreviewProperty p : properties.getProperties()) {
                properties.removeProperty(p);
            }

            //Set new properties
            for (PreviewProperty property : newProperties.getProperties()) {
                properties.addProperty(property);
            }
        }
    }

    @Override
    public void setManagedRenderers(ManagedRenderer[] managedRenderers) {
        //Validate no null ManagedRenderers
        for (ManagedRenderer managedRenderer : managedRenderers) {
            if (managedRenderer == null) {
                throw new IllegalArgumentException("managedRenderers should not contain null values");
            }
        }

        this.managedRenderers = managedRenderers;
        completeManagedRenderersListIfNecessary();
        prepareManagedListeners();
        reloadProperties();
    }

    @Override
    public Renderer[] getManagedEnabledRenderers() {
        if (managedRenderers != null) {
            ArrayList<Renderer> renderers = new ArrayList<>();
            for (ManagedRenderer mr : managedRenderers) {
                if (mr.isEnabled()) {
                    renderers.add(mr.getRenderer());
                }
            }
            return renderers.toArray(new Renderer[0]);
        } else {
            return null;
        }
    }

    //PERSISTENCE
    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        initProperties();
        //Write PreviewProperties:
        for (PreviewProperty property : properties.getProperties()) {
            String propertyName = property.getName();
            Object propertyValue = property.getValue();
            if (propertyValue != null) {
                String text = Serialization.getValueAsText(propertyValue);
                if (text != null) {
                    writer.writeStartElement("previewproperty");
                    writer.writeAttribute("name", propertyName);
                    writer.writeCharacters(text);
                    writer.writeEndElement();
                }
            }
        }

        //Write preview simple values:
        Iterator<Entry<String, Object>> simpleValuesIterator = properties.getSimpleValues().iterator();
        while (simpleValuesIterator.hasNext()) {
            Entry<String, Object> simpleValueEntry;
            simpleValueEntry = simpleValuesIterator.next();

            if (simpleValueEntry.getKey().equals("width")
                    || simpleValueEntry.getKey().equals("height")) {
                continue;
            }

            Object value = simpleValueEntry.getValue();
            if (value != null) {
                Class clazz = value.getClass();
                String text = Serialization.getValueAsText(value);
                if (text != null) {
                    writer.writeStartElement("previewsimplevalue");
                    writer.writeAttribute("name", simpleValueEntry.getKey());
                    writer.writeAttribute("class", clazz.getName());
                    writer.writeCharacters(text);
                    writer.writeEndElement();
                }
            }
        }

        //Write model managed renderers:
        if (managedRenderers != null) {
            for (ManagedRenderer managedRenderer : managedRenderers) {
                writer.writeStartElement("managedrenderer");
                writer.writeAttribute("class", managedRenderer.getRenderer().getClass().getName());
                writer.writeAttribute("enabled", String.valueOf(managedRenderer.isEnabled()));
                writer.writeEndElement();
            }
        }
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        PreviewProperties props = getProperties();

        String propName = null;
        boolean isSimpleValue = false;
        String simpleValueClass = null;

        List<ManagedRenderer> managedRenderersList = new ArrayList<>();
        Map<String, Renderer> availableRenderers = new HashMap<>();
        for (Renderer renderer : Lookup.getDefault().lookupAll(Renderer.class)) {
            availableRenderers.put(renderer.getClass().getName(), renderer);
            Class superClass = renderer.getClass().getSuperclass();
            if (superClass != null && superClass.getName().startsWith("org.gephi.preview.plugin.renderers.")) {
                availableRenderers.put(superClass.getName(), renderer);//For plugins replacing a default renderer
            }
        }

        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("previewproperty".equalsIgnoreCase(name)) {
                        propName = reader.getAttributeValue(null, "name");
                        isSimpleValue = false;
                    } else if ("previewsimplevalue".equalsIgnoreCase(name)) {
                        propName = reader.getAttributeValue(null, "name");
                        simpleValueClass = reader.getAttributeValue(null, "class");
                        isSimpleValue = true;
                    } else if ("managedrenderer".equalsIgnoreCase(name)) {
                        String rendererClass = reader.getAttributeValue(null, "class");
                        if (availableRenderers.containsKey(rendererClass)) {
                            managedRenderersList.add(new ManagedRenderer(availableRenderers.get(rendererClass), Boolean.parseBoolean(reader.getAttributeValue(null, "enabled"))));
                        }
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        if (propName != null) {
                            if (!isSimpleValue) {//Read PreviewProperty:
                                PreviewProperty p = props.getProperty(propName);
                                if (p != null) {
                                    Object value = Serialization.readValueFromText(reader.getText(), p.getType());
                                    if (value != null) {
                                        p.setValue(value);
                                    }
                                }
                            } else {//Read preview simple value:
                                if (simpleValueClass != null) {
                                    if (!propName.equals("width")
                                            && !propName.equals("height")) {
                                        Object value = Serialization.readValueFromText(reader.getText(), simpleValueClass);
                                        if (value != null) {
                                            props.putValue(propName, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("previewmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    propName = null;
                    break;
            }
        }

        if (!managedRenderersList.isEmpty()) {
            setManagedRenderers(managedRenderersList.toArray(new ManagedRenderer[0]));
        }
    }

    @Override
    public PreviewMouseListener[] getEnabledMouseListeners() {
        return enabledMouseListeners;
    }
}
