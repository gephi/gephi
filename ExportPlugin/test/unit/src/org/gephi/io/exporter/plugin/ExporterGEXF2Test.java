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
package org.gephi.io.exporter.plugin;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicString;
import org.gephi.data.attributes.type.IntegerList;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class ExporterGEXF2Test {

    private ExporterGEXF exporter;

    @Before
    public void setUp() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.getCurrentWorkspace();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel attributeModel = attributeController.getModel();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraph();

        Node n1 = graphModel.factory().newNode("n1");
        graph.addNode(n1);
        Node n2 = graphModel.factory().newNode("n2");
        graph.addNode(n2);
        Node n3 = graphModel.factory().newNode("n3");
        graph.addNode(n3);
        Node n4 = graphModel.factory().newNode("n4");
        graph.addNode(n4, n3);

        Edge e1 = graphModel.factory().newEdge(n1, n2, 1f, true);
        graph.addEdge(e1);
        Edge e2 = graphModel.factory().newEdge(n2, n4, 3f, true);
        graph.addEdge(e2);
        Edge e3 = graphModel.factory().newEdge(n2, n1, 1f, true);
        graph.addEdge(e3);

        AttributeColumn nodeCol1 = attributeModel.getNodeTable().addColumn("col1", "Column 1", AttributeType.STRING, AttributeOrigin.DATA, "default value col1");
        AttributeColumn nodeCol2 = attributeModel.getNodeTable().addColumn("col2 listint", AttributeType.LIST_INTEGER);
        attributeModel.getNodeTable().addColumn("col3 listchar", AttributeType.LIST_CHARACTER);
        attributeModel.getNodeTable().addColumn("col4 int", AttributeType.INT);
        attributeModel.getNodeTable().addColumn("col5 liststring", AttributeType.LIST_STRING);
        AttributeColumn nodeTimeCol = attributeModel.getNodeTable().addColumn(DynamicModel.TIMEINTERVAL_COLUMN, AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY);

        AttributeColumn edgeCol1 = attributeModel.getEdgeTable().addColumn("col1", AttributeType.DYNAMIC_INT);
        AttributeColumn edgeCol2 = attributeModel.getEdgeTable().addColumn("col2", AttributeType.DYNAMIC_STRING);

        n1.getNodeData().getAttributes().setValue(nodeCol1.getIndex(), "test");
        n1.getNodeData().getAttributes().setValue(nodeCol2.getIndex(), new IntegerList(new int[]{1, 2, 3, 4}));

        n1.getNodeData().getAttributes().setValue(nodeTimeCol.getIndex(), new TimeInterval(1, 5));
        List<Interval> intervalList = new ArrayList<Interval>();
        intervalList.add(new Interval(1.0, 2.0));
        intervalList.add(new Interval(3.0, 4.0));
        n2.getNodeData().getAttributes().setValue(nodeTimeCol.getIndex(), new TimeInterval(intervalList));

        DynamicInteger dynamicInteger = new DynamicInteger(new Interval(4., 10., 100));
        e1.getEdgeData().getAttributes().setValue(edgeCol1.getIndex(), dynamicInteger);

        List<Interval<String>> stringIntervalList = new ArrayList<Interval<String>>();
        stringIntervalList.add(new Interval<String>(Double.NEGATIVE_INFINITY,1.,"a"));
        stringIntervalList.add(new Interval<String>(2.,3.,"b"));
        stringIntervalList.add(new Interval<String>(4.,Double.POSITIVE_INFINITY,"c"));
        DynamicString dynamicString = new DynamicString(stringIntervalList);
        e3.getEdgeData().getAttributes().setValue(edgeCol2.getIndex(), dynamicString);

        exporter = new ExporterGEXF();
        exporter.setWorkspace(workspace);
    }

    @After
    public void tearDown() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.closeCurrentProject();
        exporter = null;
    }

    @Test
    public void testExport() {
        StringWriter stringWriter = new StringWriter();
        exporter.setWriter(stringWriter);
        exporter.setExportColors(true);
        exporter.setExportPosition(true);
        exporter.setExportSize(true);

        exporter.execute();

        System.out.println(stringWriter.toString());
    }
}
