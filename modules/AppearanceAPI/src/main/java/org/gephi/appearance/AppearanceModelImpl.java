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
import org.gephi.appearance.api.AttributeFunction;
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
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.types.TimeMap;
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
    private final Map<Graph, FunctionsModel> functions = new HashMap<>();
    //Forced
    private final Set<String> forcedRanking;
    private final Set<String> forcedPartition;
    private final List<Column> forcedColumnsRefresh;

    public AppearanceModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.defaultInterpolator = Interpolator.LINEAR;
        this.functionLock = new Object();
        this.transformerUIs = initTransformerUIs();
        this.nodeTransformers = initNodeTransformers();
        this.edgeTransformers = initEdgeTransformers();
        this.forcedPartition = new HashSet<>();
        this.forcedRanking = new HashSet<>();
        this.forcedColumnsRefresh = new ArrayList<>();

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
    public Function getNodeFunction(Graph graph, Column column, Class<? extends Transformer> transformer) {
        for (Function f : refreshFunctions(graph).getNodeFunctions()) {
            if (f.isAttribute() && f.getTransformer().getClass().equals(transformer)
                    && ((AttributeFunction) f).getColumn().equals(column)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public Function getEdgeFunction(Graph graph, Column column, Class<? extends Transformer> transformer) {
        for (Function f : refreshFunctions(graph).getEdgeFunctions()) {
            if (f.isAttribute() && f.getTransformer().getClass().equals(transformer)
                    && ((AttributeFunction) f).getColumn().equals(column)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public Function getNodeFunction(Graph graph, GraphFunction graphFunction, Class<? extends Transformer> transformer) {
        String id = getNodeId(transformer, graphFunction);
        for (Function f : refreshFunctions(graph).getNodeFunctions()) {
            if (((FunctionImpl) f).getId().equals(id) && f.getTransformer().getClass().equals(transformer)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public Function getEdgeFunction(Graph graph, GraphFunction graphFunction, Class<? extends Transformer> transformer) {
        String id = getEdgeId(transformer, graphFunction);
        for (Function f : refreshFunctions(graph).getEdgeFunctions()) {
            if (((FunctionImpl) f).getId().equals(id) && f.getTransformer().getClass().equals(transformer)) {
                return f;
            }
        }
        return null;
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

    protected void forceRanking(Function function) {
        if (function instanceof AttributeFunction) {
            Column col = ((AttributeFunction) function).getColumn();
            String id = getIdCol(col);
            forcedColumnsRefresh.add(col);
            forcedPartition.remove(id);
            forcedRanking.add(id);
        }
    }

    protected void forcePartition(Function function) {
        if (function instanceof AttributeFunction) {
            Column col = ((AttributeFunction) function).getColumn();
            String id = getIdCol(col);
            forcedColumnsRefresh.add(col);
            forcedRanking.remove(id);
            forcedPartition.add(id);
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
        public Class<? extends Element> getElementClass() {
            return Node.class;
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
            if (!rankings.containsKey(getIdStr(GraphFunction.NODE_DEGREE.getId()))) {
                rankings.put(getIdStr(GraphFunction.NODE_DEGREE.getId()), new DegreeRankingImpl(graph));
            }
            if (graph.isDirected()) {
                if (!rankings.containsKey(getIdStr(GraphFunction.NODE_INDEGREE.getId()))) {
                    DirectedGraph directedGraph = (DirectedGraph) graph;
                    rankings.put(getIdStr(GraphFunction.NODE_INDEGREE.getId()), new InDegreeRankingImpl(directedGraph));
                    rankings.put(getIdStr(GraphFunction.NODE_OUTDEGREE.getId()), new OutDegreeRankingImpl(directedGraph));
                }
            } else {
                rankings.remove(getIdStr(GraphFunction.NODE_INDEGREE.getId()));
                rankings.remove(getIdStr(GraphFunction.NODE_OUTDEGREE.getId()));
            }

            // Degree functions
            for (Transformer t : getRankingTransformers()) {
                String degreeId = getId(t, GraphFunction.NODE_DEGREE.getId());
                RankingImpl degreeRanking = rankings.get(getIdStr(GraphFunction.NODE_DEGREE.getId()));
                if (!graphFunctions.containsKey(degreeId)) {
                    String name = NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.Degree.name");
                    graphFunctions.put(degreeId, new GraphFunctionImpl(degreeId, name, Node.class, graph, t, getTransformerUI(t), degreeRanking, defaultInterpolator));
                }
                degreeRanking.refresh();

                String indegreeId = getId(t, GraphFunction.NODE_INDEGREE.getId());
                String outdegreeId = getId(t, GraphFunction.NODE_OUTDEGREE.getId());

                RankingImpl indegreeRanking = rankings.get(getIdStr(GraphFunction.NODE_INDEGREE.getId()));
                RankingImpl outdegreeRanking = rankings.get(getIdStr(GraphFunction.NODE_OUTDEGREE.getId()));
                if (indegreeRanking != null && outdegreeRanking != null) {
                    if (!graphFunctions.containsKey(indegreeId)) {
                        String inDegreeName = NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.InDegree.name");
                        String outDegreeName = NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.OutDegree.name");
                        graphFunctions.put(indegreeId, new GraphFunctionImpl(indegreeId, inDegreeName, Node.class, graph, t, getTransformerUI(t), indegreeRanking, defaultInterpolator));
                        graphFunctions.put(outdegreeId, new GraphFunctionImpl(outdegreeId, outDegreeName, Node.class, graph, t, getTransformerUI(t), outdegreeRanking, defaultInterpolator));
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
        public Class<? extends Element> getElementClass() {
            return Edge.class;
        }

        @Override
        public Table getTable() {
            return graph.getModel().getEdgeTable();
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
            if (!rankings.containsKey(getIdStr(GraphFunction.EDGE_WEIGHT.getId()))) {
                rankings.put(getIdStr(GraphFunction.EDGE_WEIGHT.getId()), new EdgeWeightRankingImpl(graph));
            }
            if (graph.getModel().isMultiGraph()) {
                if (!partitions.containsKey(getIdStr(GraphFunction.EDGE_TYPE.getId()))) {
                    partitions.put(getIdStr(GraphFunction.EDGE_TYPE.getId()), new EdgeTypePartitionImpl(graph));
                }
            } else {
                partitions.remove(getIdStr(GraphFunction.EDGE_TYPE.getId()));
            }

            // Weight function
            for (Transformer t : getRankingTransformers()) {
                String weightId = getId(t, GraphFunction.EDGE_WEIGHT.getId());
                RankingImpl ranking = rankings.get(getIdStr(GraphFunction.EDGE_WEIGHT.getId()));
                if (!graphFunctions.containsKey(weightId)) {
                    String name = NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Weight.name");
                    graphFunctions.put(weightId, new GraphFunctionImpl(weightId, name, Edge.class, graph, t, getTransformerUI(t), ranking, defaultInterpolator));
                }
                ranking.refresh();
            }

            // Type Function
            for (Transformer t : getPartitionTransformers()) {
                String typeId = getId(t, GraphFunction.EDGE_TYPE.getId());
                PartitionImpl partition = partitions.get(getIdStr(GraphFunction.EDGE_TYPE.getId()));
                if (partition != null) {
                    if (!graphFunctions.containsKey(typeId)) {
                        String name = NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Type.name");
                        graphFunctions.put(typeId, new GraphFunctionImpl(typeId, name, Edge.class, graph, t, getTransformerUI(t), partition));
                    }
                    partition.refresh();
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
            return getFunctions(edgeFunctionsModel).toArray(new Function[0]);
        }

        private List<Function> getFunctions(ElementFunctionsModel model) {
            model.refreshFunctions();
            List<Function> functions = new ArrayList<>();
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
            simpleFunctions = new HashMap<>();
            graphFunctions = new HashMap<>();
            attributeFunctions = new HashMap<>();
            columnObservers = new HashMap<>();
            graphObserver = graph.getModel().createGraphObserver(graph, false);
            partitions = new HashMap<>();
            rankings = new HashMap<>();

            // Init simple
            initSimpleFunctions();
        }

        public abstract Iterable<T> getElements();

        public abstract Table getTable();

        public abstract Index<T> getIndex(boolean localScale);

        public abstract List<Transformer> getTransformers();

        public abstract String getIdPrefix();

        public abstract void refreshGraphFunctions();

        public abstract Class<? extends Element> getElementClass();

        public Partition getPartition(Column column) {
            return partitions.get(getIdCol(column));
        }

        protected void refreshFunctions() {
            graph.readLock();
            boolean graphHasChanged = graphObserver.isNew() || graphObserver.hasGraphChanged();
            if (graphHasChanged) {
                if (graphObserver.isNew()) {
                    graphObserver.hasGraphChanged();
                }
                refreshGraphFunctions();
            }
            refreshAttributeFunctions(graphHasChanged);
            graph.readUnlock();
        }

        private void refreshAttributeFunctions(boolean graphHasChanged) {
            Set<Column> columns = new HashSet<>();
            for (Column column : getTable()) {
                if (!column.isProperty()) {
                    columns.add(column);
                }
            }

            //Clean
            for (Iterator<Map.Entry<Column, ColumnObserver>> itr = columnObservers.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<Column, ColumnObserver> entry = itr.next();
                if (!columns.contains(entry.getKey()) || forcedColumnsRefresh.contains(entry.getKey())) {
                    rankings.remove(getIdCol(entry.getKey()));
                    partitions.remove(getIdCol(entry.getKey()));
                    for (Transformer t : getTransformers()) {
                        attributeFunctions.remove(getId(t, entry.getKey()));
                    }
                    itr.remove();
                    if (!entry.getValue().isDestroyed()) {
                        entry.getValue().destroy();
                    }
                }
            }

            //Get columns to be refreshed
            Set<Column> toRefreshColumns = new HashSet<>(forcedColumnsRefresh);
            for (Column column : columns) {
                if (!columnObservers.containsKey(column)) {
                    columnObservers.put(column, column.createColumnObserver(false));
                    toRefreshColumns.add(column);
                } else if (columnObservers.get(column).hasColumnChanged() || graphHasChanged) {
                    toRefreshColumns.add(column);
                }
            }
            forcedColumnsRefresh.clear();

            //Refresh ranking and partitions
            for (Column column : toRefreshColumns) {
                RankingImpl ranking = rankings.get(getIdCol(column));
                PartitionImpl partition = partitions.get(getIdCol(column));
                if (ranking == null && partition == null) {
                    String id = getIdCol(column);
                    if (forcedPartition.contains(id) || (!forcedRanking.contains(id) && isPartition(graph, column))) {
                        if (column.isIndexed()) {
                            partition = new AttributePartitionImpl(column, getIndex(false));
                        } else {
                            partition = new AttributePartitionImpl(column, graph);
                        }
                        partitions.put(getIdCol(column), partition);
                    } else if (forcedRanking.contains(id) || (!forcedPartition.contains(id) && isRanking(graph, column))) {
                        if (column.isIndexed()) {
                            ranking = new AttributeRankingImpl(column, getIndex(localScale));
                        } else {
                            ranking = new AttributeRankingImpl(column, graph);
                        }
                        rankings.put(getIdCol(column), ranking);
                    }
                }
                if (ranking != null) {
                    ranking.refresh();
                }
                if (partition != null) {
                    partition.refresh();
                }
            }

            //Ranking functions
            for (Transformer t : getRankingTransformers()) {
                for (Column col : toRefreshColumns) {
                    RankingImpl ranking = rankings.get(getIdCol(col));
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
                    PartitionImpl partition = partitions.get(getIdCol(col));
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
                    simpleFunctions.put(id, new SimpleFunctionImpl(id, getElementClass(), graph, transformer, getTransformerUI(transformer)));
                }
            }
        }

        protected TransformerUI getTransformerUI(Transformer transformer) {
            return transformerUIs.get(transformer.getClass());
        }

        protected List<Transformer> getRankingTransformers() {
            List<Transformer> res = new ArrayList<>();
            for (Transformer t : getTransformers()) {
                if (t instanceof RankingTransformer) {
                    res.add(t);
                }
            }
            return res;
        }

        protected List<Transformer> getPartitionTransformers() {
            List<Transformer> res = new ArrayList<>();
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

        protected String getIdStr(String suffix) {
            return getIdPrefix() + "_" + suffix;
        }
    }

    protected String getIdCol(Column column) {
        return (AttributeUtils.isNodeColumn(column) ? "node" : "edge") + "_column_" + column.getId();
    }

    protected String getNodeId(Class<? extends Transformer> transformer, GraphFunction graphFunction) {
        return "node_" + transformer.getSimpleName() + "_" + graphFunction.getId();
    }

    protected String getEdgeId(Class<? extends Transformer> transformer, GraphFunction graphFunction) {
        return "edge_" + transformer.getSimpleName() + "_" + graphFunction.getId();
    }

    private boolean isPartition(Graph graph, Column column) {
        double ratio;
        if (column.isDynamic()) {
            if (!column.isNumber()) {
                return true;
            }
            Set<Object> set = new HashSet<>();
            boolean hasNullValue = false;
            int elements = 0;
            ElementIterable<? extends Element> iterable = AttributeUtils.isNodeColumn(column) ? graph.getNodes() : graph.getEdges();
            for (Element el : iterable) {
                TimeMap val = (TimeMap) el.getAttribute(column);
                if (val != null) {
                    Object[] va = val.toValuesArray();
                    for (Object v : va) {
                        if (v != null) {
                            set.add(v);
                        } else {
                            hasNullValue = true;
                        }
                        elements++;
                    }
                }
            }
            ratio = set.size() / (double) elements;
        } else if (column.isIndexed()) {
            if (!column.isNumber()) {
                return true;
            }
            Index index;
            if (AttributeUtils.isNodeColumn(column)) {
                index = graphModel.getNodeIndex(graph.getView());
            } else {
                index = graphModel.getEdgeIndex(graph.getView());
            }
            int valueCount = index.countValues(column);
            int elementCount = index.countElements(column);
            ratio = valueCount / (double) elementCount;
        } else {
            return false;
        }
        Class typeClass = column.getTypeClass();
        typeClass = column.isDynamic() ? AttributeUtils.getStaticType(typeClass) : typeClass;
        if (typeClass.equals(Integer.class) || typeClass.equals(Byte.class) || typeClass.equals(Short.class)) {
            return ratio <= 0.3;
        }
        return ratio <= 0.05;
    }

    private boolean isRanking(Graph graph, Column column) {
        if (column.isDynamic() && column.isNumber()) {
            ElementIterable<? extends Element> iterable = AttributeUtils.isNodeColumn(column) ? graph.getNodes() : graph.getEdges();
            for (Element el : iterable) {
                if (el.getAttribute(column, graph.getView()) != null) {
                    iterable.doBreak();
                    return true;
                }
            }
        } else if (!column.isDynamic() && column.isIndexed() && column.isNumber()) {
            Index index;
            if (AttributeUtils.isNodeColumn(column)) {
                index = localScale ? graphModel.getNodeIndex(graph.getView()) : graphModel.getNodeIndex();
            } else {
                index = localScale ? graphModel.getEdgeIndex(graph.getView()) : graphModel.getEdgeIndex();
            }
            if (index.countValues(column) > 0) {
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
        Map<Class, TransformerUI> uis = new HashMap<>();

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
        List<Transformer> res = new ArrayList<>();
        for (Transformer transformer : Lookup.getDefault().lookupAll(Transformer.class)) {
            if (transformer.isNode()) {
                res.add(transformer);
            }
        }
        return res;
    }

    private List<Transformer> initEdgeTransformers() {
        List<Transformer> res = new ArrayList<>();
        for (Transformer transformer : Lookup.getDefault().lookupAll(Transformer.class)) {
            if (transformer.isEdge()) {
                res.add(transformer);
            }
        }
        return res;
    }
}
