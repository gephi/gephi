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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;

/**
 *
 * @author mbastian
 */
public abstract class FunctionImpl implements Function {

    protected final String id;
    protected final Class<? extends Element> elementClass;
    protected final String name;
    protected final Graph graph;
    protected final Column column;
    protected final Transformer transformer;
    protected final TransformerUI transformerUI;
    protected final PartitionImpl partition;
    protected final RankingImpl ranking;
    protected Interpolator interpolator;

    protected FunctionImpl(String id, String name, Class<? extends Element> elementClass, Graph graph, Column column, Transformer transformer, TransformerUI transformerUI, PartitionImpl partition, RankingImpl ranking, Interpolator interpolator) {
        if (id == null) {
            throw new NullPointerException("The id can't be null");
        }
        this.id = id;
        this.name = name;
        this.elementClass = elementClass;
        this.column = column;
        this.graph = graph;
        this.interpolator = interpolator;
        try {
            this.transformer = transformer.getClass().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.transformerUI = transformerUI;
        this.partition = partition;
        this.ranking = ranking;
    }

    @Override
    public void transform(Element element, Graph graph) {
        if (isSimple()) {
            ((SimpleTransformer) transformer).transform(element);
        } else if (isRanking()) {
            Number val = ranking.getValue(element, graph);
            if (val == null) {
                Logger.getLogger("").log(Level.WARNING, "The element with id ''{0}'' has a null value for ranking. Using 0 instead", element.getId());
                val = 0;
            }
            ((RankingTransformer) transformer).transform(element, ranking, interpolator, val);
        } else if (isPartition()) {
            Object val = partition.getValue(element, graph);
            ((PartitionTransformer) transformer).transform(element, partition, val);
        }
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
    public Graph getGraph() {
        return graph;
    }

    @Override
    public Class<? extends Element> getElementClass() {
        return elementClass;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        return id;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
