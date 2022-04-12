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

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;

/**
 * @author mbastian
 */
public abstract class FunctionImpl implements Function {

    protected final AppearanceModelImpl model;
    protected final Class<? extends Element> elementClass;
    protected final String name;
    protected final Column column;
    protected final Transformer transformer;
    protected final TransformerUI transformerUI;
    protected final PartitionImpl partition;
    protected final RankingImpl ranking;
    protected final AtomicInteger version;
    // Version
    protected WeakReference<Graph> lastGraph;
    protected boolean lastTransformNullValues;

    protected FunctionImpl(AppearanceModelImpl model, String name, Class<? extends Element> elementClass, Column column,
                           Transformer transformer, TransformerUI transformerUI, PartitionImpl partition,
                           RankingImpl ranking) {
        if (name == null) {
            throw new NullPointerException("The name can't be null");
        }
        this.model = model;
        this.name = name;
        this.elementClass = elementClass;
        this.column = column;
        try {
            this.transformer = transformer.getClass().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.transformerUI = transformerUI;
        this.partition = partition;
        this.ranking = ranking;
        this.version =
            new AtomicInteger(partition != null ? partition.getVersion(model.getPartitionGraph()) : Integer.MIN_VALUE);
        this.lastGraph = partition != null ? new WeakReference<>(model.getPartitionGraph()) : null;
        this.lastTransformNullValues = model.isTransformNullValues();
    }

    @Override
    public void transform(Element element) {
        Graph graph = getGraph();
        if (isSimple()) {
            ((SimpleTransformer) transformer).transform(element);
        } else if (isRanking()) {
            transformRanking(element, graph, ranking.getMinValue(graph), ranking.getMaxValue(graph));
        } else if (isPartition()) {
            transformPartition(element, graph);
        }
    }

    @Override
    public void transformAll(Iterable<? extends Element> elementIterable) {
        Graph graph = getGraph();
        if (isSimple()) {
            elementIterable.forEach(((SimpleTransformer) transformer)::transform);
        } else if (isRanking()) {
            final Number minValue = ranking.getMinValue(graph);
            final Number maxValue = ranking.getMaxValue(graph);
            elementIterable.forEach(e -> transformRanking(e, graph, minValue, maxValue));
        } else if (isPartition()) {
            elementIterable.forEach(e -> transformPartition(e, graph));
        }
    }

    private void transformPartition(Element element, Graph graph) {
        Object val = partition.getValue(element, graph);
        if (val != null || model.isTransformNullValues()) {
            ((PartitionTransformer) transformer).transform(element, partition, val);
        }
    }

    private void transformRanking(Element element, Graph graph, Number minValue, Number maxValue) {
        Number val = ranking.getValue(element, graph);
        if (val != null) {
            float normalizedValue = ranking.normalize(val, ranking.getInterpolator(), minValue, maxValue);
            ((RankingTransformer) transformer).transform(element, ranking, val, normalizedValue);
        } else if (model.isTransformNullValues()) {
            ((RankingTransformer) transformer).transform(element, ranking, null, 0f);
        }
    }

    public boolean hasChanged() {
        if (isPartition()) {
            Graph graph = model.getPartitionGraph();

            // Check if view has changed
            boolean viewChanged = false;
            synchronized (this) {
                if (lastGraph == null) {
                    lastGraph = new WeakReference<>(graph);
                } else {
                    Graph lg = lastGraph.get();
                    lastGraph = null;
                    if (lg == null || lg != graph) {
                        viewChanged = true;
                        lastGraph = new WeakReference<>(graph);
                    }
                }

                // Check if transformNullValues was changed
                if (lastTransformNullValues != model.isTransformNullValues()) {
                    viewChanged = true;
                }
                lastTransformNullValues = model.isTransformNullValues();
            }

            int newVersion = partition.getVersion(graph);
            return version.getAndSet(newVersion) != newVersion || viewChanged;
        }
        return false;
    }

    @Override
    public boolean isValid() {
        if (isRanking()) {
            return ranking.isValid(getGraph());
        } else if (isPartition()) {
            return partition.isValid(getGraph());
        }
        return true;
    }

    @Override
    public Graph getGraph() {
        if (isRanking()) {
            return model.getRankingGraph();
        } else if (isPartition()) {
            return model.getPartitionGraph();
        }
        return model.getGraphModel().getGraph();
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public TransformerUI getUI() {
        return transformerUI;
    }

    @Override
    public boolean isSimple() {
        return ranking == null && partition == null;
    }

    @Override
    public boolean isAttribute() {
        return column != null;
    }

    @Override
    public boolean isPartition() {
        return partition != null;
    }

    @Override
    public boolean isRanking() {
        return ranking != null;
    }

    @Override
    public Class<? extends Element> getElementClass() {
        return elementClass;
    }

    @Override
    public AppearanceModelImpl getModel() {
        return model;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FunctionImpl other = (FunctionImpl) obj;
        return Objects.equals(this.name, other.name);
    }
}
