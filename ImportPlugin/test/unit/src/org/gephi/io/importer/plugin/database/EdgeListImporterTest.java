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

package org.gephi.io.importer.plugin.database;

import java.sql.Connection;
import java.sql.SQLException;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.io.database.drivers.MySQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.importer.api.Report;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu
 */
public class EdgeListImporterTest {

    private EdgeListDatabaseImpl database;
    private ContainerLoader containerLoader;
    private ContainerUnloader containerUnloader;

    public EdgeListImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        database = new EdgeListDatabaseImpl();
        database.setDBName("test");
        database.setHost("localhost");
        database.setPort(3306);
        database.setUsername("root");
        database.setPasswd("");
        database.setNodeQuery("SELECT * FROM node LIMIT 0,1000");
        database.setEdgeQuery("SELECT * FROM edge WHERE id_node_from < 1000 AND id_node_to < 1000");
        database.setSQLDriver(new MySQLDriver());

        Container cont = Lookup.getDefault().lookup(Container.class);
        containerLoader = cont.getLoader();
        containerUnloader = cont.getUnloader();
    }

    @After
    public void tearDown() {
        database = null;
        containerLoader = null;
    }

    @Test
    public void testConnection() {
        System.out.println("-Try to connect at " + SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName()));
        try {
            Connection connection = database.getSQLDriver().getConnection(SQLUtils.getUrl(database.getSQLDriver(), database.getHost(), database.getPort(), database.getDBName()), database.getUsername(), database.getPasswd());
            System.out.println("---Database connection established");
            connection.close();
            System.out.println("-Database connection terminated");
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            //fail(ex.getMessage());
        }
    }

    @Test
    public void testImport() throws Exception {

        ImporterEdgeList importer = new ImporterEdgeList();
        importer.setDatabase(database);
        importer.execute(containerLoader);

        boolean hasId = false;
        boolean hasLabel = false;
        boolean hasX = false;
        for (NodeDraftGetter node : containerUnloader.getNodes()) {
            if (node.getId() != null) {
                hasId = true;
            }
            if (node.getLabel() != null) {
                hasLabel = true;
            }
            if (node.getX() != 0) {
                hasX = true;
            }
        }

        System.out.println("Id=" + hasId);
        System.out.println("Label=" + hasLabel);
        System.out.println("X=" + hasX);

        //Look at attributes
        System.out.println("--Node Attributes cols");
        for (AttributeColumn col : containerLoader.getAttributeModel().getNodeTable().getColumns()) {
            System.out.println(col.getIndex() + ":" + col.getId() + "(" + col.getType() + ")");
        }

        //Look at attributes
        System.out.println("--Edge Attributes cols");
        for (AttributeColumn col : containerLoader.getAttributeModel().getEdgeTable().getColumns()) {
            System.out.println(col.getIndex() + ":" + col.getId() + "(" + col.getType() + ")");
        }
    }
}
