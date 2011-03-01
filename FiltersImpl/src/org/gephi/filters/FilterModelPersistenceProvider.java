/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.filters;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class FilterModelPersistenceProvider implements WorkspacePersistenceProvider {

    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        FilterModelImpl filterModel = workspace.getLookup().lookup(FilterModelImpl.class);
        if (filterModel != null) {
            this.model = filterModel;
            try {
                writeXML(writer);
            } catch (XMLStreamException ex) {
                this.model = null;
                throw new RuntimeException(ex);
            }
        }
        this.model = null;
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) {
        FilterModelImpl filterModel = new FilterModelImpl(workspace);
        this.model = filterModel;
        try {
            readXML(reader);
        } catch (XMLStreamException ex) {
            this.model = null;
            throw new RuntimeException(ex);
        }
        workspace.add(filterModel);
        this.model = null;
    }

    public String getIdentifier() {
        return "filtermodel";
    }
    //PERSISTENCE
    private int queryId = 0;
    private FilterModelImpl model;

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("filtermodel");
        writer.writeAttribute("autorefresh", String.valueOf(model.isAutoRefresh()));

        //Queries
        writer.writeStartElement("queries");
        queryId = 0;
        for (Query query : model.getQueries()) {
            writeQuery(writer, query, -1);
        }
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeQuery(XMLStreamWriter writer, Query query, int parentId) throws XMLStreamException {
        writer.writeStartElement("query");
        int id = queryId++;
        writer.writeAttribute("id", String.valueOf(id));
        if (parentId != -1) {
            writer.writeAttribute("parent", String.valueOf(parentId));
        }
        Filter filter = query.getFilter();
        FilterBuilder builder = model.getLibrary().getBuilder(filter);
        writer.writeAttribute("builder", builder.getClass().getName());
        writer.writeAttribute("filter", filter.getClass().getName());

        //Params
        for (int i = 0; i < query.getPropertiesCount(); i++) {
            FilterProperty prop = query.getFilter().getProperties()[i];
            writeParameter(writer, i, prop);
        }

        writer.writeEndElement();

        for (Query child : query.getChildren()) {
            writeQuery(writer, child, id);
        }
    }

    private void writeParameter(XMLStreamWriter writer, int index, FilterProperty property) {
        try {
            PropertyEditor editor = property.getPropertyEditor();
            if (editor == null) {
                editor = PropertyEditorManager.findEditor(property.getValueType());
            }
            if (editor == null) {
                return;
            }
            Object val = property.getValue();
            editor.setValue(val);
            writer.writeStartElement("parameter");
            writer.writeAttribute("index", String.valueOf(index));
            writer.writeCharacters(editor.getAsText());
            writer.writeEndElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readXML(XMLStreamReader reader) throws XMLStreamException {
        String autofresh = reader.getAttributeValue(null, "autorefresh");
        if (autofresh != null && !autofresh.isEmpty()) {
            model.setAutoRefresh(Boolean.parseBoolean(autofresh));
        }

        Map<Integer, Query> idMap = new HashMap<Integer, Query>();
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("query".equalsIgnoreCase(name)) {
                    String id = reader.getAttributeValue(null, "id");
                    String parent = reader.getAttributeValue(null, "parent");
                    Query query = readQuery(reader);
                    if (query != null) {
                        idMap.put(Integer.parseInt(id), query);
                        if (parent != null) {
                            int parentId = Integer.parseInt(parent);
                            Query parentQuery = idMap.get(parentId);
                            model.setSubQuery(parentQuery, query);
                        } else {
                            //Top query
                            model.addFirst(query);
                        }
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("filtermodel".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    private Query readQuery(XMLStreamReader reader) throws XMLStreamException {
        String builderClassName = reader.getAttributeValue(null, "builder");
        String filterClassName = reader.getAttributeValue(null, "filter");
        FilterBuilder builder = null;
        for (FilterBuilder fb : model.getLibrary().getLookup().lookupAll(FilterBuilder.class)) {
            if (fb.getClass().getName().equals(builderClassName)) {
                if (filterClassName != null) {
                    if (fb.getFilter().getClass().getName().equals(filterClassName)) {
                        builder = fb;
                        break;
                    }
                } else {
                    builder = fb;
                    break;
                }
            }
        }
        if (builder == null) {
            for (CategoryBuilder catBuilder : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
                for (FilterBuilder fb : catBuilder.getBuilders()) {
                    if (fb.getClass().getName().equals(builderClassName)) {
                        if (filterClassName != null) {
                            if (fb.getFilter().getClass().getName().equals(filterClassName)) {
                                builder = fb;
                                break;
                            }
                        } else {
                            builder = fb;
                            break;
                        }
                    }
                }
            }
        }

        if (builder != null) {
            //Create filter
            Filter filter = builder.getFilter();
            FilterController fc = Lookup.getDefault().lookup(FilterController.class);
            Query query = fc.createQuery(filter);

            FilterProperty property = null;
            boolean end = false;
            while (reader.hasNext() && !end) {
                Integer eventType = reader.next();
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String name = reader.getLocalName();
                    if ("parameter".equalsIgnoreCase(name)) {
                        int index = Integer.parseInt(reader.getAttributeValue(null, "index"));
                        property = query.getFilter().getProperties()[index];
                    }
                } else if (eventType.equals(XMLStreamReader.CHARACTERS) && property != null) {
                    try {
                        PropertyEditor editor = property.getPropertyEditor();
                        if (editor == null) {
                            editor = PropertyEditorManager.findEditor(property.getValueType());
                        }
                        if (editor != null) {
                            String textValue = reader.getText();
                            editor.setAsText(textValue);
                            property.setValue(editor.getValue());
                            model.updateParameters(query);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                    property = null;
                    if ("query".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                }
            }
            return query;
        }
        return null;
    }
}
