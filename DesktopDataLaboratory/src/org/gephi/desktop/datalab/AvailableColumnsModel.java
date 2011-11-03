/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.desktop.datalab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.gephi.data.attributes.api.AttributeColumn;

/**
 * Class to keep available state (in data laboratory) of the columns of a table of a workspace.
 * Useful, but also necessary to limit the maximum number of available columns when there are a lot of columns.
 * @author Eduardo
 */
public class AvailableColumnsModel {

    private static final int MAX_AVAILABLE_COLUMNS = 20;
    private ArrayList<AttributeColumn> availableColumns = new ArrayList<AttributeColumn>();

    public boolean isColumnAvailable(AttributeColumn column) {
        return availableColumns.contains(column);
    }

    /**
     * Add a column as available if it can be added.
     * @param column Column to add
     * @return True if the column was successfully added, false otherwise (no more columns can be available)
     */
    public boolean addAvailableColumn(AttributeColumn column) {
        if (canAddAvailableColumn()) {
            if (!availableColumns.contains(column)) {
                availableColumns.add(column);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove an available column from the model if possible.
     * @param column Column to make not available
     * @return True if the column could be removed
     */
    public boolean removeAvailableColumn(AttributeColumn column) {
        return availableColumns.remove(column);
    }

    /**
     * Clear all available columns
     */
    public void removeAllColumns() {
        availableColumns.clear();
    }

    /**
     * Indicates if more columns can be made available a the moment
     * @return
     */
    public boolean canAddAvailableColumn() {
        return availableColumns.size() < MAX_AVAILABLE_COLUMNS;
    }

    /**
     * Return available columns, sorted by index
     * @return 
     */
    public AttributeColumn[] getAvailableColumns() {
        Collections.sort(availableColumns, new Comparator<AttributeColumn>() {

            public int compare(AttributeColumn o1, AttributeColumn o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
        return availableColumns.toArray(new AttributeColumn[0]);
    }

    public int getAvailableColumnsCount() {
        return availableColumns.size();
    }
}
