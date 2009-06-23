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
import java.sql.SQLException;
import java.sql.Statement;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.database.Database;
import org.gephi.io.database.DatabaseType;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.DatabaseImporter;

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

        Statement s = connection.createStatement();
        s.executeQuery(database.getNodeQuery());
        ResultSet rs = s.getResultSet();
        int count = 0;
        while (rs.next()) {
            NodeDraft node = factory.newNodeDraft();
            int id = rs.getInt("id");
            String label = rs.getString("label");

            node.setId("" + id);
            node.setLabel(label);
            container.addNode(node);
            ++count;
        }
        rs.close();
        s.close();

    }

    private void getEdges(Connection connection) throws SQLException {

        //Factory
        ContainerLoader.ContainerFactory factory = container.factory();

        Statement s = connection.createStatement();
        s.executeQuery(database.getEdgeQuery());
        ResultSet rs = s.getResultSet();
        int count = 0;
        while (rs.next()) {
            EdgeDraft edge = factory.newEdgeDraft();
            int id = rs.getInt("id");
            int idSource = rs.getInt("id_node_from");
            int idTarget = rs.getInt("id_node_to");
            NodeDraft ndSource = container.getNode("" + idSource);
            NodeDraft ndTarget = container.getNode("" + idTarget);
            edge.setSource(ndSource);
            edge.setTarget(ndTarget);
            edge.setId("" + id);

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

    public boolean isMatchingImporter(DatabaseType databaseType) {
        if (databaseType instanceof EdgeList) {
            return true;
        }
        return false;
    }
}
