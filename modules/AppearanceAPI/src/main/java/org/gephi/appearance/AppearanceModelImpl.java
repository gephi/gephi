/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public class AppearanceModelImpl implements AppearanceModel {

    private final Workspace workspace;
    private final GraphModel graphModel;
    private final Interpolator defaultInterpolator;
    private boolean localScale = false;
    // Transformers
    private final List<Transformer> nodeTransformers;
    private final List<Transformer> edgeTransformers;
    // Transformer UIS
    private final Map<Class, TransformerUI> transformerUIs;
    //Functions
    private final Object functionLock;
    //
    private final FunctionsModel functionsMain;
    private final Map<Graph, FunctionsModel> functions = new HashMap<Graph, FunctionsModel>();

    public AppearanceModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.defaultInterpolator = Interpolator.LINEAR;
        this.functionLock = new Object();
        this.transformerUIs = initTransformerUIs();
        this.nodeTransformers = initNodeTransformers();
        this.edgeTransformers = initEdgeTransformers();

        //Functions
        functionsMain = new FunctionsModel(graphModel.getGraph());
        refreshFunctions(graphModel.getGraph());
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean isLocalScale() {
        return localScale;
    }

    @Override
    public Function[] getNodeFunctions(Graph graph) {
        return refreshFunctions(graph).getNodeFunctions();
    }

    @Override
    public Function[] getEdgeFunctions(Graph graph) {
        return refreshFunctions(graph).getEdgeFunctions();
    }

    @Override
    public Partition getNodePartition(Graph graph, Column column) {
        synchronized (functionLock) {
            FunctionsModel m;
            if (graph.getView().isMainView()) {
                m = functionsMain;
            } else {
                m = functions.get(graph);
            }
            if (m != null) {
                return m.nodeFunctionsModel.getPartition(column);
            }
            return null;
        }
    }

    @Override
    public Partition getEdgePartition(Graph graph, Column column) {
        synchronized (functionLock) {
            FunctionsModel m;
            if (graph.getView().isMainView()) {
                m = functionsMain;
            } else {
                m = functions.get(graph);
            }
            if (m != null) {
                return m.edgeFunctionsModel.getPartition(column);
            }
            return null;
        }
    }

    private FunctionsModel refreshFunctions(Graph graph) {
        synchronized (functionLock) {

            FunctionsModel m;
            if (graph.getView().isMainView()) {
                m = functionsMain;
            } else {
                m = functions.get(graph);
                if (m == null) {
                    m = new FunctionsModel(graph);
                    functions.put(graph, m);
                }
            }

            //Check and detroy old
            for (Iterator<Map.Entry<Graph, FunctionsModel>> it = functions.entrySet().iterator();
                    it.hasNext();) {
                Map.Entry<Graph, FunctionsModel> entry = it.next();
                if (entry.getKey().getView().isDestroyed()) {
                    it.remove();
                }
            }
            return m;
        }
    }

    private class NodeFunctionsModel extends ElementFunctionsModel<Node> {

        public NodeFunctionsModel(Graph graph) {
            super(graph);
        }

        @Override
        public Iterable<Node> getElements() {
            return graph.getNodes();
        }

        @Override
        public Table getTable() {
            return graph.getModel().getNodeTable();
        }

        @Override
        public Index<Node> getIndex(boolean localScale) {
            return localScale ? graph.getModel().getNodeIndex(graph.getView()) : graph.getModel().getNodeIndex();
        }

        @Override
        public List<Transformer> getTransformers() {
            return nodeTransformers;
        }

        @Override
        public String getIdPrefix() {
            return "node";
        }

        @Override
        public void refreshGraphFunctions() {
            if (!rankings.containsKey(getId("degree"))) {
                rankings.put(getId("degree"), new DegreeRankingImpl(graph));
            }
            if (graph.isDirected()) {
                if (!rankings.containsKey(getId("indegree"))) {
                    DirectedGraph directedGraph = (DirectedGraph) graph;
                    rankings.put(getId("indegree"), new InDegreeRankingImpl(directedGraph));
                    rankings.put(getId("outdegree"), new OutDegreeRankingImpl(directedGraph));
                }
            } else {
                rankings.remove(getId("indegree"));
                rankings.remove(getId("outdegree"));
            }

            // Degree functions
            for (Transformer t : getRankingTransformers()) {
                String degreeId = getId(t, "degree");
                RankingImpl degreeRanking = rankings.get(getId("degree"));
                if (!graphFunctions.containsKey(degreeId)) {
                    String name = NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.Degree.name");
                    graphFunctions.put(degreeId, new GraphFunctionImpl(degreeId, name, graph, t, getTransformerUI(t), degreeRanking, defaultInterpolator));
                }
                degreeRanking.refresh();

                String indegreeId = getId(t, "indegree");
                String outdegreeId = getId(t, "outdegree");

                RankingImpl indegreeRanking = rankings.get(getId("indegree"));
                RankingImpl outdegreeRanking = rankings.get(getId("outdegree"));
                if (indegreeRanking != null && outdegreeRanking != null) {
                    if (!graphFunctions.containsKey(indegreeId)) {
                        String inDegreeName = NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.InDegree.name");
                        String outDegreeName = NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.OutDegree.name");
                        graphFunctions.put(indegreeId, new GraphFunctionImpl(indegreeId, inDegreeName, graph, t, getTransformerUI(t), indegreeRanking, defaultInterpolator));
                        graphFunctions.put(outdegreeId, new GraphFunctionImpl(outdegreeId, outDegreeName, graph, t, getTransformerUI(t), outdegreeRanking, defaultInterpolator));
                    }
                    indegreeRanking.refresh();
                    outdegreeRanking.refresh();
                } else {
                    graphFunctions.remove(indegreeId);
                    graphFunctions.remove(outdegreeId);
                }
            }
        }
    }

    private class EdgeFunctionsModel extends ElementFunctionsModel<Edge> {

        public EdgeFunctionsModel(Graph graph) {
            super(graph);
        }

        @Override
        public Iterable<Edge> getElements() {
            return graph.getEdges();
        }

        @Override
        public Table getTable() {
            return graph.getModel().getNodeTable();
        }

        @Override
        public Index<Edge> getIndex(boolean localScale) {
            return localScale ? graph.getModel().getEdgeIndex(graph.getView()) : graph.getModel().getEdgeIndex();
        }

        @Override
        public List<Transformer> getTransformers() {
            return edgeTransformers;
        }

        @Override
        public String getIdPrefix() {
            return "edge";
        }

        @Override
        public void refreshGraphFunctions() {
            if (!rankings.containsKey(getId("weight"))) {
                rankings.put(getId("weight"), new EdgeWeightRankingImpl(graph));
            }
            if (graph.getModel().isMultiGraph()) {
                if (!partitions.containsKey(getId("type"))) {
                    partitions.put(getId("type"), new EdgeTypePartitionImpl(graph));
                }
            } else {
                partitions.remove(getId("type"));
            }

            // Weight function
            for (Transformer t : getRankingTransformers()) {
                String weightId = getId(t, "weight");
                RankingImpl ranking = rankings.get(getId("weight"));
                if (!graphFunctions.containsKey(weightId)) {
                    String name = NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Weight.name");
                    graphFunctions.put(weightId, new GraphFunctionImpl(weightId, name, graph, t, getTransformerUI(t), ranking, defaultInterpolator));
                }
                ranking.refresh();
            }

            // Type Function
            for (Transformer t : getPartitionTransformers()) {
                String typeId = getId(t, "type");
                PartitionImpl partition = partitions.get(getId("type"));
                if (partition != null) {
                    if (!graphFunctions.containsKey(typeId)) {
                        String name = NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Type.name");
                        graphFunctions.put(typeId, new GraphFunctionImpl(typeId, name, graph, t, getTransformerUI(t), partition));
                    }
                    //Refresh
                } else {
                    graphFunctions.remove(typeId);
                }
            }
        }
    }

    private class FunctionsModel {

        protected final Graph graph;
        protected final NodeFunctionsModel nodeFunctionsModel;
        protected final EdgeFunctionsModel edgeFunctionsModel;

        public FunctionsModel(Graph graph) {
            this.graph = graph;
            this.nodeFunctionsModel = new NodeFunctionsModel(graph);
            this.edgeFunctionsModel = new EdgeFunctionsModel(graph);
        }

        public Function[] getNodeFunctions() {
            return getFunctions(nodeFunctionsModel).toArray(new Function[0]);
        }

        public Function[] getEdgeFunctions() {
            return getFunctions(nodeFunctionsModel).toArray(new Function[0]);
        }

        private List<Function> getFunctions(ElementFunctionsModel model) {
            model.refreshFunctions();
            List<Function> functions = new ArrayList<Function>();
            functions.addAll(model.simpleFunctions.values());
            functions.addAll(model.graphFunctions.values());
            functions.addAll(model.attributeFunctions.values());
            return functions;
        }
    }

    private abstract class ElementFunctionsModel<T extends Element> {

        protected final Graph graph;
        protected final GraphObserver graphObserver;
        protected final Map<Column, ColumnObserver> columnObservers;
        protected final Map<String, SimpleFunctionImpl> simpleFunctions;
        protected final Map<String, GraphFunctionImpl> graphFunctions;
        protected final Map<String, AttributeFunctionImpl> attributeFunctions;
        protected final Map<String, PartitionImpl> partitions;
        protected final Map<String, RankingImpl> rankings;

        protected ElementFunctionsModel(Graph graph) {
            this.graph = graph;
            simpleFunctions = new HashMap<String, SimpleFunctionImpl>();
            graphFunctions = new HashMap<String, GraphFunctionImpl>();
            attributeFunctions = new HashMap<String, AttributeFunctionImpl>();
            columnObservers = new HashMap<Column, ColumnObserver>();
            graphObserver = graph.getModel().createGraphObserver(graph, false);
            partitions = new HashMap<String, PartitionImpl>();
            rankings = new HashMap<String, RankingImpl>();

            // Init simple
            initSimpleFunctions();
        }

        public abstract Iterable<T> getElements();

        public abstract Table getTable();

        public abstract Index<T> getIndex(boolean localScale);

        public abstract List<Transformer> getTransformers();

        public abstract String getIdPrefix();

        public abstract void refreshGraphFunctions();

        public Partition getPartition(Column column) {
            return partitions.get(getId(column));
        }

        protected void refreshFunctions() {
            graph.readLock();
            boolean graphHasChanged = graphObserver.isNew() || graphObserver.hasGraphChanged();
            if (graphHasChanged) {
                refreshGraphFunctions();
            }
            refreshAttributeFunctions(graphHasChanged);
            graph.readUnlock();
        }

        private void refreshAttributeFunctions(boolean graphHasChanged) {
            Set<Column> columns = new HashSet<Column>();
            for (Column column : getTable()) {
                if (!column.isProperty() && column.isIndexed()) {
                    columns.add(column);
                }
            }

            //Clean
            for (Iterator<Map.Entry<Column, ColumnObserver>> itr = columnObservers.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<Column, ColumnObserver> entry = itr.next();
                if (!columns.contains(entry.getKey())) {
                    rankings.remove(getId(entry.getKey()));
                    partitions.remove(getId(entry.getKey()));
                    for (Transformer t : getTransformers()) {
                        attributeFunctions.remove(getId(t, entry.getKey()));
                    }
                    itr.remove();
                }
            }

            //Get columns to be refreshed
            List<Column> toRefreshColumns = new ArrayList<Column>();
            for (Column column : columns) {
                if (!columnObservers.containsKey(column)) {
                    columnObservers.put(column, column.createColumnObserver());
                    toRefreshColumns.add(column);
                } else if (columnObservers.get(column).hasColumnChanged() || graphHasChanged) {
                    toRefreshColumns.add(column);
                }
            }

            //Refresh ranking and partitions
            for (Column column : toRefreshColumns) {
                RankingImpl ranking = rankings.get(getId(column));
                PartitionImpl partition = partitions.get(getId(column));
                if (ranking == null && partition == null) {
                    if (isPartition(graph, column)) {
                        partition = new AttributePartitionImpl(column, getIndex(false));
                        partitions.put(getId(column), partition);
                    } else {
                        ranking = new AttributeRankingImpl(column, getIndex(localScale));
                        rankings.put(getId(column), ranking);
                    }
                }
                if (ranking != null) {
                    ranking.refresh();
                }
                if (partition != null) {
                    //Refresh
                }
            }

            //Ranking functions
            for (Transformer t : getRankingTransformers()) {
                for (Column col : toRefreshColumns) {
                    RankingImpl ranking = rankings.get(getId(col));
                    if (ranking != null) {
                        String id = getId(t, col);
                        if (!attributeFunctions.containsKey(id)) {
                            attributeFunctions.put(id, new AttributeFunctionImpl(id, graph, col, t, getTransformerUI(t), ranking, defaultInterpolator));
                        }
                    }
                }
            }

            //Partition functions
            for (Transformer t : getPartitionTransformers()) {
                for (Column col : toRefreshColumns) {
                    PartitionImpl partition = partitions.get(getId(col));
                    if (partition != null) {
                        String id = getId(t, col);
                        if (!attributeFunctions.containsKey(id)) {
                            attributeFunctions.put(id, new AttributeFunctionImpl(id, graph, col, t, getTransformerUI(t), partition));
                        }
                    }
                }
            }
        }

        private void initSimpleFunctions() {
            for (Transformer transformer : getTransformers()) {
                if (transformer instanceof SimpleTransformer) {
                    String id = getId(transformer, "simple");
                    simpleFunctions.put(id, new SimpleFunctionImpl(id, graph, transformer, getTransformerUI(transformer)));
                }
            }
        }

        protected TransformerUI getTransformerUI(Transformer transformer) {
            return transformerUIs.get(transformer.getClass());
        }

        protected List<Transformer> getRankingTransformers() {
            List<Transformer> res = new ArrayList<Transformer>();
            for (Transformer t : getTransformers()) {
                if (t instanceof RankingTransformer) {
                    res.add(t);
                }
            }
            return res;
        }

        protected List<Transformer> getPartitionTransformers() {
            List<Transformer> res = new ArrayList<Transformer>();
            for (Transformer t : getTransformers()) {
                if (t instanceof PartitionTransformer) {
                    res.add(t);
                }
            }
            return res;
        }

        protected String getId(Transformer transformer, Column column) {
            return getIdPrefix() + "_" + transformer.getClass().getSimpleName() + "_column_" + column.getId();
        }

        protected String getId(Transformer transformer, String suffix) {
            return getIdPrefix() + "_" + transformer.getClass().getSimpleName() + "_" + suffix;
        }

        protected String getId(Column column) {
            return getIdPrefix() + "_column_" + column.getId();
        }

        protected String getId(String suffix) {
            return getIdPrefix() + "_" + suffix;
        }
    }

    private boolean isPartition(Graph graph, Column column) {
        Index index;
        if (AttributeUtils.isNodeColumn(column)) {
            index = graphModel.getNodeIndex(graph.getView());
        } else {
            index = graphModel.getEdgeIndex(graph.getView());
        }
        int valueCount = index.countValues(column);
        int elementCount = index.countElements(column);
        double ratio = valueCount / (double) elementCount;
        if (column.isNumber()) {
            Class columnTypeClass = column.getTypeClass();
            if (columnTypeClass.equals(Integer.class)) {
                if (ratio < 0.6) {
                    return true;
                }
            } else if (ratio < 0.1) {
                return true;
            }
        } else if (ratio < 0.8) {
            return true;
        }
        return false;
    }

    private boolean isRanking(Graph graph, Column column) {
        if (column.isNumber()) {
            Index index;
            if (AttributeUtils.isNodeColumn(column)) {
                index = localScale ? graphModel.getNodeIndex(graph.getView()) : graphModel.getNodeIndex();
            } else {
                index = localScale ? graphModel.getEdgeIndex(graph.getView()) : graphModel.getEdgeIndex();
            }
            if (index.countValues(column) > 0 && !isPartition(graph, column)) {
                return true;
            }
        }
        return false;
    }

    public void setLocalScale(boolean localScale) {
        this.localScale = localScale;
    }

    protected GraphModel getGraphModel() {
        return graphModel;
    }

    private Map<Class, TransformerUI> initTransformerUIs() {
        //Index UIs
        Map<Class, TransformerUI> uis = new HashMap<Class, TransformerUI>();

        for (TransformerUI ui : Lookup.getDefault().lookupAll(TransformerUI.class)) {
            Class transformerClass = ui.getTransformerClass();
            if (transformerClass == null) {
                throw new NullPointerException("Transformer class can' be null");
            }
            if (uis.containsKey(transformerClass)) {
                throw new RuntimeException("A Transformer can't be attach to multiple TransformerUI");
            }
            uis.put(transformerClass, ui);
        }
        return uis;
    }

    private List<Transformer> initNodeTransformers() {
        List<Transformer> res = new ArrayList<Transformer>();
        for (Transformer transformer : Lookup.getDefault().lookupAll(Transformer.class)) {
            if (transformer.isNode()) {
                res.add(transformer);
            }
        }
        return res;
    }

    private List<Transformer> initEdgeTransformers() {
        List<Transformer> res = new ArrayList<Transformer>();
        for (Transformer transformer : Lookup.getDefault().lookupAll(Transformer.class)) {
            if (transformer.isEdge()) {
                res.add(transformer);
            }
        }
        return res;
    }
}
