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
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class FilterModelPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        FilterModelImpl filterModel = workspace.getLookup().lookup(FilterModelImpl.class);
        if (filterModel != null) {
            try {
                writeXML(writer, filterModel);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        FilterModelImpl filterModel = workspace.getLookup().lookup(FilterModelImpl.class);
        if (filterModel == null) {
            filterModel = new FilterModelImpl(workspace);
            workspace.add(filterModel);
        }
        try {
            readXML(reader, filterModel);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "filtermodel";
    }
    //PERSISTENCE
    private int queryId = 0;

    public void writeXML(XMLStreamWriter writer, FilterModelImpl model) throws XMLStreamException {
        writer.writeStartElement("autorefresh");
        writer.writeAttribute("value", String.valueOf(model.isAutoRefresh()));
        writer.writeEndElement();

        //Queries
        writer.writeStartElement("queries");
        queryId = 0;
        for (Query query : model.getQueries()) {
            writeQuery("query", writer, model, query, -1);
        }
        writer.writeEndElement();

        //Saved queries
        writer.writeStartElement("savedqueries");
        for (Query query : model.getLibrary().getLookup().lookupAll(Query.class)) {
            writeQuery("savedquery", writer, model, query, -1);
        }
        writer.writeEndElement();
    }

    private void writeQuery(String code, XMLStreamWriter writer, FilterModelImpl model, Query query, int parentId) throws XMLStreamException {
        writer.writeStartElement(code);
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
            writeQuery(code, writer, model, child, id);
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

    public void readXML(XMLStreamReader reader, FilterModelImpl model) throws XMLStreamException {
        Map<Integer, Query> idMap = new HashMap<>();
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("autorefresh".equalsIgnoreCase(name)) {
                    String val = reader.getAttributeValue(null, "value");
                    model.setAutoRefresh(Boolean.parseBoolean(val));
                } else if ("query".equalsIgnoreCase(name)) {
                    String id = reader.getAttributeValue(null, "id");
                    String parent = reader.getAttributeValue(null, "parent");
                    Query query = readQuery(reader, model);
                    if (query != null) {
                        idMap.put(Integer.parseInt(id), query);
                        if (parent != null) {
                            int parentId = Integer.parseInt(parent);
                            Query parentQuery = idMap.get(parentId);

                            //A plugin filter may be missing, or the parent filter could not be deserialized.
                            //For example a partition filter, which depends on partitions, and partitions are not serialized
                            if (parentQuery != null) {
                                model.setSubQuery(parentQuery, query);
                            }
                        } else {
                            //Top query
                            model.addFirst(query);
                        }
                    }
                } else if ("savedquery".equalsIgnoreCase(name)) {
                    String id = reader.getAttributeValue(null, "id");
                    String parent = reader.getAttributeValue(null, "parent");
                    Query query = readQuery(reader, model);
                    if (query != null) {
                        idMap.put(Integer.parseInt(id), query);
                        if (parent != null) {
                            int parentId = Integer.parseInt(parent);
                            Query parentQuery = idMap.get(parentId);

                            if (parentQuery != null) {
                                AbstractQueryImpl impl = (AbstractQueryImpl) parentQuery;
                                impl.addSubQuery(query);
                            }
                        } else {
                            model.getLibrary().saveQuery(query);
                        }
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("filtermodel".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }

        //Init filters
        Graph graph;
        graph = model.getGraphModel().getGraph();

        for (Query rootQuery : model.getQueries()) {
            for (Query q : rootQuery.getDescendantsAndSelf()) {
                Filter filter = q.getFilter();
                if (filter instanceof NodeFilter || filter instanceof EdgeFilter || filter instanceof ElementFilter) {
                    FilterProcessor filterProcessor = new FilterProcessor();
                    filterProcessor.init(filter, graph);
                }
            }
        }
    }

    private Query readQuery(XMLStreamReader reader, FilterModelImpl model) throws XMLStreamException {
        String builderClassName = reader.getAttributeValue(null, "builder");
        String filterClassName = reader.getAttributeValue(null, "filter");
        FilterBuilder builder = null;
        for (FilterBuilder fb : model.getLibrary().getLookup().lookupAll(FilterBuilder.class)) {
            if (fb.getClass().getName().equals(builderClassName)) {
                if (filterClassName != null) {
                    if (fb.getFilter(model.getWorkspace()).getClass().getName().equals(filterClassName)) {
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
                for (FilterBuilder fb : catBuilder.getBuilders(model.getWorkspace())) {
                    if (fb.getClass().getName().equals(builderClassName)) {
                        if (filterClassName != null) {
                            if (fb.getFilter(model.getWorkspace()).getClass().getName().equals(filterClassName)) {
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
            Filter filter = builder.getFilter(model.getWorkspace());
            Query query;
            if (filter instanceof Operator) {
                query = new OperatorQueryImpl((Operator) filter);
            } else {
                query = new FilterQueryImpl(builder, filter);
            }

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

                            if (editor instanceof AttributeColumnPropertyEditor) {
                                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                                GraphModel graphModel = gc.getGraphModel(model.getWorkspace());
                                ((AttributeColumnPropertyEditor) editor).setGraphModel(graphModel);
                            }

                            editor.setAsText(textValue);
                            property.setValue(editor.getValue());
                            model.updateParameters(query);

                            if (editor instanceof AttributeColumnPropertyEditor) {
                                ((AttributeColumnPropertyEditor) editor).setGraphModel(null);
                            }
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
