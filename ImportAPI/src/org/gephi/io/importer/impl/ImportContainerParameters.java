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
package org.gephi.io.importer.impl;

import org.gephi.io.importer.api.EdgeDefault;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerParameters {

    private boolean selfLoops = true;
    private boolean parallelEdges = true;
    private boolean autoNode = true;
    private boolean autoScale = true;
    private boolean removeEdgeWithWeightZero = false;
    private boolean undirectedSumDirectedEdgesWeight = false;
    private boolean removeIntervalsOverlapping = true;
    private boolean mergeParallelEdgesWeight = true;
    private boolean mergeParallelEdgesAttributes = true;
    private boolean duplicateWithLabels = false;
    private EdgeDefault edgeDefault = EdgeDefault.DIRECTED;

    public boolean isAutoNode() {
        return autoNode;
    }

    public void setAutoNode(boolean autoNode) {
        this.autoNode = autoNode;
    }

    public boolean isParallelEdges() {
        return parallelEdges;
    }

    public void setParallelEdges(boolean parallelEdges) {
        this.parallelEdges = parallelEdges;
    }

    public boolean isSelfLoops() {
        return selfLoops;
    }

    public void setSelfLoops(boolean selfLoops) {
        this.selfLoops = selfLoops;
    }

    public EdgeDefault getEdgeDefault() {
        return edgeDefault;
    }

    public void setEdgeDefault(EdgeDefault edgeDefault) {
        this.edgeDefault = edgeDefault;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public boolean isRemoveEdgeWithWeightZero() {
        return removeEdgeWithWeightZero;
    }

    public void setRemoveEdgeWithWeightZero(boolean removeEdgeWithWeightZero) {
        this.removeEdgeWithWeightZero = removeEdgeWithWeightZero;
    }

    public boolean isUndirectedSumDirectedEdgesWeight() {
        return undirectedSumDirectedEdgesWeight;
    }

    public void setUndirectedSumDirectedEdgesWeight(boolean undirectedSumDirectedEdgesWeight) {
        this.undirectedSumDirectedEdgesWeight = undirectedSumDirectedEdgesWeight;
    }

    public boolean isRemoveIntervalsOverlapping() {
        return removeIntervalsOverlapping;
    }

    public void setRemoveIntervalsOverlapping(boolean removeIntervalsOverlapping) {
        this.removeIntervalsOverlapping = removeIntervalsOverlapping;
    }

    public boolean isMergeParallelEdgesWeight() {
        return mergeParallelEdgesWeight;
    }

    public void setMergeParallelEdgesWeight(boolean mergeParallelEdgesWeight) {
        this.mergeParallelEdgesWeight = mergeParallelEdgesWeight;
    }

    public boolean isDuplicateWithLabels() {
        return duplicateWithLabels;
    }

    public void setDuplicateWithLabels(boolean duplicateWithLabels) {
        this.duplicateWithLabels = duplicateWithLabels;
    }

    public boolean isMergeParallelEdgesAttributes() {
        return mergeParallelEdgesAttributes;
    }

    public void setMergeParallelEdgesAttributes(boolean mergeParallelEdgesAttributes) {
        this.mergeParallelEdgesAttributes = mergeParallelEdgesAttributes;
    }
}
