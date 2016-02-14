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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.types.TimeMap;

/**
 *
 * @author mbastian
 */
public class AttributePartitionImpl extends PartitionImpl {

    protected final Graph graph;
    protected final Index index;
    protected final Column column;
    protected final Map<Object, Integer> parts;
    protected int elements;

    public AttributePartitionImpl(Column column, Index index) {
        super();
        this.column = column;
        this.index = index;
        this.graph = null;
        this.parts = null;
    }

    public AttributePartitionImpl(Column column, Graph graph) {
        super();
        this.column = column;
        this.index = null;
        this.graph = graph;
        this.parts = new HashMap<>();
    }

    @Override
    protected void refresh() {
        if (graph != null) {
            parts.clear();
            elements = 0;
            ElementIterable<? extends Element> iterable = AttributeUtils.isNodeColumn(column) ? graph.getNodes() : graph.getEdges();
            for (Element el : iterable) {
                TimeMap val = (TimeMap) el.getAttribute(column);
                if (val != null) {
                    Object[] va = val.toValuesArray();
                    for (Object v : va) {
                        Integer count = parts.get(v);
                        if (count == null) {
                            count = 0;
                        }
                        parts.put(v, ++count);
                        elements++;
                    }
                }
            }
        }
    }

    @Override
    public Object getValue(Element element, Graph gr) {
        if (graph != null) {
            TimeMap val = (TimeMap) element.getAttribute(column);
            if (val != null) {
                return val.get(gr.getView().getTimeInterval(), Estimator.FIRST);
            }
            return null;
        }
        return element.getAttribute(column);
    }

    @Override
    public Collection getValues() {
        if (index != null) {
            return index.values(column);
        } else {
            return parts.keySet();
        }
    }

    @Override
    public int getElementCount() {
        if (index != null) {
            return index.countElements(column);
        } else {
            return elements;
        }
    }

    @Override
    public int count(Object value) {
        if (index != null) {
            return index.count(column, value);
        } else {
            Integer c = parts.get(value);
            return c != null ? c : 0;
        }
    }

    @Override
    public float percentage(Object value) {
        if (index != null) {
            int count = index.count(column, value);
            return 100f * ((float) count / index.countElements(column));
        } else {
            Integer c = parts.get(value);
            return 100f * (c != null ? c.floatValue() / elements : 0f);
        }
    }

    @Override
    public int size() {
        if (index != null) {
            return index.countValues(column);
        } else {
            return parts.size();
        }
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.column != null ? this.column.hashCode() : 0);
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
        final AttributePartitionImpl other = (AttributePartitionImpl) obj;
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        return true;
    }
}
