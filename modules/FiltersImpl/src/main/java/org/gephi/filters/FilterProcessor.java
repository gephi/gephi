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
package org.gephi.filters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.*;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterProcessor {

    public Graph process(AbstractQueryImpl query, GraphModel graphModel) {
        Graph graph = graphModel.getGraph();

        graph.writeLock();
        try {
            List<GraphView> views = new ArrayList<>();
            query = simplifyQuery(query);
            AbstractQueryImpl[] tree = getTree(query, true);
            for (int i = 0; i < tree.length; i++) {
                AbstractQueryImpl q = tree[tree.length - i - 1];
                Graph[] input;
                if (q.getChildrenCount() > 0) {
                    input = new Graph[q.getChildrenCount()];
                    for (int j = 0; j < input.length; j++) {
                        input[j] = q.getChildAt(j).getResult();
                    }
                } else {
                    //Leaves
                    GraphView newView = graphModel.copyView(graphModel.getGraph().getView());
                    views.add(newView);
                    input = new Graph[]{graphModel.getGraph(newView)};    //duplicate root
                }
                //PROCESS
                if (q instanceof OperatorQueryImpl && !((OperatorQueryImpl) q).isSimple()) {
                    OperatorQueryImpl operatorQuery = (OperatorQueryImpl) q;
                    Operator op = (Operator) operatorQuery.getFilter();
                    Subgraph[] inputSG = new Subgraph[input.length];
                    for (int j = 0; j < inputSG.length; j++) {
                        inputSG[j] = (Subgraph) input[j];
                    }
                    q.setResult(op.filter(inputSG));
                } else if (q instanceof OperatorQueryImpl && ((OperatorQueryImpl) q).isSimple()) {
                    OperatorQueryImpl operatorQuery = (OperatorQueryImpl) q;
                    Operator op = (Operator) operatorQuery.getFilter();
                    GraphView newView = graphModel.copyView(graphModel.getGraph().getView());
                    views.add(newView);
                    Graph newGraph = graphModel.getGraph(newView);
                    List<Filter> filters = new ArrayList<>();
                    for (int k = 0; k < operatorQuery.getChildrenCount(); k++) {
                        Filter filter = operatorQuery.getChildAt(k).getFilter();
                        if (init(filter, newGraph)) {
                            filters.add(filter);
                        }
                    }
                    q.setResult(op.filter(newGraph, filters.toArray(new Filter[0])));
                } else {
                    FilterQueryImpl filterQuery = (FilterQueryImpl) q;
                    Filter filter = filterQuery.getFilter();
                    if (filter instanceof NodeFilter && filter instanceof EdgeFilter) {
                        processNodeFilter((NodeFilter) filter, input[0]);
                        processEdgeFilter((EdgeFilter) filter, input[0]);
                        q.setResult(input[0]);
                    } else if (filter instanceof NodeFilter) {
                        processNodeFilter((NodeFilter) filter, input[0]);
                        q.setResult(input[0]);
                    } else if (filter instanceof EdgeFilter) {
                        processEdgeFilter((EdgeFilter) filter, input[0]);
                        q.setResult(input[0]);
                    } else if (filter instanceof ComplexFilter) {
                        ComplexFilter cf = (ComplexFilter) filter;
                        q.setResult(cf.filter(input[0]));
                    } else {
                        q.setResult(input[0]);  //Put input as result, the filter don't do anything
                    }
                }
            }
            Graph finalResult = tree[0].result;

            //Destroy intermediate views
            GraphView finalView = finalResult.getView();
            for (GraphView v : views) {
                if (v != finalView && !v.isMainView()) {
                    graphModel.destroyView(v);
                }
            }

            return finalResult;
        } finally {
            graph.writeUnlock();
            graph.readUnlockAll();
        }
    }

    private void processNodeFilter(NodeFilter nodeFilter, Graph graph) {
        if (init(nodeFilter, graph)) {
            List<Node> nodesToRemove = new ArrayList<>();
            for (Node n : graph.getNodes()) {
                if (!nodeFilter.evaluate(graph, n)) {
                    nodesToRemove.add(n);
                }
            }

            if (!nodesToRemove.isEmpty()) {
                graph.removeAllNodes(nodesToRemove);
            }
            nodeFilter.finish();
        }
    }

    private void processEdgeFilter(EdgeFilter edgeFilter, Graph graph) {
        if (init(edgeFilter, graph)) {
            List<Edge> edgesToRemove = new ArrayList<>();
            for (Edge e : graph.getEdges()) {
                if (!edgeFilter.evaluate(graph, e)) {
                    edgesToRemove.add(e);
                }
            }

            if (!edgesToRemove.isEmpty()) {
                graph.removeAllEdges(edgesToRemove);
            }
            edgeFilter.finish();
        }
    }

    private AbstractQueryImpl simplifyQuery(AbstractQueryImpl query) {
        AbstractQueryImpl copy = query.copy();
        for (AbstractQueryImpl q : getTree(copy, false)) {
            if (q instanceof OperatorQueryImpl && q.getChildrenCount() > 0) {
                boolean canSimplify = true;
                for (AbstractQueryImpl child : q.children) {
                    if (child.getChildrenCount() > 0 || !(child.getFilter() instanceof NodeFilter || child.getFilter() instanceof EdgeFilter || child.getFilter() instanceof ElementFilter)) {
                        canSimplify = false;
                    }
                }
                if (canSimplify) {
                    ((OperatorQueryImpl) q).setSimple(true);
                }
            }
        }
        return copy;
    }

    private AbstractQueryImpl[] getTree(AbstractQueryImpl query, boolean ignoreSimple) {
        ArrayList<AbstractQueryImpl> tree = new ArrayList<>();
        int pointer = 0;
        tree.add(query);
        while (pointer < tree.size()) {
            AbstractQueryImpl q = tree.get(pointer++);
            if (q.children.size() > 0) {
                if (!(q instanceof OperatorQueryImpl && ((OperatorQueryImpl) q).isSimple())) {
                    tree.addAll(q.children);
                }
            }
        }
        return tree.toArray(new AbstractQueryImpl[0]);
    }

    public boolean init(Filter filter, Graph graph) {
        boolean res = true;

        //Init res
        if (filter instanceof NodeFilter) {
            res = ((NodeFilter) filter).init(graph);
        } else if (filter instanceof EdgeFilter) {
            res = ((EdgeFilter) filter).init(graph);
        }

        //Range
        if (filter instanceof RangeFilter) {
            RangeFilter rangeFilter = (RangeFilter) filter;
            Number[] values = rangeFilter.getValues(graph);
            NumberComparator comparator = new NumberComparator();
            Number min = null;
            Number max = null;
            if (values != null) {
                for (Number n : values) {
                    min = min == null ? n : comparator.min(min, n);
                    max = max == null ? n : comparator.max(max, n);
                }
            }

            Range previousRange = (Range) rangeFilter.getRangeProperty().getValue();
            Range newRange;
            if (min == null || max == null) {
                newRange = null;
                rangeFilter.getRangeProperty().setValue(newRange);
            } else if (previousRange == null) {
                newRange = new Range(min, max, min, max, values);
                rangeFilter.getRangeProperty().setValue(newRange);
            } else if (previousRange != null && (previousRange.getMinimum() == null || previousRange.getMaximum() == null)) {
                //Opening projects
                newRange = new Range(previousRange.getLowerBound(), previousRange.getUpperBound(), min, max, previousRange.isLeftInclusive(), previousRange.isRightInclusive(), values);
                rangeFilter.getRangeProperty().setValue(newRange);
            } else {
                //Collect some info
                boolean stickyLeft = previousRange.getMinimum().equals(previousRange.getLowerBound());
                boolean stickyRight = previousRange.getMaximum().equals(previousRange.getUpperBound());
                Number lowerBound = previousRange.getLowerBound();
                Number upperBound = previousRange.getUpperBound();

                //The inteval grows on the right
                if (stickyRight && comparator.superior(max, upperBound)) {
                    upperBound = max;
                }

                //The interval grows on the left
                if (stickyLeft && comparator.inferior(min, lowerBound)) {
                    lowerBound = min;
                }

                //The interval shrinks on the right
                if (comparator.superior(upperBound, max)) {
                    upperBound = max;
                }

                //The interval shrinks on the left
                if (comparator.inferior(lowerBound, min)) {
                    lowerBound = min;
                }

                newRange = new Range(lowerBound, upperBound, min, max, previousRange.isLeftInclusive(), previousRange.isRightInclusive(), values);
                if (!newRange.equals(previousRange)) {
                    rangeFilter.getRangeProperty().setValue(newRange);
                }
            }
        }

        return res;
    }

    private static class NumberComparator implements Comparator<Number> {

        public boolean superior(Number a, Number b) {
            return compare(a, b) > 0;
        }

        public boolean inferior(Number a, Number b) {
            return compare(a, b) < 0;
        }

        public Number min(Number a, Number b) {
            int c = compare(a, b);
            return c < 0 ? a : b;
        }

        public Number max(Number a, Number b) {
            int c = compare(a, b);
            return c > 0 ? a : b;
        }

        @Override
        public int compare(Number number1, Number number2) {
            if (((Object) number2).getClass().equals(((Object) number1).getClass())) {
                if (number1 instanceof Comparable) {
                    return ((Comparable) number1).compareTo(number2);
                }
            }
            // for all different Number types, let's check there double values
            if (number1.doubleValue() < number2.doubleValue()) {
                return -1;
            }
            if (number1.doubleValue() > number2.doubleValue()) {
                return 1;
            }
            return 0;
        }
    }
}
