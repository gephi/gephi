package org.gephi.desktop.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.desktop.attributes.api.AttributesUIModel;
import org.gephi.desktop.attributes.options.AttributesPreferences;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.Model;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;

public class AttributesUIModelImpl implements AttributesUIModel, Model, WorkspaceXMLPersistenceProvider {

    private final Workspace workspace;
    private final GraphModel graphModel;
    private final Set<String> hiddenNodeColumnIds = new HashSet<>();
    private final Set<String> hiddenEdgeColumnIds = new HashSet<>();
    private boolean editMode = false;
    private boolean showNullColumns = AttributesPreferences.isShowNullColumns();
    private boolean includeProperties = AttributesPreferences.isIncludeProperties();
    private Node[] selectedNodes = null;
    private Edge[] selectedEdges = null;

    public AttributesUIModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = workspace.getLookup().lookup(GraphModel.class);

        if (graphModel == null) {
            return;
        }

        // Hidden by default
        hiddenNodeColumnIds.add(craftColumnId(graphModel.defaultColumns().nodeTimeSet()));
        hiddenNodeColumnIds.add(craftColumnId(graphModel.defaultColumns().degree()));
        hiddenNodeColumnIds.add(craftColumnId(graphModel.defaultColumns().inDegree()));
        hiddenNodeColumnIds.add(craftColumnId(graphModel.defaultColumns().outDegree()));
        hiddenEdgeColumnIds.add(craftColumnId(graphModel.defaultColumns().edgeTimeSet()));
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }


    public GraphModel getGraphModel() {
        return graphModel;
    }

    public List<Column> getEligibleColumns() {
        if (selectedEdges != null) {
            return getEligibleEdgeColumns();
        }
        return getEligibleNodeColumns();
    }

    public List<Column> getEligibleNodeColumns() {
        Table nodeTable = graphModel.getNodeTable();
        final boolean directed = graphModel.isDirected();
        final Column nodeTimesetCol = graphModel.defaultColumns().nodeTimeSet();
        final Column nodeDegreeCol = graphModel.defaultColumns().degree();
        final Column inDegreeCol = graphModel.defaultColumns().inDegree();
        final Column outDegreeCol = graphModel.defaultColumns().outDegree();
        List<Column> cols = StreamSupport.stream(nodeTable.spliterator(), false)
            .filter(column -> {
                if (column == nodeTimesetCol && graphModel.isDynamic()) {
                    return true;
                }
                if (column == nodeDegreeCol) {
                    return !editMode;
                }
                return !column.isProperty();
            }).collect(Collectors.toCollection(ArrayList::new));
        if (!editMode) {
            if (directed) {
                cols.add(0, outDegreeCol);
                cols.add(0, inDegreeCol);
            }
            cols.add(0, nodeDegreeCol);
        }
        return cols;
    }

    public List<Column> getEligibleEdgeColumns() {
        Table edgeTable = graphModel.getEdgeTable();
        final Column edgeTimesetCol = graphModel.defaultColumns().edgeTimeSet();
        return StreamSupport.stream(edgeTable.spliterator(), false)
            .filter(column -> {
                if (column == edgeTimesetCol && graphModel.isDynamic()) {
                    return true;
                }
                return !column.isProperty();
            }).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Column> getSelectedColumns() {
        return getEligibleColumns().stream().filter(this::isColumnVisible).toList();
    }

    public Node[] getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(Node[] selectedNodes) {
        this.selectedNodes = selectedNodes;
        this.selectedEdges = null;
    }

    public void resetSelection() {
        this.selectedNodes = null;
        this.selectedEdges = null;
    }

    public Edge[] getSelectedEdges() {
        return selectedEdges;
    }

    public void setSelectedEdges(Edge[] selectedEdges) {
        this.selectedEdges = selectedEdges;
        this.selectedNodes = null;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isShowNullColumns() {
        return showNullColumns;
    }

    public void setShowNullColumns(boolean showNullColumns) {
        this.showNullColumns = showNullColumns;
    }

    public boolean isIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
    }

    public boolean isColumnVisible(Column column) {
        return !getHiddenSetForColumn(column).contains(craftColumnId(column));
    }

    private Set<String> getHiddenSetForColumn(Column column) {
        if (column.getTable() == graphModel.getEdgeTable()) {
            return hiddenEdgeColumnIds;
        }
        return hiddenNodeColumnIds;
    }

    private String craftColumnId(Column column) {
        String id = column.getId();
        if (column.isProperty()) {
            id = "_property_" + id;
        }
        return id;
    }

    public void setColumnHidden(Column column, boolean hidden) {
        String columnId = craftColumnId(column);
        Set<String> hiddenSet = getHiddenSetForColumn(column);
        if (hidden) {
            hiddenSet.add(columnId);
        } else {
            hiddenSet.remove(columnId);
        }
    }

    public Set<String> getHiddenNodeColumnIds() {
        return Collections.unmodifiableSet(hiddenNodeColumnIds);
    }

    public Set<String> getHiddenEdgeColumnIds() {
        return Collections.unmodifiableSet(hiddenEdgeColumnIds);
    }

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        try {
            writer.writeStartElement("editmode");
            writer.writeCharacters(String.valueOf(editMode));
            writer.writeEndElement();

            writer.writeStartElement("shownullcolumns");
            writer.writeCharacters(String.valueOf(showNullColumns));
            writer.writeEndElement();

            writer.writeStartElement("includeproperties");
            writer.writeCharacters(String.valueOf(includeProperties));
            writer.writeEndElement();

            for (String columnId : hiddenNodeColumnIds) {
                writer.writeStartElement("hiddencolumn");
                writer.writeAttribute("id", columnId);
                writer.writeEndElement();
            }

            for (String columnId : hiddenEdgeColumnIds) {
                writer.writeStartElement("hiddenedgecolumn");
                writer.writeAttribute("id", columnId);
                writer.writeEndElement();
            }
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        try {
            boolean end = false;
            boolean readHiddenNodeColumns = false;
            boolean readHiddenEdgeColumns = false;
            while (reader.hasNext() && !end) {
                int eventType = reader.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("editmode".equalsIgnoreCase(name)) {
                        editMode = Boolean.parseBoolean(reader.getElementText());
                    } else if ("shownullcolumns".equalsIgnoreCase(name)) {
                        showNullColumns = Boolean.parseBoolean(reader.getElementText());
                    } else if ("includeproperties".equalsIgnoreCase(name)) {
                        includeProperties = Boolean.parseBoolean(reader.getElementText());
                    } else if ("hiddencolumn".equalsIgnoreCase(name)) {
                        if (!readHiddenNodeColumns) {
                            hiddenNodeColumnIds.clear();
                            readHiddenNodeColumns = true;
                        }
                        String id = reader.getAttributeValue(null, "id");
                        if (id != null) {
                            hiddenNodeColumnIds.add(id);
                        }
                    } else if ("hiddenedgecolumn".equalsIgnoreCase(name)) {
                        if (!readHiddenEdgeColumns) {
                            hiddenEdgeColumnIds.clear();
                            readHiddenEdgeColumns = true;
                        }
                        String id = reader.getAttributeValue(null, "id");
                        if (id != null) {
                            hiddenEdgeColumnIds.add(id);
                        }
                    }
                } else if (eventType == XMLEvent.END_ELEMENT) {
                    if (getIdentifier().equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "attributesuimodel";
    }
}
