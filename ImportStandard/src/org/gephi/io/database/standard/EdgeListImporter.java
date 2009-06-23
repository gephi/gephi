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
import org.gephi.data.properties.EdgeProperties;
import org.gephi.data.properties.NodeProperties;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.database.Database;
import org.gephi.io.database.DatabaseType;
import org.gephi.io.database.EdgeListDatabase;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.DatabaseImporter;
import org.gephi.io.importer.PropertiesAssociations;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeListImporter implements DatabaseImporter {

    private EdgeListDatabase database;
    private ContainerLoader container;

    public void importData(Database database, ContainerLoader container) throws Exception {
        this.database = (EdgeListDatabase) database;
        this.container = container;

        Connection connection = null;
        try {
            System.out.println("Try to connect at " + SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName()));
            connection = database.getSQLDriver().getConnection(SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName()), database.getUsername(), database.getPasswd());
            System.out.println("Database connection established");
            getNodes(connection);
            getEdges(connection);
            getNodesAttributes(connection);
            getEdgesAttributes(connection);
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */ }
            }
        }
    }

    private void getNodes(Connection connection) throws SQLException {

        //Factory
        ContainerLoader.ContainerFactory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        Statement s = connection.createStatement();
        s.executeQuery(database.getNodeQuery());
        ResultSet rs = s.getResultSet();
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
        ContainerLoader.ContainerFactory factory = container.factory();

        //Properties
        PropertiesAssociations properties = database.getPropertiesAssociations();

        Statement s = connection.createStatement();
        s.executeQuery(database.getEdgeQuery());
        ResultSet rs = s.getResultSet();
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

    public boolean isMatchingImporter(DatabaseType databaseType) {
        if (databaseType instanceof EdgeList) {
            return true;
        }
        return false;
    }
}
