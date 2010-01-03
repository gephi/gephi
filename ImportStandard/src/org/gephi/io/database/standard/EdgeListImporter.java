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
package org.gephi.io.database.standard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.database.Database;
import org.gephi.io.database.DatabaseType;
import org.gephi.io.database.EdgeListDatabase;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.DatabaseImporter;
import org.gephi.io.importer.PropertiesAssociations;
import org.gephi.io.importer.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.PropertiesAssociations.NodeProperties;
import org.gephi.io.logging.Issue;
import org.gephi.io.logging.Report;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = DatabaseImporter.class)
public class EdgeListImporter implements DatabaseImporter {

    private Report report;
    private EdgeListDatabase database;
    private ContainerLoader container;
    private Connection connection;

    public boolean importData(Database database, ContainerLoader container, Report report) throws Exception {
        this.database = (EdgeListDatabase) database;
        this.container = container;
        this.report = report;

        try {
            importData();
        } catch (Exception e) {
            clean();
            throw e;
        }
        clean();
        return true;
    }

    private void clean() {
        //Close connection
        if (connection != null) {
            try {
                connection.close();
                report.log("Database connection terminated");
            } catch (Exception e) { /* ignore close errors */ }
        }

        report = null;
        database = null;
        connection = null;
        container = null;
    }

    private void importData() throws Exception {
        //Connect database
        String url = SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName());
        try {
            report.log("Try to connect at " + url);
            connection = database.getSQLDriver().getConnection(url, database.getUsername(), database.getPasswd());
            report.log("Database connection established");
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                    report.log("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */ }
            }
            report.logIssue(new Issue("Failed to connect at " + url, Issue.Level.CRITICAL, ex));
        }
        if (connection == null) {
            report.logIssue(new Issue("Failed to connect at " + url, Issue.Level.CRITICAL));
        }

        report.log(database.getPropertiesAssociations().getInfos());
        getNodes(connection);
        getEdges(connection);
        getNodesAttributes(connection);
        getEdgesAttributes(connection);
    }

    private void getNodes(Connection connection) throws SQLException {

        //Factory
        ContainerLoader.DraftFactory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        Statement s = connection.createStatement();
        try {
            s.executeQuery(database.getNodeQuery());
        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to execute Node query", Issue.Level.SEVERE, ex));
            return;
        }

        ResultSet rs = s.getResultSet();
        findNodeAttributesColumns(rs);
        AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        int count = 0;
        while (rs.next()) {
            NodeDraft node = factory.newNodeDraft();
            for (int i = 0; i < columnsCount; i++) {
                String columnName = metaData.getColumnLabel(i + 1);
                NodeProperties p = properties.getNodeProperty(columnName);
                if (p != null) {
                    injectNodeProperty(p, rs, i + 1, node);
                } else {
                    //Inject node attributes
                    AttributeColumn col = nodeClass.getColumn(columnName);
                    injectNodeAttribute(rs, i + 1, col, node);
                }
            }
            container.addNode(node);
            ++count;
        }
        rs.close();
        s.close();

    }

    private void getEdges(Connection connection) throws SQLException {

        //Factory
        ContainerLoader.DraftFactory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        Statement s = connection.createStatement();
        try {
            s.executeQuery(database.getEdgeQuery());
        } catch (SQLException ex) {
            report.logIssue(new Issue("Failed to execute Edge query", Issue.Level.SEVERE, ex));
            return;
        }
        ResultSet rs = s.getResultSet();
        findEdgeAttributesColumns(rs);
        AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        int count = 0;
        while (rs.next()) {
            EdgeDraft edge = factory.newEdgeDraft();
            for (int i = 0; i < columnsCount; i++) {
                String columnName = metaData.getColumnLabel(i + 1);
                EdgeProperties p = properties.getEdgeProperty(columnName);
                if (p != null) {
                    injectEdgeProperty(p, rs, i + 1, edge);
                } else {
                    //Inject edge attributes
                    AttributeColumn col = edgeClass.getColumn(columnName);
                    injectEdgeAttribute(rs, i + 1, col, edge);
                }
            }

            container.addEdge(edge);
            ++count;
        }
        rs.close();
        s.close();
    }

    private void getNodesAttributes(Connection connection) throws SQLException {
    }

    private void getEdgesAttributes(Connection connection) throws SQLException {
    }

    private void injectNodeProperty(NodeProperties p, ResultSet rs, int column, NodeDraft nodeDraft) throws SQLException {
        switch (p) {
            case ID:
                String id = rs.getString(column);
                if (id != null) {
                    nodeDraft.setId(id);
                }
                break;
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    nodeDraft.setLabel(label);
                }
                break;
            case X:
                float x = rs.getFloat(column);
                if (x != 0) {
                    nodeDraft.setX(x);
                }
                break;
            case Y:
                float y = rs.getFloat(column);
                if (y != 0) {
                    nodeDraft.setY(y);
                }
                break;
            case Z:
                float z = rs.getFloat(column);
                if (z != 0) {
                    nodeDraft.setZ(z);
                }
                break;
            case R:
                break;
            case G:
                break;
            case B:
                break;
        }
    }

    private void injectEdgeProperty(EdgeProperties p, ResultSet rs, int column, EdgeDraft edgeDraft) throws SQLException {
        switch (p) {
            case ID:
                String id = rs.getString(column);
                if (id != null) {
                    edgeDraft.setId(id);
                }
                break;
            case LABEL:
                String label = rs.getString(column);
                if (label != null) {
                    edgeDraft.setLabel(label);
                }
                break;
            case SOURCE:
                String source = rs.getString(column);
                if (source != null) {
                    NodeDraft sourceNode = container.getNode(source);
                    edgeDraft.setSource(sourceNode);
                }
                break;
            case TARGET:
                String target = rs.getString(column);
                if (target != null) {
                    NodeDraft targetNode = container.getNode(target);
                    edgeDraft.setTarget(targetNode);
                }
                break;
            case WEIGHT:
                float weight = rs.getFloat(column);
                if (weight != 0) {
                    edgeDraft.setWeight(weight);
                }
                break;
            case R:
                break;
            case G:
                break;
            case B:
                break;
        }
    }

    private void injectNodeAttribute(ResultSet rs, int columnIndex, AttributeColumn column, NodeDraft draft) {
        switch (column.getType()) {
            case BOOLEAN:
                try {
                    boolean val = rs.getBoolean(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a BOOLEAN value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case DOUBLE:
                try {
                    double val = rs.getDouble(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a DOUBLE value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case FLOAT:
                try {
                    float val = rs.getFloat(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a FLOAT value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case INT:
                try {
                    int val = rs.getInt(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a INT value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case LONG:
                try {
                    long val = rs.getLong(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a LONG value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            default: //String
                try {
                    String val = rs.getString(columnIndex);
                    if (val != null) {
                        draft.addAttributeValue(column, val);
                    } else {
                        report.logIssue(new Issue("Failed to get a STRING value for node attribute '" + column.getId() + "'", Issue.Level.WARNING));
                    }
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a STRING value for node attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
        }
    }

    private void injectEdgeAttribute(ResultSet rs, int columnIndex, AttributeColumn column, EdgeDraft draft) {
        switch (column.getType()) {
            case BOOLEAN:
                try {
                    boolean val = rs.getBoolean(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a BOOLEAN value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case DOUBLE:
                try {
                    double val = rs.getDouble(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a DOUBLE value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case FLOAT:
                try {
                    float val = rs.getFloat(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a FLOAT value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case INT:
                try {
                    int val = rs.getInt(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a INT value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            case LONG:
                try {
                    long val = rs.getLong(columnIndex);
                    draft.addAttributeValue(column, val);
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a LONG value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
            default: //String
                try {
                    String val = rs.getString(columnIndex);
                    if (val != null) {
                        draft.addAttributeValue(column, val);
                    } else {
                        report.logIssue(new Issue("Failed to get a BOOLEAN value for edge attribute '" + column.getId() + "'", Issue.Level.WARNING));
                    }
                } catch (SQLException ex) {
                    report.logIssue(new Issue("Failed to get a STRING value for edge attribute '" + column.getId() + "'", Issue.Level.SEVERE, ex));
                }
                break;
        }
    }

    private void findNodeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        AttributeTable nodeClass = container.getAttributeModel().getNodeTable();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            NodeProperties p = database.getPropertiesAssociations().getNodeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute

                AttributeType type = AttributeType.STRING;
                switch (metaData.getColumnType(i + 1)) {
                    case Types.BIGINT:
                        type = AttributeType.LONG;
                        break;
                    case Types.INTEGER:
                        type = AttributeType.INT;
                        break;
                    case Types.TINYINT:
                        type = AttributeType.INT;
                        break;
                    case Types.SMALLINT:
                        type = AttributeType.INT;
                        break;
                    case Types.BOOLEAN:
                        type = AttributeType.BOOLEAN;
                        break;
                    case Types.FLOAT:
                        type = AttributeType.FLOAT;
                        break;
                    case Types.DOUBLE:
                        type = AttributeType.DOUBLE;
                        break;
                    case Types.VARCHAR:
                        type = AttributeType.STRING;
                        break;
                    default:
                        report.logIssue(new Issue("Unknown SQL Type " + metaData.getColumnType(i + 1) + ", STRING used.", Issue.Level.WARNING));
                        break;
                }
                report.log("Node attribute found: " + columnName + "(" + type + ")");
                nodeClass.addColumn(columnName, type);
            }
        }
    }

    private void findEdgeAttributesColumns(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnsCount = metaData.getColumnCount();
        AttributeTable edgeClass = container.getAttributeModel().getEdgeTable();
        for (int i = 0; i < columnsCount; i++) {
            String columnName = metaData.getColumnLabel(i + 1);
            EdgeProperties p = database.getPropertiesAssociations().getEdgeProperty(columnName);
            if (p == null) {
                //No property associated to this column is found, so we append it as an attribute
                AttributeType type = AttributeType.STRING;
                switch (metaData.getColumnType(i + 1)) {
                    case Types.BIGINT:
                        type = AttributeType.LONG;
                        break;
                    case Types.INTEGER:
                        type = AttributeType.INT;
                        break;
                    case Types.TINYINT:
                        type = AttributeType.INT;
                        break;
                    case Types.SMALLINT:
                        type = AttributeType.INT;
                        break;
                    case Types.BOOLEAN:
                        type = AttributeType.BOOLEAN;
                        break;
                    case Types.FLOAT:
                        type = AttributeType.FLOAT;
                        break;
                    case Types.DOUBLE:
                        type = AttributeType.DOUBLE;
                        break;
                    case Types.VARCHAR:
                        type = AttributeType.STRING;
                        break;
                    default:
                        report.logIssue(new Issue("Unknown SQL Type " + metaData.getColumnType(i + 1) + ", STRING used.", Issue.Level.WARNING));
                        break;
                }

                report.log("Edge attribute found: " + columnName + "(" + type + ")");
                edgeClass.addColumn(columnName, type);
            }
        }
    }

    public boolean isMatchingImporter(DatabaseType databaseType) {
        if (databaseType instanceof EdgeList) {
            return true;
        }
        return false;
    }
}
