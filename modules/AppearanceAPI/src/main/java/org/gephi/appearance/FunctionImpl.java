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

import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.api.SimpleFunction;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author mbastian
 */
public class FunctionImpl implements RankingFunction, PartitionFunction, SimpleFunction {

    protected final AppearanceModelImpl model;
    protected final Column column;
    protected final Transformer transformer;
    protected final TransformerUI transformerUI;
    protected final PartitionImpl partition;
    protected final RankingImpl ranking;

    public FunctionImpl(AppearanceModelImpl model, Column column, Transformer transformer, TransformerUI transformerUI) {
        this(model, column, transformer, transformerUI, null, null);
    }

    public FunctionImpl(AppearanceModelImpl model, Column column, Transformer transformer, TransformerUI transformerUI, RankingImpl ranking) {
        this(model, column, transformer, transformerUI, null, ranking);
    }

    public FunctionImpl(AppearanceModelImpl model, Column column, Transformer transformer, TransformerUI transformerUI, PartitionImpl partition) {
        this(model, column, transformer, transformerUI, partition, null);
    }

    public FunctionImpl(AppearanceModelImpl model, Column column, Transformer transformer, TransformerUI transformerUI, PartitionImpl partition, RankingImpl ranking) {
        this.model = model;
        this.column = column;
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
    public void transform(Element element) {
        if (isSimple()) {
            ((SimpleTransformer) transformer).transform(element);
        } else if (isRanking()) {
            Number val = (Number) element.getAttribute(column);
            ((RankingTransformer) transformer).transform(element, ranking, val);
        } else if (isPartition()) {
            Object val = element.getAttribute(column);
            ((PartitionTransformer) transformer).transform(element, partition, val);
        }
    }

    @Override
    public Column getColumn() {
        return column;
    }

    public AppearanceModelImpl getModel() {
        return model;
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
        return column == null;
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
    public Partition getPartition() {
        return partition;
    }

    @Override
    public Ranking getRanking() {
        return ranking;
    }

    @Override
    public String toString() {
        if (column != null) {
            if (column.getTitle() != null) {
                return column.getTitle();
            } else {
                return column.getId();
            }
        }
        return super.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.column != null ? this.column.hashCode() : 0);
        hash = 47 * hash + (this.transformer != null ? this.transformer.hashCode() : 0);
        hash = 47 * hash + (this.partition != null ? this.partition.hashCode() : 0);
        hash = 47 * hash + (this.ranking != null ? this.ranking.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FunctionImpl other = (FunctionImpl) obj;
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        if (this.transformer != other.transformer && (this.transformer == null || !this.transformer.equals(other.transformer))) {
            return false;
        }
        if (this.partition != other.partition && (this.partition == null || !this.partition.equals(other.partition))) {
            return false;
        }
        if (this.ranking != other.ranking && (this.ranking == null || !this.ranking.equals(other.ranking))) {
            return false;
        }
        return true;
    }
}
