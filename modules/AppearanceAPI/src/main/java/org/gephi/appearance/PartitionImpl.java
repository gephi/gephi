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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.gephi.appearance.api.Partition;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Index;

/**
 *
 * @author mbastian
 */
public class PartitionImpl implements Partition {

    private final Index index;
    private final Column column;
    private final Map<Object, Color> colorMap;

    public PartitionImpl(Column column, Index index) {
        this.column = column;
        this.index = index;
        this.colorMap = new HashMap<Object, Color>();
    }

    @Override
    public Iterable getValues() {
        return index.values(column);
    }

    @Override
    public int getElementCount() {
        return index.countElements(column);
    }

    @Override
    public int count(Object value) {
        return index.count(column, value);
    }

    @Override
    public Color getColor(Object value) {
        return colorMap.get(value);
    }

    @Override
    public void setColor(Object value, Color color) {
        colorMap.put(value, color);
    }

    @Override
    public float percentage(Object value) {
        int count = index.count(column, value);
        return (float) count / index.countElements(column);
    }

    @Override
    public int size() {
        return index.countValues(column);
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
        final PartitionImpl other = (PartitionImpl) obj;
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        return true;
    }
}
