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
package org.gephi.statistics.plugin;

import java.util.Random;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class MemoryTest {

    private static final int NODES = 10000;
    private static final int EDGES = 50000;
    private GraphModel graphModel;
    private AttributeModel attributeModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeModel = attributeController.getModel();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel();
        DirectedGraph graph = graphModel.getDirectedGraph();

        for (int i = 0; i < NODES; i++) {
            Node newNode = graphModel.factory().newNode();
            graph.addNode(newNode);
        }

        Random random = new Random();
        int j = 0;
        while (j < EDGES) {
            Node source = graph.getNode(random.nextInt(NODES));
            Node target = graph.getNode(random.nextInt(NODES));
            if (graph.getEdge(source, target) == null) {
                graph.addEdge(graphModel.factory().newEdge(source, target));
                j++;
            }
        }
    }

    @After
    public void tearDown() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.closeCurrentProject();
        graphModel = null;
        attributeModel = null;
    }

    @Test
    public void testGraphDistance() {
        System.out.println("Start Brandes");
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel, attributeModel);
        System.out.println("Diameter: " + distance.getDiameter());
        System.out.println("AVg Path Length: " + distance.getPathLength());
    }
}
