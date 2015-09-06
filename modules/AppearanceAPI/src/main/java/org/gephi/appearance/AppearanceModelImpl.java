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
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author mbastian
 */
public class AppearanceModelImpl implements AppearanceModel {

    private final Workspace workspace;
    private final GraphModel graphModel;
    private final Interpolator defaultInterpolator;
    private boolean localScale = false;
    //Functions
    private final Object functionLock;
    private List<Function> nodeFunctions;
    private List<Function> edgeFunctions;

    public AppearanceModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.defaultInterpolator = Interpolator.LINEAR;
        this.functionLock = new Object();

        //Functions
        refreshFunctions();
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
    public Function[] getNodeFunctions() {
        refreshFunctions();
        return nodeFunctions.toArray(new Function[0]);
    }

    @Override
    public Function[] getEdgeFunctions() {
        refreshFunctions();
        return edgeFunctions.toArray(new Function[0]);
    }

    private void refreshFunctions() {
        synchronized (functionLock) {
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

            //Index existing funcs
            Set<Column> attributeNodeFunctions = new HashSet<Column>();
            Set<Column> attributeEdgeFunctions = new HashSet<Column>();
            if (nodeFunctions != null) {
                for (Function f : nodeFunctions) {
                    if (f.isAttribute()) {
                        attributeNodeFunctions.add(((AttributeFunction) f).getColumn());
                    }
                }
            }
            if (edgeFunctions != null) {
                for (Function f : edgeFunctions) {
                    if (f.isAttribute()) {
                        attributeEdgeFunctions.add(((AttributeFunction) f).getColumn());
                    }
                }
            }

            //Simple transformers
            if (nodeFunctions == null) {
                nodeFunctions = new ArrayList<Function>();
                edgeFunctions = new ArrayList<Function>();

                for (Transformer transformer : Lookup.getDefault().lookupAll(Transformer.class)) {
                    if (transformer instanceof SimpleTransformer) {
                        if (transformer.isNode()) {
                            nodeFunctions.add(new FunctionImpl(this, null, transformer, uis.get(transformer.getClass())));
                        }
                        if (transformer.isEdge()) {
                            edgeFunctions.add(new FunctionImpl(this, null, transformer, uis.get(transformer.getClass())));
                        }
                    }
                }
            }
            //Atts
            Set<Column> foundNodeColumns = new HashSet<Column>();
            Set<Column> foundEdgeColumns = new HashSet<Column>();
            for (Transformer transformer : Lookup.getDefault().lookupAll(Transformer.class)) {
                if (transformer instanceof RankingTransformer || transformer instanceof PartitionTransformer) {
                    if (transformer.isNode()) {
                        for (Column col : graphModel.getNodeTable()) {
                            if (!col.isProperty()) {
                                Index index = localScale ? graphModel.getNodeIndex(graphModel.getVisibleView()) : graphModel.getNodeIndex();
                                if (transformer instanceof RankingTransformer && isRanking(col) && !attributeNodeFunctions.contains(col)) {
                                    nodeFunctions.add(new FunctionImpl(this, col, transformer, uis.get(transformer.getClass()), new RankingImpl(col, index, defaultInterpolator)));
                                } else if (transformer instanceof PartitionTransformer && isPartition(col) && !attributeNodeFunctions.contains(col)) {
                                    nodeFunctions.add(new FunctionImpl(this, col, transformer, uis.get(transformer.getClass()), new PartitionImpl(col, index)));
                                }
                                foundNodeColumns.add(col);
                            }
                        }
                    }
                    if (transformer.isEdge()) {
                        for (Column col : graphModel.getEdgeTable()) {
                            if (!col.isProperty() && col.isNumber()) {
                                Index index = localScale ? graphModel.getEdgeIndex(graphModel.getVisibleView()) : graphModel.getEdgeIndex();
                                if (transformer instanceof RankingTransformer && isRanking(col) && !attributeEdgeFunctions.contains(col)) {
                                    edgeFunctions.add(new FunctionImpl(this, col, transformer, uis.get(transformer.getClass()), new RankingImpl(col, index, defaultInterpolator)));
                                } else if (transformer instanceof PartitionTransformer && isPartition(col) && !attributeEdgeFunctions.contains(col)) {
                                    edgeFunctions.add(new FunctionImpl(this, col, transformer, uis.get(transformer.getClass()), new PartitionImpl(col, index)));
                                }
                                foundEdgeColumns.add(col);
                            }
                        }
                    }
                }
            }
            attributeNodeFunctions.removeAll(foundNodeColumns);
            attributeEdgeFunctions.removeAll(foundEdgeColumns);

            //Remove
            for (Iterator<Function> nodeItr = nodeFunctions.iterator(); nodeItr.hasNext();) {
                Function f = nodeItr.next();
                if (f.isAttribute() && attributeNodeFunctions.contains(((AttributeFunction) f).getColumn())) {
                    nodeItr.remove();
                }
            }
            for (Iterator<Function> edgeItr = edgeFunctions.iterator(); edgeItr.hasNext();) {
                Function f = edgeItr.next();
                if (f.isAttribute() && attributeEdgeFunctions.contains(((AttributeFunction) f).getColumn())) {
                    edgeItr.remove();
                }
            }
        }
    }

    private boolean isPartition(Column column) {
        Index index;
        if (AttributeUtils.isNodeColumn(column)) {
            index = localScale ? graphModel.getNodeIndex(graphModel.getVisibleView()) : graphModel.getNodeIndex();
        } else {
            index = localScale ? graphModel.getEdgeIndex(graphModel.getVisibleView()) : graphModel.getEdgeIndex();
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
            } else {
                if (ratio < 0.1) {
                    return true;
                }
            }
        } else {
            if (ratio < 0.8) {
                return true;
            }
        }
        return false;
    }

    private boolean isRanking(Column column) {
        if (column.isNumber()) {
            Index index;
            if (AttributeUtils.isNodeColumn(column)) {
                index = localScale ? graphModel.getNodeIndex(graphModel.getVisibleView()) : graphModel.getNodeIndex();
            } else {
                index = localScale ? graphModel.getEdgeIndex(graphModel.getVisibleView()) : graphModel.getEdgeIndex();
            }
            if (index.countValues(column) > 0 && !isPartition(column)) {
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
}
