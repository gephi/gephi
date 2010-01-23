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
package org.gephi.filters.impl;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterModelImpl implements FilterModel {

    private FilterLibraryImpl filterLibraryImpl;
    private LinkedList<Query> queries;
    private Query currentQuery;
    private boolean filtering;
    //Listeners
    private List<ChangeListener> listeners;

    public FilterModelImpl() {
        filterLibraryImpl = new FilterLibraryImpl();
        queries = new LinkedList<Query>();
        listeners = new ArrayList<ChangeListener>();
    }

    public FilterLibrary getLibrary() {
        return filterLibraryImpl;
    }

    public Query[] getQueries() {
        return queries.toArray(new Query[0]);
    }

    public boolean hasQuery(Query query) {
        for (Query q : getQueries()) {
            if (q == query) {
                return true;
            }
        }
        return false;
    }

    public void addFirst(Query function) {
        queries.addFirst(function);
        fireChangeEvent();
    }

    public void addLast(Query function) {
        queries.addLast(function);
        fireChangeEvent();
    }

    public void set(int index, Query function) {
        queries.set(index, function);
    }

    public void remove(Query query) {
        queries.remove(query);
        fireChangeEvent();
    }

    public void rename(Query query, String name) {
        ((AbstractQueryImpl) query).setName(name);
        fireChangeEvent();
    }

    public void setSubQuery(Query query, Query subQuery) {
        //Clean
        if (queries.contains(subQuery)) {
            queries.remove(subQuery);
        }
        if (subQuery.getParent() != null) {
            ((AbstractQueryImpl) subQuery.getParent()).removeSubQuery(subQuery);
        }

        //Set
        AbstractQueryImpl impl = (AbstractQueryImpl) query;
        impl.addSubQuery(subQuery);
        fireChangeEvent();
    }

    public void removeSubQuery(Query query, Query parent) {
        AbstractQueryImpl impl = (AbstractQueryImpl) parent;
        impl.removeSubQuery(query);
        ((AbstractQueryImpl) query).setParent(null);
        fireChangeEvent();
    }

    public int getIndex(Query function) {
        int i = 0;
        for (Query f : queries) {
            if (f == function) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean isFiltering() {
        return currentQuery != null && filtering;
    }

    public boolean isSelecting() {
        return currentQuery != null && !filtering;
    }

    public void setFiltering(boolean filtering) {
        this.filtering = filtering;
    }

    public void setSelecting(boolean filtering) {
        this.filtering = filtering;
    }

    public Query getCurrentQuery() {
        return currentQuery;
    }

    public void setCurrentQuery(Query currentQuery) {
        this.currentQuery = currentQuery;
        fireChangeEvent();
    }

    public void propertyChanged(FilterProperty property) {
        Filter filter = property.getFilter();
        for (Query q : getAllQueries()) {
            if (filter == q.getFilter()) {
                if (q instanceof FilterQueryImpl) {
                    ((FilterQueryImpl) q).updateParameters();
                }
            }
        }
        System.out.println(property.getName() + " changed");
        fireChangeEvent();
    }

    public Query[] getAllQueries() {
        List<Query> result = new ArrayList<Query>();
        LinkedList<Query> stack = new LinkedList<Query>();
        stack.addAll(queries);
        while (!stack.isEmpty()) {
            Query q = stack.pop();
            result.add(q);
            for (Query child : q.getChildren()) {
                stack.add(child);
            }
        }
        return result.toArray(new Query[0]);
    }

    public void addChangeListener(ChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(evt);
        }
    }
    //PERSISTENCE
    private int queryId = 0;

    public Element writeXML(Document document) {
        Element filterModelE = document.createElement("filtermodel");

        //Queries
        Element queriesE = document.createElement("queries");
        queryId = 0;
        for (Query query : queries) {
            writeQuery(document, queriesE, query, -1);
        }
        filterModelE.appendChild(queriesE);
        return filterModelE;
    }

    private void writeQuery(Document document, Element parentElement, Query query, int parentId) {
        Element queryE = document.createElement("query");
        int id = queryId++;
        queryE.setAttribute("id", String.valueOf(id));
        if (parentId != -1) {
            queryE.setAttribute("parent", String.valueOf(parentId));
        }
        //Params
        for (int i = 0; i < query.getParametersCount(); i++) {
            FilterProperty prop = query.getFilter().getProperties()[i];
            Element paramE = writeParameter(document, i, prop.getProperty());
            if (paramE != null) {
                queryE.appendChild(paramE);
            }
        }
        //Filter
        FilterBuilder builder = filterLibraryImpl.getBuilder(query.getFilter());
        queryE.setAttribute("builder", builder.getClass().getName());

        parentElement.appendChild(queryE);

        for (Query child : query.getChildren()) {
            writeQuery(document, parentElement, child, id);
        }
    }

    private Element writeParameter(Document document, int index, Property property) {
        Element parameterE = document.createElement("parameter");
        parameterE.setAttribute("index", String.valueOf(index));
        try {
            PropertyEditor editor = property.getPropertyEditor();
            if (editor == null) {
                editor = PropertyEditorManager.findEditor(property.getValueType());
            }
            if (editor == null) {
                return null;
            }
            editor.setValue(property.getValue());
            parameterE.setTextContent(editor.getAsText());
            return parameterE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void readXML(Element filterModelE) {
        queries.clear();

        Map<Integer, Query> idMap = new HashMap<Integer, Query>();
        NodeList queryList = filterModelE.getElementsByTagName("query");
        for (int i = 0; i < queryList.getLength(); i++) {
            Node n = queryList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element queryE = (Element) n;
                Query query = readQuery(queryE);
                if (query != null) {
                    idMap.put(Integer.parseInt(queryE.getAttribute("id")), query);
                    if (queryE.hasAttribute("parent")) {
                        int parentId = Integer.parseInt(queryE.getAttribute("parent"));
                        Query parentQuery = idMap.get(parentId);
                        setSubQuery(parentQuery, query);
                    } else {
                        //Top query
                        addFirst(query);
                    }
                }
            }
        }
    }

    private Query readQuery(Element queryE) {
        String builderClassName = queryE.getAttribute("builder");
        FilterBuilder builder = null;
        for (FilterBuilder fb : filterLibraryImpl.getLookup().lookupAll(FilterBuilder.class)) {
            if (fb.getClass().getName().equals(builderClassName)) {
                builder = fb;
            }
        }
        if (builder != null) {
            //Create filter
            Filter filter = builder.getFilter();
            FilterController fc = Lookup.getDefault().lookup(FilterController.class);
            Query query = fc.createQuery(filter);

            //Params
            NodeList paramList = queryE.getElementsByTagName("parameter");
            for (int i = 0; i < paramList.getLength(); i++) {
                Node n = paramList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element paramE = (Element) n;
                    int index = Integer.parseInt(paramE.getAttribute("index"));
                    Property property = query.getFilter().getProperties()[index].getProperty();
                    try {
                        PropertyEditor editor = property.getPropertyEditor();
                        if (editor == null) {
                            editor = PropertyEditorManager.findEditor(property.getValueType());
                        }
                        if (editor != null) {
                            String textValue = paramE.getTextContent();
                            editor.setAsText(textValue);
                            property.setValue(editor.getValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return query;
        }
        return null;
    }
}
