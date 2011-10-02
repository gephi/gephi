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
