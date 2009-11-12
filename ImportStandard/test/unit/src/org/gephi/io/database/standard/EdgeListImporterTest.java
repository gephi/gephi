package org.gephi.io.database.standard;

import java.sql.Connection;
import java.sql.SQLException;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.io.container.Container;
import org.gephi.io.container.ContainerLoader;
import org.gephi.io.container.ContainerUnloader;
import org.gephi.io.database.EdgeListDatabase;
import org.gephi.io.database.drivers.MySQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.logging.Report;
import org.gephi.io.processor.NodeDraftGetter;
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

    private EdgeListDatabase database;
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

        EdgeListImporter importer = new EdgeListImporter();
        importer.importData(database, containerLoader, new Report());

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
        for(AttributeColumn col : containerLoader.getAttributeModel().getNodeTable().getColumns()) {
            System.out.println(col.getIndex()+":"+col.getId()+ "("+col.getType()+")");
        }

        //Look at attributes
        System.out.println("--Edge Attributes cols");
        for(AttributeColumn col : containerLoader.getAttributeModel().getEdgeTable().getColumns()) {
            System.out.println(col.getIndex()+":"+col.getId()+ "("+col.getType()+")");
        }
    }
}