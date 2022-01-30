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
import org.gephi.appearance.api.Interpolator;
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
    private final Interpolator defaultInterpolator;
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
    // LocalScale (if true, uses visible graph)
    private boolean localScale = false;

    public AppearanceModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.defaultInterpolator = Interpolator.LINEAR;
        this.transformerUIs = initTransformerUIs();
        this.nodeTransformers = initNodeTransformers();
        this.edgeTransformers = initEdgeTransformers();

        degreeRanking = new DegreeRankingImpl();
        inDegreeRanking = new InDegreeRankingImpl();
        outDegreeRanking = new OutDegreeRankingImpl();
        edgeWeightRanking = new EdgeWeightRankingImpl();
        edgeTypePartition = new EdgeTypePartitionImpl();
        nodeAttributeRankings = new WeakHashMap<>();
        edgeAttributeRankings = new WeakHashMap<>();
        nodeAttributePartitions = new WeakHashMap<>();
        edgeAttributePartitions = new WeakHashMap<>();
    }

    @Override
    public Function[] getNodeFunctions() {
        List<Function> res = new ArrayList<>();
        res.addAll(getNodeRankingFunctions());
        res.addAll(getAttributeFunctions(graphModel.getNodeTable()));
        res.addAll(getNodeSimpleFunctions());
        return res.toArray(new Function[0]);
    }

    @Override
    public Function[] getEdgeFunctions() {
        List<Function> res = new ArrayList<>();
        res.addAll(getRankingAndPartitionEdgeFunctions());
        res.addAll(getAttributeFunctions(graphModel.getEdgeTable()));
        res.addAll(getEdgeSimpleFunctions());
        return res.toArray(new Function[0]);
    }

    @Override
    public Partition getNodePartition(Graph graph, Column column) {
        return null;
    }

    @Override
    public Partition getEdgePartition(Graph graph, Column column) {
        return null;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean isLocalScale() {
        return localScale;
    }

    public void setLocalScale(boolean localScale) {
        this.localScale = localScale;
    }

    private List<Function> getNodeRankingFunctions() {
        return nodeTransformers.stream().filter(t -> t instanceof RankingTransformer).flatMap(t -> {
            List<Function> res = new ArrayList<>();
            res.addAll(getDegreeFunctions(t));

            return res.stream();
        }).collect(Collectors.toList());
    }

    private List<Function> getAttributeFunctions(Table table) {
        List<Function> res = new ArrayList<>();
        List<Transformer> transformers = table.isNodeTable() ? nodeTransformers : edgeTransformers;
        for (Column column : table) {
            if (!column.isProperty()) {
                transformers.stream().forEach(t -> {
                    if (column.isNumber() && t instanceof RankingTransformer) {
                        res.addAll(getAttributeFunctions(column, t));
                    } else if (t instanceof PartitionTransformer) {
                        res.addAll(getAttributeFunctions(column, t));
                    }
                });
            }
        }
        return null;
    }

    private List<Function> getAttributeFunctions(Column column, Transformer transformer) {
        List<Function> res = new ArrayList<>();
        if (transformer instanceof RankingTransformer) {
            if (transformer.isNode()) {
                RankingImpl ranking = nodeAttributeRankings.computeIfAbsent(column, k -> new AttributeRankingImpl(column));
                res.add(new AttributeFunctionImpl(getId("node", transformer, column), column,
                    transformer, getTransformerUI(transformer), ranking));
            }
            if (transformer.isEdge()) {
                RankingImpl ranking = edgeAttributeRankings.computeIfAbsent(column, k -> new AttributeRankingImpl(column));
                res.add(new AttributeFunctionImpl(getId("edge", transformer, column), column,
                    transformer, getTransformerUI(transformer), ranking));
            }
        } else if (transformer instanceof PartitionTransformer) {
            if (transformer.isNode()) {
                PartitionImpl partition = nodeAttributePartitions.computeIfAbsent(column, k -> new AttributePartitionImpl(column));
                res.add(new AttributeFunctionImpl(getId("node", transformer, column), column, transformer,
                    getTransformerUI(transformer), partition));
            }
            if (transformer.isEdge()) {
                PartitionImpl partition = edgeAttributePartitions.computeIfAbsent(column, k -> new AttributePartitionImpl(column));
                res.add(new AttributeFunctionImpl(getId("edge", transformer, column), column, transformer,
                    getTransformerUI(transformer), partition));
            }
        }
        return res;
    }

    private List<Function> getNodeSimpleFunctions() {
        return Lookup.getDefault().lookupAll(Transformer.class).stream()
            .filter(t -> t instanceof SimpleTransformer && t.isNode())
            .map(t -> new SimpleFunctionImpl(getId("node", t, "simple"), Node.class, t, getTransformerUI(t)))
            .collect(Collectors.toList());
    }

    private List<Function> getEdgeSimpleFunctions() {
        return Lookup.getDefault().lookupAll(Transformer.class).stream()
            .filter(t -> t instanceof SimpleTransformer && t.isEdge())
            .map(t -> new SimpleFunctionImpl(getId("edge", t, "simple"), Edge.class, t, getTransformerUI(t)))
            .collect(Collectors.toList());
    }

    private List<GraphFunctionImpl> getDegreeFunctions(Transformer transformer) {
        List<GraphFunctionImpl> res = new ArrayList<>();
        TransformerUI transformerUI = getTransformerUI(transformer);

        res.add(new GraphFunctionImpl(NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.Degree.name"),
            Node.class, transformer, transformerUI, degreeRanking));

        res.add(new GraphFunctionImpl(NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.InDegree.name"),
            Node.class, transformer, transformerUI, inDegreeRanking));
        res.add(
            new GraphFunctionImpl(NbBundle.getMessage(AppearanceModelImpl.class, "NodeGraphFunction.OutDegree.name"),
                Node.class, transformer, transformerUI, outDegreeRanking));
        return res;
    }

    private List<GraphFunctionImpl> getRankingAndPartitionEdgeFunctions() {
        return edgeTransformers.stream().flatMap(t -> {
            List<GraphFunctionImpl> res = new ArrayList<>();
            TransformerUI transformerUI = getTransformerUI(t);

            if (t instanceof RankingTransformer) {
                res.add(new GraphFunctionImpl(
                    NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Weight.name"), Edge.class, t,
                    transformerUI, edgeWeightRanking));
            }

            if (t instanceof PartitionTransformer) {
                res.add(
                    new GraphFunctionImpl(NbBundle.getMessage(AppearanceModelImpl.class, "EdgeGraphFunction.Type.name"),
                        Edge.class, t, transformerUI, edgeTypePartition));
            }
            return res.stream();
        }).collect(Collectors.toList());
    }

    protected TransformerUI getTransformerUI(Transformer transformer) {
        return transformerUIs.get(transformer.getClass());
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
        return Lookup.getDefault().lookupAll(Transformer.class).stream().filter(Transformer::isNode).collect(
            Collectors.toList());
    }

    private List<Transformer> initEdgeTransformers() {
        return Lookup.getDefault().lookupAll(Transformer.class).stream().filter(Transformer::isEdge).collect(
            Collectors.toList());
    }

    private String getId(Column column) {
        return "column_" + column.getId();
    }

    private String getId(String prefix, Transformer transformer, Column column) {
        return prefix + "_" + transformer.getClass().getSimpleName() + "_column_" + column.getId();
    }


    private String getId(String prefix, Transformer transformer, String suffix) {
        return prefix + "_" + transformer.getClass().getSimpleName() + "_" + suffix;
    }
}
