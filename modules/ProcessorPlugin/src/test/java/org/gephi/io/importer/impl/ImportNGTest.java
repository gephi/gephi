/*
Copyright 2008-2017 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2017 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2017 Gephi Consortium.
 */
package org.gephi.io.importer.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.plugin.MergeProcessor;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Eduardo Ramos
 */
public class ImportNGTest {

    private final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
    private final ImportController importController = Lookup.getDefault().lookup(ImportController.class);
    private final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
    private final Processor defaultProcessor = Lookup.getDefault().lookup(Processor.class);
    private final Processor mergeProcessor = new MergeProcessor();
    private Workspace workspace;
    private ImportContainerImpl container, container2, container3;

    private static final double EPS = 0.001;

    @BeforeMethod
    public void setup(Method method) {
        String testName = method.getName();
        System.out.println("Starting test: " + testName);

        projectController.newProject();
        workspace = projectController.getCurrentWorkspace();

        container = new ImportContainerImpl();
        container2 = new ImportContainerImpl();
        container3 = new ImportContainerImpl();

        container.setReport(new Report());
        container2.setReport(new Report());
        container3.setReport(new Report());
    }

    @AfterMethod
    public void teardown() {
        projectController.closeCurrentProject();
        workspace = null;
        container = null;
        container2 = null;
        container3 = null;
    }

    private void showReport(Report report) {
        System.out.println(report.getText());
        Iterator<Issue> issuesIterator = report.getIssues(100);
        while (issuesIterator.hasNext()) {
            Issue issue = issuesIterator.next();

            System.out.println(issue);
        }
    }

    private void setEdgesMergeStrategy(EdgeMergeStrategy edgeMergeStrategy) {
        container.setEdgesMergeStrategy(edgeMergeStrategy);
        container2.setEdgesMergeStrategy(edgeMergeStrategy);
        container3.setEdgesMergeStrategy(edgeMergeStrategy);
    }

    private NodeDraft buildNode(ContainerLoader container, String id) {
        NodeDraft node = container.factory().newNodeDraft(id);
        return node;
    }

    private EdgeDraft buildEdge(ContainerLoader container, NodeDraft source, NodeDraft target) {
        return buildEdge(container, source, target, 1);
    }

    private EdgeDraft buildEdge(ContainerLoader container, NodeDraft source, NodeDraft target, double weight) {
        return buildEdge(container, source, target, weight, null);
    }

    private EdgeDraft buildEdge(ContainerLoader container, NodeDraft source, NodeDraft target, double weight, Object type) {
        EdgeDraft edge = container.factory().newEdgeDraft();
        edge.setSource(source);
        edge.setTarget(target);
        edge.setWeight(weight);

        edge.setType(type);

        Assert.assertNotNull(edge);

        return edge;
    }

    private void buildMergeWeightsTestGraph(boolean multipleContainers) {
        buildMergeWeightsTestGraph(multipleContainers, false);
    }
    
    private void buildMergeWeightsTestGraph(boolean multipleContainers, boolean differentTypes) {
        if (multipleContainers) {
            NodeDraft node1 = buildNode(container, "1");
            NodeDraft node2 = buildNode(container, "2");
            NodeDraft node1_2 = buildNode(container2, "1");
            NodeDraft node2_2 = buildNode(container2, "2");
            NodeDraft node1_3 = buildNode(container3, "1");
            NodeDraft node2_3 = buildNode(container3, "2");

            container.addNode(node1);
            container.addNode(node2);
            container2.addNode(node1_2);
            container2.addNode(node2_2);
            container3.addNode(node1_3);
            container3.addNode(node2_3);

            EdgeDraft edge12_1 = buildEdge(container, node1, node2, 1.0, differentTypes ? "1" : null);
            EdgeDraft edge12_2 = buildEdge(container2, node1_2, node2_2, 1.0, differentTypes ? "2" : null);
            EdgeDraft edge12_3 = buildEdge(container3, node1_3, node2_3, 4.2, differentTypes ? "3" : null);
            EdgeDraft edge21 = buildEdge(container3, node2_3, node1_3);
            EdgeDraft edge22 = buildEdge(container2, node2_2, node2_2, 1.5);

            container.addEdge(edge12_1);

            container2.addEdge(edge12_2);
            container3.addEdge(edge12_3);

            container3.addEdge(edge21);
            container2.addEdge(edge22);
        } else {
            NodeDraft node1 = buildNode(container, "1");
            NodeDraft node2 = buildNode(container, "2");

            container.addNode(node1);
            container.addNode(node2);

            EdgeDraft edge12_1 = buildEdge(container, node1, node2, 1.0, differentTypes ? "1" : null);
            EdgeDraft edge12_2 = buildEdge(container, node1, node2, 1.0, differentTypes ? "2" : null);
            EdgeDraft edge12_3 = buildEdge(container, node1, node2, 4.2, differentTypes ? "3" : null);
            EdgeDraft edge21 = buildEdge(container, node2, node1);
            EdgeDraft edge22 = buildEdge(container, node2, node2, 1.5);

            container.addEdge(edge12_1);

            container.addEdge(edge12_2);
            container.addEdge(edge12_3);
            container.addEdge(edge21);
            container.addEdge(edge22);
        }
    }

    private Graph processMergeWeightsTestGraph(boolean multipleContainers) {
        if (multipleContainers) {
            importController.process(new Container[]{container, container2, container3}, mergeProcessor, workspace);

            showReport(container.getReport());
            showReport(container2.getReport());
            showReport(container3.getReport());
            showReport(mergeProcessor.getReport());
            Assert.assertTrue(mergeProcessor.getReport().isEmpty());
        } else {
            importController.process(container, defaultProcessor, workspace);
            showReport(container.getReport());
            showReport(defaultProcessor.getReport());
            Assert.assertTrue(defaultProcessor.getReport().isEmpty());
        }

        Graph graph = graphController.getGraphModel(workspace).getGraph();

        Edge edge21 = graph.getEdge(graph.getNode("2"), graph.getNode("1"));
        Assert.assertNotNull(edge21);
        Assert.assertEquals(edge21.getWeight(), 1.0);

        Edge edge22 = graph.getEdge(graph.getNode("2"), graph.getNode("2"));
        Assert.assertNotNull(edge22);
        Assert.assertEquals(edge22.getWeight(), 1.5);

        return graph;
    }

    private void checkWeightsSummed(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 3);

        Edge edge12 = graph.getEdge(graph.getNode("1"), graph.getNode("2"));
        Assert.assertNotNull(edge12);
        Assert.assertEquals(edge12.getWeight(), 6.2);
    }

    private void checkWeightsAveraged(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 3);

        Edge edge12 = graph.getEdge(graph.getNode("1"), graph.getNode("2"));
        Assert.assertNotNull(edge12);
        Assert.assertEquals(edge12.getWeight(), 2.0666, EPS);
    }

    private void checkWeightsMaxKept(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 3);

        Edge edge12 = graph.getEdge(graph.getNode("1"), graph.getNode("2"));
        Assert.assertNotNull(edge12);
        Assert.assertEquals(edge12.getWeight(), 4.2);
    }

    private void checkWeightsMinKept(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 3);

        Edge edge12 = graph.getEdge(graph.getNode("1"), graph.getNode("2"));
        Assert.assertNotNull(edge12);
        Assert.assertEquals(edge12.getWeight(), 1.0);
    }

    private void checkWeightsFirstKept(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 3);

        Edge edge12 = graph.getEdge(graph.getNode("1"), graph.getNode("2"));
        Assert.assertNotNull(edge12);
        Assert.assertEquals(edge12.getWeight(), 1.0);
    }

    private void checkWeightsLastKept(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 3);

        Edge edge12 = graph.getEdge(graph.getNode("1"), graph.getNode("2"));
        Assert.assertNotNull(edge12);
        Assert.assertEquals(edge12.getWeight(), 4.2);
    }

    private void checkWeightsNotMerged(boolean multipleContainers) {
        Graph graph = processMergeWeightsTestGraph(multipleContainers);

        Assert.assertEquals(graph.getNodeCount(), 2);
        Assert.assertEquals(graph.getEdgeCount(), 5);

        Set<Double> allWeights = new HashSet<>();
        double sum = 0;
        for (int edgeType : graph.getModel().getEdgeTypes()) {
            for (Edge edge : graph.getEdges(graph.getNode("1"), graph.getNode("2"), edgeType)) {
                allWeights.add(edge.getWeight());
                sum += edge.getWeight();
            }
        }

        Assert.assertTrue(allWeights.contains(1.0));
        Assert.assertTrue(allWeights.contains(4.2));
        Assert.assertEquals(sum, 6.2, EPS);
    }

    @Test
    public void testProcessContainer_Default_Weights_Merged_as_Sum() {
        buildMergeWeightsTestGraph(false);
        checkWeightsSummed(false);
    }

    @Test
    public void testProcessContainer_Weights_Sum() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.SUM);
        checkWeightsSummed(false);
    }

    @Test
    public void testProcessContainer_Weights_Average() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.AVG);
        checkWeightsAveraged(false);
    }

    @Test
    public void testProcessContainer_Weights_Max() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.MAX);
        checkWeightsMaxKept(false);
    }

    @Test
    public void testProcessContainer_Weights_Min() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.MIN);
        checkWeightsMinKept(false);
    }

    @Test
    public void testProcessContainer_Weights_First() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.FIRST);
        checkWeightsFirstKept(false);
    }

    @Test
    public void testProcessContainer_Weights_Last() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.LAST);
        checkWeightsLastKept(false);
    }

    @Test
    public void testProcessContainer_Weights_DifferentTypesNotMerged() {
        buildMergeWeightsTestGraph(false, true);
        setEdgesMergeStrategy(EdgeMergeStrategy.SUM);
        checkWeightsNotMerged(false);
    }

    @Test
    public void testProcessContainer_Weights_NoMerge() {
        buildMergeWeightsTestGraph(false);
        setEdgesMergeStrategy(EdgeMergeStrategy.NO_MERGE);
        checkWeightsNotMerged(false);
    }

    @Test
    public void testMultiProcessContainer_Default_Weights_Merged_as_Sum() {
        buildMergeWeightsTestGraph(true);
        checkWeightsSummed(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_Sum() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.SUM);
        checkWeightsSummed(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_Average() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.AVG);
        checkWeightsAveraged(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_Max() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.MAX);
        checkWeightsMaxKept(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_Min() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.MIN);
        checkWeightsMinKept(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_First() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.FIRST);
        checkWeightsFirstKept(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_Last() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.LAST);
        checkWeightsLastKept(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_DifferentTypesNotMerged() {
        buildMergeWeightsTestGraph(true, true);
        setEdgesMergeStrategy(EdgeMergeStrategy.SUM);
        checkWeightsNotMerged(true);
    }

    @Test
    public void testMultiProcessContainer_Weights_NoMerge() {
        buildMergeWeightsTestGraph(true);
        setEdgesMergeStrategy(EdgeMergeStrategy.NO_MERGE);
        checkWeightsNotMerged(true);
    }
}
