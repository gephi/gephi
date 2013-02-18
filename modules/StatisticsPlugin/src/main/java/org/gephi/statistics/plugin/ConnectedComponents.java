/*
Copyright 2008-2011 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
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
package org.gephi.statistics.plugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;

/**
 *
 * @author pjmcswee
 */
public class ConnectedComponents implements Statistics, LongTask {

    public static final String WEAKLY = "componentnumber";
    public static final String STRONG = "strongcompnum";
    private boolean isDirected;
    private ProgressTicket progress;
    private boolean isCanceled;
    private int componentCount;
    private int stronglyCount;
    private int[] componentsSize;
    int count;

    public ConnectedComponents() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {

        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraphVisible();
        weaklyConnected(undirectedGraph, attributeModel);
        if (isDirected) {
            HierarchicalDirectedGraph directedGraph = graphModel.getHierarchicalDirectedGraphVisible();
            top_tarjans(directedGraph, attributeModel);
        }
    }

    // weakly connected components using dijoint set forests
    // should be more effective on large graphs (actually order of magnitude faster)
    public void weaklyConnected(HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {

        hgraph.readLock();

        Progress.start(progress);
        Progress.switchToIndeterminate(progress);
        Progress.setDisplayName(progress, "CC: Adding nodes to forest");
        
        // add all nodes to dsf, each in its own component
        DisjointForest<Node> dsf = new DisjointForest<Node>(hgraph.getNodeCount());
        for (Node n: hgraph.getNodes()){
            dsf.add(n);
        }
        
        // for each edge connect the components
        Progress.setDisplayName(progress, "CC: Joining trees");
        for (Edge e:hgraph.getEdges()) {
            dsf.union(e.getSource(), e.getTarget());
        }
        
        // assign component ids to all nodes
        Progress.setDisplayName(progress, "CC: Setting attributes");
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn(WEAKLY);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(WEAKLY, "Component ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }
        HashMap<Integer, Integer> nid2cid = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> csizes = new HashMap<Integer, Integer>();
        int lastcid = 0;
        for (Node n: hgraph.getNodes()) {
            Node rep = dsf.find(n);
            int nid = rep.getId();
            
            // map nid to cid
            if (!nid2cid.containsKey(nid)) {
                nid2cid.put(nid, lastcid++);
            }
            int cid = nid2cid.get(nid);
            csizes.put(cid, (csizes.containsKey(cid))?csizes.get(cid) + 1:1);

            // set the component id to id of representative node
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            row.setValue(componentCol, cid);
        }

        hgraph.readUnlock();

        // conunt components
        componentCount = csizes.size();
        componentsSize = new int[nid2cid.size()];
        for (int i = 0; i < nid2cid.size(); i++) {
            componentsSize[i] = csizes.get(i);
        }
    }
    
    public void weaklyConnected0(HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        componentCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn(WEAKLY);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(WEAKLY, "Component ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        List<Integer> sizeList = new ArrayList<Integer>();

        hgraph.readLock();

        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            index++;
        }


        int N = hgraph.getNodeCount();

        //Keep track of which nodes have been seen
        int[] color = new int[N];

        Progress.start(progress, hgraph.getNodeCount());
        int seenCount = 0;
        while (seenCount < N) {
            //The search Q
            LinkedList<Node> Q = new LinkedList<Node>();
            //The component-list
            LinkedList<Node> component = new LinkedList<Node>();

            //Seed the seach Q
            NodeIterable iter = hgraph.getNodes();
            for (Node first : iter) {
                if (color[indicies.get(first)] == 0) {
                    Q.add(first);
                    iter.doBreak();
                    break;
                }
            }

            //While there are more nodes to search
            while (!Q.isEmpty()) {
                if (isCanceled) {
                    hgraph.readUnlock();
                    return;
                }
                //Get the next Node and add it to the component list
                Node u = Q.removeFirst();
                component.add(u);

                //Iterate over all of u's neighbors
                EdgeIterable edgeIter = hgraph.getEdgesAndMetaEdges(u);

                //For each neighbor
                for (Edge edge : edgeIter) {
                    Node reachable = hgraph.getOpposite(u, edge);
                    int id = indicies.get(reachable);
                    //If this neighbor is unvisited
                    if (color[id] == 0) {
                        color[id] = 1;
                        //Add it to the search Q
                        Q.addLast(reachable);
                        //Mark it as used 

                        Progress.progress(progress, seenCount);
                    }
                }
                color[indicies.get(u)] = 2;
                seenCount++;
            }
            for (Node s : component) {
                AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                row.setValue(componentCol, componentCount);
            }
            sizeList.add(component.size());
            componentCount++;
        }
        hgraph.readUnlock();

        componentsSize = new int[sizeList.size()];
        for (int i = 0; i < sizeList.size(); i++) {
            componentsSize[i] = sizeList.get(i);
        }
    }

    public void top_tarjans(HierarchicalDirectedGraph hgraph, AttributeModel attributeModel) {
        count = 1;
        stronglyCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn(STRONG);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(STRONG, "Strongly-Connected ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        hgraph.readLock();

        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int v = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, v);
            v++;
        }
        int N = hgraph.getNodeCount();
        int[] index = new int[N];
        int[] low_index = new int[N];

        while (true) {
            //The search Q
            LinkedList<Node> S = new LinkedList<Node>();
            //The component-list
            //LinkedList<Node> component = new LinkedList<Node>();
            //Seed the seach Q
            Node first = null;
            NodeIterable iter = hgraph.getNodes();
            for (Node u : iter) {
                if (index[indicies.get(u)] == 0) {
                    first = u;
                    iter.doBreak();
                    break;
                }
            }
            if (first == null) {
                hgraph.readUnlockAll();
                return;
            }
            tarjans(componentCol, S, hgraph, first, index, low_index, indicies);
        }
    }

    private void tarjans(AttributeColumn col, LinkedList<Node> S, HierarchicalDirectedGraph hgraph, Node f, int[] index, int[] low_index, HashMap<Node, Integer> indicies) {
        int id = indicies.get(f);
        index[id] = count;
        low_index[id] = count;
        count++;
        S.addFirst(f);
        EdgeIterable edgeIter = hgraph.getOutEdgesAndMetaOutEdges(f);
        for (Edge e : edgeIter) {
            Node u = hgraph.getOpposite(f, e);
            int x = indicies.get(u);
            if (index[x] == 0) {
                tarjans(col, S, hgraph, u, index, low_index, indicies);
                low_index[id] = Math.min(low_index[x], low_index[id]);
            } else if (S.contains(u)) {
                low_index[id] = Math.min(low_index[id], index[x]);
            }
        }
        if (low_index[id] == index[id]) {
            Node v = null;
            while (v != f) {
                v = S.removeFirst();
                AttributeRow row = (AttributeRow) v.getNodeData().getAttributes();
                row.setValue(col, stronglyCount);
            }
            stronglyCount++;
        }
    }

    public int getConnectedComponentsCount() {
        return componentCount;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public int[] getComponentsSize() {
        return componentsSize;
    }

    public int getGiantComponent() {
        int[] sizes = getComponentsSize();
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > max) {
                max = sizes[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public String getReport() {
        Map<Integer, Integer> sizeDist = new HashMap<Integer, Integer>();
        for(int v : componentsSize) {
            if(!sizeDist.containsKey(v)) {
                sizeDist.put(v, 0);
            }
            sizeDist.put(v, sizeDist.get(v) + 1);
        }
        
        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(sizeDist, "Size Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Size Distribution",
                "Size (number of nodes)",
                "Count",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "cc-size-distribution.png");

        NumberFormat f = new DecimalFormat("#0.000");


        String report = "<HTML> <BODY> <h1>Connected Components Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Number of Weakly Connected Components: " + componentCount + "<br>"
                + (isDirected ? "Number of Stronlgy Connected Components: " + stronglyCount + "<br>" : "")
                + "<br /><br />"+imageFile
                + "<br />" + "<h2> Algorithm: </h2>"
                + "Robert Tarjan, <i>Depth-First Search and Linear Graph Algorithms</i>, in SIAM Journal on Computing 1 (2): 146–160 (1972)<br />"
                + "</BODY> </HTML>";

        return report;
    }

    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }
    
    /**
    * A disjoint-union data structure (backed by a HashMap) supporting
    * find and union operations in (amortized) &alpha;(n) time, where
    * &alpha;(n) is the very slow-growing inverse of the Ackermann
    * function. This data structure also supports the standard set
    * operations with the exception of removal operations, which will
    * result in an {@link UnsupportedOperationException} being thrown.
    * @author Nick Watson
    *
    * @param <E> The type of elements contained.
    */

    private class DisjointForest<E> implements Set<E> {

            private class SetNode {

                    E elem;
                    SetNode parent;
                    int rank;

                    public SetNode(E e) {
                            elem = e;
                            parent = this;
                            rank = 0;
                    }

            }

            private HashMap<E, SetNode> map;

            /**
             * Constructs a new Disjoint Forest. <br />
             * O(1)
             */
            public DisjointForest() {
                    map = new HashMap<E, SetNode>();
            }

            /**
             * Constructs a new Disjoint Forest, with the specified
             * initial capacity. <br />
             * O(1)
             * @param initialCapacity The initial capacity
             */
            public DisjointForest(int initialCapacity) {
                    map = new HashMap<E, SetNode>(initialCapacity);
            }

            /**
             * Makes a new disjoint set containing the given element. <br />
             * O(1)
             * @param elem The element to add
             * @return The newly added element (i.e. <code>elem</code>), or
             * <code>null</code> if <code>elem</code> is already contained
             * in this set.
             */
            public E makeSet(E elem) {
                    if (map.containsKey(elem)) return null;
                    map.put(elem, new SetNode(elem));
                    return elem;
            }

            private SetNode find(SetNode n) {
                    if (n == n.parent) return n;
                    n.parent = find(n.parent); // flatten
                    return n.parent;
            }

            /**
             * Finds the set containing the given element. <br />
             * O(&alpha;(n))
             * @param elem The element to find
             * @return The representative of the set containing the element
             * @throws IllegalArgumentException If <code>elem</code> is not an
             * element of this forest.
             */
            public E find(E elem) {
                    SetNode n = map.get(elem);
                    if (n == null) throw new IllegalArgumentException(
                            "Non-existent element passed as representative.");
                    return find(n).elem;
            }

            private E union(SetNode n1, SetNode n2) {
                    SetNode r1 = find(n1), r2 = find(n2);
                    if (r1 == r2) return r1.elem;
                    if (r1.rank > r2.rank) {
                            r2.parent = r1;
                            return r1.elem;
                    }
                    r1.parent = r2;
                    if (r1.rank == r2.rank) r2.rank++;
                    return r2.elem;
            }

            /**
             * Merges two disjoint sets by taking their union. <br />
             * O(&alpha;(n))
             * @param repr1 The representative of the first set to merge
             * @param repr2 The representative of the second set to merge
             * @return The representative of the union of the two sets
             * @throws IllegalArgumentException If <code>repr1</code> or
             * <code>repr2</code> is not an element of this forest.
             */
            public E union(E repr1, E repr2) {
                    SetNode n1 = map.get(repr1), n2 = map.get(repr2);
                    if (n1 == null) throw new IllegalArgumentException(
                            "Non-existent element passed as representative 1.");
                    if (n2 == null) throw new IllegalArgumentException(
                            "Non-existent element passed as representative 2.");
                    return union(n1, n2);
            }

            /**
             * Gets whether the disjoint forest is empty. <br />
             * O(1)
             * @return Whether the disjoint forest is empty
             */
            @Override
            public boolean isEmpty() { return map.isEmpty(); }

            /**
             * Gets the size of the disjoint forest. <br />
             * O(1)
             * @return The size of the disjoint forest
             */
            @Override
            public int size() { return map.size(); }

            /**
             * Makes a new disjoint set containing the given element. <br />
             * O(1)
             * @param elem The element to add
             * @return True if the element wasn't already contained in this
             * disjoint forest
             * @see #makeSet
             */
            @Override
            public boolean add(E elem) {
                    return makeSet(elem) != null;
            }

            /**
             * Adds an element to the given set. <code>add(elem, null)</code>
             * is equivalent to <code>makeSet(elem)</code>. <br />
             * O(&alpha;(n))
             * @param elem The element to add
             * @param repr The representative of the set to add it to
             * @return The representative of the set containing the added
             * element, or <code>null</code> if <code>elem</code> is already
             * contained in this set.
             * @throws IllegalArgumentException If <code>repr</code> is not an
             * element of this forest.
             */
            public E add(E elem, E repr) {
                    if (repr == null) return makeSet(elem);
                    if (map.containsKey(elem)) return null;
                    SetNode r = map.get(repr);
                    if (r == null) throw new IllegalArgumentException(
                                    "Non-existent element passed as representative.");
                    SetNode n = new SetNode(elem);
                    map.put(elem, n);
                    return union(r, n);
            }

            /**
             * Adds all elements in the given collection to this disjoint
             * forest, putting each one in <i>its own set</i>. <br />
             * O(n)
             * @param c The collection to add
             * @return True if any of the elements were added successfully
             */
            @Override
            public boolean addAll(Collection<? extends E> c) {
                    boolean changed = false;
                    for (E e : c) changed |= add(e);
                    return changed;
            }

            /**
             * Adds all elements in the given collection to this disjoint
             * forest, optionally putting them all in the same set. <br />
             * O(n)
             * @param c The collection to add
             * @param sameSet Whether to put all newly added elements in the
             * same set
             * @return True if any of the elements were added successfully
             */
            public boolean addAll(Collection<? extends E> c, boolean sameSet) {
                    if (c.isEmpty()) return false;
                    if (!sameSet) return addAll(c);
                    E root = null;
                    for (E e : c) {
                            if (root == null) root = makeSet(e);
                            else add(e, root);
                    }
                    return root != null;
            }

            /**
             * Removes all elements from this disjoint forest (in place). <br />
             * O(n)
             */
            @Override
            public void clear() {
                    map.clear();
            }

            /**
             * Gets whether this disjoint forest contains the
             * given element. <br />
             * O(1)
             * @param elem The object to search for
             * @return Whether the element is present
             */
            @Override
            public boolean contains(Object elem) {
                    return map.containsKey(elem);
            }

            /**
             * Gets whether this disjoint forest contains all
             * of the elements in the given collection. <br />
             * O(n)
             * @param c The collection to check
             * @return Whether all elements are present
             */
            @Override
            public boolean containsAll(Collection<?> c) {
                    for (Object o : c) {
                            if (!map.containsKey(o)) return false;
                    }
                    return true;
            }

            /**
             * Provides an iterator for this disjoint set. The
             * iterator provides no guarantee as to the order of
             * iteration and does not support removals. <br />
             * O(1)
             * @return An iterator for this disjoint forest
             */
            @Override
            public Iterator<E> iterator() {
                    return new Iterator<E>() {

                            Iterator<E> it = map.keySet().iterator();

                            @Override
                            public boolean hasNext() {
                                    return it.hasNext();
                            }

                            @Override
                            public E next() {
                                    return it.next();
                            }

                            @Deprecated
                            @Override
                            public void remove() {
                                    throw new UnsupportedOperationException("Removals not supported");
                            }			
                    };
            }

            /**
             * Gets the HashMap backing this disjoint forest
             * @return The backing HashMap
             */
            HashMap<E, SetNode> map() { return map; }

            /**
             * Compares the specified object with this set for equality.
             * Returns true if the specified object is also a set, and
             * the two sets have the same size and elements. <br />
             * O(n)
             * @param o The object to be compared for equality
             * @return True if the object is equal
             */
            @Override
            public boolean equals(Object o) {
                    return map.keySet().equals(o instanceof DisjointForest ?
                                    ((DisjointForest<?>)o).map().keySet() : o);
            }

            /**
             * Returns the hash code value for this set. The hash code of a
             * set is defined to be the sum of the hash codes of the elements
             * in the set, where the hash code of a null element is defined to
             * be zero. <br />
             * O(n)
             * @return A hash value for this set
             */
            @Override
            public int hashCode() {
                    return map.keySet().hashCode();
            }

            /**
             * Returns an array containing all of the elements
             * in this set, in no particular order. <br />
             * O(n)
             * @return An array containing the elements in this set
             */
            @Override
            public Object[] toArray() {
                    return map.keySet().toArray();
            }

            /**
             * Returns an array containing all of the elements in this
             * set, in no particular order; the runtime type of the
             * returned array is that of the specified array. <br />
             * O(n)
             * @return An array of the given type containing the elements
             * in this set
             */
            @Override
            public <T> T[] toArray(T[] a) {
                    return map.keySet().toArray(a);
            }

            /**
             * Converts this Disjoint Forest to a comma-separated
             * list of its elements. <br />
             * O(n)
             * @return A string representing this disjoint forest
             */
            @Override
            @SuppressWarnings("unchecked")
            public String toString() {
                    StringBuffer b = new StringBuffer();		
                    E[] e = toArray((E[])new Object[0]);
                    for (int i = 0; i < e.length; i++) {
                            b.append(e[i].toString());
                            if (i != e.length-1) b.append(",");
                    }
                    return b.toString();
            }

            /**
             * Removals are not supported.
             * @param elem Unused
             * @throws UnsupportedOperationException Always
             */
            @Override
            @Deprecated
            public boolean remove(Object elem) {
                    throw new UnsupportedOperationException("Removals not supported");
            }

            /**
             * Removals are not supported.
             * @param c Unused
             * @throws UnsupportedOperationException Always
             */
            @Override
            @Deprecated
            public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Removals not supported");
            }

            /**
             * Removals are not supported.
             * @param c Unused
             * @throws UnsupportedOperationException Always
             */
            @Override
            @Deprecated
            public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Removals not supported");
            }
    }

}
