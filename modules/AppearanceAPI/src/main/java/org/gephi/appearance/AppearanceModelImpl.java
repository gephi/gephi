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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author mbastian
 */
public class AppearanceModelImpl implements AppearanceModel {

    private final Workspace workspace;
    private final GraphModel graphModel;
    // Transformers
    private final List<Transformer> nodeTransformers;
    private final List<Transformer> edgeTransformers;
    // Transformer UIS
    private final Map<Class, TransformerUI> transformerUIs;
    // Ranking and partitions
    private final DegreeRankingImpl degreeRanking;
    private final InDegreeRankingImpl inDegreeRanking;
    private final OutDegreeRankingImpl outDegreeRanking;
    private final EdgeWeightRankingImpl edgeWeightRanking;
    private final EdgeTypePartitionImpl edgeTypePartition;
    private final Map<Column, AttributeRankingImpl> nodeAttributeRankings;
    private final Map<Column, AttributeRankingImpl> edgeAttributeRankings;
    private final Map<Column, AttributePartitionImpl> nodeAttributePartitions;
    private final Map<Column, AttributePartitionImpl> edgeAttributePartitions;
    // Static functions
    private final List<FunctionImpl> nodeStaticFunctions;
    private final List<FunctionImpl> edgeStaticFunctions;
    // LocalScale (if true, uses visible graph)
    private boolean rankingLocalScale = false;
    private boolean partitionLocalScale = false;
    // Null values settings
    private boolean transformNullValues = false;

    public AppearanceModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.transformerUIs = initTransformerUIs();
        this.nodeTransformers = initNodeTransformers();
        this.edgeTransformers = initEdgeTransformers();

        degreeRanking = new DegreeRankingImpl(graphModel.defaultColumns().degree());
        inDegreeRanking = new InDegreeRankingImpl(graphModel.defaultColumns().inDegree());
        outDegreeRanking = new OutDegreeRankingImpl(graphModel.defaultColumns().outDegree());
        edgeWeightRanking = new EdgeWeightRankingImpl();
        edgeTypePartition = new EdgeTypePartitionImpl(graphModel.defaultColumns().edgeType(),
            graphModel.getConfiguration().getEdgeLabelType());
        nodeAttributeRankings = new WeakHashMap<>();
        edgeAttributeRankings = new WeakHashMap<>();
        nodeAttributePartitions = new WeakHashMap<>();
        edgeAttributePartitions = new WeakHashMap<>();

        // Static functions
        nodeStaticFunctions = getNodeSimpleFunctions();
        nodeStaticFunctions.addAll(getNodeRankingFunctions());
        edgeStaticFunctions = getEdgeSimpleFunctions();
        edgeStaticFunctions.addAll(getRankingAndPartitionEdgeFunctions());

        //Init
        initAttributeRankingsAndPartitions();
    }

    protected Graph getRankingGraph() {
        if (rankingLocalScale) {
            return graphModel.getGraphVisible();
        } else {
            return graphModel.getGraph();
        }
    }

    protected Graph getPartitionGraph() {
        if (partitionLocalScale) {
            return graphModel.getGraphVisible();
        } else {
            return graphModel.getGraph();
        }
    }

    @Override
    public Function[] getNodeFunctions() {
        List<FunctionImpl> res = new ArrayList<>();
        res.addAll(nodeStaticFunctions);
        res.addAll(getAttributeFunctions(graphModel.getNodeTable()));
        return res.stream().filter(FunctionImpl::isValid).toArray(Function[]::new);
    }

    @Override
    public Function[] getEdgeFunctions() {
        List<FunctionImpl> res = new ArrayList<>();
        res.addAll(edgeStaticFunctions);
        res.addAll(getAttributeFunctions(graphModel.getEdgeTable()));
        return res.stream().filter(FunctionImpl::isValid).toArray(Function[]::new);
    }

    @Override
    public Function getNodeFunction(Column column, Class<? extends Transformer> transformer) {
        return getFunction(column, transformer);
    }

    @Override
    public Function getEdgeFunction(Column column, Class<? extends Transformer> transformer) {
        return getFunction(column, transformer);
    }

    private Function getFunction(Column column, Class<? extends Transformer> transformer) {
        if (column.isProperty()) {
            GraphFunction graphFunction = convertColumnToGraphFunction(column);
            List<GraphFunctionImpl> funcs =
                column.getTable().isNodeTable() ? getNodeRankingFunctions() : getRankingAndPartitionEdgeFunctions();
            return funcs.stream()
                .filter(f -> f.getTransformer().getClass().equals(transformer) &&
                    f.getGraphFunction().equals(graphFunction))
                .filter(FunctionImpl::isValid)
                .findFirst().orElse(null);
        } else {
            return getAttributeFunctions(column.getTable()).stream()
                .filter(f -> f.getTransformer().getClass().equals(transformer) && f.getColumn().equals(column))
                .filter(FunctionImpl::isValid)
                .findFirst().orElse(null);
        }
    }

    @Override
    public Partition getNodePartition(Column column) {
        return initAttributePartition(column);
    }

    @Override
    public Partition getEdgePartition(Column column) {
        return initAttributePartition(column);
    }

    protected RankingImpl[] getNodeRankings() {
        List<RankingImpl> rankings = new ArrayList<>();
        rankings.add(degreeRanking);
        rankings.add(inDegreeRanking);
        rankings.add(outDegreeRanking);
        rankings.addAll(nodeAttributeRankings.values());
        return rankings.toArray(new RankingImpl[0]);
    }

    protected RankingImpl[] getEdgeRankings() {
        List<RankingImpl> rankings = new ArrayList<>();
        rankings.add(edgeWeightRanking);
        rankings.addAll(edgeAttributeRankings.values());
        return rankings.toArray(new RankingImpl[0]);
    }

    protected PartitionImpl[] getNodePartitions() {
        return nodeAttributePartitions.values().toArray(new AttributePartitionImpl[0]);
    }

    protected PartitionImpl[] getEdgePartitions() {
        List<PartitionImpl> partitions = new ArrayList<>();
        partitions.add(edgeTypePartition);
        partitions.addAll(edgeAttributePartitions.values());
        return partitions.toArray(new PartitionImpl[0]);
    }

    protected RankingImpl getDegreeRanking() {
        return degreeRanking;
    }

    protected Ranking getInDegreeRanking() {
        return inDegreeRanking;
    }

    protected Ranking getOutDegreeRanking() {
        return outDegreeRanking;
    }

    protected Ranking getEdgeWeightRanking() {
        return edgeWeightRanking;
    }

    protected Partition getEdgeTypePartition() {
        return edgeTypePartition;
    }

    protected RankingImpl getNodeRanking(Column column) {
        return nodeAttributeRankings.get(column);
    }

    protected Ranking getEdgeRanking(Column column) {
        return edgeAttributeRankings.get(column);
    }

    // Only for testing
    protected int countNodeAttributeRanking() {
        return nodeAttributeRankings.size();
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean isRankingLocalScale() {
        return rankingLocalScale;
    }

    public void setRankingLocalScale(boolean localScale) {
        this.rankingLocalScale = localScale;
    }

    @Override
    public boolean isPartitionLocalScale() {
        return partitionLocalScale;
    }

    public void setPartitionLocalScale(boolean localScale) {
        this.partitionLocalScale = localScale;
    }

    @Override
    public boolean isTransformNullValues() {
        return transformNullValues;
    }

    public void setTransformNullValues(boolean transformNullValues) {
        this.transformNullValues = transformNullValues;
    }

    private List<GraphFunctionImpl> getNodeRankingFunctions() {
        return nodeTransformers.stream().filter(t -> t instanceof RankingTransformer)
            .flatMap(t -> getDegreeFunctions(t).stream()).collect(Collectors.toList());
    }

    private void cleanAttributeRankingsAndPartitions(Table table) {
        if (table.isNodeTable()) {
            nodeAttributeRankings.keySet().removeIf(c -> !c.exists());
            nodeAttributePartitions.keySet().removeIf(c -> !c.exists());
        } else {
            edgeAttributeRankings.keySet().removeIf(c -> !c.exists());
            edgeAttributePartitions.keySet().removeIf(c -> !c.exists());
        }
    }

    private List<AttributeFunctionImpl> getAttributeFunctions(Table table) {
        cleanAttributeRankingsAndPartitions(table);

        List<AttributeFunctionImpl> res = new ArrayList<>();
        List<Transformer> transformers = table.isNodeTable() ? nodeTransformers : edgeTransformers;
        for (Column column : table) {
            if (!column.isProperty()) {
                transformers.forEach(t -> {
                    if ((column.isNumber() && t instanceof RankingTransformer) || t instanceof PartitionTransformer) {
                        res.addAll(getAttributeFunctions(column, t));
                    }
                });
            } else if (column.isDynamic() && !column.isDynamicAttribute()) {
                transformers.stream().filter(t -> t instanceof RankingTransformer).forEach(t -> {
                    res.addAll(getTimesetFunctions(column, t));
                });
            }
        }
        return res;
    }

    private List<AttributeFunctionImpl> getTimesetFunctions(Column column, Transformer transformer) {
        List<AttributeFunctionImpl> res = new ArrayList<>();
        if (transformer instanceof RankingTransformer && column.isDynamic() && !column.isDynamicAttribute()) {
            if (transformer.isNode() && column.getTable().isNodeTable()) {
                RankingImpl ranking =
                    nodeAttributeRankings.computeIfAbsent(column, k -> new TimesetRankingImpl(column));
                res.add(new AttributeFunctionImpl(this, getId("node", transformer, column), column,
                    transformer, getTransformerUI(transformer), ranking));
            }
            if (transformer.isEdge() && column.getTable().isEdgeTable()) {
                RankingImpl ranking =
                    edgeAttributeRankings.computeIfAbsent(column, k -> new TimesetRankingImpl(column));
                res.add(new AttributeFunctionImpl(this, getId("edge", transformer, column), column,
                    transformer, getTransformerUI(transformer), ranking));
            }
        }
        return res;
    }

    private List<AttributeFunctionImpl> getAttributeFunctions(Column column, Transformer transformer) {
        List<AttributeFunctionImpl> res = new ArrayList<>();
        if (transformer instanceof RankingTransformer && column.isNumber()) {
            if (transformer.isNode() && column.getTable().isNodeTable()) {
                RankingImpl ranking =
                    nodeAttributeRankings.computeIfAbsent(column, k -> new AttributeRankingImpl(column));
                res.add(new AttributeFunctionImpl(this, getId("node", transformer, column), column,
                    transformer, getTransformerUI(transformer), ranking));
            }
            if (transformer.isEdge() && column.getTable().isEdgeTable()) {
                RankingImpl ranking =
                    edgeAttributeRankings.computeIfAbsent(column, k -> new AttributeRankingImpl(column));
                res.add(new AttributeFunctionImpl(this, getId("edge", transformer, column), column,
                    transformer, getTransformerUI(transformer), ranking));
            }
        }
        if (transformer instanceof PartitionTransformer) {
            if (transformer.isNode() && column.getTable().isNodeTable()) {
                PartitionImpl partition =
                    nodeAttributePartitions.computeIfAbsent(column, k -> new AttributePartitionImpl(column));
                res.add(new AttributeFunctionImpl(this, getId("node", transformer, column), column, transformer,
                    getTransformerUI(transformer), partition));
            }
            if (transformer.isEdge() && column.getTable().isEdgeTable()) {
                PartitionImpl partition =
                    edgeAttributePartitions.computeIfAbsent(column, k -> new AttributePartitionImpl(column));
                res.add(new AttributeFunctionImpl(this, getId("edge", transformer, column), column, transformer,
                    getTransformerUI(transformer), partition));
            }
        }
        return res;
    }

    private void initAttributeRankingsAndPartitions() {
        for (Column column : graphModel.getNodeTable()) {
            if (!column.isProperty()) {
                if (column.isNumber()) {
                    nodeAttributeRankings.put(column, new AttributeRankingImpl(column));
                }
                nodeAttributePartitions.put(column, new AttributePartitionImpl(column));
            }
        }
        for (Column column : graphModel.getEdgeTable()) {
            if (!column.isProperty()) {
                if (column.isNumber()) {
                    edgeAttributeRankings.put(column, new AttributeRankingImpl(column));
                }
                edgeAttributePartitions.put(column, new AttributePartitionImpl(column));
            }
        }
    }

    private AttributePartitionImpl initAttributePartition(Column column) {
        if (!column.isProperty()) {
            if (column.getTable().isNodeTable()) {
                return nodeAttributePartitions.computeIfAbsent(column, k -> new AttributePartitionImpl(column));
            } else if (column.getTable().isEdgeTable()) {
                return edgeAttributePartitions.computeIfAbsent(column, k -> new AttributePartitionImpl(column));
            }
        }
        return null;
    }

    private List<FunctionImpl> getNodeSimpleFunctions() {
        return Lookup.getDefault().lookupAll(Transformer.class).stream()
            .filter(t -> t instanceof SimpleTransformer && t.isNode())
            .map(t -> new SimpleFunctionImpl(this, getId("node", t, "simple"), Node.class, t, getTransformerUI(t)))
            .collect(Collectors.toList());
    }

    private List<FunctionImpl> getEdgeSimpleFunctions() {
        return Lookup.getDefault().lookupAll(Transformer.class).stream()
            .filter(t -> t instanceof SimpleTransformer && t.isEdge())
            .map(t -> new SimpleFunctionImpl(this, getId("edge", t, "simple"), Edge.class, t, getTransformerUI(t)))
            .collect(Collectors.toList());
    }

    private List<GraphFunctionImpl> getDegreeFunctions(Transformer transformer) {
        List<GraphFunctionImpl> res = new ArrayList<>();
        TransformerUI transformerUI = getTransformerUI(transformer);

        res.add(
            new GraphFunctionImpl(this, GraphFunction.NODE_DEGREE, getId("node", transformer, "degree"),
                NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.Degree.name"),
                Node.class, transformer, transformerUI, degreeRanking));

        res.add(new GraphFunctionImpl(this, GraphFunction.NODE_INDEGREE, getId("node", transformer, "indegree"),
            NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.InDegree.name"),
            Node.class, transformer, transformerUI, inDegreeRanking));
        res.add(
            new GraphFunctionImpl(this, GraphFunction.NODE_OUTDEGREE, getId("node", transformer, "outdegree"),
                NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.OutDegree.name"),
                Node.class, transformer, transformerUI, outDegreeRanking));
        return res;
    }

    private List<GraphFunctionImpl> getRankingAndPartitionEdgeFunctions() {
        return edgeTransformers.stream().flatMap(t -> {
            List<GraphFunctionImpl> res = new ArrayList<>();
            TransformerUI transformerUI = getTransformerUI(t);

            if (t instanceof RankingTransformer) {
                res.add(new GraphFunctionImpl(this, GraphFunction.EDGE_WEIGHT, getId("edge", t, "weight"),
                    NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Weight.name"), Edge.class, t,
                    transformerUI, edgeWeightRanking));
            }

            if (t instanceof PartitionTransformer) {
                res.add(
                    new GraphFunctionImpl(this, GraphFunction.EDGE_TYPE, getId("edge", t, "type"),
                        NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Type.name"),
                        Edge.class, t, transformerUI, edgeTypePartition));
            }
            return res.stream();
        }).collect(Collectors.toList());
    }

    protected TransformerUI getTransformerUI(Transformer transformer) {
        return transformerUIs.get(transformer.getClass());
    }

    @Override
    public GraphModel getGraphModel() {
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
        return Lookup.getDefault().lookupAll(Transformer.class).stream().filter(Transformer::isNode).collect(
            Collectors.toList());
    }

    private List<Transformer> initEdgeTransformers() {
        return Lookup.getDefault().lookupAll(Transformer.class).stream().filter(Transformer::isEdge).collect(
            Collectors.toList());
    }

    private GraphFunction convertColumnToGraphFunction(Column column) {
        GraphFunction graphFunction = null;
        if (column.equals(graphModel.defaultColumns().degree())) {
            graphFunction = GraphFunction.NODE_DEGREE;
        } else if (column.equals(graphModel.defaultColumns().inDegree())) {
            graphFunction = GraphFunction.NODE_INDEGREE;
        } else if (column.equals(graphModel.defaultColumns().outDegree())) {
            graphFunction = GraphFunction.NODE_OUTDEGREE;
        } else if (column.equals(graphModel.defaultColumns().edgeType())) {
            graphFunction = GraphFunction.EDGE_TYPE;
        } else if (column.getTable().isEdgeTable() && column.getId().equals("weight")) {
            graphFunction = GraphFunction.EDGE_WEIGHT;
        }
        return graphFunction;
    }

    private String getId(String prefix, Transformer transformer, Column column) {
        return prefix + "_" + transformer.getClass().getSimpleName() + "_column_" + column.getId();
    }


    private String getId(String prefix, Transformer transformer, String suffix) {
        return prefix + "_" + transformer.getClass().getSimpleName() + "_" + suffix;
    }
}
