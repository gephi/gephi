/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic;

import javax.xml.datatype.DatatypeConfigurationException;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphController;
import org.gephi.project.api.ProjectController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.io.importer.api.Container;
import org.openide.util.Lookup;
import org.gephi.io.importer.api.ImportController;
import java.io.File;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Graph;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import static org.junit.Assert.*;

/**
 * Unit test for DynamicGraphImpl class.
 *
 * @author Cezary Bartosiak
 */
public class DynamicGraphImplTest {
	private GraphModel graphModel;
	
	@BeforeClass
	public static void setUpClass() throws Exception { }

	@AfterClass
	public static void tearDownClass() throws Exception { }

	@Before
	public void setUp() {
		ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
		projectController.newProject();
		projectController.newWorkspace(projectController.getCurrentProject());

		ImportController importController = Lookup.getDefault().lookup(ImportController.class);

		Container container;
		try {
			File file = new File(getClass().getResource("/org/gephi/dynamic/test_graph.gexf").toURI());
			container = importController.importFile(file);
		}
		catch (Exception ex) {
			Exceptions.printStackTrace(ex);
			return;
		}

		importController.process(container, new DefaultProcessor(), projectController.getCurrentWorkspace());

		GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
		graphModel = graphController.getModel();
	}

	@After
	public void tearDown() {
		graphModel = null;
	}

	@Test
	public void testGetAttributesValues() throws DatatypeConfigurationException {
		System.out.println("getAttributesValues(Node, double)");
		DynamicGraphImpl instance = new DynamicGraphImpl(graphModel.getGraph());
		System.out.println("low:              " + DynamicUtilities.getXMLDateStringFromDouble(instance.getLow()));
		System.out.println("high:             " + DynamicUtilities.getXMLDateStringFromDouble(instance.getHigh()));
		System.out.println("(low + high) / 2: " + DynamicUtilities.getXMLDateStringFromDouble(
				(instance.getLow() + instance.getHigh()) / 2));
		Object[] values = instance.getAttributesValues(graphModel.getGraph().getNode(0),
							(instance.getLow() + instance.getHigh()) / 2);
		for (Object value : values)
			System.out.print(value + ", ");
		System.out.println("\n");
	}

	@Test
	public void testGetLow() {
		System.out.println("getLow()");
		DynamicGraphImpl instance = new DynamicGraphImpl(graphModel.getGraph());
		instance.setInterval(1.0, 3.0);
		assertEquals(instance.getLow(), 1.0, 0.01);
		System.out.println();
	}

	@Test
	public void testGetHigh() {
		System.out.println("getHigh()");
		DynamicGraphImpl instance = new DynamicGraphImpl(graphModel.getGraph());
		instance.setInterval(1.0, 3.0);
		assertEquals(instance.getHigh(), 3.0, 0.01);
		System.out.println();
	}

	@Test
	public void testGetUnderlyingGraph() {
		System.out.println("getUnderlyingGraph()");
		DynamicGraphImpl instance = new DynamicGraphImpl(graphModel.getGraph());
		assertEquals(graphModel.getGraph(), instance.getUnderlyingGraph());
		System.out.println();
	}

	@Test
	public void testGetInterval() {
		System.out.println("getInterval()");
		double low  = 1.0;
		double high = 3.0;
		DynamicGraphImpl instance = new DynamicGraphImpl(graphModel.getGraph());
		instance.setInterval(low, high);
		TimeInterval t1 = instance.getInterval();
		TimeInterval t2 = new TimeInterval(1.0, 3.0);
		assertEquals(t1, t2);
		System.out.println();
	}

	@Test
	public void testEquals() {
		System.out.println("equals(Object)");
		DynamicGraphImpl instance1 = new DynamicGraphImpl(graphModel.getGraph());
		DynamicGraphImpl instance2 = new DynamicGraphImpl(graphModel.newView().getGraphModel().getGraph());
		DynamicGraphImpl instance3 = new DynamicGraphImpl(makeGraph2());
		boolean expResult1 = true;
		boolean result1    = instance1.equals(instance1);
		boolean expResult2 = true;
		boolean result2    = instance1.equals(instance2);
		boolean expResult3 = false;
		boolean result3    = instance2.equals(instance3);
		assertEquals(expResult1, result1);
		assertEquals(expResult2, result2);
		assertEquals(expResult3, result3);
		System.out.println();
	}

	@Test
	public void testHashCode() {
		System.out.println("hashCode()");
		DynamicGraphImpl instance1 = new DynamicGraphImpl(graphModel.getGraph());
		DynamicGraphImpl instance2 = new DynamicGraphImpl(graphModel.newView().getGraphModel().getGraph());
		assertEquals(instance1.hashCode(), instance1.hashCode());
		assertEquals(instance1.hashCode(), instance2.hashCode());
		System.out.println("instance1.hashcode(): " + instance1.hashCode());
		System.out.println("instance2.hashcode(): " + instance2.hashCode());
		System.out.println();
	}

	@Test
	public void testToString() {
		System.out.println("toString()");
		DynamicGraphImpl instance = new DynamicGraphImpl(graphModel.getGraph());
		String expResult = graphModel.getGraph().toString();
		String result    = instance.toString();
		assertEquals(expResult, result);
		System.out.println("expResult: " + expResult);
		System.out.println("result:    " + result);
		System.out.println();
		System.out.println();
	}

	private Graph makeGraph2() {
		Graph graph = graphModel.newView().getGraphModel().getGraph();
		graph.clearEdges();
		return graph;
	}
}
