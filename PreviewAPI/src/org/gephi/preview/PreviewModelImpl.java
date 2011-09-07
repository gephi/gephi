/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.preview;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.presets.DefaultPreset;
import org.gephi.preview.spi.Renderer;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewModelImpl implements PreviewModel {
    
    private final Workspace workspace;
    //Items
    private final Map<String, List<Item>> typeMap;
    private final Map<Object, Object> sourceMap;
    //Properties
    private PreviewProperties properties;
    //Dimensions
    private Dimension dimensions;
    private Point topLeftPosition;
    
    public PreviewModelImpl(Workspace workspace) {
        typeMap = new HashMap<String, List<Item>>();
        sourceMap = new HashMap<Object, Object>();
        this.workspace = workspace;
    }
    
    private synchronized void initProperties() {
        if (properties == null) {
            properties = new PreviewProperties();

            //Properties from renderers
            for (Renderer renderer : Lookup.getDefault().lookupAll(Renderer.class)) {
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
            typeList = new ArrayList<Item>(items.length);
            typeList.addAll(Arrays.asList(items));
            typeMap.put(type, typeList);

            //Add to source map
            for (Item item : items) {
                Object value = sourceMap.get(item.getSource());
                if (value == null) {
                    sourceMap.put(item.getSource(), item);
                } else if (value instanceof List) {
                    ((List) value).add(item);
                } else {
                    List<Item> list = new ArrayList<Item>();
                    list.add((Item) value);
                    list.add(item);
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
    public Dimension getDimensions() {
        return dimensions;
    }
    
    @Override
    public Point getTopLeftPosition() {
        return topLeftPosition;
    }
    
    public void setDimensions(Dimension dimensions) {
        this.dimensions = dimensions;
    }
    
    public void setTopLeftPosition(Point topLeftPosition) {
        this.topLeftPosition = topLeftPosition;
    }

    //PERSISTENCE
    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("previewmodel");
        
        for (PreviewProperty property : properties.getProperties()) {
            String propertyName = property.getName();
            Object propertyValue = property.getValue();
            if (propertyValue != null) {
                PropertyEditor editor = PropertyEditorManager.findEditor(propertyValue.getClass());
                if (editor != null) {
                    writer.writeStartElement("previewproperty");
                    writer.writeAttribute("name", propertyName);
                    editor.setValue(propertyValue);
                    writer.writeCharacters(editor.getAsText());
                    writer.writeEndElement();
                }
            }
        }
        
        writer.writeEndElement();
    }
    
    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        PreviewProperties props = getProperties();
        
        String propName = null;
        
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();
            
            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("previewproperty".equalsIgnoreCase(name)) {
                        propName = reader.getAttributeValue(null, "name");
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        if (propName != null) {
                            PreviewProperty p = props.getProperty(propName);
                            if (p != null) {
                                PropertyEditor editor = PropertyEditorManager.findEditor(p.getType());
                                if (editor != null) {
                                    editor.setAsText(reader.getText());
                                    if (editor.getValue() != null) {
                                        try {
                                            p.setValue(editor.getValue());
                                        } catch (Exception e) {
                                            e.printStackTrace();
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
                    name = null;
                    break;
            }
        }
    }
}
