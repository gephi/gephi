/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.database.standard;

import java.sql.Connection;
import java.sql.SQLException;
import org.gephi.io.container.Container;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.ContainerUnloader;
import org.gephi.io.database.EdgeListDatabase;
import org.gephi.io.database.drivers.MySQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.processor.NodeDraftGetter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu
 */
public class EdgeListImporterTest extends NbTestCase {

    private EdgeListDatabase database;
    private ContainerLoader containerLoader;
    private ContainerUnloader containerUnloader;

    public EdgeListImporterTest() {
        super("EdgeListImporterTest");
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
        database.setEdgeQuery("SELECT * FROM edge WHERE source < 1000 AND target < 1000");
        database.setSQLDriver(new MySQLDriver());

        Container cont = Lookup.getDefault().lookup(Container.class);
        cont.setErrorMode(Container.ErrorMode.REPORT);
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
            fail(ex.getMessage());
        }
    }

    @Test
    public void testImport() throws Exception {

        EdgeListImporter importer = new EdgeListImporter();
        importer.importData(database, containerLoader);

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

    }
}