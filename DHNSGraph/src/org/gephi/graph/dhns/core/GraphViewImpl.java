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
package org.gephi.graph.dhns.core;

import java.util.WeakHashMap;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.dhns.graph.AbstractGraphImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphViewImpl implements GraphView {

    private final Dhns dhns;
    private final int viewId;
    private final TreeStructure structure;
    private final StructureModifier structureModifier;
    private int nodesEnabled;
    private int edgesCountTotal;
    private int mutualEdgesTotal;
    private int edgesCountEnabled;
    private int mutualEdgesEnabled;
    private int metaEdgesCountTotal;
    private int mutualMetaEdgesTotal;
    //RefCounting
    private final WeakHashMap<AbstractGraphImpl, Boolean> graphsMap = new WeakHashMap<AbstractGraphImpl, Boolean>();

    public GraphViewImpl(Dhns dhns, int viewId) {
        this.dhns = dhns;
        this.viewId = viewId;
        this.structure = new TreeStructure(this);
        this.structureModifier = new StructureModifier(dhns, this);
    }

    public void addGraphReference(AbstractGraphImpl graph) {
        graphsMap.put(graph, Boolean.TRUE);

        //Track graph references
        /*StackTraceElement[] elm = Thread.currentThread().getStackTrace();
        int i;
        for (i = 1; i < elm.length; i++) {
        if (!elm[i].getClassName().startsWith("org.gephi.graph")) {
        break;
        }
        }
        System.out.println("View " + viewId + " : " + elm[i].toString());*/
    }

    public boolean hasGraphReference() {
        return !graphsMap.isEmpty();
    }

    public int getViewId() {
        return viewId;
    }

    public TreeStructure getStructure() {
        return structure;
    }

    public StructureModifier getStructureModifier() {
        return structureModifier;
    }

    public boolean isMainView() {
        return viewId == 0;
    }

    public void incNodesEnabled(int shift) {
        nodesEnabled += shift;
    }

    public void decNodesEnabled(int shift) {
        nodesEnabled -= shift;
    }

    public void incEdgesCountTotal(int shift) {
        edgesCountTotal += shift;
    }

    public void incEdgesCountEnabled(int shift) {
        edgesCountEnabled += shift;
    }

    public void incMutualEdgesTotal(int shift) {
        mutualEdgesTotal += shift;
    }

    public void incMutualEdgesEnabled(int shift) {
        mutualEdgesEnabled += shift;
    }

    public void decEdgesCountTotal(int shift) {
        edgesCountTotal -= shift;
    }

    public void decEdgesCountEnabled(int shift) {
        edgesCountEnabled -= shift;
    }

    public void decMutualEdgesTotal(int shift) {
        mutualEdgesTotal -= shift;
    }

    public void decMutualEdgesEnabled(int shift) {
        mutualEdgesEnabled -= shift;
    }

    public void incMetaEdgesCount(int shift) {
        metaEdgesCountTotal += shift;
    }

    public void decMetaEdgesCount(int shift) {
        metaEdgesCountTotal -= shift;
    }

    public void incMutualMetaEdgesTotal(int shift) {
        mutualMetaEdgesTotal += shift;
    }

    public void decMutualMetaEdgesTotal(int shift) {
        mutualMetaEdgesTotal -= shift;
    }

    public int getEdgesCountEnabled() {
        return edgesCountEnabled;
    }

    public void setEdgesCountEnabled(int edgesCountEnabled) {
        this.edgesCountEnabled = edgesCountEnabled;
    }

    public int getEdgesCountTotal() {
        return edgesCountTotal;
    }

    public void setEdgesCountTotal(int edgesCountTotal) {
        this.edgesCountTotal = edgesCountTotal;
    }

    public int getMutualEdgesEnabled() {
        return mutualEdgesEnabled;
    }

    public void setMutualEdgesEnabled(int mutualEdgesEnabled) {
        this.mutualEdgesEnabled = mutualEdgesEnabled;
    }

    public int getMutualEdgesTotal() {
        return mutualEdgesTotal;
    }

    public void setMutualEdgesTotal(int mutualEdgesTotal) {
        this.mutualEdgesTotal = mutualEdgesTotal;
    }

    public int getNodesEnabled() {
        return nodesEnabled;
    }

    public void setNodesEnabled(int nodesEnabled) {
        this.nodesEnabled = nodesEnabled;
    }

    public int getMetaEdgesCountTotal() {
        return metaEdgesCountTotal;
    }

    public void setMetaEdgesCountTotal(int metaEdgesCountTotal) {
        this.metaEdgesCountTotal = metaEdgesCountTotal;
    }

    public int getMutualMetaEdgesTotal() {
        return mutualMetaEdgesTotal;
    }

    public void setMutualMetaEdgesTotal(int mutualMetaEdgesTotal) {
        this.mutualMetaEdgesTotal = mutualMetaEdgesTotal;
    }

    public Dhns getGraphModel() {
        return dhns;
    }
}
