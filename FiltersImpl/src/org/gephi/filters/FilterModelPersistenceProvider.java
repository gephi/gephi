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
        FilterModelImpl filterModel = workspace.getLookup().lookup(FilterModelImpl.class);
        if (filterModel == null) {
            filterModel = new FilterModelImpl(workspace);
            workspace.add(filterModel);
        }
        this.model = filterModel;
        try {
            readXML(reader);
        } catch (XMLStreamException ex) {
            this.model = null;
            throw new RuntimeException(ex);
        }
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
